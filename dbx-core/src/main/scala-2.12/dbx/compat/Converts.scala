package dbx.compat

import scala.collection.convert.ImplicitConversionsToScala._

object Converts {

  def asSeq[T](list: java.util.List[T]) = {
    list.toSeq
  }

}
