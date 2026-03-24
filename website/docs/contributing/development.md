---
sidebar_position: 1
title: Development Guide
---

# Development Guide

## Prerequisites

- **JDK 11+** (the project compiles with `-release:11`)
- **sbt** (Scala Build Tool)
- **Node.js** (for the documentation website only)

## Clone and Build

```bash
git clone https://github.com/eff3ct/criteria4s.git
cd criteria4s
sbt compile
```

## Running Tests

```bash
# Run all tests
sbt test

# Run tests for a specific module
sbt "sql / test"
sbt "mongodb / test"
sbt "elasticsearch / test"
sbt "postgresql / test"
sbt "mysql / test"
sbt "sparksql / test"
sbt "sql-jdbc / test"
sbt "mongodb-driver / test"
sbt "elasticsearch-client / test"
```

## Project Structure

```
criteria4s/
├── core/                    # Core types: Criteria, Ref, Show, Predicate, Conjunction, Transform
├── sql/                     # Base SQL dialect (trait SQL, SQLExpr)
├── postgresql/              # PostgreSQL dialect (double-quoted columns)
├── mysql/                   # MySQL dialect (backtick-quoted columns)
├── sparksql/                # Spark SQL dialect (backtick-quoted columns)
├── mongodb/                 # MongoDB dialect ($operator JSON)
├── elasticsearch/           # Elasticsearch dialect (Query DSL JSON)
├── sql-jdbc/                # JDBC integration (toWhereClause, appendTo)
├── mongodb-driver/          # MongoDB driver integration (toBson)
├── elasticsearch-client/    # Elasticsearch client integration (toQuery)
├── examples/                # Example usage code
├── criteria4s-docs/         # mdoc documentation project
├── docs/                    # Documentation source (mdoc input)
├── website/                 # Docusaurus website
└── project/                 # sbt build configuration
    ├── Build.scala          # Common build settings, scalac options, headers
    ├── Dependencies.scala   # Library dependency versions
    ├── Version.scala        # Scala version
    └── SonatypePublish.scala # Publishing configuration
```

### Module Dependency Graph

```
core
├── sql
│   ├── postgresql
│   ├── mysql
│   ├── sparksql
│   └── sql-jdbc
├── mongodb
│   └── mongodb-driver
└── elasticsearch
    └── elasticsearch-client
```

## Running Documentation Locally

The documentation uses [mdoc](https://scalameta.org/mdoc/) for type-checked Scala code blocks and [Docusaurus](https://docusaurus.io/) for the website.

### Compile mdoc

```bash
sbt docs/mdoc
```

This compiles all ````scala mdoc` code blocks in `docs/` and outputs the result to `website/docs/`.

### Start the Development Server

```bash
cd website
npm install   # first time only
npm start
```

This starts a local Docusaurus dev server at `http://localhost:3000`.

### Full Workflow

```bash
# 1. Edit files in docs/
# 2. Compile with mdoc
sbt docs/mdoc
# 3. Preview
cd website && npm start
```

## Code Style

### scalafmt

The project uses scalafmt for code formatting. Format your code before committing:

```bash
sbt scalafmtAll
sbt scalafmtCheckAll  # CI check
```

### License Headers

All source files must include the MIT license header. The `sbt-header` plugin manages this automatically:

```bash
sbt headerCreateAll   # Add missing headers
sbt headerCheckAll    # CI check
```

### Compiler Warnings

The build enables strict warnings that are treated as guidance:

- `-Wunused:imports` -- unused imports
- `-Wunused:privates` -- unused private members
- `-Wunused:locals` -- unused local definitions
- `-Wunused:params` -- unused parameters

## Publishing

The project publishes to Maven Central via Sonatype. This is handled by CI -- you should not need to publish manually.
