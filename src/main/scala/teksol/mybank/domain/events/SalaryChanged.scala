package teksol.mybank.domain.events

import teksol.domain.FamilyId
import teksol.infrastructure.Event
import teksol.mybank.domain.models.{AccountId, Salary}

case class SalaryChanged(familyId: FamilyId, accountId: AccountId, newSalary: Salary) extends Event {
    override def toJson: String = s"""{"family_id":${familyId.toJson},"account_id":${accountId.toJson},"new_salary":${newSalary.toJson}}"""
}
