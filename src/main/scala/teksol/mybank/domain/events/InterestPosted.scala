package teksol.mybank.domain.events

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import teksol.domain.FamilyId
import teksol.infrastructure.{Event, ToJson}
import teksol.mybank.domain.models.{AccountId, Amount}

import scala.language.implicitConversions

case class InterestPosted(familyId: FamilyId, accountId: AccountId, postedOn: LocalDate, amount: Amount) extends Event {
    implicit def localDateToJson(date: LocalDate): ToJson = new ToJson {
        override def toJson: String = "\"" + date.format(DateTimeFormatter.ISO_DATE) + "\""
    }

    override def toJson: String = s"""{"family_id":${familyId.toJson},"account_id":${accountId.toJson},"posted_on":${postedOn.toJson},"amount":${amount.toJson}}"""
}