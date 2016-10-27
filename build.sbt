name := """E-Cat"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.8"

libraryDependencies ++= Seq(
  jdbc,
  cache,
  ws
)
libraryDependencies += "org.scalaz" %% "scalaz-core" % "7.2.6"
libraryDependencies += "com.chuusai" %% "shapeless" % "2.3.2"
libraryDependencies += "org.typelevel" %% "shapeless-scalaz" % "0.4"
libraryDependencies += "sandbox" %% "schema" % "1.1"
libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.4"

unmanagedJars in Compile += file("./project/lib/ecatSOAP.jar")

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically."
routesGenerator := InjectedRoutesGenerator
//
routesImport ++= List("ecat.model.Bindables._",
                      "controllers.BlockApi.OrderList",
                       "java.time.LocalDateTime",
                       "play.api.libs.json._",
                       "schema.RecordFilters.Filter",
                       "ecat.model.Schema._",
                       "ecat.model.Filters.hotelFilterReads",
                       "ecat.model.ajax.catctrl.CategoryControlProtocol._",
                       "java.math.BigInteger")

TwirlKeys.templateImports ++= List("ecat.model.Schema._",
                                    "shapeless._",
                                    "shapeless.record._")

