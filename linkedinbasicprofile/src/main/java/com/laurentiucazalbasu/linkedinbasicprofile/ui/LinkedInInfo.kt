package com.laurentiucazalbasu.linkedinbasicprofile.ui

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.laurentiucazalbasu.linkedinbasicprofile.R
import com.laurentiucazalbasu.linkedinbasicprofile.models.BasicInformation
import com.laurentiucazalbasu.linkedinbasicprofile.models.Resource
import com.laurentiucazalbasu.linkedinbasicprofile.ui.viewmodel.LinkedInViewModel
import kotlinx.android.synthetic.main.activity_linked_in.*
import java.net.URL

class LinkedInInfo : AppCompatActivity() {

    companion object {
        const val PROFILE_INFORMATION = "PROFILE_INFORMATION"
        const val REQUEST_CODE = 2806

        private const val CLIENT_ID = "CLIENT_ID"
        private const val CLIENT_SECRET = "CLIENT_SECRET"
        private const val REDIRECT_URI = "REDIRECT_URI"
        private const val SCOPE = "SCOPE"

        fun startActivity(
            activity: Activity,
            clientId: String,
            clientSecret: String,
            redirectUri: String,
            scope: String
        ) {
            val intent = Intent(activity, LinkedInInfo::class.java)
            intent.putExtra(CLIENT_ID, clientId)
            intent.putExtra(CLIENT_SECRET, clientSecret)
            intent.putExtra(REDIRECT_URI, redirectUri)
            intent.putExtra(SCOPE, scope)
            activity.startActivityForResult(intent, REQUEST_CODE)
        }
    }

    private lateinit var clientId: String
    private lateinit var clientSecret: String
    private lateinit var redirectUri: String
    private lateinit var scope: String

    private val viewModel by lazy {
        ViewModelProviders.of(this@LinkedInInfo)
            .get(LinkedInViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linked_in)

        loadObjects()
        initObservers()
        loadLogin()
    }

    private fun loadObjects() {
        clientId = intent.getStringExtra(CLIENT_ID) ?: throw IllegalArgumentException("CLIENT_ID not found")
        clientSecret = intent.getStringExtra(CLIENT_SECRET) ?: throw IllegalArgumentException("CLIENT_SECRET not found")
        redirectUri = intent.getStringExtra(REDIRECT_URI) ?: throw IllegalArgumentException("REDIRECT_URI not found")
        scope = intent.getStringExtra(SCOPE) ?: throw IllegalArgumentException("SCOPE not found")
    }

    private fun initObservers() {
        viewModel.profileInformation.observe(this, Observer { response ->
            when (response) {
                is Resource.Loading -> showLoading(true)
                is Resource.Success -> {
                    showLoading(false)
                    sendProfileInformation(response.data)
                }
                is Resource.Error -> {
                    showLoading(false)
                    Toast.makeText(applicationContext, response.error, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun sendProfileInformation(data: BasicInformation) {
        val intent = Intent()
        intent.putExtra(PROFILE_INFORMATION, data)
        intent.putExtra("TEST", "TEST")
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun loadLogin() {
        initWebView(
            "http://www.linkedin.com/oauth/v2/authorization?" +
                    "client_id=$clientId" +
                    "&response_type=code" +
                    "&redirect_uri=$redirectUri" +
                    "&scope=$scope"
        )
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun initWebView(url: String) {
        val webSettings = webView.settings
        webSettings.domStorageEnabled = true
        webSettings.javaScriptEnabled = true
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE
        webSettings.setAppCacheEnabled(true)
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                showLoading(true)
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                showLoading(false)
            }

            @Suppress("OverridingDeprecatedMember")
            override fun shouldOverrideUrlLoading(webView: WebView, url: String): Boolean {
                return shouldOverrideUrlLoading(url)
            }

            @TargetApi(Build.VERSION_CODES.N)
            override fun shouldOverrideUrlLoading(webView: WebView, request: WebResourceRequest): Boolean {
                val uri = request.url
                return shouldOverrideUrlLoading(uri.toString())
            }

            private fun shouldOverrideUrlLoading(url: String): Boolean {
                if (url.startsWith(redirectUri)) {
                    val query = URL(url).query
                    if (query.startsWith("code")) {
                        webView.visibility = View.GONE
                        loadUserData(query.substring(5))
                    } else {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                } else {
                    webView.loadUrl(url)
                }
                return false
            }
        }

        WebView.setWebContentsDebuggingEnabled(true)
        webView.clearCache(true)
        webView.clearHistory()
        webView.loadUrl(url)
    }

    private fun loadUserData(code: String) {
        viewModel.loadProfileInformation(
            code, redirectUri, clientId, clientSecret, scope
        )
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            progressBar.visibility = View.VISIBLE
        } else {
            progressBar.visibility = View.GONE
        }
    }
}
