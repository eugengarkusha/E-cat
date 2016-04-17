name := """E-Cat"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws,
  specs2 % Test
)
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.2"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.0"
libraryDependencies += "org.typelevel" %% "shapeless-scalaz" % "0.4"
unmanagedJars in Compile += file("./lib/ecatWsClient.jar")

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
routesGenerator := InjectedRoutesGenerator

routesImport ++= List("ecat.model.Bindables._", "java.time.LocalDateTime", "ecat.model.ajax.Mappings._, play.api.libs.json._")
