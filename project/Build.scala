import sbt._
import Keys._
import sbt.Project.projectToRef

// ScalaJSPlugin

import org.scalajs.sbtplugin.ScalaJSPlugin

//import org.scalajs.sbtplugin.ScalaJSPlugin.AutoImport._

import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._

// PlayScalajs plugins

import playscalajs.ScalaJSPlay
import playscalajs.ScalaJSPlay.autoImport.{sourceMapsBase, sourceMapsDirectories}
import playscalajs.PlayScalaJS.autoImport.{scalaJSProjects, scalaJSProd}

//
import com.typesafe.sbt.web.Import.pipelineStages
import com.typesafe.sbt.packager.universal.UniversalKeys
import com.typesafe.sbt.gzip.Import.gzip
import play.Play._
import play.Play.autoImport._
import PlayKeys._

object ApplicationBuild extends Build {

  override def rootProject = Some(showcase_server)

  /**
   *
   */
  lazy val showcase_shared = (crossProject.crossType(CrossType.Pure) in file("shared")).
    settings(
      scalaVersion := Versions.scala,
      libraryDependencies ++= Dependencies.shared.value,

  //No need for the TestFramework in "shared"
//      /**
//       * No need for
//       * utest.jsrunner.Plugin.utestJsSettings
//       * see https://github.com/lihaoyi/utest/blob/master/jsPlugin/Plugin.scala
//       */
//      testFrameworks += new TestFramework("utest.runner.Framework")
    )
    .jsConfigure(_ enablePlugins ScalaJSPlay)
    .jsSettings(sourceMapsBase := baseDirectory.value / "..")

  lazy val showcase_shared_jvm = showcase_shared.jvm
  lazy val showcase_shared_js = showcase_shared.js

  /**
   *
   */
  lazy val showcase_server = (project in file("jvm")).settings(
    scalaVersion := Versions.scala,
    scalaJSProjects := Seq(showcase_client),
    pipelineStages := Seq(scalaJSProd, gzip),
    libraryDependencies ++= Seq(
      "com.vmunier" %% "play-scalajs-scripts" % "0.1.0",
      "org.webjars" % "jquery" % "1.11.1"),
    libraryDependencies ++= Dependencies.jvm.value,
      // For bindableChar
    routesImport += "config.Routes._"/*,
    EclipseKeys.skipParents in ThisBuild := false*/)
    .enablePlugins(play.PlayScala)
    .aggregate(showcase_client)
    .dependsOn(projectToRef(showcase_shared_jvm))

  /**
   *
   */
  lazy val showcase_client = (project in file("js")).settings(
    scalaVersion := Versions.scala,
    persistLauncher := true,
    persistLauncher in Test := false,
    sourceMapsDirectories += showcase_shared_js.base / "..",
    unmanagedSourceDirectories in Compile := Seq((scalaSource in Compile).value),
    libraryDependencies ++= Seq("org.scala-js" %%% "scalajs-dom" % "0.8.0"),
    libraryDependencies ++= Dependencies.js.value,
    /**
     * No need for
     * utest.jsrunner.Plugin.utestJsSettings
     * see https://github.com/lihaoyi/utest/blob/master/jsPlugin/Plugin.scala
     */
    testFrameworks += new TestFramework("utest.runner.Framework"),
    jsDependencies ++= Seq(
      RuntimeDOM,
      //"org.webjars" % "jquery" % Versions.jquery / "jquery.js"
      "org.webjars" % "jquery" % "1.10.2" / "jquery.js"
      //"com.lihaoyi" %% "upickle" % Versions.uPickle
    )).
    enablePlugins(ScalaJSPlugin, ScalaJSPlay).
    dependsOn(showcase_shared_js)


  // Only if you use IntelliJ: the shared project makes IntelliJ happy without using symlinks
  lazy val showcase_shared_dev = Project("shared", file("shared"))
}

object Dependencies {
  val shared = Def.setting(Seq(
    "com.lihaoyi" %% "upickle" % Versions.uPickle,
    // @todo: uPickle must not be last...?? gives compile errors "upickle cannot read"
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
    , "com.lihaoyi" %%% "utest" % Versions.uTest % "test"
  ))
}

object Versions {
  val app = "0.1.0-SNAPSHOT"
  val scala = "2.11.2"
  //val scalaJSVersion = "-RC2"
  val scalajsDom = "0.7.0"
  val jquery = "1.11.1"
  val playScalajsSourcemaps = "0.1.0"
  val uPickle = "0.2.6"
  val uTest = "0.3.0"
  val scalaTags = "0.4.5"
  val scalaRx = "0.2.7"
}
