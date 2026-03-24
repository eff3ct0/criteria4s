import Dependencies.munit

lazy val criteria4s: Project =
  project
    .in(file("."))
    .disablePlugins(Build, AssemblyPlugin, HeaderPlugin)
    .aggregate(core, sql, mongodb, postgresql, mysql, sparksql, elasticsearch, examples)
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

lazy val mysql: Project =
  (project in file("mysql"))
    .settings(
      name                := "criteria4s-mysql",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(sql)

lazy val sparksql: Project =
  (project in file("sparksql"))
    .settings(
      name                := "criteria4s-sparksql",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(sql)

lazy val elasticsearch: Project =
  (project in file("elasticsearch"))
    .settings(
      name                := "criteria4s-elasticsearch",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(core)

lazy val examples: Project =
  (project in file("examples"))
    .settings(
      name := "criteria4s-examples"
    )
    .dependsOn(core, sql, mongodb, postgresql, mysql, sparksql, elasticsearch)
