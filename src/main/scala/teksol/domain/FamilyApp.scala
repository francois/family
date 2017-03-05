package teksol.domain

import java.util.Locale

trait FamilyApp {
    def createFamily(familyId: FamilyId, name: FamilyName, locale: Locale)
}
