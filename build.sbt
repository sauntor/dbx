import scala.xml.transform.{RewriteRule, RuleTransformer}
import scala.xml.{NodeSeq, Node => XmlNode, _}

name := "dbx"
ThisBuild / version := "2.1.2"
ThisBuild / organization := "com.lingcreative"
ThisBuild / organizationName := "LingCreative Studio"
ThisBuild / scalaVersion := "2.13.3"
ThisBuild / crossScalaVersions := Seq("2.11.8", "2.12.8", "2.13.3")

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-unchecked",
  "-language:postfixOps",
  "-language:implicitConversions",
  "-Danorm.macro.debug=true")

ThisBuild / description := "A transaction management library for `Scala` users, migrated from `Spring Framework`."
ThisBuild / homepage := Some(url(s"https://github.com/sauntor/dbx"))
ThisBuild / licenses := Seq(("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0.html")))
ThisBuild / scmInfo := Some(ScmInfo(
  url("https://github.com/sauntor/dbx"),
  "https://github.com/sauntor/dbx.git",
  Some("https://github.com/sauntor/dbx.git")
))
ThisBuild / developers := List(
  Developer("sauntor", "适然(Sauntor)", "sauntor@yeah.net", url("http://github.com/sauntor"))
)
// Remove all additional repository other than Maven Central from POM
ThisBuild / pomIncludeRepository := { _ => false }
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / publishMavenStyle := true

lazy val `dbx` = (project in file("."))
  .aggregate(
    `dbx-core`,
    `dbx-anorm`
  )
  .settings(
//    crossScalaVersions := Nil,
    publish / skip := true
  )

