package com.example.samolet

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import androidx.fragment.app.replace
import com.example.samolet.databinding.ActivityMainBinding
import com.example.samolet.databinding.ActivityStartBinding
import com.example.samolet.databinding.FragmentMethodBinding

class StartActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityStartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportFragmentManager.commit {
            replace<HomeFragment>(R.id.fragment_container)
            addToBackStack(HomeFragment::class.java.simpleName)
        }
    }
}