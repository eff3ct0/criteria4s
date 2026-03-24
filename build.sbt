import Dependencies.{ProjectOps, munit}

lazy val criteria4s: Project =
  project
    .in(file("."))
    .disablePlugins(Build, AssemblyPlugin, HeaderPlugin)
    .aggregate(core, sql, mongodb, postgresql, examples)
    .settings(
      name := "criteria4s"
    )

lazy val core: Project =
  (project in file("core"))
    .settings(
      name                := "criteria4s-core",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )

lazy val sql: Project =
  (project in file("sql"))
    .settings(
      name                := "criteria4s-sql",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(core)

lazy val mongodb: Project =
  (project in file("mongodb"))
    .settings(
      name                := "criteria4s-mongodb",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(core)

lazy val postgresql: Project =
  (project in file("postgresql"))
    .settings(
      name                := "criteria4s-postgresql",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(sql)

lazy val examples: Project =
  (project in file("examples"))
    .settings(
      name := "criteria4s-examples"
    )
    .dependsOn(core, sql, mongodb, postgresql)
    .withKindProjector
