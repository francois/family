package teksol.mybank.domain

import teksol.infrastructure.ToSql

case class Amount(value: BigDecimal) extends Comparable[Amount] with ToSql {
    def +(amount: Amount) = Amount(value + amount.value)

    def -(amount: Amount) = Amount(value - amount.value)

    def *(amount: Amount) = Amount(value * amount.value)

    def /(amount: Amount) = Amount(value / amount.value)

    def isZero = this == Amount.ZERO

    override def compareTo(o: Amount): Int = value.compareTo(o.value)

    override def toSql: AnyRef = value.bigDecimal
}

object Amount {
    val MINUS_ONE = Amount(-1)
    val ZERO = Amount(0)
    val ONE = Amount(1)
    val TWO = Amount(2)
    val TEN = Amount(10)
}
