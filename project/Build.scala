import sbtheader.HeaderPlugin.autoImport.{HeaderLicense, headerLicense}
import sbtheader.{HeaderPlugin, License}
import sbt.Keys._
import sbt._
import sbt.plugins.JvmPlugin
import sbtassembly.AssemblyKeys._
import sbtassembly.{MergeStrategy, PathList}

object Build extends AutoPlugin {

  override def requires: Plugins = JvmPlugin && HeaderPlugin

  override def trigger: PluginTrigger = allRequirements

  lazy val jvmSettings: Seq[String] = Seq(
    "-XX:+UseG1GC",
    "-XshowSettings:vm",
    "-XX:+PrintCommandLineFlags"
  )

  lazy val localJvmSettings: Seq[String] =
    Seq(
      "-Xms384M",
      "-Xmx384M",
      "-XX:MaxMetaspaceSize=150M",
      "-XX:+PrintCommandLineFlags",
      "-Duser.timezone=GMT"
    )

  override def projectSettings: Seq[Setting[_]] =
    Vector(
      ThisBuild / organizationName   := "eff3ct",
      ThisBuild / organization       := "com.eff3ct",
      ThisBuild / scalaVersion       := Version.Scala,
      ThisBuild / crossScalaVersions := Vector(scalaVersion.value),
      ThisBuild / javacOptions       := Seq("-g:none"),
      ThisBuild / run / javaOptions ++= localJvmSettings,
      ThisBuild / run / fork  := true,
      ThisBuild / Test / fork := true,
      headerLicense           := Some(headerIOLicense),
      ThisBuild / scalacOptions ++= Vector(
        "-release:11",
        "-deprecation",
        "-feature",
        "-encoding", "UTF-8",
        "-unchecked",
        "-language:implicitConversions",
        "-Wunused:imports",
        "-Wunused:privates",
        "-Wunused:locals",
        "-Wunused:params",
        "-Xkind-projector"
      ),
      Compile / console / scalacOptions ~= (_.filterNot(
        Set(
          "-Wunused:imports"
        )
      )),
      ThisBuild / updateOptions := updateOptions.value
        .withCachedResolution(cachedResolution = false),
      // do not build and publish scaladocs
      ThisBuild / Compile / doc / sources := Seq.empty,
      // Remove this one because: https://github.com/sbt/sbt-ci-release/issues/168
      // ThisBuild / Compile / packageDoc / publishArtifact := false,
      // show full stack traces and test case durations
      ThisBuild / Test / testOptions += Tests.Argument("-oDF"),
      // -v Log "test run started" / "test started" / "test run finished" events on log level "info" instead of "debug"
      // -a Show stack traces and exception class name for AssertionErrors
      ThisBuild / Test / testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a")
    ) ++ SonatypePublish.projectSettings

  lazy val assemblySettings = Seq(
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x =>
        val oldStrategy = (assembly / assemblyMergeStrategy).value
        oldStrategy(x)
    },
    assembly / assemblyJarName := s"${name.value}-${version.value}.jar"
  )

  /**
   * SBT Header Plugin
   */

  lazy val headerText: String =
    """|MIT License
       |
       |Copyright (c) 2024-2026 Rafael Fernandez
       |
       |Permission is hereby granted, free of charge, to any person obtaining a copy
       |of this software and associated documentation files (the "Software"), to deal
       |in the Software without restriction, including without limitation the rights
       |to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
       |copies of the Software, and to permit persons to whom the Software is
       |furnished to do so, subject to the following conditions:
       |
       |The above copyright notice and this permission notice shall be included in all
       |copies or substantial portions of the Software.
       |
       |THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
       |IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
       |FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
       |AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
       |LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
       |OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
       |SOFTWARE.
       |""".stripMargin

  lazy val headerIOLicense: License.Custom =
    HeaderLicense.Custom(headerText)
}
