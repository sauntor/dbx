package dbx.sql

import anorm.NamedParameter._
import org.scalatest.WordSpec

class SQLFactorySpecs extends WordSpec {
  case class FindBook(id: Option[Int] = None, title: Option[String] = None, price: Option[(Double, Double)] = None) extends SQLFactory {
    S("SELECT id, title, price FROM book")
    WHERE {
      OR {
        id.foreach { id => S("id = {id}" P "id" -> id) }
        title.foreach { title => S("title = {title}" P "title" -> title) }
      }
      price.foreach { price =>
        AND {
          S("price >= {priceGE}" P "priceGE" -> price._1)
          S("price < {priceLT}" P "priceLT" -> price._2)
        }
      }
    }
  }


  "SQLFactory" should {
    "no `WHERE` when no condition clause" in {
      val PlainSQL(string, params) = FindBook().sql
      assert(string == "SELECT id, title, price FROM book")
      assert(params.isEmpty)
    }
    "have `WHERE` & `AND` keyword" in {
      val PlainSQL(string, params) = FindBook(price = Some((10, 20)))
      assert(string == "SELECT id, title, price FROM book WHERE price >= {priceGE} AND price < {priceLT}")
      assert(params.contains("priceGE") && params("priceGE") == 10)
      assert(params.contains("priceLT") && params("priceLT") == 20)
    }
    "have `WHERE` & *NO* `OR` keyword" in {
      val PlainSQL(string, params) = FindBook(id = Some(99)).sql
      assert(string == "SELECT id, title, price FROM book WHERE id = {id}")
      assert(params.contains("id") && params("id") == 99)
    }
    "have `WHERE` & `OR` keyword" in {
      val PlainSQL(string, params) = FindBook(id = Some(99), title = Some("Scala in Action")).sql
      assert(string == "SELECT id, title, price FROM book WHERE id = {id} OR title = {title}")
      assert(params.contains("id") && params("id") == 99)
      assert(params.contains("title") && params("title") == "Scala in Action")
    }
    "have `WHERE` & `OR` & `AND` keyword" in {
      val PlainSQL(string, params) = FindBook(id = Some(99), title = Some("Scala in Action"), price = Some((10, 20))).sql
      assert(string == "SELECT id, title, price FROM book WHERE (id = {id} OR title = {title}) AND price >= {priceGE} AND price < {priceLT}")
      assert(params.contains("id") && params("id") == 99)
      assert(params.contains("title") && params("title") == "Scala in Action")
      assert(params.contains("priceGE") && params("priceGE") == 10)
      assert(params.contains("priceLT") && params("priceLT") == 20)
    }
  }
}
