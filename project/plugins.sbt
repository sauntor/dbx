resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
//resolvers += "Sonatype Release Repository" at "http://repo.typesafe.com/typesafe/releases/"
//resolvers += "Sonatype Snapshot Repository" at "http://repo.typesafe.com/typesafe/shapshots/"
addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.10")
addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")
addSbtPlugin("org.xerial.sbt" % "sbt-sonatype" % "3.9.4")
