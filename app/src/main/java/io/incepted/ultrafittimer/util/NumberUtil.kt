package io.incepted.ultrafittimer.util

import java.nio.charset.MalformedInputException

object NumberUtil {

    fun convertToValidRoundNumber(input: String): Int {
        return try {
            input.toInt()
        } catch (ex: Exception) {
            ex.printStackTrace()
            -1
        }
    }
}