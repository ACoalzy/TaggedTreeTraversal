name := "TaggedTreeTraversal"

version := "0.1"

scalaVersion := "2.12.8"

libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.0" % "test"

scalacOptions ++= Seq(
  "-encoding", "utf8", // Option and arguments on same line
//  "-Xfatal-warnings",  // New lines for each options
  "-deprecation",
  "-unchecked",
  "-language:implicitConversions",
  "-language:higherKinds",
  "-language:existentials",
  "-language:postfixOps"
)
