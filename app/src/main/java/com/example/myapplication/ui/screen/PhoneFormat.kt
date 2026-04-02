package com.example.myapplication.ui.screen

fun normalizeUzPhone(input: String): String {
    val digits = input.filter(Char::isDigit)
    val localDigits = when {
        digits.startsWith("998") -> digits.drop(3)
        else -> digits
    }.take(9)

    val builder = StringBuilder("+998")
    if (localDigits.isNotEmpty()) builder.append(' ')
    localDigits.forEachIndexed { index, char ->
        builder.append(char)
        if (index == 1 || index == 4 || index == 6) {
            if (index != localDigits.lastIndex) builder.append(' ')
        }
    }
    return builder.toString()
}

fun dialablePhoneNumber(input: String): String {
    val digits = input.filter(Char::isDigit)
    return when {
        digits.length == 9 -> "998$digits"
        digits.length >= 12 && digits.startsWith("998") -> digits.take(12)
        else -> digits
    }
}
