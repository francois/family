package teksol.mybank.domain

import java.time.LocalDate

import teksol.domain.FamilyId

case class Goal(familyId: FamilyId, accountId: AccountId, goalId: GoalId, name: GoalName, dueOn: LocalDate, target: Amount)
