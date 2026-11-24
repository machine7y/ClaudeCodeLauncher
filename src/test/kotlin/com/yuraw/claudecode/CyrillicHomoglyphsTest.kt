package com.yuraw.claudecode

import com.yuraw.claudecode.CyrillicHomoglyphs.ShellSyntax
import org.junit.Assert.assertEquals
import org.junit.Test

class CyrillicHomoglyphsTest {

    private fun assertBoth(input: String, expected: String) {
        assertEquals("cmd: $input", expected, CyrillicHomoglyphs.normalize(input, ShellSyntax.CMD))
        assertEquals("posix: $input", expected, CyrillicHomoglyphs.normalize(input, ShellSyntax.POSIX))
    }

    @Test
    fun replacesWordsThatTurnFullyLatin() {
        assertBoth("claude -с", "claude -c")
        assertBoth("сlaude --mоdel sonnet", "claude --model sonnet")
    }

    @Test
    fun keepsDoubleQuotedText() {
        assertBoth("claude \"добавь флаг -с в скрипт\" -с", "claude \"добавь флаг -с в скрипт\" -c")
        assertBoth("claude \"привет\"", "claude \"привет\"")
        assertBoth("claude --append-system-prompt=\"текст -с\" -с", "claude --append-system-prompt=\"текст -с\" -c")
    }

    @Test
    fun keepsRealCyrillicWords() {
        assertBoth("claude привет -с", "claude привет -c")
    }

    @Test
    fun unclosedQuoteRunsToEndOfLine() {
        assertBoth("claude -p \"не закрыта -с", "claude -p \"не закрыта -с")
    }

    @Test
    fun wordOfOnlyLookalikesTurnsLatin() {
        // Known and accepted limitation.
        assertBoth("claude оса", "claude oca")
    }

    @Test
    fun singleQuotesAreQuotesOnlyInPosix() {
        assertEquals("claude 'сухое' -c", CyrillicHomoglyphs.normalize("claude 'сухое' -с", ShellSyntax.POSIX))
        // In cmd "'" is an ordinary character, so the word is 'сухое' with apostrophes.
        assertEquals("claude 'cyxoe' -c", CyrillicHomoglyphs.normalize("claude 'сухое' -с", ShellSyntax.CMD))
    }

    @Test
    fun posixBackslashEscapes() {
        // Escaped quote outside quotes does not open a quote.
        assertEquals("claude \\\"x -c", CyrillicHomoglyphs.normalize("claude \\\"x -с", ShellSyntax.POSIX))
        // Escaped quote inside double quotes does not close it.
        assertEquals("claude \"a \\\" -с\" -c", CyrillicHomoglyphs.normalize("claude \"a \\\" -с\" -с", ShellSyntax.POSIX))
        // Backslash is literal inside single quotes, so the quote closes there.
        assertEquals("claude 'a\\' -c", CyrillicHomoglyphs.normalize("claude 'a\\' -с", ShellSyntax.POSIX))
    }

    @Test
    fun cmdCaretEscapes() {
        // ^" outside quotes is a literal quote and does not open a quote.
        assertEquals("claude ^\"x -c", CyrillicHomoglyphs.normalize("claude ^\"x -с", ShellSyntax.CMD))
        // ^ is literal inside double quotes, so the quote closes at the next ".
        assertEquals("claude \"a^\" -c", CyrillicHomoglyphs.normalize("claude \"a^\" -с", ShellSyntax.CMD))
        // Backslash is an ordinary character in cmd.
        assertEquals("claude \"a\\\" -c", CyrillicHomoglyphs.normalize("claude \"a\\\" -с", ShellSyntax.CMD))
    }

    @Test
    fun isIdempotent() {
        val input = "сlaude \"привет -с\" привет --mоdel оса 'x -с"
        for (syntax in ShellSyntax.values()) {
            val once = CyrillicHomoglyphs.normalize(input, syntax)
            assertEquals(once, CyrillicHomoglyphs.normalize(once, syntax))
        }
    }
}
