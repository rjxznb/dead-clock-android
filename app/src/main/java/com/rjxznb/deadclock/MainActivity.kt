package com.rjxznb.deadclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rjxznb.deadclock.reminder.PersistentNotification
import com.rjxznb.deadclock.ui.CountdownScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PersistentNotification.update(this)
        setContent {
            CountdownScreen()
        }
    }
}
