package teksol.mybank.postgres

import java.sql.ResultSet
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

import org.springframework.jdbc.core.{JdbcTemplate, RowMapper}
import org.springframework.util.Assert
import teksol.domain.FamilyId
import teksol.infrastructure.ToSql
import teksol.mybank.MyBankApp
import teksol.mybank.domain._

import scala.collection.JavaConverters._

class PostgresMyBankApp(private[this] val jdbcTemplate: JdbcTemplate) extends MyBankApp {
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
    }

    override def listAccounts(familyId: FamilyId): Set[Account] = {
        Assert.notNull(familyId, "familyId")

        val list = jdbcTemplate.query("" +
                "SELECT family_id, account_id, name, coalesce(sum(amount), 0) AS balance " +
                "FROM mybank.accounts " +
                "LEFT JOIN mybank.entries USING (family_id, account_id) " +
                "WHERE family_id = ?::uuid " +
                "GROUP BY family_id, account_id, name", familyRowMapper, familyId.toSql)

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

        createEntry(familyId, accountId, postedOn, description, amount * Amount.MINUS_ONE)
    }

    override def createGoal(familyId: FamilyId, accountId: AccountId, goalId: GoalId, name: GoalName, dueOn: LocalDate, target: Amount): Unit = {
        Assert.notNull(familyId, "familyId")
        Assert.notNull(accountId, "accountId")
        Assert.notNull(goalId, "goalId")
        Assert.notNull(name, "name")
        Assert.notNull(dueOn, "dueOn")
        Assert.notNull(target, "target")
        Assert.isTrue(target.compareTo(Amount.ZERO) > 0, "target must be zero or positive, found " + target)

        jdbcTemplate.update("INSERT INTO mybank.goals(family_id, account_id, goal_id, name, due_on, target) VALUES (?::uuid, ?::uuid, ?::uuid, ?::text, ?::date, ?::numeric)",
            familyId.toSql, accountId.toSql, goalId.toSql, name.toSql, dueOn.toSql, target.toSql)
    }

    override def listGoals(familyId: FamilyId, accountId: AccountId): Set[Goal] = {
        Assert.notNull(familyId, "familyId")
        Assert.notNull(accountId, "accountId")

        val list = jdbcTemplate.query("" +
                "SELECT family_id, account_id, goal_id, name, due_on, target " +
                "FROM mybank.goals " +
                "WHERE family_id = ?::uuid " +
                "  AND account_id = ?::uuid", goalRowMapper, familyId.toSql, accountId.toSql)

        list.asScala.toSet
    }

    override def applyInterestsToAllFamilies(postedOn: LocalDate): Unit = ()

    private[this] val familyRowMapper: RowMapper[Account] = (rs: ResultSet, _: Int) => {
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
        val name = GoalName(rs.getString("name"))
        val target = Amount(rs.getBigDecimal("target"))

        Goal(familyId, accountId, goalId, name, dueOn, target)
    }

    private[this] def createEntry(familyId: FamilyId, accountId: AccountId, postedOn: LocalDate, description: Description, amount: Amount) = {
        jdbcTemplate.update("INSERT INTO mybank.entries(family_id, account_id, posted_on, description, amount) VALUES (?::uuid, ?::uuid, ?::date, ?::text, ?::numeric)",
            familyId.toSql, accountId.toSql, postedOn.toSql, description.toSql, amount.toSql)
    }
}
