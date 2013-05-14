import sbt._
import Keys._
import play.Project._

object ApplicationBuild extends Build {

  val appName         = "play-scala-mybatis-kissthinker"
  val appVersion      = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    // Add your project dependencies here,
    jdbc,
    anorm,
	  "org.specs2" %% "specs2" % "1.12.3" % "test" withSources() withJavadoc(),
	  "org.mybatis.scala" %% "mybatis-scala-core" % "1.0.1" withSources() withJavadoc()
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    // Add your own project settings here
  )
}