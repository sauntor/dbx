package dbx.compat

import scala.collection.convert.decorateAsScala._

object Converts {

  def asSeq[T](list: java.util.List[T]): Seq[T] = {
    list.asScala
  }

}
