package teksol.mybank.infrastructure

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import teksol.domain.FamilyId
import teksol.infrastructure.{Event, ToJson}
import teksol.mybank.domain.{AccountId, Amount, GoalId}

case class GoalCreated(familyId: FamilyId, accountId: AccountId, goalId: GoalId, dueOn: LocalDate, target: Amount) extends Event {
    implicit def localDateToJson(date: LocalDate): ToJson = new ToJson {
        override def toJson: String = "\"" + date.format(DateTimeFormatter.ISO_DATE) + "\""
    }

    override def toJson: String = s"""{"family_id":${familyId.toJson},"account_id":${accountId.toJson},"due_on":${dueOn.toJson},"target":${target.toJson}}"""
}
