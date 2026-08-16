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

## Read the rules first

**→ [`docs/shared/node-library-rules.md`](docs/shared/node-library-rules.md)**

The build rules, the API surface you can use, node design, and a word about trust. Every rule
there has a **silent** failure mode — a node that never appears, logging that vanishes into
nowhere, a saved graph that can't find its nodes again — so it is worth ten minutes before you
write a build file.

In short: `compileOnly` the API, relocate everything you bundle, keep `mergeServiceFiles()`,
always `@Node.Type` prefixed with your library id, and apply the JavaFX plugin. **This template
already does all of that** — the rules matter when you start changing the build.

That file is maintained in
[HouseGraph](https://github.com/jaymcole/HouseGraph/blob/main/docs/shared/node-library-rules.md)
and synced here. Editing the copy in your repository is fine once you've used the template —
it's your repository now — but it won't flow back upstream.

## License

MIT — see [LICENSE](LICENSE). Change it to whatever suits your library.
