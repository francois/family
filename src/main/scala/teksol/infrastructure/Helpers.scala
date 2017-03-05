package teksol.infrastructure

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object Helpers {
    implicit def localDateToJson(date: LocalDate): ToJson = new ToJson {
        override def toJson: String = "\"" + date.format(DateTimeFormatter.ISO_DATE) + "\""
    }
    implicit def localeToJson(locale: Locale): ToJson = new ToJson {
        override def toJson: String = "\"" + locale.toLanguageTag + "\""
    }
}
