package com.yuraw.claudecode

// Fixes a common typo on Russian keyboard layouts: letters like Cyrillic
// 'с' (U+0441) are visually identical to Latin 'c' (U+0063) but are a
// different character, so a CLI parses "-с" as an unknown flag instead of
// "-c". Only letters with a near-perfect visual match to a Latin letter are
// mapped, so real Cyrillic text elsewhere is left alone.
object CyrillicHomoglyphs {

    private val lookalikes = mapOf(
        'А' to 'A', 'В' to 'B', 'Е' to 'E', 'К' to 'K', 'М' to 'M', 'Н' to 'H',
        'О' to 'O', 'Р' to 'P', 'С' to 'C', 'Т' to 'T', 'У' to 'Y', 'Х' to 'X',
        'а' to 'a', 'е' to 'e', 'о' to 'o', 'р' to 'p', 'с' to 'c', 'у' to 'y', 'х' to 'x'
    )

    fun normalize(input: String): String =
        buildString(input.length) {
            for (ch in input) append(lookalikes[ch] ?: ch)
        }
}
