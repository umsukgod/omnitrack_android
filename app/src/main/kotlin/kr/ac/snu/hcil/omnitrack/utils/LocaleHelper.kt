package kr.ac.snu.hcil.omnitrack.utils

import android.content.Context
import android.os.Build
import android.preference.PreferenceManager
import kr.ac.snu.hcil.omnitrack.R
import java.util.*

/**
 * Created by Young-Ho on 5/28/2017.
 *
 * reference: http://gunhansancar.com/change-language-programmatically-in-android/
 */
object LocaleHelper {
    const val PREF_KEY_SELECTED_LANGUAGE = "pref_selected_language"
    const val PREF_USE_DEVICE_DEFAULT = "pref_language_follow_device_setting"

    fun useDeviceLanguage(context: Context): Boolean {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        return pref.getBoolean(PREF_USE_DEVICE_DEFAULT, true)
    }

    fun getLanguageCode(context: Context): String {
        val pref = PreferenceManager.getDefaultSharedPreferences(context)
        val nearestDeviceLanguage = getNearestLanguageToDevice(context)
        if (useDeviceLanguage(context)) {
            println("nearest device language is ${nearestDeviceLanguage}")
            return nearestDeviceLanguage
        } else {
            return pref.getString(PREF_KEY_SELECTED_LANGUAGE, nearestDeviceLanguage)
        }
    }

    fun getNearestLanguageToDevice(context: Context): String {
        val deviceLanguage = Locale.getDefault().language
        val supportedLanguages = context.resources.getStringArray(R.array.supported_language_codes)
        if (supportedLanguages.contains(deviceLanguage)) {
            return deviceLanguage
        } else {
            return "en"
        }
    }

    fun wrapContextWithLocale(context: Context, language: String): Context {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val conf = context.resources.configuration
        conf.setLocale(locale)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return context.createConfigurationContext(conf)
        } else {
            context.resources.updateConfiguration(conf, context.resources.displayMetrics)
            return context
        }
    }

}