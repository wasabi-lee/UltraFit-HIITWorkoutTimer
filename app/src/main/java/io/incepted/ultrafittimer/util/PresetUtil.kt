package io.incepted.ultrafittimer.util

object PresetUtil {

    /**
     * Takes the concatenated string (delimited by the specified delimiter) and
     * returns a String formatted for user display.
     * Uses the first value of each work/rest list for display.
     *
     * @param works A string value representing a list of work seconds (i.e. -> 10, 20, 20, 10, 50, ...)
     * @param rests A string value representing a list of rest seconds (i.e. -> 5, 20, 10, 30, 10, ...)
     * @return A readable format of the input string (i.e. -> WORK 00:10 - REST 00:05 - 8 ROUNDS)
     */

    @JvmStatic
    fun workSettingToDisplayFormat(works: String, rests: String): String {
        if (works == "-1" && rests == "-1") return "-"

        val workArr = works.split(DbDelimiter.DELIMITER)
        val restArr = rests.split(DbDelimiter.DELIMITER)
        val roundCount = workArr.size

        val displayWork = TimerUtil.secondsToTimeString(workArr[0].toInt())
        val displayRest = TimerUtil.secondsToTimeString(restArr[0].toInt())

        return if (roundCount > 0) "WORK $displayWork - REST $displayRest - " +
                "$roundCount ${if(roundCount == 1) "ROUND" else "ROUNDS"}"
        else "-"
    }


    /**
     * Takes the concatenated string (delimited by the specified delimiter) and
     * returns a String formatted for user display
     *
     * @param names A string value representing a list workout names, concatenated by a delimiter
     *      (i.e. -> "Work,Work,Jog,Jumping Jacks, ...")
     * @return A readable format of the input String.
     *      (i.e. -> "Work x 2 - Jog x 1 - Jumping Jacks x 1 - ...")
     */

    @JvmStatic
    fun workNamesToDisplayFormat(names: String): String {
        if (names == "-1") return "-"

        val nameArr = names.split(DbDelimiter.DELIMITER)

        if (nameArr.isEmpty()) return ""

        var prev = nameArr[0]
        var count = 0
        var result = ""
        for (i in 0 until nameArr.size) {
            if (prev == nameArr[i]) {
                count++
                if (i != nameArr.size - 1)
                    continue
            }
            result += "$prev x $count - "
            prev = nameArr[i]
            count = 1
        }

        result = result.trim()
        return result.substring(0, result.length-1)
    }
}