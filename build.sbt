import Dependencies.{munit, mongodbDriverSync, elasticsearchJava, clickhouseClientV2}

lazy val criteria4s: Project =
  project
    .in(file("."))
    .disablePlugins(Build, AssemblyPlugin, HeaderPlugin)
    .aggregate(core, sql, mongodb, postgresql, mysql, sparksql, duckdb, clickhouse, elasticsearch, `sql-jdbc`, `mongodb-driver`, `elasticsearch-client`, `clickhouse-client`, examples)
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

lazy val duckdb: Project =
  (project in file("duckdb"))
    .settings(
      name                := "criteria4s-duckdb",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(sql)

lazy val clickhouse: Project =
  (project in file("clickhouse"))
    .settings(
      name                := "criteria4s-clickhouse",
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

lazy val `sql-jdbc`: Project =
  (project in file("sql-jdbc"))
    .settings(
      name                := "criteria4s-sql-jdbc",
      publish / skip      := false,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(sql)

lazy val `mongodb-driver`: Project =
  (project in file("mongodb-driver"))
    .settings(
      name                := "criteria4s-mongodb-driver",
      publish / skip      := false,
      libraryDependencies += mongodbDriverSync,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(mongodb)

lazy val `elasticsearch-client`: Project =
  (project in file("elasticsearch-client"))
    .settings(
      name                := "criteria4s-elasticsearch-client",
      publish / skip      := false,
      libraryDependencies += elasticsearchJava,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(elasticsearch)

lazy val `clickhouse-client`: Project =
  (project in file("clickhouse-client"))
    .settings(
      name                := "criteria4s-clickhouse-client",
      publish / skip      := false,
      libraryDependencies += clickhouseClientV2,
      libraryDependencies += munit % Test,
      testFrameworks      += new TestFramework("munit.Framework")
    )
    .dependsOn(clickhouse)

lazy val docs: Project =
  (project in file("criteria4s-docs"))
    .enablePlugins(MdocPlugin)
    .disablePlugins(Build, AssemblyPlugin, HeaderPlugin)
    .settings(
      name       := "criteria4s-docs",
      mdocIn     := (ThisBuild / baseDirectory).value / "docs",
      mdocOut    := (ThisBuild / baseDirectory).value / "website" / "docs",
      mdocVariables := Map(
        "VERSION" -> "1.0.0"
      ),
      publish / skip := true
    )
    .dependsOn(core, sql, mongodb, postgresql, mysql, sparksql, duckdb, clickhouse, elasticsearch, `sql-jdbc`, `mongodb-driver`, `elasticsearch-client`, `clickhouse-client`)

lazy val examples: Project =
  (project in file("examples"))
    .settings(
      name := "criteria4s-examples"
    )
    .dependsOn(core, sql, mongodb, postgresql, mysql, sparksql, duckdb, clickhouse, elasticsearch)
