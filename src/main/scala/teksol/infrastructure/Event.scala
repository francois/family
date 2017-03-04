package teksol.infrastructure

import teksol.domain.FamilyId

trait Event {
    def toJson: String
}

case class FamilyCreated(familyId: FamilyId) extends Event {
    override def toJson: String = s"""{"family_id":${familyId.toJson}}"""
}
