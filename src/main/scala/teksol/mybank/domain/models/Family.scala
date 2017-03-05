package teksol.mybank.domain.models

import java.util.{Locale, UUID}

import teksol.domain.FamilyId
import teksol.infrastructure.EventBus
import teksol.mybank.domain.events.InterestRateChanged
import teksol.mybank.infrastructure.MyBankRepository

case class Family(familyId: FamilyId,
                  locale: Locale,
                  repository: MyBankRepository,
                  eventBus: EventBus) {
    def accounts: Set[Account] = repository.listAccounts(familyId)

    def changeYearlyInterestRate(yearlyInterestRate: InterestRate): Unit = {
        repository.changeYearlyInterestRate(familyId, yearlyInterestRate)
        eventBus.publish(InterestRateChanged(familyId, yearlyInterestRate))
    }

    def createAccount(name: AccountName): Account = {
        val account = Account(familyId, AccountId(UUID.randomUUID()), locale, name, Amount.ZERO, Amount.ZERO, repository, eventBus)
        repository.saveAccount(account)
        account
    }
}
