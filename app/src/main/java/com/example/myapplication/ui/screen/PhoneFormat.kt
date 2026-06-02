package com.example.myapplication.ui.screen

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

fun normalizeUzPhone(input: String): String {
    return formatUzPhone(localUzDigits(input))
}

fun normalizeUzPhone(input: TextFieldValue): TextFieldValue {
    val formatted = normalizeUzPhone(input.text)
    val digitsBeforeCursor = localUzDigits(input.text.take(input.selection.end)).length
    val cursor = phoneCursorPosition(digitsBeforeCursor, formatted)
    return TextFieldValue(
        text = formatted,
        selection = TextRange(cursor)
    )
}

fun isUzPhoneComplete(input: String): Boolean {
    return dialablePhoneNumber(input).length == 12
}

fun dialablePhoneNumber(input: String): String {
    val digits = input.filter(Char::isDigit)
    return when {
        digits.length == 9 -> "998$digits"
        digits.length >= 12 && digits.startsWith("998") -> digits.take(12)
        else -> digits
    }
}

private fun localUzDigits(input: String): String {
    val digits = input.filter(Char::isDigit)
    return when {
        digits.startsWith("998") -> digits.drop(3)
        digits.startsWith("8") && digits.length > 9 -> digits.drop(1)
        else -> digits
    }.take(9)
}

private fun formatUzPhone(localDigits: String): String {
    val builder = StringBuilder("+998")
    if (localDigits.isNotEmpty()) builder.append(' ')
    localDigits.forEachIndexed { index, char ->
        builder.append(char)
        if ((index == 1 || index == 4 || index == 6) && index != localDigits.lastIndex) {
            builder.append(' ')
        }
    }
    return builder.toString()
}

private fun phoneCursorPosition(localDigitCount: Int, formattedPhone: String): Int {
    if (localDigitCount <= 0) {
        return formattedPhone.length.coerceAtMost(5)
    }

    var seenDigits = 0
    formattedPhone.forEachIndexed { index, char ->
        if (char.isDigit()) {
            if (seenDigits >= 3) {
                val localIndex = seenDigits - 3
                if (localIndex == localDigitCount - 1) {
                    return index + 1
                }
            }
            seenDigits += 1
        }
    }
    return formattedPhone.length
}
