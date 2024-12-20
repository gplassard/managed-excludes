package com.github.gplassard.managedexcludes

class Constants {
    companion object {
        const val EXCLUDE_FILE_NAME = ".managed-excludes"
        const val COMMENT_PREFIX = "#"
        const val BAZELPROJECT_FILE_EXTENSION = ".bazelproject"
        val BAZEL_WORKSPACE_FILE_NAMES = setOf(
            "WORKSPACE.bazel",
            "WORKSPACE",
        )
    }
}
