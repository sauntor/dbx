package dbx.compat

import scala.jdk.CollectionConverters._

object Converts {

  def asSeq[T](list: java.util.List[T]) = {
    list.asScala.toSeq
  }

}
