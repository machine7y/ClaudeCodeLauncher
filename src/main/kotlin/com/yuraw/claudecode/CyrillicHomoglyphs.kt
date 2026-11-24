package com.yuraw.claudecode

// Fixes a common typo on Russian keyboard layouts: letters like Cyrillic
// 'с' (U+0441) are visually identical to Latin 'c' (U+0063) but are a
// different character, so a CLI parses "-с" as an unknown flag instead of
// "-c".
//
// Only unquoted words that turn fully Latin are changed ("-с", "сlaude",
// "--mоdel"). Quoted text (a prompt, as a rule) is kept exactly as typed, and
// so is an unquoted word with any Cyrillic letter that has no Latin twin
// ("привет"): that is real Russian text, not a wrong-layout typo. A Cyrillic
// word made only of look-alikes ("оса") is still turned Latin; such words are
// rare unquoted in a command line.
object CyrillicHomoglyphs {

    // How the shell that runs the command line treats quotes and escapes.
    enum class ShellSyntax(
        val quotes: String,
        val escape: Char,
        val escapeWorksInDoubleQuotes: Boolean
    ) {
        // cmd.exe: only double quotes; '^' escapes outside quotes and is a
        // plain character inside them; "'" is a plain character.
        CMD("\"", '^', false),

        // POSIX shells: double and single quotes; '\' escapes outside quotes
        // and inside double quotes, but not inside single quotes.
        POSIX("\"'", '\\', true);

        companion object {
            // Must match the shell ClaudeCodeLauncher runs the command in.
            fun forCurrentOs(): ShellSyntax =
                if (System.getProperty("os.name").lowercase().contains("win")) CMD else POSIX
        }
    }

    private val lookalikes = mapOf(
        'А' to 'A', 'В' to 'B', 'Е' to 'E', 'К' to 'K', 'М' to 'M', 'Н' to 'H',
        'О' to 'O', 'Р' to 'P', 'С' to 'C', 'Т' to 'T', 'У' to 'Y', 'Х' to 'X',
        'а' to 'a', 'е' to 'e', 'о' to 'o', 'р' to 'p', 'с' to 'c', 'у' to 'y', 'х' to 'x'
    )

    private const val NO_QUOTE = '\u0000'

    // Words are split by whitespace and quote boundaries; an unclosed quote
    // runs to the end of the line. Escape pairs are copied as they are and
    // never open or close a quote.
    fun normalize(input: String, syntax: ShellSyntax = ShellSyntax.forCurrentOs()): String {
        val result = StringBuilder(input.length)
        val word = StringBuilder()
        var openQuote = NO_QUOTE
        var index = 0
        while (index < input.length) {
            val ch = input[index]
            val escapeActive = openQuote == NO_QUOTE ||
                (openQuote == '"' && syntax.escapeWorksInDoubleQuotes)
            val isEscapePair = ch == syntax.escape && escapeActive && index + 1 < input.length
            val consumedLength = if (isEscapePair) 2 else 1
            val consumed = input.substring(index, index + consumedLength)
            when {
                openQuote != NO_QUOTE -> {
                    result.append(consumed)
                    if (!isEscapePair && ch == openQuote) openQuote = NO_QUOTE
                }
                isEscapePair -> word.append(consumed)
                ch in syntax.quotes || ch.isWhitespace() -> {
                    result.append(replaceInWord(word.toString())).append(ch)
                    word.setLength(0)
                    if (!ch.isWhitespace()) openQuote = ch
                }
                else -> word.append(ch)
            }
            index += consumedLength
        }
        return result.append(replaceInWord(word.toString())).toString()
    }

    private fun replaceInWord(word: String): String {
        val replaced = buildString(word.length) {
            for (ch in word) append(lookalikes[ch] ?: ch)
        }
        return if (replaced.any { Character.UnicodeBlock.of(it) == Character.UnicodeBlock.CYRILLIC }) word else replaced
    }
}
