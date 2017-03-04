package teksol.mybank.domain

import java.util.UUID

import teksol.infrastructure.{ToParam, ToSql}

case class EntryId(id: UUID) extends ToParam with ToSql {
    override def toParam: String = id.toString

    override def toSql: AnyRef = id.toString
}
