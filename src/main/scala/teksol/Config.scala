package teksol

import javax.sql.DataSource

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.support.TransactionTemplate

trait Config {
    def dataSource: DataSource

    lazy val jdbcTemplate: JdbcTemplate = new JdbcTemplate(dataSource)

    lazy val transactionManager: PlatformTransactionManager = new DataSourceTransactionManager(dataSource)
    lazy val transactionTemplate: TransactionTemplate = new TransactionTemplate(transactionManager)
}
