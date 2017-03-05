package teksol.mybank.domain.models

import teksol.infrastructure.ToSql

case class AccountName(name: String) extends ToSql{
    override def toSql: AnyRef = name
}
