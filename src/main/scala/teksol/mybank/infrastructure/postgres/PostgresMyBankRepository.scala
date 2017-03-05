package teksol.mybank.infrastructure.postgres

import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.{Locale, UUID}

import org.springframework.jdbc.core.{JdbcTemplate, RowMapper}
import org.springframework.util.Assert
import teksol.domain.FamilyId
import teksol.infrastructure._
import teksol.mybank.domain.events.InterestRateChanged
import teksol.mybank.domain.models._
import teksol.mybank.infrastructure.MyBankRepository

import scala.collection.JavaConverters._
import scala.language.implicitConversions

class PostgresMyBankRepository(private[this] val jdbcTemplate: JdbcTemplate, private[this] val eventBus: EventBus) extends MyBankRepository {
    implicit def localDateToToSql(date: LocalDate): ToSql = new ToSql {
        Assert.notNull(date, "date")

        override def toSql: AnyRef = date.format(DateTimeFormatter.ISO_DATE)
    }

    override def findFamily(familyId: FamilyId): Family =
        jdbcTemplate.queryForObject("" +
                "SELECT family_id, locale " +
                "FROM mybank.families " +
                "WHERE family_id = ?::uuid " +
                "LIMIT 1", familyRowMapper, familyId.toSql)

    override def saveAccount(account: Account): Unit = {
        jdbcTemplate.update("INSERT INTO mybank.accounts(family_id, account_id, name, salary) VALUES (?::uuid, ?::uuid, ?::text, ?::numeric)",
            account.familyId.toSql, account.accountId.toSql, account.name.toSql, account.salary.toSql)
    }

    override def listAccounts(familyId: FamilyId): Set[Account] = {
        Assert.notNull(familyId, "familyId")

        val list = jdbcTemplate.query("" +
                "SELECT family_id, account_id, locale, name, salary, coalesce(sum(amount), 0) AS balance " +
                "FROM mybank.accounts " +
                "INNER JOIN mybank.families USING (family_id) " +
                "LEFT JOIN mybank.entries USING (family_id, account_id) " +
                "WHERE family_id = ?::uuid " +
                "GROUP BY family_id, account_id, locale, name, salary", accountRowMapper, familyId.toSql)

        list.asScala.toSet
    }

    override def listAccountEntries(account: Account): Set[Entry] = {
        val list = jdbcTemplate.query("" +
                "SELECT family_id, account_id, entry_id, posted_on, description, amount " +
                "FROM mybank.entries " +
                "WHERE family_id = ?::uuid AND account_id = ?::uuid", entryRowMapper, account.familyId.toSql, account.accountId.toSql)

        list.asScala.toSet
    }

    override def saveGoal(goal: Goal): Unit = {
        jdbcTemplate.update("INSERT INTO mybank.goals(family_id, account_id, goal_id, description, due_on, target) VALUES (?::uuid, ?::uuid, ?::uuid, ?::text, ?::date, ?::numeric)",
            goal.familyId.toSql, goal.accountId.toSql, goal.goalId.toSql, goal.description.toSql, goal.dueOn.toSql, goal.target.toSql)
    }

    override def listGoals(account: Account): Set[Goal] = {
        val list = jdbcTemplate.query("" +
                "SELECT family_id, account_id, goal_id, description, due_on, target " +
                "FROM mybank.goals " +
                "WHERE family_id = ?::uuid " +
                "  AND account_id = ?::uuid", goalRowMapper, account.familyId.toSql, account.accountId.toSql)

        list.asScala.toSet
    }

    override def changeYearlyInterestRate(familyId: FamilyId, yearlyInterestRate: InterestRate): Unit = {
        jdbcTemplate.update("UPDATE mybank.families SET yearly_interest_rate = ?::numeric WHERE family_id = ?::uuid",
            yearlyInterestRate.toSql, familyId.toSql)
    }

    override def changeSalary(account: Account, newSalary: Salary): Unit = {
        jdbcTemplate.update("UPDATE mybank.accounts SET salary = ?::numeric WHERE family_id = ?::uuid AND account_id = ?::uuid",
            newSalary.toSql, account.familyId.toSql, account.accountId.toSql)
    }

    override def saveEntry(entry: Entry): Unit = saveEntries(Set(entry))

