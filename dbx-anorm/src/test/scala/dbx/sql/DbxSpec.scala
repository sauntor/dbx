package dbx.sql

import java.nio.file.Paths
import java.sql.{Connection, DriverManager}

import net.sf.log4jdbc.DriverSpy
import org.scalatest.{BeforeAndAfterAll, WordSpec}

trait DbxSpec extends WordSpec with BeforeAndAfterAll {
  private var initialized = false

  override protected def beforeAll(): Unit = {
    DriverManager.registerDriver(new DriverSpy)
  }

  override protected def afterAll(): Unit = {
    val connection = openConnection()
    connection.createStatement().execute("SHUTDOWN IMMEDIATELY")
  }

  def openConnection(initScript: String = "package"): Connection = {
    var jdbcUrl = "jdbc:log4jdbc:h2:mem:dbx;MODE=MySQL;DATABASE_TO_LOWER=FALSE;TRACE_LEVEL_FILE=1"
    if (!initialized) {
      initialized = true
      val script = s"dbx-anorm/src/test/resources/script/${initScript}.sql"
      val realpath = Paths.get(sys.props("user.dir"), script.split("/"): _*).toUri
      jdbcUrl = s"${jdbcUrl};INIT=create schema if not exists test\\;runscript from '${realpath}'"
    }
    DriverManager.getConnection(jdbcUrl, "sa", "sa")
  }

}
