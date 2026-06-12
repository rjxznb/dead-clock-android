package com.rjxznb.deadclock.core

import android.content.Context
import android.content.res.Configuration
import java.util.Locale

/** 应用内语言切换：用包装过的 Context 覆盖资源语言 */
object LocaleHelper {
    fun wrap(base: Context): Context {
        val lang = base.getSharedPreferences("deadclock", Context.MODE_PRIVATE)
            .getString("appLanguage", "system") ?: "system"
        if (lang == "system") return base
        val locale = if (lang == "zh") Locale.SIMPLIFIED_CHINESE else Locale.ENGLISH
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
