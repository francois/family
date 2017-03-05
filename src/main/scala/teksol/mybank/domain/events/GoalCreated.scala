package teksol.mybank.domain.events

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import teksol.domain.FamilyId
import teksol.infrastructure.{Event, ToJson}
import teksol.mybank.domain.models.{AccountId, Amount, GoalId}

case class GoalCreated(familyId: FamilyId, accountId: AccountId, goalId: GoalId, dueOn: LocalDate, target: Amount) extends Event {
    import teksol.infrastructure.Helpers._

    override def toJson: String = s"""{"family_id":${familyId.toJson},"account_id":${accountId.toJson},"due_on":${dueOn.toJson},"target":${target.toJson}}"""
}
