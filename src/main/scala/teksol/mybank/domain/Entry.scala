package teksol.mybank.domain

import java.time.LocalDate

import teksol.domain.FamilyId

case class Entry(familyId: FamilyId, accountId: AccountId, entryId: EntryId, postedOn: LocalDate, description: Description, amount: Amount)
