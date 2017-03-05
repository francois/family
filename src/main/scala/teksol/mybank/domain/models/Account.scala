package teksol.mybank.domain.models

import java.time.LocalDate
import java.util.UUID

import org.springframework.util.Assert
import teksol.domain.FamilyId
import teksol.infrastructure.EventBus
import teksol.mybank.domain.events.EntryPosted
import teksol.mybank.infrastructure.MyBankRepository

case class Account(familyId: FamilyId,
                   accountId: AccountId,
                   name: AccountName,
                   balance: Amount,
                   repository: MyBankRepository,
                   eventBus: EventBus) {
    def entries: Set[Entry] = repository.listAccountEntries(this)

    def postDeposit(postedOn: LocalDate, description: EntryDescription, amount: Amount): Unit = {
        Assert.notNull(amount, "amount")
        Assert.isTrue(amount.isPositive, "amount must be positive")

        val entry = Entry(familyId, accountId, EntryId(UUID.randomUUID), postedOn, description, amount, repository, eventBus)
        repository.saveEntry(entry)
        eventBus.publish(EntryPosted(familyId, accountId, entry.entryId, postedOn, amount))
    }

    def postWithdrawal(postedOn: LocalDate, description: EntryDescription, amount: Amount): Unit = {
        Assert.notNull(amount, "amount")
        Assert.isTrue(amount.isPositive, "amount must be positive")

        val entry = Entry(familyId, accountId, EntryId(UUID.randomUUID), postedOn, description, amount.negate, repository, eventBus)
        repository.saveEntry(entry)
        eventBus.publish(EntryPosted(familyId, accountId, entry.entryId, postedOn, amount.negate))
    }

    def goals: Set[Goal] = repository.listGoals(this)

    def createGoal(description: GoalDescription, dueOn: LocalDate, target: Amount): Goal = {
        val goal = Goal(familyId, accountId, GoalId(UUID.randomUUID()), description, dueOn, target, repository, eventBus)
        repository.saveGoal(goal)
        goal
    }
}
