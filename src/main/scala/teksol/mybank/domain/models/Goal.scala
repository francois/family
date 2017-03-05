package teksol.mybank.domain.models

import java.time.LocalDate

import org.springframework.util.Assert
import teksol.domain.FamilyId
import teksol.infrastructure.{EventBus, ToJson}
import teksol.mybank.infrastructure.MyBankRepository

case class Goal(familyId: FamilyId,
                accountId: AccountId,
                goalId: GoalId,
                description: GoalDescription,
                dueOn: LocalDate,
                target: Amount,
                repository: MyBankRepository,
                eventBus: EventBus) extends ToJson {

    import teksol.infrastructure.Helpers._

    Assert.isTrue(target.isPositive, "target amount must be > 0")

    override def toJson: String =
        s"""{"family_id":${familyId.toJson},
           |"account_id":${accountId.toJson},
           |"goal_id":${goalId.toJson},
           |"description":${description.toJson},
           |"due_on":${dueOn.toJson},
           |"target":${target.toJson}}""".stripMargin.replaceAll("\n", "")
}
