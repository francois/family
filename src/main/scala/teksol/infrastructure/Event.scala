package teksol.infrastructure

import java.util.Locale

import teksol.domain.FamilyId

import scala.language.implicitConversions

trait Event {
    def toJson: String
}

case class FamilyCreated(familyId: FamilyId, locale: Locale) extends Event {
    implicit def localeToJson(locale: Locale): ToJson = new ToJson {
        override def toJson: String = "\"" + locale.getDisplayName() + "\""
    }

    override def toJson: String = s"""{"family_id":${familyId.toJson},"locale":${locale.toJson}}"""
}
