package com.github.gplassard.managedexcludes.parser

object BazelProjectParser {

    fun parseExcludedDirectories(content: String): Set<String> {
        val regex = Regex("^\\s{2,}(.+)\$")

        return content.lineSequence()
            .filterNot { it.trim().startsWith("#") }  // ignore all comment lines
            .dropWhile { it.trim() != "directories:" }      // Drop lines until "directories:" is found
            .drop(1)                                        // Skip the "directories:" line
            .takeWhile { regex.matches(it) }                // Take lines that match the indentation pattern
            .map { regex.matchEntire(it)!!.groupValues[1].trim() }
            .filter { it.startsWith("-") } // only keep lines that are using exclusions (starting with a -)
            .map { it.substring(1).trim() }
            .toSet()
    }
}