    override def saveEntries(entries: Set[Entry]): Unit = {
        val placeholders = entries.toSeq.map(_ => "(?::uuid, ?::uuid, ?::uuid, ?::date, ?::text, ?::numeric)").mkString(", ")
        val args = entries.toSeq.flatMap { entry =>
            Array[AnyRef](
                entry.familyId.toSql,
                entry.accountId.toSql,
                entry.entryId.toSql,
                entry.postedOn.toSql,
                entry.description.toSql,
                entry.amount.toSql)
        }

        jdbcTemplate.update(s"INSERT INTO mybank.entries(family_id, account_id, entry_id, posted_on, description, amount) VALUES $placeholders", args: _*)
    }

    override def listAccountsAndTheirInterestRates: Set[AccountWithInterest] = {
        val list = jdbcTemplate.query("" +
                "SELECT family_id, account_id, name, yearly_interest_rate, locale, salary, coalesce(sum(amount), 0) AS balance " +
                "FROM mybank.accounts " +
                "INNER JOIN mybank.families USING (family_id) " +
                "LEFT JOIN mybank.entries USING (family_id, account_id) " +
                "GROUP BY family_id, account_id, yearly_interest_rate, locale, salary", interestRowMapper)
        list.asScala.toSet
    }

    override def saveFamily(family: Family): Unit = {
        jdbcTemplate.update("INSERT INTO mybank.families(family_id, locale, yearly_interest_rate) VALUES (?::uuid, ?::text, ?::numeric)",
            family.familyId.toSql, family.locale.toLanguageTag, DEFAULT_INTEREST_RATE.toSql)
    }

    // Default to 10% interest rate for new families.
    private[this] val DEFAULT_INTEREST_RATE = InterestRate(10)

    private[this] val familyRowMapper: RowMapper[Family] = (rs: ResultSet, _: Int) => {
        val familyId = FamilyId(UUID.fromString(rs.getString("family_id")))
        val locale = Locale.forLanguageTag(rs.getString("locale"))
        Family(familyId, locale, this, eventBus)
    }

    private[this] val interestRowMapper: RowMapper[AccountWithInterest] = (rs: ResultSet, _: Int) => {
        val familyId = FamilyId(UUID.fromString(rs.getString("family_id")))
        val accountId = AccountId(UUID.fromString(rs.getString("account_id")))
        val name = AccountName(rs.getString("name"))
        val locale = Locale.forLanguageTag(rs.getString("locale"))
        val salary = Amount(rs.getBigDecimal("salary"))
        val balance = Amount(rs.getBigDecimal("balance"))
        val yearlyInterestRate = InterestRate(rs.getBigDecimal("yearly_interest_rate"))

        AccountWithInterest(familyId, accountId, locale, name, salary, balance, yearlyInterestRate, this, eventBus)
    }

    private[this] val accountRowMapper: RowMapper[Account] = (rs: ResultSet, _: Int) => {
        val familyId = FamilyId(UUID.fromString(rs.getString("family_id")))
        val accountId = AccountId(UUID.fromString(rs.getString("account_id")))
        val name = AccountName(rs.getString("name"))
        val salary = Amount(rs.getBigDecimal("salary"))
        val balance = Amount(rs.getBigDecimal("balance"))
        val locale = Locale.forLanguageTag(rs.getString("locale"))

        Account(familyId, accountId, locale, name, salary, balance, this, eventBus)
    }

    private[this] val entryRowMapper: RowMapper[Entry] = (rs: ResultSet, _: Int) => {
        val familyId = FamilyId(UUID.fromString(rs.getString("family_id")))
        val accountId = AccountId(UUID.fromString(rs.getString("account_id")))
        val entryId = EntryId(UUID.fromString(rs.getString("entry_id")))
        val postedOn = rs.getDate("posted_on").toLocalDate
        val description = EntryDescription(rs.getString("description"))
        val amount = Amount(rs.getBigDecimal("amount"))

        Entry(familyId, accountId, entryId, postedOn, description, amount, this, eventBus)
    }

    private[this] val goalRowMapper: RowMapper[Goal] = (rs: ResultSet, _: Int) => {
        val familyId = FamilyId(UUID.fromString(rs.getString("family_id")))
        val accountId = AccountId(UUID.fromString(rs.getString("account_id")))
        val goalId = GoalId(UUID.fromString(rs.getString("goal_id")))
        val dueOn = rs.getDate("due_on").toLocalDate
        val description = GoalDescription(rs.getString("description"))
        val target = Amount(rs.getBigDecimal("target"))

        Goal(familyId, accountId, goalId, description, dueOn, target, this, eventBus)
    }
}
