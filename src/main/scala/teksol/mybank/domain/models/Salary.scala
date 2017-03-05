package teksol.mybank.domain.models

import teksol.infrastructure.{ToJson, ToSql}

case class Salary(value: BigDecimal) extends Comparable[Salary] with ToSql with ToJson {
    /**
      * Returns a new Amount instance where the scale of the value is to pennies.
      *
      * The rounding mode that is used is pennies, hence this method's name.
      *
      * @return A ceiling'd copy of this amount instance.
      */
    def ceilPennies = Amount(value.setScale(2, BigDecimal.RoundingMode.CEILING))

    def negate = Amount(value * -1)

    def <(other: Amount): Boolean = value.compareTo(other.value) < 0

    def >(other: Amount): Boolean = value.compareTo(other.value) > 0

    def +(amount: Amount) = Amount(value + amount.value)

    def -(amount: Amount) = Amount(value - amount.value)

    def *(rate: InterestRate) = Amount(value * rate.value)

    def *(num: BigDecimal) = Amount(value * num)

    def /(denominator: BigDecimal) = Amount(value / denominator)

    def isPositive: Boolean = value.compareTo(0) > 0

    def isNegative: Boolean = value.compareTo(0) < 0

    def isZero: Boolean = this == Salary.ZERO

    override def compareTo(o: Salary): Int = value.compareTo(o.value)

    override def toSql: AnyRef = value.bigDecimal

    override def toJson: String = "\"" + value + "\""
}

object Salary {
    val ZERO = Salary(0)
}
