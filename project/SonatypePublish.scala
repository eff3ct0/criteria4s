import sbt.Keys._
import sbt.{url, _}
import xerial.sbt.Sonatype._
import xerial.sbt.Sonatype.autoImport._

import scala.collection.Seq

object SonatypePublish {

  def projectSettings: Seq[Setting[_]] = Seq(
    ThisBuild / publish / skip         := true,
    ThisBuild / versionScheme          := Some("early-semver"),
    ThisBuild / sonatypeCredentialHost := sonatypeCentralHost,
    ThisBuild / organization           := "com.eff3ct",
    ThisBuild / organizationName       := "eff3ct",
    ThisBuild / organizationHomepage   := Some(url("https://github.com/eff3ct0")),
    ThisBuild / homepage               := Some(url("https://github.com/eff3ct0/criteria4s")),
    ThisBuild / licenses               := Seq("MIT" -> url("https://opensource.org/licenses/MIT")),
    ThisBuild / description := "A simple DSL to create criterias for filtering data in Scala.",
    ThisBuild / scmInfo := Some(
      ScmInfo(
        browseUrl = url("https://github.com/eff3ct0/criteria4s"),
        connection = "scm:git@github.com:eff3ct0/criteria4s.git"
      )
    ),
    ThisBuild / developers := List(
      Developer(
        id = "rafafrdz",
        name = "Rafael Fernandez",
        email = "hi@rafaelfernandez.dev",
        url = url("https://rafaelfernandez.dev")
      )
    ),
    ThisBuild / sonatypeProjectHosting := Some(
      GitHubHosting(
        user = "eff3ct0",
        repository = "criteria4s",
        email = "hi@rafaelfernandez.dev"
      )
    )
  )

}
