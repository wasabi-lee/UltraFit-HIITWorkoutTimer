package io.incepted.ultrafittimer.util

import java.nio.charset.MalformedInputException

object NumberUtil {

    fun convertToValidRoundNumber(input: String, offset: Int): Int {
        return try {
            input.toInt() + offset
        } catch (ex: Exception) {
            ex.printStackTrace()
            -1
        }
    }
}