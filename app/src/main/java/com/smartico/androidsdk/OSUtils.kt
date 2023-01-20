package com.smartico.androidsdk

import android.provider.Settings
import java.util.*

internal class OSUtils {
    companion object {
        fun deviceId(): String {
            SmarticoSdk.instance.context.get()?.let {
                return Settings.Secure.getString(
                    it.contentResolver, Settings.Secure.ANDROID_ID
                )
            }
            return ""
        }

        fun generateNextRandomId(): String {
            // 59dfa61a-eeae-4afb-9f94-a0201269b0b2
            // http://guid.us/Test/GUID
            val part1 = getRandomHexString(8)
            val part2 = getRandomHexString(4)
            val part3 = getRandomHexString(4)
            val part4 = getRandomHexString(4)
            val part5 = getRandomHexString(12)

            return "${part1}-${part2}-${part3}-${part4}-${part5}".lowercase()
        }

        fun getRandomHexString(numchars: Int): String {
            val r = Random()
            val sb = StringBuffer()
            while (sb.length < numchars) {
                sb.append(Integer.toHexString(r.nextInt()))
            }
            return sb.toString().substring(0, numchars)
        }

        fun timezoneOffsetInMins(): Int {
            return TimeZone.getDefault().rawOffset / (1000 * 60)
        }
    }
}