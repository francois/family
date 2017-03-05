package teksol.infrastructure

import java.util.Locale

import org.springframework.util.Assert
import teksol.mybank.domain.{Amount, InterestRate}

trait I18n {
    def translate(locale: Locale, key: String, default: Option[String] = None, params: Map[String, String]): Option[String]

    def amountWithDelimiter(locale: Locale, amount: Amount): String

    def numberToPercentage(locale: Locale, interestRate: InterestRate): String
}

class InMemoryI18n(private[this] val dictionary: Map[Locale, Map[String, String]]) extends I18n {
    override def translate(locale: Locale, key: String, default: Option[String] = None, params: Map[String, String] = Map.empty): Option[String] = {
        Assert.notNull(locale, "locale")
        Assert.notNull(key, "key")

        dictionary.get(locale) match {
            case None => default
            case Some(translations) =>
                translations.get(key) match {
                    case None => default
                    case Some(value) =>
                        val result = params.foldLeft(value) {
                            case (memo, (param, replacement)) =>
                                memo.replaceAll(s"""%[{]$param[}]""", replacement)
                        }
                        Some(result)
                }
        }
    }

    override def numberToPercentage(locale: Locale, interestRate: InterestRate): String = {
        Assert.notNull(locale, "locale")
        Assert.notNull(interestRate, "interestRate")

        "%,.2f %%".formatLocal(locale, interestRate.value)
    }

    override def amountWithDelimiter(locale: Locale, amount: Amount): String = {
        Assert.notNull(locale, "locale")
        Assert.notNull(amount, "value")

        "%,.2f".formatLocal(locale, amount.value.bigDecimal)
    }
}
