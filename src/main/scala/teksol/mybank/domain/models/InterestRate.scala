package teksol.mybank.domain.models

import teksol.infrastructure.{ToJson, ToSql}

case class InterestRate(value: BigDecimal) extends ToSql with ToJson {
    override def toSql: AnyRef = value.bigDecimal

    override def toJson: String = "\"" + value + "\""
}
