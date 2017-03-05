package teksol.mybank.domain.events

import teksol.domain.FamilyId
import teksol.infrastructure.Event
import teksol.mybank.domain.models.AccountId

case class AccountCreated(familyId: FamilyId, accountId: AccountId) extends Event {
    override def toJson: String = s"""{"family_id":${familyId.toJson},"account_id":${accountId.toJson}}"""
}
