package teksol.mybank.domain

import java.util.UUID

import teksol.infrastructure.{ToJson, ToParam, ToSql}

case class AccountId(id: UUID) extends ToParam with ToSql with ToJson {
    override def toParam: String = id.toString

    override def toSql: AnyRef = id.toString

    override def toJson: String = "\"" + id + "\""
}
