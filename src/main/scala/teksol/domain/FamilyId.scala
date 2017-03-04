package teksol.domain

import java.util.UUID

import teksol.infrastructure.{ToParam, ToSql}

case class FamilyId(id: UUID) extends ToParam with ToSql {
    override def toParam: String = id.toString

    override def toSql: AnyRef = id.toString
}
