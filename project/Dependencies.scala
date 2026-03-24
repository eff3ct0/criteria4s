import sbt._

object Dependencies {
  lazy val munit = "org.scalameta" %% "munit" % "1.0.0"

  lazy val mongodbDriverSync  = "org.mongodb"          % "mongodb-driver-sync"  % "5.4.0"
  lazy val elasticsearchJava  = "co.elastic.clients"   % "elasticsearch-java"   % "8.17.0"
  lazy val clickhouseClientV2 = "com.clickhouse"       % "client-v2"           % "0.9.0"
}
