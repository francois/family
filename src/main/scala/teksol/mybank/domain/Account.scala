package teksol.mybank.domain

import teksol.domain.FamilyId

case class Account(familyId: FamilyId, accountId: AccountId, name: AccountName, balance: Amount)
