package dbx.sql

import anorm.SqlParser._
import anorm._

class PackageSpecs extends DbxSpec {

  "SQLFactory" should {

    "run as BatchSql" in {
      implicit val con = openConnection()
      val rows = new SQLFactory {
        S("INSERT INTO book(title, price) VALUES({title}, {price})")
      } .batch (
        Seq("title" -> "Go Lang", "price" -> 15d),
        Seq("title" -> "HTML5 & CSS3", "price" -> 42.3d)
      ) .execute()
      println(s"inserted rows ${rows mkString(",")}")
      assert(rows(0) > 0)
    }

    "run as SimpleSql" in {
      implicit val con = openConnection()
      val parser = int("id") ~ str("title") map { case id ~ title => (id, title) }
      val books = new SQLFactory {
        S("SELECT")
        S("*")
        S("FROM book")
        WHERE {
          S("id < {id}")
        }
        S("LIMIT {offset}, {size}")
      } .on("id" -> 3500, "offset" -> 0, "size" -> 3) as parser.*
      println(s"books = ${books.mkString("|")}")
    }
  }
}
