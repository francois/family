package teksol.mybank.domain

import teksol.infrastructure.{ToJson, ToSql}

case class InterestRate(value: BigDecimal) extends ToSql with ToJson {
    override def toSql: AnyRef = value.bigDecimal

    override def toJson: String = "\"" + value + "\""
}
