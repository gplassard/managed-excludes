package com.github.gplassard.managedexcludes.parser

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class BazelWorkspaceParserTest {

    @Test
    fun parseWorkspaceName() {
        val input = """
            workspace(name = "bazel-playground")
        """.trimIndent()

        val result = BazelWorkspaceParser.parseWorkspaceName(input)

        assertThat(result).isEqualTo("bazel-playground")
    }


    @Test
    fun parseWorkspaceNameMultiline() {
        val input = """workspace(
            name = "bazel-playground",
        )
        """.trimIndent()

        val result = BazelWorkspaceParser.parseWorkspaceName(input)

        assertThat(result).isEqualTo("bazel-playground")
    }

    @Test
    fun parseWorkspaceNameMultilineOtherParameters() {
        val input = """
        workspace(
            something_else = True,
            name = "bazel-playground",
            yet_another_parameter = False
        )
        """.trimIndent()

        val result = BazelWorkspaceParser.parseWorkspaceName(input)

        assertThat(result).isEqualTo("bazel-playground")
    }

    @Test
    fun parseWorkspaceNameMultilineBunchOfOtherStuff() {
        val input = """
        something_else_is_here(True);
        
        workspace(
            something_else = True,
            name = "bazel-playground",
            yet_another_parameter = False
        )
        
        # what is this ?
        """.trimIndent()

        val result = BazelWorkspaceParser.parseWorkspaceName(input)

        assertThat(result).isEqualTo("bazel-playground")
    }

    @Test
    fun parseWorkspaceNameTypo() {
        val input = """
            workspace(
                names = "bazel-playground"
            )
        """.trimIndent()

        val result = BazelWorkspaceParser.parseWorkspaceName(input)

        assertThat(result).isNull()
    }

}
