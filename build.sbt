lazy val `criterial-dsl`: Project =
  project
    .in(file("."))
    .disablePlugins(AssemblyPlugin)
    .aggregate(core, sql, examples)
    .settings(
      name := "criterial-dsl"
    )

lazy val core: Project =
  (project in file("core"))
    .settings(
      name           := "core",
      publish / skip := false
    )

lazy val sql: Project =
  (project in file("sql"))
    .settings(
      name           := "sql",
      publish / skip := false
    )
    .dependsOn(core)

lazy val examples: Project =
  (project in file("examples"))
    .settings(
      name := "examples"
    )
    .dependsOn(core, sql)
