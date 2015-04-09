name := """SlickRemoteActors"""

version :=  """1.0.0"""

libraryDependencies ++= Seq(
	"com.typesafe.slick" %% "slick" % "2.1.0",
	"mysql" % "mysql-connector-java" % "5.1.34",
	"com.typesafe.akka" %% "akka-actor" % "2.3.8",
	"com.typesafe.akka" %% "akka-remote" % "2.3.8")
