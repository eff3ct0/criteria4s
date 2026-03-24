# AGENTS.md - Criteria4s

## Project Overview

Criteria4s is a **Scala 2.13** library providing a type-safe, data-store-agnostic DSL for
defining criteria and predicate expressions. It uses a **tagless-final / type-class** pattern
so that one polymorphic criteria definition can be evaluated against multiple backends
(SQL, MongoDB, custom dialects).

**Build tool:** sbt 1.9.8  
**Scala version:** 2.13.12  
**JVM target:** 8 (`-release:8`)  
**Organization:** `com.eff3ct`

### Module structure

| Module               | Path         | Published | Dependencies       |
|----------------------|--------------|-----------|--------------------|
| `criteria4s-core`    | `core/`      | Yes       | (none)             |
| `criteria4s-sql`     | `sql/`       | Yes       | core               |
| `criteria4s-mongodb` | `mongodb/`   | Yes       | core               |
| `criteria4s-examples`| `examples/`  | No        | core, sql, mongodb |

## Build / Lint / Test Commands

```bash
# Compile all modules
sbt compile

# Compile a single module
sbt "criteria4s-core / compile"
sbt "criteria4s-sql / compile"
sbt "criteria4s-mongodb / compile"
sbt "criteria4s-examples / compile"

# Run all tests (cross-compiled)
sbt +test

# Run tests for a single module
sbt "criteria4s-core / test"
sbt "criteria4s-sql / test"

# Run a single test class (MUnit)
sbt "criteria4s-core / testOnly com.eff3ct.criteria4s.SomeSpec"

# Run a single test by name
sbt "criteria4s-core / testOnly com.eff3ct.criteria4s.SomeSpec -- --tests=testName"

# Format all sources with Scalafmt
sbt scalafmtAll

# Check formatting without modifying files
sbt scalafmtCheckAll

# Generate/update MIT license headers on all source files
sbt headerCreateAll

# Full CI check (what CI runs)
sbt headerCreateAll scalafmtAll && sbt -v +test
```

**Note:** The test framework is MUnit (`org.scalameta %% munit % 0.7.29`). Test options
include `-oDF` (full stack traces, durations) and JUnit `-v -a`.

## Code Style Guidelines

### Formatting (Scalafmt 3.7.3)

Configuration is in `.scalafmt.conf`. Key rules:

- **Max line length:** 100 columns
- **Dialect:** `scala213`
- **Style:** `defaultWithAlign`
- **Continuation indent:** 2 at call sites
- **Import selectors:** single line
- **Rewrite rules:** `SortImports`, `RedundantBraces` (max 1 line)
- **Docstrings:** Asterisk style (`/** ... */`), no wrapping

Run `sbt scalafmtAll` before committing. CI enforces formatting.

### Imports

- Imports are **sorted automatically** by Scalafmt (`SortImports` rule)
- Use **wildcard imports** for core packages: `import com.eff3ct.criteria4s.core._`
- Use **selective imports** when importing specific members from sub-packages:
  `import com.eff3ct.criteria4s.core.PredicateBinary._`
- Alias imports with `{original => alias}` syntax when needed:
  `import com.eff3ct.criteria4s.{functions => F}`
- Keep import selectors on a single line

### Naming Conventions

- **Type aliases** for DSL types: ALL_CAPS (`AND`, `OR`, `NOT`, `GT`, `LT`, `EQ`, `NEQ`,
  `GEQ`, `LEQ`, `LIKE`, `IN`, `NOTIN`, `ISNULL`, `ISNOTNULL`, `BETWEEN`, `NOTBETWEEN`)
- **Traits** defining dialects: PascalCase (`SQL`, `MongoDB`, `CriteriaTag`)
- **Implicit vals:** camelCase with type suffix (`andConj`, `orConj`, `eqPred`, `gtPred`,
  `showColumn`)
- **Implicit classes:** PascalCase with `Implicit` suffix (`CriteriaPredImplicit`,
  `CriteriaConjImplicit`)
- **Type parameters:** single uppercase letter -- `T` for CriteriaTag, `V`/`L`/`R` for values,
  `H` for higher-kinded type class constructors
- **Package objects:** lowercase, used to export type aliases and implicits
- **Private traits as companions:** lowercase trait name, PascalCase object
  (e.g., `trait predicates` / `object predicates extends predicates`)

### Type Patterns

- **CriteriaTag bound:** All type-class instances and DSL types are parameterized by
  `T <: CriteriaTag`
- **Higher-kinded type classes:** `H[_ <: CriteriaTag]` for builder patterns
- **Context bounds** preferred for implicit type-class evidence in function signatures:
  `def lt[T <: CriteriaTag: LT, L, R](...)`
- **Implicit parameters** use `(implicit H: TypeClass[T], show: Show[V, T])` form
- **Show type class:** `Show[V, T]` renders value type `V` for dialect `T`

### Architecture Patterns

- **Tagless-final:** Abstract algebra in `core/`, concrete interpreters in `sql/` and `mongodb/`
- **Extension methods** via implicit classes in `extensions/` package
- **Function-style API** in `functions/` package (standalone `def lt(...)`, `def gt(...)`)
- **Builder pattern** for type-class instances: `build[T, H](f)` from `instances/`
- **Dialect implementation:** Extend `CriteriaTag` (e.g., `trait SQL extends CriteriaTag`),
  then provide a `SQLExpr[T <: SQL]` trait with all implicit instances

### File Organization

- One primary trait/class per file, file named after the type
- Package objects in `package.scala` files -- used for type aliases and mixin exports
- Private implementation traits use `private[packageName]` visibility
- Source layout: `src/main/scala/com/eff3ct/criteria4s/{module}/`
- Tests go in: `src/test/scala/com/eff3ct/criteria4s/{module}/`

### License Headers

Every `.scala` file **must** have the MIT license header. Run `sbt headerCreateAll` to
auto-generate headers. CI will fail if headers are missing. The header is managed by
`sbt-header` plugin and defined in `project/Build.scala`.

### Compiler Warnings

The build enables extensive `-Xlint` and `-Ywarn` flags including:
- `-Ywarn-unused:imports,implicits,locals,params,patvars,privates`
- `-Ywarn-dead-code`, `-Ywarn-extra-implicit`
- `-Xlint:adapted-args,constant,missing-interpolator,private-shadow,type-parameter-shadow`
- `-unchecked`, `-deprecation`, `-feature`

Do not leave unused imports, variables, or parameters. The compiler will warn about them.
`-Xfatal-warnings` is currently commented out but may be enabled in the future.

### Error Handling

- This is a pure DSL library with no runtime effects or exceptions
- Invalid states are prevented at compile time via type-class constraints
- `Criteria.pure[T](v: String)` is the only runtime constructor for criteria values

### Dependencies

- **Zero runtime dependencies** for core, sql, and mongodb modules
- `kind-projector` compiler plugin (`0.13.2`) used in the examples module
- MUnit (`0.7.29`) for testing

### Publishing

- Published to Sonatype Central via `sbt-ci-release`
- Version scheme: `early-semver`
- Release branches: `branch-0.x`, tags trigger releases
