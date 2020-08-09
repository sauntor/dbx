DBX
========
> A transaction management library for `Scala` users, migrated from `Spring Framework`.

## Usage
### Add Dependencies
```sbt
// Core library
libraryDependencies += "com.lingcreative" %% "dbx-core" % "2.1.0"

// for Anorm users
libraryDependencies += "com.lingcreative" %% "dbx-anorm" % "2.1.0"
```

### Play with Dbx
##### Core library usage
```scala
import dbx.api._

object PersistenceSupport extends DatabaseComponents {

  // replace `DataSource` with the really implementation you use
  override lazy val dataSource = new DataSource(username, passwd, url)

}

class UsersDAO(transactional: Transactional[Connection]) {
  def findAll() = transactional() { connection =>
    val resultSet = connection.createStatement().executeQuery("SELECT * FROM users")
    extractUsers(resultSet)
  }
}
```
##### Anorm library usage
See [SQLFactorySpecs](dbx-anorm/src/test/scala/dbx/sql/SQLFactorySpecs.scala), 
[PackageSpecs](dbx-anorm/src/test/scala/dbx/sql/PackageSpecs.scala)

### Default Configuration
```haml
dbx {
  resources = [ default ]
  default {
    resource = default
    readOnly = false
    isolation = DEFAULT
    propagation = REQUIRED
    timeout = -1
    noRollbackFor = []
    rollbackFor += java.lang.RuntimeException
    rollbackFor += java.lang.Error
  }
}
```