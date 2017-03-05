package teksol.mybank.domain.models

import teksol.infrastructure.{ToJson, ToSql}

case class AccountName(name: String) extends ToSql with ToJson {
    override def toSql: AnyRef = name

    override def toJson: String = "\"" + name.replaceAll("\"", "\\\"") + "\""
}
