package teksol

import java.time.LocalDate
import java.util.concurrent.TimeUnit
import java.util.{Locale, UUID}

import com.jolbox.bonecp.BoneCPDataSource
import org.slf4j.LoggerFactory
import teksol.domain.{FamilyId, FamilyName}
import teksol.infrastructure.{EventBus, InMemoryI18n}
import teksol.mybank.domain.models._
import teksol.mybank.domain.services.MyBankAppService
import teksol.mybank.infrastructure.postgres.PostgresMyBankRepository
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

        log.info("Booting My Bank Repository")
        val myBankRepository = new PostgresMyBankRepository(jdbcTemplate, eventBus)
        val myBankService = new MyBankAppService(myBankRepository, eventBus)
        eventBus.register(myBankService)

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

        transactionTemplate.execute((_) => {
            val family = myBankService.findFamily(smithFamilyId)
            val account = family.createAccount(AccountName("John"))
            account.postDeposit(LocalDate.now(), EntryDescription("Initial balance"), Amount(BigDecimal("44.49")))
        })

        val accounts: Set[Account] = transactionTemplate.execute((_) => myBankService.findFamily(smithFamilyId).accounts)
        accounts.foreach(log.info("{}", _))

        val johnsAccount = accounts.head
        transactionTemplate.execute((_) => {
            johnsAccount.postWithdrawal(LocalDate.now(), EntryDescription("buy candies"), Amount(BigDecimal("3.41")))
        })

        val entries: Set[Entry] = transactionTemplate.execute((_) => johnsAccount.entries)
        entries.foreach(log.info("{}", _))

        transactionTemplate.execute((_) => johnsAccount.createGoal(GoalDescription("Pokedeck"), LocalDate.now().plusDays(13), Amount(60)))
        val goals: Set[Goal] = transactionTemplate.execute((_) => johnsAccount.goals)
        goals.foreach(log.info("{}", _))

        transactionTemplate.execute((_) => myBankService.findFamily(smithFamilyId).changeYearlyInterestRate(yearlyInterestRate = InterestRate(12.5)))

        (1 to 10).foreach { day =>
            transactionTemplate.execute((_) => myBankService.applyInterestsToAllFamilies(i18n, LocalDate.now().plusDays(day)))
        }
    }
}
