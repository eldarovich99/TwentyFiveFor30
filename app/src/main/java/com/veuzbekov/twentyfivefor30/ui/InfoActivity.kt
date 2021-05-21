package com.veuzbekov.twentyfivefor30.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import by.kirich1409.viewbindingdelegate.viewBinding
import com.veuzbekov.twentyfivefor30.R
import com.veuzbekov.twentyfivefor30.databinding.ActivityInfoBinding


class InfoActivity : AppCompatActivity() {
    private val binding by viewBinding(ActivityInfoBinding::bind)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        binding.instButton.setOnClickListener {
            openInst()
        }
    }

    private fun openInst() {
        val uri: Uri = Uri.parse("http://instagram.com/eldarovich99")
        val likeIng = Intent(Intent.ACTION_VIEW, uri)

        likeIng.setPackage("com.instagram.android")

        try {
            startActivity(likeIng)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://instagram.com/eldarovich99")
                )
            )
        }
    }
}