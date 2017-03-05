package teksol

import java.time.LocalDate
import java.util.concurrent.TimeUnit
import java.util.{Locale, UUID}

import com.jolbox.bonecp.BoneCPDataSource
import org.slf4j.LoggerFactory
import teksol.domain.{FamilyId, FamilyName}
import teksol.infrastructure.{EventBus, InMemoryI18n}
import teksol.mybank.domain._
import teksol.mybank.postgres.PostgresMyBankApp
import teksol.postgres.{PostgresEventBus, PostgresFamilyApp}

object Main extends Config {
    lazy val userName: String = Option(System.getProperty("user.name")).get

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

        val en_US = Locale.US
        val fr_CA = Locale.CANADA_FRENCH
        val fr_FR = Locale.FRANCE
        val i18n = new InMemoryI18n(Map(
            en_US -> Map(
                "interests.none" -> "No interests for period",
                "interests.negative" -> "Negative interests on $ %{balance} balance, at a rate of %{rate}",
                "interests.positive" -> "Interests on $ %{balance} balance, at a rate of %{rate}"),
            fr_CA -> Map(
                "interests.none" -> "Aucun intérêts pour la période",
                "interests.negative" -> "Intérêts négatifs calculés sur un solde de %{balance} $ et un taux de %{rate}",
                "interests.positive" -> "Intérêts calculés sur un solde de %{balance} $ et un taux de %{rate}"),
            fr_FR -> Map(
                "interests.none" -> "Aucun intérêts pour la période",
                "interests.negative" -> "Intérêts négatifs calculés sur un solde de %{balance} $ et un taux de %{rate}",
                "interests.positive" -> "Intérêts calculés sur un solde de %{balance} $ et un taux de %{rate}")))

        val smithFamilyId = FamilyId(UUID.randomUUID())

        log.info("Creating family")
        transactionTemplate.execute((_) => app.createFamily(smithFamilyId, FamilyName("Smith"), fr_CA))

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
        (1 to 10).foreach { day =>
            transactionTemplate.execute((_) => myBankApp.applyInterestsToAllFamilies(i18n, LocalDate.now().plusDays(day)))
        }
    }
}
