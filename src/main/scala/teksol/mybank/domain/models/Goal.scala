package teksol.mybank.domain.models

import java.time.LocalDate

import org.springframework.util.Assert
import teksol.domain.FamilyId
import teksol.infrastructure.EventBus
import teksol.mybank.infrastructure.MyBankRepository

case class Goal(familyId: FamilyId,
                accountId: AccountId,
                goalId: GoalId,
                description: GoalDescription,
                dueOn: LocalDate,
                target: Amount,
                repository: MyBankRepository,
                eventBus: EventBus) {
    Assert.isTrue(target.isPositive, "target amount must be > 0")
}
