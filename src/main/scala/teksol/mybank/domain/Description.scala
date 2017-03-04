package teksol.mybank.domain

import teksol.infrastructure.ToSql

case class Description(value: String) extends ToSql {
    override def toSql: AnyRef = value
}
