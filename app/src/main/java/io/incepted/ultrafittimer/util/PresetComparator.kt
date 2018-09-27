package io.incepted.ultrafittimer.util

import io.incepted.ultrafittimer.db.model.Preset

object PresetComparator : Comparator<Preset> {
    override fun compare(o1: Preset?, o2: Preset?): Int {

        return if (o1?.bookmarked == true && o2?.bookmarked == false) {
            -1
        } else if (o1?.bookmarked == false && o2?.bookmarked == true) {
            1
        } else {
            (o1?.id?.toInt() ?: 0) - (o2?.id?.toInt() ?: 0)
        }
    }
}