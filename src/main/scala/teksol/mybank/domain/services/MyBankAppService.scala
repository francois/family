package teksol.mybank.domain.services

import java.time.LocalDate
import java.util.UUID

import com.sun.org.glassfish.gmbal.Description
import teksol.domain.FamilyId
import teksol.infrastructure._
import teksol.mybank.domain.events.InterestPosted
import teksol.mybank.domain.models._
import teksol.mybank.infrastructure.MyBankRepository

class MyBankAppService(private[this] val repository: MyBankRepository, private[this] val eventBus: EventBus) extends EventBusConsumer {
    def findFamily(familyId: FamilyId): Family = repository.findFamily(familyId)

    def applyInterestsToAllFamilies(i18n: I18n, postedOn: LocalDate): Unit = {
        def publishInterestEntriesCreated(entries: Set[Entry]) =
            entries.map(entryToInterestPosted).foreach(eventBus.publish)

        def entryToInterestPosted(entry: Entry) =
            InterestPosted(entry.familyId, entry.accountId, entry.postedOn, entry.amount)

        def buildEntriesForInterestBearingAccounts(accounts: Set[AccountWithInterest]) = {
            def buildDescription(account: AccountWithInterest) = {
                val formattedBalance = i18n.amountWithDelimiter(account.locale, account.balance)
                val formattedRate = i18n.numberToPercentage(account.locale, account.yearlyInterestRate)
                val i18nKey = account.balance match {
                    case Amount.ZERO => "interests.none"
                    case balance if balance.isNegative => "interests.negative"
                    case _ => "interests.positive"
                }

                i18n.translate(account.locale, i18nKey, params = Map("rate" -> formattedRate, "balance" -> formattedBalance)).map(EntryDescription.apply).get
            }

            accounts.map { account =>
                Entry(account.familyId, account.accountId, EntryId(UUID.randomUUID()), postedOn, buildDescription(account), account.interests, repository, eventBus)
            }
        }

        val accounts = repository.listAccountsAndTheirInterestRates
        val entries = buildEntriesForInterestBearingAccounts(accounts)
        repository.saveEntries(entries)
        publishInterestEntriesCreated(entries)
    }

    override def receive(event: Event): Unit = event match {
        case FamilyCreated(familyId, locale) => repository.saveFamily(Family(familyId, locale, repository, eventBus))
        case _ => () // do not react to other events: we're not interested
    }
}
