package com.ybzl.ybutil

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.ybzl.eventbus.BroadcastEventBus
import kotlinx.coroutines.launch

class SecondActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_second)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        lifecycleScope.launch {
            BroadcastEventBus.events.collect {
                if (it is LoginEvent) {
                    Log.d("BroadcastEventBus","SecondActivity:LoginEvent:${it.name}")
                } else if (it is NoLoginEvent) {
                    Log.d("BroadcastEventBus","SecondActivity:NoLoginEvent")
                }
            }
        }
    }
}