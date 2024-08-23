# Managed Excludes

![Build](https://github.com/gplassard/managed-excludes/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/PLUGIN_ID.svg)](https://plugins.jetbrains.com/plugin/PLUGIN_ID)

<!-- Plugin description -->
This plugin makes it possible to manage your project excluded files through configuration files that you can track in version control.

## Managed excludes files

The plugin watches any `.managed-excludes` files and expect the following format
```
apps/go-app/lib
# The file can contain comment
# The paths are relative to the current file's directory 
apps/go-app/app
```

## Bazel project files

The plugin also provides support for `*.bazelproject` [files](https://ij.bazel.build/docs/project-views.html) and will prompt you to track them in order to track excluded paths from the file
(only explicit exclusions are supported, there is no support for the allow pattern)

<!-- Plugin description end -->

## Installation
- Manually:

  Download the [latest release](https://github.com/gplassard/managed-excludes/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Through a custom plugin repository :
  
  You can use https://raw.githubusercontent.com/gplassard/managed-excludes/main/updatePlugins.xml as a plugin repository
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Manage Plugin Repositories...</kbd>

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
