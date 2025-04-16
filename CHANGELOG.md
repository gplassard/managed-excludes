<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# managed-excludes Changelog

## [Unreleased]

## [0.0.18] - 2025-04-16

- Bump IDEA version range

## [0.0.17] - 2024-12-21

- The plugin now infers the right bazel output directory for workspace names containing `_`

## [0.0.16] - 2024-12-20

- The plugin now autodetect `WORKSPACE.bazel` and `WORKSPACE` files and will automatically 
exclude bazel output directories unless you specifically untrack the Workspace file.

## [0.0.15] - 2024-11-18

- Change the debug view as an intention instead of an action
- Sign the plugin when releasing it
- Bump IDEA version range

## [0.0.14] - 2024-11-17

- Bump to IntelliJ 2024.3 and JDK 21

## [0.0.12] - 2024-09-13

- Fix a bug for bazelproject files containing blank lines
- Bump IDEA version range

## [0.0.11] - 2024-09-12

- Additional logging

## [0.0.10] - 2024-08-28

- Change the way path resolution is done

## [0.0.9] - 2024-08-27

- Add more debug info to the debug dialog

## [0.0.8] - 2024-08-26

- Add the debug dialog

## [0.0.7] - 2024-08-23

- Support for `*.bazelproject` [files](https://ij.bazel.build/docs/project-views.html)

## [0.0.6] - 2024-08-09

- Upgrade versions range to 242.*

## [0.0.5] - 2024-08-03

- Fix the state update

## [0.0.4] - 2024-08-02

- Manage the plugin repository

## [0.0.3] - 2024-08-02

### Added

- Initial scaffold created from [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template)
- Initial support for `.managed-excludes` files
- Fix the release process
- Add a banner on `.managed-excludes` files

[Unreleased]: https://github.com/gplassard/managed-excludes/compare/v0.0.18...HEAD
[0.0.18]: https://github.com/gplassard/managed-excludes/compare/v0.0.17...v0.0.18
[0.0.17]: https://github.com/gplassard/managed-excludes/compare/v0.0.16...v0.0.17
[0.0.16]: https://github.com/gplassard/managed-excludes/compare/v0.0.15...v0.0.16
[0.0.15]: https://github.com/gplassard/managed-excludes/compare/v0.0.14...v0.0.15
[0.0.14]: https://github.com/gplassard/managed-excludes/compare/v0.0.12...v0.0.14
[0.0.12]: https://github.com/gplassard/managed-excludes/compare/v0.0.11...v0.0.12
[0.0.11]: https://github.com/gplassard/managed-excludes/compare/v0.0.10...v0.0.11
[0.0.10]: https://github.com/gplassard/managed-excludes/compare/v0.0.9...v0.0.10
[0.0.9]: https://github.com/gplassard/managed-excludes/compare/v0.0.8...v0.0.9
[0.0.8]: https://github.com/gplassard/managed-excludes/compare/v0.0.7...v0.0.8
[0.0.7]: https://github.com/gplassard/managed-excludes/compare/v0.0.6...v0.0.7
[0.0.6]: https://github.com/gplassard/managed-excludes/compare/v0.0.5...v0.0.6
[0.0.5]: https://github.com/gplassard/managed-excludes/compare/v0.0.4...v0.0.5
[0.0.4]: https://github.com/gplassard/managed-excludes/compare/v0.0.3...v0.0.4
[0.0.3]: https://github.com/gplassard/managed-excludes/commits/v0.0.3
