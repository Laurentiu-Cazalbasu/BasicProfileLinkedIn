package com.laurentiucazalbasu.linkedinprofile

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.laurentiucazalbasu.linkedinbasicprofile.models.BasicInformation
import com.laurentiucazalbasu.linkedinbasicprofile.ui.LinkedInInfo
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btStart.setOnClickListener {
            LinkedInInfo.startActivity(
                activity = this@MainActivity,
                clientId = "clientID",
                clientSecret = "clientSecret",
                redirectUri = "redirectUri",
                scope = "scope"
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == LinkedInInfo.REQUEST_CODE) {
                val profileInformation = data?.getParcelableExtra<BasicInformation>(LinkedInInfo.PROFILE_INFORMATION)
                Toast.makeText(applicationContext, "Success", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(applicationContext, "Something went wrong", Toast.LENGTH_SHORT).show()
        }
    }
}
