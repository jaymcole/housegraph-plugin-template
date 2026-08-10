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

## Four rules

**1. `compileOnly` the API — never `implementation`.**
HouseGraph supplies `housegraph-api` and its transitive `org.json` and `slf4j-api` from its own
class loader. Bundling them gives your library its own copy of `BaseNode`, so every node in it
fails the host's `isAssignableFrom` check during discovery and simply **never appears**, with
nothing in the log to explain why. Bundling `slf4j-api` gives you a second logging binding with
no outputs attached, so all your log lines silently vanish. The installer rejects a jar
containing either, to turn those into one clear message.

**2. Relocate everything you bundle.**
All installed libraries share one class loader. Two libraries bundling different versions of the
same dependency would fight. Anything you declare `implementation` ends up in the shaded jar and
needs a `relocate` line in `shadowJar`.

**3. Keep `mergeServiceFiles()`.**
Any bundled library that uses `ServiceLoader` — DJL's engine discovery, JDBC drivers — breaks
without it, at runtime, with a confusing "no provider found".

**4. Always `@Node.Type`, prefixed with your library id.**
It pins the id your node is written under in save files, independent of the class name. Without
it, renaming or moving the class strands every graph anyone saved using it.

## Node design: control vs. action

Most nodes should be either **control-oriented** (a trigger, a timer, a branch, a loop —
deciding *when* something downstream runs) or **action-oriented** (calling an API, reading a
sensor, writing a file — doing the actual work), not both in one class.

An action node's flow ports should describe the **outcome of one invocation** — "it ran," and
optionally which of a few known results happened — not a schedule it manages itself. If your node
wants to poll on an interval, don't give it its own timer: give it a flow-in and let a
repeating-trigger node, wired upstream, decide when it fires. That keeps the action reusable with
any trigger, and directly testable by calling `process()` on it rather than needing to spin up
and tear down a timer to exercise it.

**If a request for a new node describes it both scheduling/looping/branching its own execution
*and* performing an external action, treat that as a smell** — ask whether it should be two
composable nodes (a control node feeding an action node) before building the fused version. The
one common exception is a *resource* node that owns a real connection lifecycle (a bot, a
server) — see `AutoStartable` and `NodeContentProvider` in the table below — where the
running/stopped state genuinely belongs to the node itself.

## Things that will bite you otherwise

- **You must apply the JavaFX Gradle plugin** (this template does). HouseGraph's published
  metadata names JavaFX *without* a platform classifier on purpose, so a release built on Linux
  can't pin the wrong natives into your build — which means the unclassified artifacts are
  ~300-byte stubs. Without the plugin you get `package javafx.scene does not exist`.
- **A node's static initializer runs at first instantiation, not at discovery.** The host loads
  classes with `initialize = false`. So a type you register from a static block —
  `ValueEditors.register(...)`, `TypeConverters.register(...)` — only takes effect once one of
  your nodes exists. The symptom of assuming otherwise is "my custom type isn't editable until I
  place the node twice." Registering from a constructor avoids the question.
- **`onExecuted()` reaches you on the JavaFX thread**, dispatched through the host's callback
  executor — so your UI code needs no `Platform.runLater`. Work *you* start (a socket bind, an
  HTTP call, a gateway login) does: keep that off the FX thread and hop back to show its result.
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
