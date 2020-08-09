package dbx.api

import java.sql.Connection

import com.typesafe.config.ConfigFactory
import dbx.api.Transactional.{TransactionSettings, TransactionSettingsBuilder}
import dbx.compat.Converts
import javax.sql.DataSource
import org.springframework.jdbc.datasource.{DataSourceTransactionManager, DataSourceUtils}
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator
import org.springframework.transaction.{PlatformTransactionManager, TransactionUsageException}

import scala.util.{Failure, Success, Try}

class TransactionSettingsProvider {
  def config = ConfigFactory.load("dbx")
  private lazy val settings = {
    var _settings: Map[String, TransactionSettings] = Map.empty
    Try {
      config.getConfig("dbx")
    } match {
      case Success(root) =>
        val resources = Converts.asSeq(root.getStringList("resources"))
        resources map { resource =>
          val settings = root.getConfig(resource)
          val builder = TransactionSettingsBuilder()
          builder.resource = settings.getString("resource")
          builder.readOnly = settings.getBoolean("readOnly")
          builder.isolation = Isolation.withName(settings.getString("isolation"))
          builder.propagation = Propagation.withName(settings.getString("propagation"))
          builder.timeout = settings.getInt("timeout")
          val cl = settings.getClass.getClassLoader
          builder.rollbackFor =  Converts.asSeq(settings.getStringList("rollbackFor")) map (cl.loadClass(_))
          builder.noRollbackFor  = Converts.asSeq(settings.getStringList("noRollbackFor")) map (cl.loadClass(_))
          _settings += resource -> builder.build()
        }
      case Failure(exception) =>
        logger.warn("Can't load transactionSettings, using the default settings", exception)
        val builder = TransactionSettingsBuilder()
        builder.resource = defaultSettings.resource
        builder.readOnly = defaultSettings.readOnly
        builder.isolation = defaultSettings.isolation
        builder.propagation = defaultSettings.propagation
        builder.timeout = defaultSettings.timeout
        builder.rollbackFor = defaultSettings.rollbackFor
        builder.noRollbackFor = defaultSettings.noRollbackFor
        _settings += "default" -> builder.build()
    }
    _settings
  }
  def get(resource: String): TransactionSettings = {
    settings(resource)
  }
}

trait DatabaseComponents {
  def dataSource: DataSource
  lazy val transactionManager: TransactionManagerLookup = new ProvidedDataSourceTransactionManagerLookup(Map("default" -> dataSource))
  lazy val transactionSettingsProvider: TransactionSettingsProvider = new TransactionSettingsProvider(){}
  lazy val transactional: Transactional[Connection] = new ProvidedDataSourceTransactional(Map("default" -> dataSource), transactionManager, transactionSettingsProvider)
}

class ProvidedDataSourceTransactional(dataSources: Map[String, DataSource], override val lookupTransactionManager: TransactionManagerLookup,
                                      override val settingsProvider: TransactionSettingsProvider) extends Transactional[Connection] {

  @volatile
  private var exceptionTranslators = Map.empty[String, Option[SQLErrorCodeSQLExceptionTranslator]]

  override private [api] def exceptionTranslator(resource: String): Option[SQLErrorCodeSQLExceptionTranslator] = {
    if (!exceptionTranslators.contains(resource)) {
      synchronized {
        exceptionTranslators += (resource -> dataSources.get(resource).map(new SQLErrorCodeSQLExceptionTranslator(_)))
      }
    }
    exceptionTranslators(resource)
  }

  override def obtainResource(resource: String): Resource = {
    if (!dataSources.contains(resource)) {
      throw new TransactionUsageException(s"No resource which is named [$resource] !")
    }
    DataSourceUtils.getConnection(dataSources(resource))
  }

  override protected def releaseResource(resource: String, actualResource: Resource): Unit = {
    DataSourceUtils.releaseConnection(actualResource, dataSources(resource))
  }
}

class ProvidedDataSourceTransactionManagerLookup(dataSources: Map[String, DataSource]) extends TransactionManagerLookup {

  @volatile
  private var managers = Map.empty[String, PlatformTransactionManager]

  override def lookup(resource: String): PlatformTransactionManager = {
    if (!managers.contains(resource)) {
      synchronized {
        val dataSource = dataSources.get(resource)
        if (dataSource.isEmpty)  {
          throw new TransactionUsageException(s"No resource is named [$resource] !")
        }
        managers += (resource -> new DataSourceTransactionManager(dataSource.get))
      }
    }
    managers(resource)
  }

}

