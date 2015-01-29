import sbt._
import Keys._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import playscalajs.PlayScalaJS
import com.typesafe.sbt.packager.universal.UniversalKeys
import play.Play._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  override def rootProject = Some(jvm)

  val showcase = PlayScalaJS("jvm", "js", file("."), CrossType.Full).
    jvmSettings(
      libraryDependencies ++= Dependencies.jvm.value,
      //scalaVersion := Versions.scala,
      scalacOptions ++= Seq("-feature"),
      // For bindableChar
      routesImport += "config.Routes._" 
    ).jsSettings(
      libraryDependencies ++= Dependencies.js.value,
      jsDependencies ++= Seq(
        RuntimeDOM,
        //"org.webjars" % "jquery" % Versions.jquery / "jquery.js"
        "org.webjars" % "jquery" % "1.10.2" / "jquery.js"
        //"com.lihaoyi" %% "upickle" % Versions.uPickle
      )
    ).settings(
      version := Versions.app,
      scalaVersion := Versions.scala,
      libraryDependencies ++= Dependencies.shared.value,
//      libraryDependencies ++= Seq(
//        "com.lihaoyi" %%% "utest" % Versions.uTest % "test"
//      ),

      testFrameworks += new TestFramework("utest.runner.Framework")
    )

  lazy val jvm = showcase.jvm
  lazy val js = showcase.js

  // Only if you use IntelliJ: the shared project makes IntelliJ happy without using symlinks
  lazy val scala = Project("shared", file("shared"))
}

object Dependencies {
  val shared = Def.setting(Seq(
    //"com.lihaoyi" %%% "upickle" % "0.2.4",
    "com.lihaoyi" %% "upickle" % Versions.uPickle,
    "com.lihaoyi" %% "utest" % Versions.uTest % "test"
  ))

  val jvm = Def.setting(Seq(
    filters,
    jdbc,
    anorm,
    "com.typesafe.slick" %% "slick" % "2.1.0",
    "com.typesafe.play" %% "play-slick" % "0.8.0",
    "com.lihaoyi" %% "upickle" % Versions.uPickle,
    "org.webjars" %% "webjars-play" % "2.3.0",
    //"org.webjars" % "jquery" % "2.1.1",
    "org.webjars" % "codemirror" % "4.3",
    "org.webjars" % "bootstrap" % "3.2.0",
    "org.webjars" % "font-awesome" % "4.1.0",
    "com.vmunier" %% "play-scalajs-sourcemaps" % Versions.playScalajsSourcemaps,
    "org.webjars" % "jquery" % Versions.jquery
  ))

  val js = Def.setting(Seq(
    "org.scala-js" %%% "scalajs-dom" % Versions.scalajsDom,
    "be.doeraene" %%% "scalajs-jquery" % "0.7.0",
    "com.lihaoyi" %%% "upickle" % Versions.uPickle,
    "com.lihaoyi" %%% "scalatags" % Versions.scalaTags,
    "com.lihaoyi" %%% "scalarx" % Versions.scalaRx
  ))
}

object Versions {
  val app = "0.1.0-SNAPSHOT"
  val scala = "2.11.2"
  //val scalaJSVersion = "-RC2"
  val scalajsDom = "0.7.0"
  val jquery = "1.11.1"
  val playScalajsSourcemaps = "0.1.0"
  val uPickle = "0.2.6-RC1"
  val uTest = "0.2.5-RC1"
  val scalaTags = "0.4.3-RC1"
  val scalaRx = "0.2.7-RC1"
}
