package teksol.postgres

import java.util.Locale

import org.springframework.jdbc.core.JdbcTemplate
import teksol.domain.{FamilyId, FamilyName}
import teksol.infrastructure.{EventBus, FamilyCreated}

class PostgresFamilyApp(private[this] val jdbcTemplate: JdbcTemplate, private[this] val eventBus: EventBus) extends teksol.domain.FamilyApp {
    override def createFamily(familyId: FamilyId, name: FamilyName, locale:Locale): Unit = {
        jdbcTemplate.update("INSERT INTO public.families(family_id, name, locale) VALUES (?::uuid, ?::text, ?::text)",
            familyId.toSql, name.toSql, locale.toLanguageTag)

        eventBus.publish(FamilyCreated(familyId, locale))
    }
}
