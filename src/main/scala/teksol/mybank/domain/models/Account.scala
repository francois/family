package teksol.mybank.domain.models

import java.time.LocalDate
import java.util.{Locale, UUID}

import org.springframework.util.Assert
import teksol.domain.FamilyId
import teksol.infrastructure.{EventBus, I18n, ToJson}
import teksol.mybank.domain.events.{EntryPosted, SalaryChanged, SalaryPosted}
import teksol.mybank.infrastructure.MyBankRepository

/**
  * Represents a "bank account" and person, merged together.
  *
  * @param familyId   This account's family identifier.
  * @param accountId  This account's identifier.
  * @param name       A name to identify this account.
  * @param salary     This person's per-unit salary. Unit is left at the discretion of the humans outside the system.
  * @param balance    This account's current balance in their account.
  * @param repository The repository from which this account came from.
  * @param eventBus   The event bus on which new events must be posted.
  */
case class Account(familyId: FamilyId,
                   accountId: AccountId,
                   locale: Locale,
                   name: AccountName,
                   salary: Amount,
                   balance: Amount,
                   repository: MyBankRepository,
                   eventBus: EventBus) extends ToJson {
    implicit def localeToJson(locale: Locale): ToJson = new ToJson {
        override def toJson: String = "\"" + locale.toLanguageTag + "\""
    }

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

    def changeSalaryTo(newSalary: Salary): Unit = {
        repository.changeSalary(this, newSalary)
        eventBus.publish(SalaryChanged(familyId, accountId, newSalary))
    }

    def postSalary(i18n: I18n, postedOn: LocalDate, numUnitsCompleted: Int): Unit = {
        Assert.notNull(i18n)
        Assert.notNull(postedOn)
        Assert.notNull(numUnitsCompleted)

        val amount = (salary * numUnitsCompleted).ceilPennies
        val i18nKey = amount match {
            case Amount.ZERO => "salary.none"
            case n if n.isNegative => "salary.negative"
            case _ => "salary.positive"
        }
        val description = i18n.translate(locale, i18nKey, params = Map("numUnitsCompleted" -> numUnitsCompleted.toString)).map(EntryDescription.apply).get
        val entry = Entry(familyId, accountId, EntryId(UUID.randomUUID()), postedOn, description, amount, repository, eventBus)
        repository.saveEntry(entry)
        eventBus.publish(SalaryPosted(entry.familyId, entry.accountId, entry.entryId, entry.postedOn, entry.amount))
    }

    def goals: Set[Goal] = repository.listGoals(this)

    def createGoal(description: GoalDescription, dueOn: LocalDate, target: Amount): Goal = {
        val goal = Goal(familyId, accountId, GoalId(UUID.randomUUID()), description, dueOn, target, repository, eventBus)
        repository.saveGoal(goal)
        goal
    }

    override def toJson: String = {
        s"""{"family_id":${familyId.toJson},
           |"account_id":${accountId.toJson},
           |"locale":${locale.toJson},
           |"name":${name.toJson},
           |"salary":${salary.toJson},
           |"balance":${balance.toJson}}""".stripMargin.replaceAll("\n", "")
    }
}
