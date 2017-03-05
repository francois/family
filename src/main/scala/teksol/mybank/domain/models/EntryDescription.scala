package teksol.mybank.domain.models

import teksol.infrastructure.ToSql

case class EntryDescription(value: String) extends ToSql {
    override def toSql: AnyRef = value
}
