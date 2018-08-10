package io.incepted.ultrafittimer.util

import android.databinding.InverseMethod
import java.nio.charset.MalformedInputException
import java.util.concurrent.TimeUnit

object TimerUtil {

    /**
     * Converts a raw user input into seconds (00:24) -> (24)
     * @param input A String input representing the time. ex) 00:24, 0:2, :24, 24: 8:011, etc...
     * @return The second representation in Int for the input String. ex) 24, 2, 24 etc...
     *          Returns -1 when the input is not valid or the second representation is over an hour.
     */

    @JvmStatic
    fun stringToSecondWithOffset(input: String, offset: Int): Int {
        try {
            val split: List<String> = input.split(":")
            if (split.size >= 3) return -1

            val result = when (split.size) {
                2 -> {
                    val rawMin: Long = if (split[0] == "") 0 else split[0].toLong()
                    val rawSec: Long = if (split[1] == "") 0 else split[1].toLong()
                    (TimeUnit.MINUTES.toSeconds(rawMin) + rawSec).toInt()
                }
                1 -> split[0].toInt()
                else -> -1
            }

            // This input is over an hour! Invalid input.
            if (result >= 60 * 60)
                return -1

            return if (result - offset < 0) 0 else result + offset

        } catch (ex: Exception) {
            ex.printStackTrace()
            when (ex) {
                is NumberFormatException,
                is MalformedInputException,
                is ClassCastException -> {
                    return -1
                }
                else -> throw ex
            }
        }
    }

    @JvmStatic
    fun stringToSecond(input: String): Int {
        try {
            val split: List<String> = input.split(":")
            if (split.size >= 3) return -1

            val result = when (split.size) {
                2 -> {
                    val rawMin: Long = if (split[0] == "") 0 else split[0].toLong()
                    val rawSec: Long = if (split[1] == "") 0 else split[1].toLong()
                    (TimeUnit.MINUTES.toSeconds(rawMin) + rawSec).toInt()
                }
                1 -> split[0].toInt()
                else -> -1
            }

            // This input is over an hour! Invalid input.
            if (result >= 60 * 60)
                return -1

            return result

        } catch (ex: Exception) {
            ex.printStackTrace()
            when (ex) {
                is NumberFormatException,
                is MalformedInputException,
                is ClassCastException -> {
                    return -1
                }
                else -> throw ex
            }
        }
    }


    /**
     * Converts the seconds into a readable time format (24) -> (00:24)
     *
     * @param inputSeconds Total seconds to convert into time format string. (00:00 format)
     * @return A String formatted into time form (00:00).
     *          Returns an empty String when the input is invalid.
     */
    @JvmStatic
    @InverseMethod("stringToSecond")
    fun secondsToTimeString(inputSeconds: Int): String {
        try {
            if (inputSeconds >= 60 * 60) return "-1"
            if (inputSeconds < 0) return "-1"
            if (inputSeconds == 0) return "00:00"

            val min = inputSeconds / 60
            val sec = inputSeconds % 60
            val finalMin = makeIntoTwoDigits(min)
            val finalSec = makeIntoTwoDigits(sec)
            return "$finalMin:$finalSec"

        } catch (ex: Exception) {
            ex.printStackTrace()
            when (ex) {
                is NumberFormatException,
                is MalformedInputException,
                is ClassCastException -> {
                    return "-1"
                }
                else -> throw ex
            }
        }

    }

    /**
     * Converts user input (i.e. :32, 12:0) into the readable time format (00:32, 12:00)
     */

    fun stringToTimeString(input: String, offset: Int): String {
        return secondsToTimeString(stringToSecondWithOffset(input, offset))
    }


    /**
     * Converts the seconds into two digit String format (1) -> (01)
     * @param time An Int value representing the time that needs to be formatted into two digit form.
     * @return The two digit form String value for the input.
     */

    fun makeIntoTwoDigits(time: Int): String {
        if (time < 10)
            return "0$time"
        return time.toString()
    }

}