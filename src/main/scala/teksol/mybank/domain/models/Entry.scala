package teksol.mybank.domain.models

import java.time.LocalDate

import teksol.domain.FamilyId
import teksol.infrastructure.EventBus
import teksol.mybank.infrastructure.MyBankRepository

case class Entry(familyId: FamilyId,
                 accountId: AccountId,
                 entryId: EntryId,
                 postedOn: LocalDate,
                 description: EntryDescription,
                 amount: Amount,
                 repository: MyBankRepository,
                 eventBus: EventBus)
