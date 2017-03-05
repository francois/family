package teksol.mybank.postgres

import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

import org.springframework.jdbc.core.{JdbcTemplate, RowMapper}
import org.springframework.util.Assert
import teksol.domain.FamilyId
import teksol.infrastructure._
import teksol.mybank.MyBankApp
import teksol.mybank.domain._
import teksol.mybank.infrastructure._

import scala.collection.JavaConverters._
import scala.language.implicitConversions

class PostgresMyBankApp(private[this] val jdbcTemplate: JdbcTemplate, private[this] val eventBus: EventBus) extends MyBankApp with EventBusConsumer {
    implicit def localDateToToSql(date: LocalDate): ToSql = new ToSql {
        Assert.notNull(date, "date")

        override def toSql: AnyRef = date.format(DateTimeFormatter.ISO_DATE)
    }

    override def createAccount(familyId: FamilyId, accountId: AccountId, name: AccountName): Unit = {
        Assert.notNull(familyId, "familyId")
        Assert.notNull(accountId, "accountId")
        Assert.notNull(name, "name")

        jdbcTemplate.update("INSERT INTO mybank.accounts(family_id, account_id, name) VALUES (?::uuid, ?::uuid, ?::text)",
            familyId.toSql, accountId.toSql, name.toSql)

        eventBus.publish(AccountCreated(familyId, accountId))
    }

    override def listAccounts(familyId: FamilyId): Set[Account] = {
        Assert.notNull(familyId, "familyId")

        val list = jdbcTemplate.query("" +
                "SELECT family_id, account_id, name, coalesce(sum(amount), 0) AS balance " +
                "FROM mybank.accounts " +
                "LEFT JOIN mybank.entries USING (family_id, account_id) " +
                "WHERE family_id = ?::uuid " +
                "GROUP BY family_id, account_id, name", accountRowMapper, familyId.toSql)

        list.asScala.toSet
    }

    override def listAccountEntries(familyId: FamilyId, accountId: AccountId): Set[Entry] = {
        Assert.notNull(familyId, "familyId")
        Assert.notNull(accountId, "accountId")

        val list = jdbcTemplate.query("" +
                "SELECT family_id, account_id, entry_id, posted_on, description, amount " +
                "FROM mybank.entries " +
                "WHERE family_id = ?::uuid AND account_id = ?::uuid", entryRowMapper, familyId.toSql, accountId.toSql)

        list.asScala.toSet
    }

    override def deposit(familyId: FamilyId, accountId: AccountId, postedOn: LocalDate, description: Description, amount: Amount): Unit = {
        Assert.notNull(familyId, "familyId")
        Assert.notNull(accountId, "accountId")
        Assert.notNull(postedOn, "postedOn")
        Assert.notNull(description, "description")
        Assert.notNull(amount, "amount")
        Assert.isTrue(amount.compareTo(Amount.ZERO) > 0, "amount must be zero or positive, found " + amount)

        createEntry(familyId, accountId, postedOn, description, amount)
    }

    override def withdraw(familyId: FamilyId, accountId: AccountId, postedOn: LocalDate, description: Description, amount: Amount): Unit = {
        Assert.notNull(familyId, "familyId")
        Assert.notNull(accountId, "accountId")
        Assert.notNull(postedOn, "postedOn")
        Assert.notNull(description, "description")
        Assert.notNull(amount, "amount")
        Assert.isTrue(amount.compareTo(Amount.ZERO) > 0, "amount must be zero or positive, found " + amount)

        createEntry(familyId, accountId, postedOn, description, amount.negate)
    }

    override def createGoal(familyId: FamilyId, accountId: AccountId, goalId: GoalId, description: GoalDescription, dueOn: LocalDate, target: Amount): Unit = {
        Assert.notNull(familyId, "familyId")
        Assert.notNull(accountId, "accountId")
        Assert.notNull(goalId, "goalId")
        Assert.notNull(description, "description")
        Assert.notNull(dueOn, "dueOn")
        Assert.notNull(target, "target")
        Assert.isTrue(target.compareTo(Amount.ZERO) > 0, "target must be zero or positive, found " + target)

        jdbcTemplate.update("INSERT INTO mybank.goals(family_id, account_id, goal_id, description, due_on, target) VALUES (?::uuid, ?::uuid, ?::uuid, ?::text, ?::date, ?::numeric)",
            familyId.toSql, accountId.toSql, goalId.toSql, description.toSql, dueOn.toSql, target.toSql)
        eventBus.publish(GoalCreated(familyId, accountId, goalId, dueOn, target))
    }

    override def listGoals(familyId: FamilyId, accountId: AccountId): Set[Goal] = {
        Assert.notNull(familyId, "familyId")
        Assert.notNull(accountId, "accountId")

        val list = jdbcTemplate.query("" +
                "SELECT family_id, account_id, goal_id, description, due_on, target " +
                "FROM mybank.goals " +
                "WHERE family_id = ?::uuid " +
                "  AND account_id = ?::uuid", goalRowMapper, familyId.toSql, accountId.toSql)

        list.asScala.toSet
    }

    override def updateInterestRate(familyId: FamilyId, yearlyInterestRate: InterestRate): Unit = {
        jdbcTemplate.update("UPDATE mybank.families SET yearly_interest_rate = ?::numeric WHERE family_id = ?::uuid",
            yearlyInterestRate.toSql, familyId.toSql)
        eventBus.publish(InterestRateUpdated(familyId, yearlyInterestRate))
    }

    override def applyInterestsToAllFamilies(postedOn: LocalDate): Unit = {
        val accounts = findAccountsWithInterestRates.asScala.toSet
        val entries = applyInterests(accounts, postedOn, Description("interests"))
        postInterestEntriesToDatabase(postedOn, entries)
        publishInterestEntriesCreated(entries)
    }

