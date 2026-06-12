package com.rjxznb.deadclock

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rjxznb.deadclock.core.LocaleHelper
import com.rjxznb.deadclock.reminder.PersistentNotification
import com.rjxznb.deadclock.ui.CountdownScreen

class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleHelper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PersistentNotification.update(this)
        setContent {
            CountdownScreen()
        }
    }
}
