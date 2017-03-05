package teksol.mybank.domain.models

import java.time.LocalDate

import teksol.domain.FamilyId
import teksol.infrastructure.{EventBus, ToJson}
import teksol.mybank.infrastructure.MyBankRepository

case class Entry(familyId: FamilyId,
                 accountId: AccountId,
                 entryId: EntryId,
                 postedOn: LocalDate,
                 description: EntryDescription,
                 amount: Amount,
                 repository: MyBankRepository,
                 eventBus: EventBus) extends ToJson {
    import teksol.infrastructure.Helpers._

    override def toJson: String =
        s"""{"family_id":${familyId.toJson},
           |"account_id":${accountId.toJson},
           |"entry_id":${entryId.toJson},
           |"posted_on":${postedOn.toJson},
           |"description":${description.toJson},
           |"amount":${amount.toJson}}""".stripMargin.replaceAll("\n", "")

}
