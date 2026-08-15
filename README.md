# HouseGraph node library template

A starting point for a **node library** — a set of node types that HouseGraph fetches from
your GitHub repository and loads at runtime. You don't fork HouseGraph itself.

Click **Use this template**, then:

1. Edit the `ext { }` block at the top of `build.gradle` — `pluginId`, `pluginName`,
   `pluginRepository`, `nodePackages`.
2. Set `rootProject.name` in `settings.gradle` to your `pluginId`.
3. Rename `com.example.housegraph.hello` to your own package, and keep `nodePackages` pointing
   at wherever your node classes live.
4. Write nodes. `HelloWorldNode` is a complete one to copy.
5. `git tag v0.1.0 && git push --tags`. The workflow builds the jar and attaches it to a
   GitHub release.
6. In HouseGraph: **Node Libraries… → Add from URL…**, paste this repository's URL.

```bash
./gradlew build
```

## Rules

Two rules are the same for every out-of-tree node library, and are documented once, canonically,
in HouseGraph's own docs rather than here:
[`docs/architecture/plugins.md`](https://github.com/jaymcole/HouseGraph/blob/main/docs/architecture/plugins.md#consuming-housegraph-api) —
**`compileOnly` the API, never `implementation`** (bundling it hides every node in your library
from discovery with no explanation in the log), and **always `@Node.Type`, prefixed with your
library id** (it pins the id your node is written under in save files, independent of the class
name — without it, renaming or moving the class strands every graph anyone saved using it).

Two more are specific to bundling third-party dependencies into this template's shaded jar:

**Relocate everything you bundle.**
All installed libraries share one class loader. Two libraries bundling different versions of the
same dependency would fight. Anything you declare `implementation` ends up in the shaded jar and
needs a `relocate` line in `shadowJar`.

**Keep `mergeServiceFiles()`.**
Any bundled library that uses `ServiceLoader` — DJL's engine discovery, JDBC drivers — breaks
without it, at runtime, with a confusing "no provider found".

## Node design: control vs. action

Most nodes should be either **control-oriented** (deciding *when* something downstream runs) or
**action-oriented** (doing the work, reporting only the outcome of one invocation), not both in
one class — see HouseGraph's
[`docs/architecture/nodes.md`](https://github.com/jaymcole/HouseGraph/blob/main/docs/architecture/nodes.md#designing-a-nodes-ports-control-vs-action)
for the full rationale, the worked example, and the one common exception (a resource node that
owns a real connection lifecycle — `AutoStartable` / `NodeContentProvider` in the table below).

## Things that will bite you otherwise

- **You must apply the JavaFX Gradle plugin** (this template does). HouseGraph's published
  metadata names JavaFX *without* a platform classifier on purpose, so a release built on Linux
  can't pin the wrong natives into your build — which means the unclassified artifacts are
  ~300-byte stubs. Without the plugin you get `package javafx.scene does not exist`.
- **Static-initializer timing and which thread `onExecuted()` reaches you on** work the same way
  here as in every node library — see
  [`docs/architecture/ui.md`](https://github.com/jaymcole/HouseGraph/blob/main/docs/architecture/ui.md)
  for both.
- **The asset name matters if you publish several libraries from one repository.** HouseGraph
  matches a library to its jar as `<pluginId>-<version>-all.jar`. With a single library in the
  repository there's nothing to disambiguate and any name works.

## What you can use

Everything in `housegraph-api`:

| | |
| --- | --- |
| `graph` | `BaseNode`, `NodeVariable`, `FlowPort`, `Edge`, `ProcessContext`, `ExecutionPolicy`, `TypeConverters` |
| `annotations` | `@Display.Name`, `@Node.Type`, `@Node.Disabled` |
| `sdk` | `NodeContentProvider` (inline JavaFX UI), `AutoStartable` (resume on load), `ValueEditors`, `Secrets` |
| `logging` | `Log.get(YourClass.class)` — lands in HouseGraph's own log window and file |
| `resource` | `ResourceRegistry` — long-lived resources referenced by name rather than wired |
| `storage`, `store` | `AppDirectories`, `SecretsStore`, `JsonDocumentStore` |

## A word about trust

A node library runs **inside HouseGraph's JVM with the user's full privileges**: their files,
their network, their saved secrets. There is no sandbox — `SecurityManager` is gone in Java 21+
and the module system carries no permission model. Installing a node library is exactly as
dangerous as running any other program you downloaded, and HouseGraph says so when installing
one. Please treat other people's trust accordingly.

## License

MIT — see [LICENSE](LICENSE). Change it to whatever suits your library.
