## Template ToDo list
- [x] Create a new [IntelliJ Platform Plugin Template][template] project.
- [x] Get familiar with the [template documentation][template].
- [x] Adjust the [pluginGroup](./gradle.properties), [plugin ID](./src/main/resources/META-INF/plugin.xml) and [sources package](./src/main/kotlin).
- [x] Adjust the plugin description in `README` (see [Tips][docs:plugin-description])
- [ ] Review the [Legal Agreements](https://plugins.jetbrains.com/docs/marketplace/legal-agreements.html?from=IJPluginTemplate).
- [ ] [Publish a plugin manually](https://plugins.jetbrains.com/docs/intellij/publishing-plugin.html?from=IJPluginTemplate) for the first time.
- [ ] Set the `PLUGIN_ID` in the above README badges.
- [ ] Set the [Plugin Signing](https://plugins.jetbrains.com/docs/intellij/plugin-signing.html?from=IJPluginTemplate) related [secrets](https://github.com/JetBrains/intellij-platform-plugin-template#environment-variables).
- [ ] Set the [Deployment Token](https://plugins.jetbrains.com/docs/marketplace/plugin-upload.html?from=IJPluginTemplate).
- [x] Click the <kbd>Watch</kbd> button on the top of the [IntelliJ Platform Plugin Template][template] to be notified about releases containing new features and fixes.

# Modular plugin / Remote dev (Gateway)
* https://plugins.jetbrains.com/docs/intellij/modular-plugins.html
* https://github.com/JetBrains/intellij-platform-modular-plugin-template

Key rules for Plugin Model V2:
* Root `plugin.xml`: no `<depends>`, only declares `<content>` modules. `package` attribute is deprecated — content modules should be packaged into separate jars under `lib/modules`
* Module descriptors (not in `META-INF/`): use `<dependencies><module>` for dependencies
* Split: `shared` (RPC interfaces, DTOs), `frontend` (UI, depends on `intellij.platform.frontend`), `backend` (services, depends on `intellij.platform.backend`)
* RPC: `@Rpc` interface in shared, `RemoteApiProvider` in backend, resolve with `RemoteApiProviderService.resolve()` in frontend
* Use `ProjectId` (not `Project`) across RPC boundary, all params must be `@Serializable`

https://plugins.jetbrains.com/docs/intellij/welcome.html
https://plugins.jetbrains.com/docs/intellij/basic-action-system.html
https://github.com/JetBrains/intellij-community/blob/master/platform/lang-impl/src/com/intellij/ide/projectView/actions/UnmarkRootAction.java
https://github.com/JetBrains/intellij-community/blob/master/platform/lang-impl/src/com/intellij/ide/projectView/actions/MarkRootActionBase.java
https://github.com/JetBrains/intellij-community/blob/master/python/resources/META-INF/pycharm-core.xml#L49

https://plugins.jetbrains.com/docs/intellij/useful-links.html#repositories
https://plugins.jetbrains.com/docs/intellij/kotlin-coroutines.html

# UI
* https://plugins.jetbrains.com/docs/intellij/user-interface-components.html
* https://plugins.jetbrains.com/docs/intellij/kotlin-ui-dsl-version-2.html
* https://jetbrains.design/intellij/

# Examples repos
* https://github.com/aws/aws-toolkit-jetbrains
* https://github.com/ignatov/intellij-erlang
* https://github.com/bazelbuild/intellij

# Modular plugin / Remote dev (Gateway)
* https://plugins.jetbrains.com/docs/intellij/modular-plugins.html
* https://github.com/JetBrains/intellij-platform-modular-plugin-template

Key rules for Plugin Model V2:
* Root `plugin.xml`: no `<depends>`, must have `package` attribute, only declares `<content>` modules
* Module descriptors (not in `META-INF/`): use `<dependencies><module>` for dependencies
* Split: `shared` (RPC interfaces, DTOs), `frontend` (UI, depends on `intellij.platform.frontend`), `backend` (services, depends on `intellij.platform.backend`)
* RPC: `@Rpc` interface in shared, `RemoteApiProvider` in backend, resolve with `RemoteApiProviderService.resolve()` in frontend
* Use `ProjectId` (not `Project`) across RPC boundary, all params must be `@Serializable`

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
[docs:plugin-description]: https://plugins.jetbrains.com/docs/intellij/plugin-user-experience.html#plugin-description-and-presentation
