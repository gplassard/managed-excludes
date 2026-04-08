package com.github.gplassard.managedexcludes.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test


class BazelProjectParserTest {

    @Test
    fun parseExcludedDirectories() {
        val input = """
            directories:
              #- a comment
              java/com/google/android/myproject
              -excluded
            #a comment without indentation
              javatests/com/google/android/myproject
              -javatests/com/google/android/myproject/not_this
            something_else:
              blah blah blah
              -negative
        """.trimIndent()

        val expectedExcluded = listOf(
            "excluded",
            "javatests/com/google/android/myproject/not_this",
        )

        val result = BazelProjectParser.parseExcludedDirectories(input)

        assertThat(result).containsExactlyElementsOf(expectedExcluded)
    }

}
