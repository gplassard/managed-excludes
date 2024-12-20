package com.github.gplassard.managedexcludes.parser

object BazelWorkspaceParser {

    fun parseWorkspaceName(content: String): String? {
        val regex = """workspace\(\s*(?:[\w_]+\s*=\s*[^,]*,\s*)*name\s*=\s*"([^"]+)"""".toRegex()
        val matchResult = regex.find(content)
        return matchResult?.groups?.get(1)?.value
    }
}
