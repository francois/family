package teksol.mybank.infrastructure

import teksol.domain.FamilyId
import teksol.infrastructure.Event
import teksol.mybank.domain.InterestRate

case class InterestRateUpdated(familyId: FamilyId, yearlyInterestRate: InterestRate) extends Event {
    override def toJson: String = s"""{"family_id":${familyId.toJson},"yearly_interest_rate":${yearlyInterestRate.toJson}}"""
}
