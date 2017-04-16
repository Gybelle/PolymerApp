name := """eventapp"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "org.xerial" % "sqlite-jdbc" % "3.8.6"
)

//For SQLite:
fork in run := false
resolvers += "SQLite-JDBC Repository" at "https://oss.sonatype.org/content/repositories/snapshots" //This respository has the SQLite db.
