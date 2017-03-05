package teksol.mybank.domain.events

import java.time.LocalDate

import teksol.domain.FamilyId
import teksol.infrastructure.Event
import teksol.mybank.domain.models.{AccountId, Amount, EntryId}

import scala.language.implicitConversions

case class EntryPosted(familyId: FamilyId, accountId: AccountId, entryId: EntryId, postedOn: LocalDate, amount: Amount) extends Event {

    import teksol.infrastructure.Helpers._

    override def toJson: String = s"""{"family_id":${familyId.toJson},"account_id":${accountId.toJson},"entry_id":${entryId.toJson},"posted_on":${postedOn.toJson},"amount":${amount.toJson}}"""
}
