package teksol.mybank.domain.models

import java.util.{Locale, UUID}

import teksol.domain.FamilyId
import teksol.infrastructure.EventBus
import teksol.mybank.infrastructure.MyBankRepository

case class Family(familyId: FamilyId,
                  locale: Locale,
                  repository: MyBankRepository,
                  eventBus: EventBus) {
    def accounts: Set[Account] = repository.listAccounts(familyId)

    def updateInterestRate(yearlyInterestRate: InterestRate): Unit = repository.updateInterestRate(familyId, yearlyInterestRate)

    def createAccount(name: AccountName): Account = {
        val account = Account(familyId, AccountId(UUID.randomUUID()), name, Amount.ZERO, repository, eventBus)
        repository.saveAccount(account)
        account
    }
}
