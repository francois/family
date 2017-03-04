package teksol.mybank.domain

import teksol.infrastructure.ToSql

case class GoalDescription(name: String) extends ToSql {
    override def toSql: AnyRef = name
}
