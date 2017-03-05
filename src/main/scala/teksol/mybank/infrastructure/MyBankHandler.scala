package teksol.mybank.infrastructure

import java.time.LocalDate
import java.util.UUID
import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

import org.eclipse.jetty.server.Request
import org.eclipse.jetty.server.handler.AbstractHandler
import org.slf4j.LoggerFactory
import teksol.Config
import teksol.domain.FamilyId
import teksol.mybank.domain.models.{Account, AccountId, Amount, EntryDescription}

class MyBankHandler(private[this] val config: Config) extends AbstractHandler {
    private[this] val log = LoggerFactory.getLogger(this.getClass)

    override def handle(target: String, baseRequest: Request, request: HttpServletRequest, response: HttpServletResponse): Unit = {
        baseRequest.getMethod match {
            case "GET" =>
                target.substring(1).split("/").toList match {
                    case familyId :: accountId :: Nil =>
                        config.myBankService.findAccount(FamilyId(UUID.fromString(familyId)), AccountId(UUID.fromString(accountId))) match {
                            case None => ()
                            case Some(account) =>
                                writeAccountToResponse(response, account)
                                baseRequest.setHandled(true)
                        }

                    case familyId :: Nil =>
                        config.myBankService.findFamily(FamilyId(UUID.fromString(familyId))) match {
                            case None => ()
                            case Some(family) =>
                                response.setContentType("application/json")
                                response.getWriter.print(family.toJson)
                                baseRequest.setHandled(true)
                        }

                    case random => log.info("404 Not Found (bad path): {}", random)
                }

            case "POST" =>
                target.substring(1).split("/").toList match {
                    case familyId :: accountId :: "deposit" :: Nil =>
                        for (account <- config.myBankService.findAccount(FamilyId(UUID.fromString(familyId)), AccountId(UUID.fromString(accountId)));
                             postedOn <- Option(baseRequest.getParameter("posted_on")).map(LocalDate.parse);
                             amount <- Option(baseRequest.getParameter("amount")).map(BigDecimal.apply).map(Amount.apply);
                             description <- Option(baseRequest.getParameter("description")).map(EntryDescription.apply)) {
                            account.postDeposit(postedOn, description, amount)
                            response.setStatus(HttpServletResponse.SC_CREATED)
                            baseRequest.setHandled(true)
                        }

                    case familyId :: accountId :: "withdraw" :: Nil =>
                        for (account <- config.myBankService.findAccount(FamilyId(UUID.fromString(familyId)), AccountId(UUID.fromString(accountId)));
                             postedOn <- Option(baseRequest.getParameter("posted_on")).map(LocalDate.parse);
                             amount <- Option(baseRequest.getParameter("amount")).map(BigDecimal.apply).map(Amount.apply);
                             description <- Option(baseRequest.getParameter("description")).map(EntryDescription.apply)) {
                            account.postWithdrawal(postedOn, description, amount)
                            response.setStatus(HttpServletResponse.SC_CREATED)
                            baseRequest.setHandled(true)
                        }

                    case familyId :: accountId :: "pay-salary" :: Nil =>
                        for (account <- config.myBankService.findAccount(FamilyId(UUID.fromString(familyId)), AccountId(UUID.fromString(accountId)));
                             postedOn <- Option(baseRequest.getParameter("posted_on")).map(LocalDate.parse);
                             numUnitsCompleted <- Option(baseRequest.getParameter("num_units_completed")).map(Integer.valueOf)) {
                            account.postSalary(config.i18n, postedOn, numUnitsCompleted)
                            response.setStatus(HttpServletResponse.SC_CREATED)
                            baseRequest.setHandled(true)
                        }

                    case _ => ()
                }

            case method => log.info("404 Not Found (bad method): {}", method)
        }
    }

    private[this] def writeAccountToResponse(response: HttpServletResponse, account: Account): Unit = {
        val entries = account.entries.toSeq.sortWith((e0, e1) => e0.postedOn.isAfter(e1.postedOn))
        val goals = account.goals.toSeq.sortWith((e0, e1) => e0.dueOn.isAfter(e1.dueOn))

        val accountJson = account.toJson
        val resp = new StringBuffer(accountJson.length + entries.size * 100 + goals.size * 100)
        resp.append(accountJson.substring(0, accountJson.length - 1))
        resp.append(",\"entries\":[")
        resp.append(entries.map(_.toJson).mkString(","))
        resp.append("],\"goals\":[")
        resp.append(goals.map(_.toJson).mkString(","))
        resp.append("]}")

        response.setContentType("application/json")
        response.getWriter.print(resp)
    }
}
