package com.ybzl.ybutil

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ybzl.eventbus.BroadcastEventBus
import com.ybzl.eventbus.Event
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            BroadcastEventBus.events.collect {
                if (it is LoginEvent) {
                    Log.d("BroadcastEventBus", "MainActivity:LoginEvent:${it.name}")
                } else if (it is NoLoginEvent) {
                    Log.d("BroadcastEventBus", "MainActivity:NoLoginEvent")
                }
            }
        }
        BroadcastEventBus.postEvent(LoginEvent("name"), lifecycleScope)
        BroadcastEventBus.postEvent(NoLoginEvent, lifecycleScope)
    }

    fun jumpToSecond(view: View) {
        startActivity(Intent(this, SecondActivity::class.java))
    }
}

data class LoginEvent(val name: String) : Event

data object NoLoginEvent : Event