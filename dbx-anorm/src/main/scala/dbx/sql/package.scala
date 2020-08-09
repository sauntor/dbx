package dbx

import anorm._
import dbx.sql.SQLFactory.{SqlBlock, StringBlock}

package object sql {

  type RowsSQL = SimpleSql[Row]

  implicit class DbxRichString(sql: String) {
    private var params = Array.empty[NamedParameter]
    def P(first: NamedParameter, remaining: NamedParameter*) = {
      params :+= first
      params ++= remaining
      this
    }
    def ::(first: NamedParameter, remaining: NamedParameter*) = P(first, remaining: _*)
    def block = new StringBlock(sql, params)
  }

  implicit class DbxFatSql(val sql: SQLFactory) {
    var _alias = Option.empty[String]
    var _bracket = false
    def alias(alias: String) = {
      _alias = Some(alias)
      this
    }
    def bracketed = {
      _bracket = true
      this
    }
  }

  implicit def dbxFatSqlToBlock(sql: DbxFatSql) = {
    new SqlBlock(sql.sql, sql._alias, sql._bracket)
  }
  implicit def dbxRichStringToBlock(richString: DbxRichString) = {
    richString.block
  }

  implicit def dbxSqlFactoryToRowsSQL(factory: SQLFactory): RowsSQL = {
    val sql = factory.sql
    assert(sql.nonEmpty, "Empty SQL")
    val Some((string, parameters)) = sql
    SQL(string).on(parameters: _*)
  }

  implicit class DbxSqlFactoryToBatchSql(factory: SQLFactory) {
    def batch(first: Seq[NamedParameter], other: Seq[NamedParameter]*) = {
      val sql = factory.sql
      assert(sql.nonEmpty, "Empty SQL")
      val Some((string, parameters)) = sql
      assert(parameters.isEmpty, "Sql can't take parameters before batched")
      BatchSql(string, first, other: _*)
    }
  }

  object PlainSQL {
    def unapply[S <: SQLFactory](factory: SQLFactory): Option[(String, Map[String, Any])] = {
      unapply(factory.sql)
    }
    def unapply(sql: Option[(String, Array[NamedParameter])]): Option[(String, Map[String, Any])] = {
      sql.map { s =>
        (s._1, unapply(s._2).fold(Map.empty[String, Any]){ o => o })
      }
    }
    def unapply(params: Array[NamedParameter]):Option[Map[String, Any]] = {
      Some(params map { p =>
        val tuple = p.tupled
        (tuple._1, if (tuple._2.isInstanceOf[DefaultParameterValue[_]]) tuple._2.asInstanceOf[DefaultParameterValue[Any]].value else tuple._2)
      } toMap)
    }
  }

}