    private def publishInterestEntriesCreated(entries: Set[Entry]) = {
        entries.map(entryToInterestPosted).foreach(eventBus.publish)
    }

    private[this] def postInterestEntriesToDatabase(postedOn: LocalDate, entries: Set[Entry]) = {
        val values = entries.toSeq.map(_ => "(?::uuid, ?::uuid, ?::uuid, ?::date, ?::text, ?::numeric)").mkString(", ")
        val args = entries.toSeq.flatMap { entry =>
            Array[AnyRef](
                entry.familyId.toSql,
                entry.accountId.toSql,
                entry.entryId.toSql,
                postedOn.toSql,
                entry.description.toSql,
                entry.amount.toSql)
        }

        jdbcTemplate.update(s"INSERT INTO mybank.entries(family_id, account_id, entry_id, posted_on, description, amount) VALUES $values", args: _*)
    }

    private[this] def entryToInterestPosted(entry: Entry) =
        InterestPosted(entry.familyId, entry.accountId, entry.postedOn, entry.amount)

    private[this] def applyInterests(accounts: Set[AccountWithInterest], postedOn: LocalDate, description: Description) = {
        accounts.map { account =>
            Entry(account.familyId, account.accountId, EntryId(UUID.randomUUID()), postedOn, description = description, account.interests)
        }
    }

    private def findAccountsWithInterestRates = {
        jdbcTemplate.query("" +
                "SELECT family_id, account_id, name, yearly_interest_rate, coalesce(sum(amount), 0) AS balance " +
                "FROM mybank.accounts " +
                "INNER JOIN mybank.families USING (family_id) " +
                "LEFT JOIN mybank.entries USING (family_id, account_id) " +
                "GROUP BY family_id, account_id, yearly_interest_rate", interestRowMapper)
    }

    override def receive(event: Event): Unit = event match {
        case FamilyCreated(familyId) => createFamily(familyId)
        case _ => () // do not react to other events: we're not interested
    }

    private[this] def createFamily(familyId: FamilyId): Unit = {
        jdbcTemplate.update("INSERT INTO mybank.families(family_id, yearly_interest_rate) VALUES (?::uuid, ?::numeric)",
            familyId.toSql, DEFAULT_INTEREST_RATE.toSql)
    }

    private[this] val DEFAULT_INTEREST_RATE = InterestRate(10)

    private case class AccountWithInterest(familyId: FamilyId, accountId: AccountId, name: AccountName, balance: Amount, yearlyInterestRate: InterestRate) {
        def interests: Amount = {
            val base = balance * yearlyInterestRate / 100 / 365
            if (balance.isNegative) {
                if (base > Amount.NEG_PENNY) {
                    Amount.NEG_PENNY
                } else {
                    base.ceilPennies
                }
            } else if (balance.isPositive) {
                if (base < Amount.PENNY) {
                    Amount.PENNY
                } else {
                    base.ceilPennies
                }
            } else {
                Amount.ZERO
            }
        }
    }

    private[this] val interestRowMapper: RowMapper[AccountWithInterest] = (rs: ResultSet, _: Int) => {
        val familyId = FamilyId(UUID.fromString(rs.getString("family_id")))
        val accountId = AccountId(UUID.fromString(rs.getString("account_id")))
        val name = AccountName(rs.getString("name"))
        val balance = Amount(rs.getBigDecimal("balance"))
        val yearlyInterestRate = InterestRate(rs.getBigDecimal("yearly_interest_rate"))

        AccountWithInterest(familyId, accountId, name, balance, yearlyInterestRate)
    }

    private[this] val accountRowMapper: RowMapper[Account] = (rs: ResultSet, _: Int) => {
        val familyId = FamilyId(UUID.fromString(rs.getString("family_id")))
        val accountId = AccountId(UUID.fromString(rs.getString("account_id")))
        val name = AccountName(rs.getString("name"))
        val balance = Amount(rs.getBigDecimal("balance"))

        Account(familyId, accountId, name, balance)
    }

    private[this] val entryRowMapper: RowMapper[Entry] = (rs: ResultSet, _: Int) => {
        val familyId = FamilyId(UUID.fromString(rs.getString("family_id")))
        val accountId = AccountId(UUID.fromString(rs.getString("account_id")))
        val entryId = EntryId(UUID.fromString(rs.getString("entry_id")))
        val postedOn = rs.getDate("posted_on").toLocalDate
        val description = Description(rs.getString("description"))
        val amount = Amount(rs.getBigDecimal("amount"))

        Entry(familyId, accountId, entryId, postedOn, description, amount)
    }

    private[this] val goalRowMapper: RowMapper[Goal] = (rs: ResultSet, _: Int) => {
        val familyId = FamilyId(UUID.fromString(rs.getString("family_id")))
        val accountId = AccountId(UUID.fromString(rs.getString("account_id")))
        val goalId = GoalId(UUID.fromString(rs.getString("goal_id")))
        val dueOn = rs.getDate("due_on").toLocalDate
        val description = GoalDescription(rs.getString("description"))
        val target = Amount(rs.getBigDecimal("target"))

        Goal(familyId, accountId, goalId, description, dueOn, target)
    }

    private[this] def createEntry(familyId: FamilyId, accountId: AccountId, postedOn: LocalDate, description: Description, amount: Amount) = {
        jdbcTemplate.update("INSERT INTO mybank.entries(family_id, account_id, posted_on, description, amount) VALUES (?::uuid, ?::uuid, ?::date, ?::text, ?::numeric)",
            familyId.toSql, accountId.toSql, postedOn.toSql, description.toSql, amount.toSql)

        eventBus.publish(EntryPosted(familyId, accountId, postedOn, amount))
    }
}
