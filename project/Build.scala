import sbt._
import Keys._
import Process._
import java.io.File



object ScalaMeterBuild extends Build {

  val publishUser = "SONATYPE_USER"
  val publishPass = "SONATYPE_PASS"

  val userPass = for {
    user <- sys.env.get(publishUser)
    pass <- sys.env.get(publishPass)
  } yield (user, pass)

  val publishCreds: Seq[Setting[_]] = Seq(userPass match {
    case Some((user, pass)) =>
      credentials += Credentials("Sonatype Nexus Repository Manager", "oss.sonatype.org", user, pass)
    case None =>
      // prevent publishing
      publish <<= streams.map(_.log.info("Publishing to Sonatype is disabled since the \"" + publishUser + "\" and/or \"" + publishPass + "\" environment variables are not set."))
  })

  val scalaMeterSettings = Defaults.defaultSettings ++ publishCreds ++ Seq(
    scalaVersion := "2.11.0",
    crossScalaVersions := Seq("2.10.4", "2.11.0"),
    scalacOptions ++= Seq("-deprecation", "-unchecked", "-feature", "-Xlint"),
    resolvers ++= Seq(
      "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      "Sonatype OSS Releases" at "https://oss.sonatype.org/content/repositories/releases"
    ),
    libraryDependencies <++= (scalaVersion)(sv => dependencies(sv)),
    publishMavenStyle := true,
    publishTo <<= version { (v: String) =>
      val nexus = "https://oss.sonatype.org/"
      if (v.trim.endsWith("SNAPSHOT"))
        Some("snapshots" at nexus + "content/repositories/snapshots")
      else
        Some("releases"  at nexus + "service/local/staging/deploy/maven2")
    },
    publishArtifact in Test := false,
    pomIncludeRepository := { _ => false },
    pomExtra :=
      <url>http://scalameter.github.io/</url>
      <licenses>
        <license>
          <name>BSD-style</name>
          <url>http://opensource.org/licenses/BSD-3-Clause</url>
          <distribution>repo</distribution>
        </license>
      </licenses>
      <scm>
        <url>git@github.com:scalameter/scalameter.git</url>
        <connection>scm:git:git@github.com:scalameter/scalameter.git</connection>
      </scm>
      <developers>
        <developer>
          <id>axel22</id>
          <name>Aleksandar Prokopec</name>
          <url>http://axel22.github.com/</url>
        </developer>
      </developers>
  )

  def dependencies(scalaVersion: String) = CrossVersion.partialVersion(scalaVersion) match {
    case Some((2,11)) => List (
      "org.scalatest" %% "scalatest" % "2.1.3" % "test",
      "com.github.wookietreiber" %% "scala-chart" % "0.4.2",
      "org.apache.commons" % "commons-math3" % "3.2",
      "org.scala-tools.testing" % "test-interface" % "0.5",
      "org.scala-lang" % "scala-reflect" % "2.11.0",
      "org.scala-lang.modules" %% "scala-xml" % "1.0.1",
      "org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1"
      )
    case Some((2,10)) => List (
      "org.scalatest" %% "scalatest" % "2.1.2" % "test",
      "com.github.wookietreiber" %% "scala-chart" % "0.4.0",
      "org.apache.commons" % "commons-math3" % "3.2",
      "org.scala-tools.testing" % "test-interface" % "0.5"
      )
    case _ => Nil
  }

  /* projects */

  lazy val scalameter = Project(
    "scalameter",
    file("."),
    settings = scalaMeterSettings
  ) dependsOn (
  )

  lazy val scalameterTools = Project(
    "scalameter-tools",
    file("./scalameter-tools"),
    settings = scalaMeterSettings
  ) dependsOn (
    scalameter
  )
}
