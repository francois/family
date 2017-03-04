package teksol.domain

import teksol.infrastructure.ToSql

case class FamilyName(name: String) extends ToSql {
    override def toSql: AnyRef = name
}
