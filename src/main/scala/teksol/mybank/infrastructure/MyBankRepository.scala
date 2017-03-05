package teksol.mybank.infrastructure

import teksol.domain.FamilyId
import teksol.mybank.domain.models._

trait MyBankRepository {
    def findFamily(familyId: FamilyId): Option[Family]

    def findAccount(familyId: FamilyId, accountId: AccountId): Option[Account]

    def listAccounts(familyId: FamilyId): Set[Account]

    def listAccountEntries(account: Account): Set[Entry]

    def listGoals(account: Account): Set[Goal]

    def listAccountsAndTheirInterestRates: Set[AccountWithInterest]

    def saveFamily(family: Family): Unit

    def saveAccount(account: Account): Unit

    def saveGoal(goal: Goal): Unit

    def saveEntry(entry: Entry): Unit

    def saveEntries(entries: Set[Entry]): Unit

    def changeYearlyInterestRate(familyId: FamilyId, yearlyInterestRate: InterestRate): Unit

    def changeSalary(account: Account, newSalary: Salary): Unit
}
