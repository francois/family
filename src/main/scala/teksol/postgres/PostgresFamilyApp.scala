package teksol.postgres

import org.springframework.jdbc.core.JdbcTemplate
import teksol.domain.{FamilyId, FamilyName}

class PostgresFamilyApp(private[this] val jdbcTemplate: JdbcTemplate) extends teksol.domain.FamilyApp {
    override def createFamily(familyId: FamilyId, name: FamilyName): Unit = {
        jdbcTemplate.update("INSERT INTO public.families(family_id, name) VALUES (?::uuid, ?::text)",
            familyId.toSql, name.toSql)
    }
}
