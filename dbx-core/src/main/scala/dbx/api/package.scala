package dbx

import java.sql.Connection

import org.slf4j.{Logger, LoggerFactory}

package object api {

  val logger: Logger = LoggerFactory.getLogger("dbx")

  object defaultSettings {
    val resource: String = "default"
    val readOnly: Boolean = true
    val isolation: Isolation.Value = Isolation.DEFAULT
    val propagation: Propagation.Value = Propagation.REQUIRED
    val noRollbackFor: Seq[Class[_]] = Seq.empty
    val rollbackFor: Seq[Class[_]] = Seq(classOf[RuntimeException], classOf[Error])
    val timeout: Int = -1
  }

  type TransactionalConnection = Transactional[Connection]

}
