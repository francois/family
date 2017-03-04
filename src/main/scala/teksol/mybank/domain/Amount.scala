package teksol.mybank.domain

import java.math.{MathContext, RoundingMode}

import teksol.infrastructure.{ToJson, ToSql}

case class Amount(value: BigDecimal) extends Comparable[Amount] with ToSql with ToJson {
    def truncateToPennies = {
        val idx = value.toString.indexOf(".")
        Amount(BigDecimal(value.toString.substring(0, idx + 3))) // the period + 2 digits == 3 digits after the period
    }

    def negate = Amount(value * -1)

    def <(other: Amount) = value.compareTo(other.value) < 0

    def >(other: Amount) = value.compareTo(other.value) > 0

    def +(amount: Amount) = Amount(value + amount.value)

    def -(amount: Amount) = Amount(value - amount.value)

    def *(rate: InterestRate) = Amount(value * rate.value)

    def *(num: BigDecimal) = Amount(value * num)

    def /(denominator: BigDecimal) = Amount(value / denominator)

    def isPositive = value.compareTo(0) > 0

    def isNegative = value.compareTo(0) < 0

    def isZero = this == Amount.ZERO

    override def compareTo(o: Amount): Int = value.compareTo(o.value)

    override def toSql: AnyRef = value.bigDecimal

    override def toJson: String = "\"" + value + "\""
}

object Amount {
    val PENNY = Amount(BigDecimal("0.01"))
    val NEG_PENNY = Amount(BigDecimal("-0.01"))
    val MINUS_ONE = Amount(-1)
    val ZERO = Amount(0)
    val ONE = Amount(1)
    val TWO = Amount(2)
    val TEN = Amount(10)
}
