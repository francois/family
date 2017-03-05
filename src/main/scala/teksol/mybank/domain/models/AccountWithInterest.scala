package teksol.mybank.domain.models

import java.util.Locale

import teksol.domain.FamilyId
import teksol.infrastructure.EventBus
import teksol.mybank.infrastructure.MyBankRepository

case class AccountWithInterest(familyId: FamilyId,
                               accountId: AccountId,
                               locale: Locale,
                               name: AccountName,
                               salary: Amount,
                               balance: Amount,
                               yearlyInterestRate: InterestRate,
                               repository: MyBankRepository,
                               eventBus: EventBus) {
    def interests: Amount = {
        val base = balance * yearlyInterestRate / 100 / 365
        if (balance.isNegative) {
            if (base > Amount.NEG_PENNY) {
                Amount.NEG_PENNY
            } else {
                base.ceilPennies
            }
        } else if (balance.isPositive) {
            if (base < Amount.PENNY) {
                Amount.PENNY
            } else {
                base.ceilPennies
            }
        } else {
            Amount.ZERO
        }
    }
}
