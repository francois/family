package teksol.mybank.domain.models

import teksol.infrastructure.{ToJson, ToSql}

case class EntryDescription(description: String) extends ToSql with ToJson {
    override def toSql: AnyRef = description

    override def toJson: String = "\"" + description.replaceAll("\"", "\\\"") + "\""
}
