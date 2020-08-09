package dbx.sql

import java.lang.{StringBuilder => JStringBuilder}

import anorm.NamedParameter

import scala.reflect.runtime.universe

class SQLFactory {
  import SQLFactory._

  var block: Block = new PlainBlock


  def S(sql: Block) = {
    block.append(sql)
  }
  def S(sql: String) = {
    assert(sql != null)
    val fragment = sql.trim
    assert(fragment.nonEmpty)
    block.append(new StringBlock(fragment))
  }

  def BLOCK[B <: Block : universe.TypeTag](_block: () => Unit) = {
    val previousBlock = block
    block = newBlock[B]
    _block()
    previousBlock.append(block)
    block = previousBlock
  }

  def AND(block: => Unit) = {
    BLOCK[AndBlock](() => block)
  }

  def OR(block: => Unit) = {
    BLOCK[OrBlock](() => block)
  }

  def WHERE(block: => Unit) = {
    BLOCK[WhereBlock](() => block)
  }

  def sql: Option[(String, Array[NamedParameter])] = {
    block.string.map { string =>
      (string, block.parameters)
    }
  }

  implicit class PlainStringAppender(string: String) {
    def ! = S(string)
  }

  implicit class RichStringAppender(string: DbxRichString) {
    def ! = S(string)
  }

}

object SQLFactory {
  trait Block {
    def keyword: Option[String] = None

    protected var blocks = List.empty[Block]
    private var params = Array.empty[NamedParameter]

    def bracketed: Boolean = false
    def shouldBracketed(block: Block): Boolean = true
    def isEmpty: Boolean = {
      if (blocks.isEmpty) {
        true
      } else {
        blocks.foldLeft(0) { (flag, block) => { flag + (if (block.isEmpty) 0 else 1) }} == 0
      }
    }

    def append(sql: String) = {
      blocks :+= new StringBlock(sql)
    }
    def append(sql: String, parameter: NamedParameter, remaining: NamedParameter*) = {
      blocks :+= new StringBlock(sql)
      params ++= (parameter +: remaining)
    }

    def append(sql: Block) = {
      blocks :+= sql
    }

    def string: Option[String] = {
      if (isEmpty) {
        None
      } else {
        Some(stringBuilder.toString)
      }
    }

    def parameters: Array[NamedParameter] = {
      var parameters = params.map(p => p)
      blocks foreach { fragment =>
        parameters ++= fragment.parameters
      }
      parameters
    }

    def stringBuilder: JStringBuilder = {
      val builder = new JStringBuilder
      var first = true
      blocks.filter(!_.isEmpty).foldLeft(builder) { (b, f) =>
        val _first = first
        if (!first && keyword.nonEmpty) {
          b.append(' ').append(keyword.get)
        } else {
          first = false
        }
        val sql = f.string
        if (sql.nonEmpty) {
          if (shouldBracketed(f) && f.bracketed) {
            builder.append("(").append(sql.get).append(")")
          } else {
            if (!_first) b.append(' ')
            b.append(sql.get)
          }
        } else {
          builder
        }
      }
      builder
    }
  }

  class SqlBlock(sql: SQLFactory, alias: Option[String] = None, bracket:Boolean = false) extends Block {
    override def append(sql: Block): Unit = ???
    override def append(sql: String): Unit = ???
    override def bracketed: Boolean = bracket

    override def string(): Option[String] = {
      sql.block.string.map { string =>
        if (alias.nonEmpty) s"(${string}) AS ${alias.get}"
        else string
      }
    }
  }

  class StringBlock(sql: String, _params: Array[NamedParameter] = Array.empty) extends Block {
    override def string(): Option[String] = Some(sql)
    override def parameters: Array[NamedParameter] = _params
    override def append(sql: Block): Unit = ???
    override def append(sql: String): Unit = ???
    override def isEmpty: Boolean = false
  }

  class WhereBlock extends Block {
    override def keyword: Option[String] = Some("AND")

    override def shouldBracketed(block: Block): Boolean = {
      blocks.size > 1 && blocks(0).isInstanceOf[OrBlock]
    }

    override def string(): Option[String] = {
      if (isEmpty) {
        None
      } else {
        val builder = stringBuilder
        builder.insert(0, "WHERE ")
        Some(builder.toString)
      }
    }
  }

  class PlainBlock extends Block

  class AndBlock extends Block {
    override def keyword: Option[String] = Some("AND")
  }

  class OrBlock extends Block {
    override def keyword: Option[String] = Some("OR")

    override def bracketed: Boolean = {
      blocks.length > 1
    }
  }

  def newBlock[B <: Block: universe.TypeTag] = {
    val tag = implicitly[universe.TypeTag[B]]
    if (!cache.contains(tag)) {
      val ctorSymbol = tag.tpe.decl(universe.termNames.CONSTRUCTOR).asMethod
      val ctor = tag.mirror.reflectClass(tag.tpe.typeSymbol.asClass).reflectConstructor(ctorSymbol)
      synchronized {
        cache += tag -> ctor
      }
    }
    cache(tag)().asInstanceOf[B]
  }

  @volatile
  private var cache = Map.empty[universe.TypeTag[_], universe.MethodMirror]
}
