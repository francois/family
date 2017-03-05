package teksol.mybank

import java.time.LocalDate

import teksol.domain.FamilyId
import teksol.infrastructure.I18n
import teksol.mybank.domain._

trait MyBankApp {
    def createAccount(familyId: FamilyId, accountId: AccountId, name: AccountName)

    def listAccounts(familyId: FamilyId): Set[Account]

    def listAccountEntries(familyId: FamilyId, accountId: AccountId): Set[Entry]

    def deposit(familyId: FamilyId, accountId: AccountId, postedOn: LocalDate, description: Description, amount: Amount)

    def withdraw(familyId: FamilyId, accountId: AccountId, postedOn: LocalDate, description: Description, amount: Amount)

    def createGoal(familyId: FamilyId, accountId: AccountId, goalId: GoalId, name: GoalDescription, dueOn: LocalDate, target: Amount)

    def listGoals(familyId: FamilyId, accountId: AccountId): Set[Goal]

    def applyInterestsToAllFamilies(i18n: I18n, postedOn: LocalDate)

    def updateInterestRate(familyId: FamilyId, yearlyInterestRate: InterestRate)
}
