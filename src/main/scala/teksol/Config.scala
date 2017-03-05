package teksol

import java.util.Locale
import javax.sql.DataSource

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import teksol.infrastructure.{EventBus, InMemoryI18n}
import teksol.mybank.domain.services.MyBankAppService
import teksol.mybank.infrastructure.postgres.PostgresMyBankRepository
import teksol.postgres.{PostgresEventBus, PostgresFamilyApp}

trait Config {
    def dataSource: DataSource

    lazy val jdbcTemplate: JdbcTemplate = new JdbcTemplate(dataSource)

    lazy val transactionManager: PlatformTransactionManager = new DataSourceTransactionManager(dataSource)
    lazy val transactionTemplate: TransactionTemplate = new TransactionTemplate(transactionManager)

    lazy val eventBus: EventBus = new PostgresEventBus(jdbcTemplate)
    lazy val app = new PostgresFamilyApp(jdbcTemplate, eventBus)
    lazy val myBankRepository = new PostgresMyBankRepository(jdbcTemplate, eventBus)
    lazy val myBankService = {
        val service = new MyBankAppService(myBankRepository, eventBus)
        eventBus.register(service)
        service
    }

    val en_US = Locale.US
    val fr_CA = Locale.CANADA_FRENCH
    val fr_FR = Locale.FRANCE

    lazy val i18n = new InMemoryI18n(Map(
        en_US -> Map(
            "salary.none" -> "No completed chores this period",
            "salary.positive" -> "%{numUnitsCompleted} completed this week",
            "salary.negative" -> "%{numUnitsCompleted} completed this week",
            "interests.none" -> "No interests for period",
            "interests.negative" -> "Negative interests on $ %{balance} balance, at a rate of %{rate}",
            "interests.positive" -> "Interests on $ %{balance} balance, at a rate of %{rate}"),
        fr_CA -> Map(
            "salary.none" -> "Aucune tâche ménagères complétées cette semaine",
            "salary.positive" -> "%{numUnitsCompleted} tâches ménagères complétées cette semaine",
            "salary.negative" -> "%{numUnitsCompleted} tâches ménagères complétées cette semaine",
            "interests.none" -> "Aucun intérêts pour la période",
            "interests.negative" -> "Intérêts négatifs calculés sur un solde de %{balance} $ et un taux de %{rate}",
            "interests.positive" -> "Intérêts calculés sur un solde de %{balance} $ et un taux de %{rate}"),
        fr_FR -> Map(
            "salary.none" -> "Aucune tâche ménagères complétées cette semaine",
            "salary.positive" -> "%{numUnitsCompleted} tâches ménagères complétées cette semaine",
            "salary.negative" -> "%{numUnitsCompleted} tâches ménagères complétées cette semaine",
            "interests.none" -> "Aucun intérêts pour la période",
            "interests.negative" -> "Intérêts négatifs calculés sur un solde de %{balance} $ et un taux de %{rate}",
            "interests.positive" -> "Intérêts calculés sur un solde de %{balance} $ et un taux de %{rate}")))
}
