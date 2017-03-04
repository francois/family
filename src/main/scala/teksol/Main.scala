package teksol

import java.time.LocalDate
import java.util.UUID
import java.util.concurrent.TimeUnit

import com.jolbox.bonecp.BoneCPDataSource
import org.slf4j.LoggerFactory
import teksol.domain.{FamilyId, FamilyName}
import teksol.infrastructure.EventBus
import teksol.mybank.domain._
import teksol.mybank.postgres.PostgresMyBankApp
import teksol.postgres.{PostgresEventBus, PostgresFamilyApp}

object Main extends Config {
    lazy val userName = System.getProperty("user.name")

    lazy val dataSource: BoneCPDataSource = {
        Class.forName("org.postgresql.Driver")

        val ds = new BoneCPDataSource()
        ds.setJdbcUrl("jdbc:postgresql://localhost:5432/" + userName)
        ds.setUsername(userName)
        ds.setPassword("")
        ds.setMinConnectionsPerPartition(1)
        ds.setMaxConnectionsPerPartition(4)
        ds.setIdleMaxAge(1, TimeUnit.DAYS)
        ds
    }

    def main(args: Array[String]): Unit = {
        val log = LoggerFactory.getLogger("main")

        log.info("Instantiating EventBus")
        val eventBus: EventBus = new PostgresEventBus(jdbcTemplate)

        log.info("Instantiating FamilyApp")
        val app = new PostgresFamilyApp(jdbcTemplate, eventBus)

        log.info("Booting My Bank")
        val myBankApp = new PostgresMyBankApp(jdbcTemplate, eventBus)
        eventBus.register(myBankApp)

        val smithFamilyId = FamilyId(UUID.randomUUID())

        log.info("Creating family")
        transactionTemplate.execute((_) => app.createFamily(smithFamilyId, FamilyName("Smith")))

        val johnAccountId = AccountId(UUID.randomUUID())
        transactionTemplate.execute((_) => {
            myBankApp.createAccount(smithFamilyId, johnAccountId, AccountName("John"))
            myBankApp.deposit(smithFamilyId, johnAccountId, LocalDate.now(), Description("Initial balance"), Amount(BigDecimal("44.49")))
        })

        val accounts: Set[Account] = transactionTemplate.execute((_) => myBankApp.listAccounts(smithFamilyId))
        accounts.foreach(log.info("{}", _))

        transactionTemplate.execute((_) => {
            myBankApp.withdraw(smithFamilyId, johnAccountId, LocalDate.now(), Description("buy candies"), Amount(BigDecimal("3.41")))
        })

        val entries: Set[Entry] = transactionTemplate.execute((_) => myBankApp.listAccountEntries(smithFamilyId, johnAccountId))
        entries.foreach(log.info("{}", _))

        transactionTemplate.execute((_) => myBankApp.createGoal(smithFamilyId, johnAccountId, GoalId(UUID.randomUUID()), GoalDescription("Pokedeck"), LocalDate.of(2017, 9, 1), Amount(60)))
        val goals: Set[Goal] = transactionTemplate.execute((_) => myBankApp.listGoals(smithFamilyId, johnAccountId))
        goals.foreach(log.info("{}", _))

        transactionTemplate.execute((_) => myBankApp.updateInterestRate(smithFamilyId, InterestRate(BigDecimal("12.5"))))

        transactionTemplate.execute((_) => myBankApp.applyInterestsToAllFamilies(LocalDate.now()))
    }
}