lazy val `dbx-core` = (project in file("dbx-core"))
  .settings(
    publishM2 := {
      assembly.value
      publishM2.value

    },
    publishLocal := {
      assembly.value
      publishLocal.value
    },
    isSnapshot in assembly := true,
    publishArtifact in Test := false,
    publishArtifact in (Compile, packageBin) := false,
    publishArtifact in (Compile, packageSrc) := true,
    test in assembly := {},
    artifact in (Compile, assembly) := {
      val art = (artifact in (Compile, assembly)).value
      art.withName(name.value).withClassifier(None)
    },
    addArtifact(artifact in (Compile, assembly), assembly),
    assemblyShadeRules in assembly := Seq(
      ShadeRule.rename("org.apache.commons.logging.**" -> "dbx.$internal$.logging.@1").inAll,
      ShadeRule.rename("org.springframework.jdbc.*Exception" -> "dbx.exceptions.@1").inAll,
      ShadeRule.zap("org.springframework.jdbc.support.JdbcAccessor").inAll,
      ShadeRule.zap("org.springframework.jdbc.support.Key*").inAll,
      ShadeRule.zap("org.springframework.jdbc.support.DatabaseStart*").inAll,
      ShadeRule.zap("org.springframework.jdbc.support.Sql*").inAll,
      ShadeRule.zap("org.springframework.jdbc.support.Gen*").inAll,
      ShadeRule.rename("org.springframework.jdbc.support.*SQL*" -> "dbx.exceptions.codec.@1SQL@2").inAll,
      ShadeRule.rename("org.springframework.jdbc.support.Meta*" -> "dbx.exceptions.codec.Meta@1").inAll,
      ShadeRule.rename("org.springframework.jdbc.support.sql-*" -> "dbx.exceptions.codec.sql-@1").inAll,
      ShadeRule.rename("org.springframework.dao.*Exception" -> "dbx.exceptions.@1").inAll,
      ShadeRule.rename("org.springframework.**" -> "dbx.$internal$.@1").inAll,
      ShadeRule.zap("org.springframework.cglib.**").inAll,
      ShadeRule.zap("org.springframework.objenesis.**").inAll,
      ShadeRule.zap("org.springframework.core.codec.**").inAll,
      ShadeRule.zap("org.springframework.core.log.**").inAll,
      ShadeRule.zap("org.springframework.core.serializer.**").inAll,
      ShadeRule.zap("org.springframework.core.style.**").inAll,
      ShadeRule.zap("org.springframework.core.task.**").inAll,
      ShadeRule.zap("org.springframework.util.backoff.**").inAll,
      ShadeRule.zap("org.springframework.util.concurrent.**").inAll,
      ShadeRule.zap("org.springframework.util.unit.**").inAll,
      ShadeRule.zap("org.springframework.util.function.**").inAll,
      ShadeRule.zap("org.springframework.beans.annotation.**").inAll,
      ShadeRule.zap("org.springframework.beans.support.**").inAll,
      ShadeRule.zap("org.springframework.beans.propertyeditors.**").inAll,
      ShadeRule.zap("org.springframework.beans.factory.annotation.**").inAll,
      ShadeRule.zap("org.springframework.beans.factory.groovy.**").inAll,
      ShadeRule.zap("org.springframework.beans.factory.wiring.**").inAll,
      ShadeRule.zap("org.springframework.beans.factory.serviceloader.**").inAll,
      ShadeRule.zap("org.springframework.jdbc.config.**").inAll,
      ShadeRule.zap("org.springframework.jdbc.core.**").inAll,
      ShadeRule.zap("org.springframework.jdbc.object.**").inAll,
      ShadeRule.zap("org.springframework.jdbc.support.*.**").inAll,
      ShadeRule.zap("org.springframework.jdbc.datasource.embedded.**").inAll,
      ShadeRule.zap("org.springframework.jdbc.datasource.lookup.**").inAll,
      ShadeRule.zap("org.springframework.jdbc.datasource.init.**").inAll,
      ShadeRule.zap("org.springframework.dao.annotation.**").inAll,
      ShadeRule.zap("org.springframework.dao.support.**").inAll,
      ShadeRule.zap("org.springframework.transaction.annotation.**").inAll,
      ShadeRule.zap("org.springframework.transaction.config.**").inAll,
      ShadeRule.zap("org.springframework.transaction.event.**").inAll,
      ShadeRule.zap("org.springframework.transaction.interceptor.Match*").inAll,
      ShadeRule.zap("org.springframework.transaction.interceptor.Method*").inAll,
      ShadeRule.zap("org.springframework.transaction.interceptor.Name*").inAll,
      ShadeRule.zap("org.springframework.transaction.interceptor.Bean*").inAll,
      ShadeRule.zap("org.springframework.transaction.interceptor.*Proxy*").inAll,
      ShadeRule.zap("org.springframework.transaction.interceptor.*Aspect*").inAll,
      ShadeRule.zap("org.springframework.transaction.interceptor.*Interceptor*").inAll,
      ShadeRule.zap("org.springframework.transaction.jta.**").inAll,
      ShadeRule.zap("org.springframework.jca.**").inAll
    ),
    assemblyOption in assembly := (assemblyOption in assembly).value.copy(includeScala = false),
    assemblyExcludedJars in assembly := {
      val cp = (fullClasspath in assembly).value
      cp filter {_.data.getName.matches(".*(config|HikariCP|slf4j-api).*") }
    },
    assemblyMergeStrategy in assembly := {
      case PathList("META-INF", _ @ _*) => MergeStrategy.discard
      case x =>
        val oldStrategy = (assemblyMergeStrategy in assembly).value
        oldStrategy(x)
    },
    libraryDependencies ++= Seq(
      "org.springframework" % "spring-tx" % "5.1.9.RELEASE" exclude("org.springframework", "spring-jcl"),
      "org.springframework" % "spring-jdbc" % "5.1.9.RELEASE"  exclude("org.springframework", "spring-jcl"),
      "com.typesafe" % "config" % "1.3.3",
      "org.slf4j" % "slf4j-api" % "1.7.25",
      "org.slf4j" % "jcl-over-slf4j" % "1.7.25"
    ),
    pomPostProcess := { (node: XmlNode) =>
      new RuleTransformer(new RewriteRule {
        override def transform(node: XmlNode): NodeSeq = node match {
          case e: Elem if e.label == "dependency" && {
            e.child.exists(child => child.label == "groupId" && child.text == "org.springframework") ||
            e.child.exists(child => child.label == "artifactId" && child.text == "jcl-over-slf4j") ||
            e.child.exists(child => child.label == "artifactId" && child.text == "scala-library")
          } =>
            val organization = e.child.filter(_.label == "groupId").flatMap(_.text).mkString
            val artifact = e.child.filter(_.label == "artifactId").flatMap(_.text).mkString
            val version = e.child.filter(_.label == "version").flatMap(_.text).mkString
            Comment(s"$organization#$artifact;$version has been omitted")
          case _ => node
        }
      }).transform(node).head
    }
  )

lazy val `dbx-anorm` = (project in file("dbx-anorm"))
  .settings(
    libraryDependencies += "org.playframework.anorm" %% "anorm" % "2.6.4",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3" % Test,
    libraryDependencies += "com.googlecode.log4jdbc" % "log4jdbc" % "1.2" % Test,
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.0.9" % Test,
    libraryDependencies += "com.h2database" % "h2" % "1.4.199" % Test,
    publishArtifact in Test := false
  )

