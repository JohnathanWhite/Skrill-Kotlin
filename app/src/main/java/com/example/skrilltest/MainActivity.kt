package com.example.skrilltest

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebView.WebViewTransport
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var requestPermissionsButton: Button
    private lateinit var openWebViewButton: Button
    private lateinit var webViewContainer: FrameLayout // Container for WebView instances

    private var dynamicWebView: WebView? = null // Keep track of the dynamically created WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        requestPermissionsButton = findViewById(R.id.request_permissions_button)
        openWebViewButton = findViewById(R.id.open_webview_button)
        webViewContainer = findViewById(R.id.webview_container)

        // Set up the initial WebView
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.settings.setSupportMultipleWindows(true)
        webView.visibility = View.GONE

        webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.let {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        it.grant(it.resources)
                    } else {
                        ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
                        currentPermissionRequest = it
                    }
                }
            }

            override fun onCreateWindow(view: WebView?, isDialog: Boolean, isUserGesture: Boolean, resultMsg: android.os.Message?): Boolean {
                // Create a new WebView dynamically
                dynamicWebView = WebView(this@MainActivity).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    webChromeClient = this@MainActivity.webView.webChromeClient
                    webViewClient = WebViewClient()
                }



                // Add the new WebView to the container
                webViewContainer.addView(dynamicWebView)

                // Send the new WebView to the transport
                val transport = resultMsg?.obj as? WebViewTransport
                transport?.webView = dynamicWebView
                resultMsg?.sendToTarget()

                return true
            }

            override fun onCloseWindow(window: WebView?) {
                // Remove the dynamic WebView when closed
                webViewContainer.removeView(window)
                window?.destroy()
                dynamicWebView = null
            }
        }

        // Set up button listeners
        requestPermissionsButton.setOnClickListener {
            requestPermissions()
        }

        openWebViewButton.setOnClickListener {
            webView.visibility = View.VISIBLE
            webView.loadUrl("https://bitpay.com/invoice?v=4&id=Q8eSXLZjLKWWpuPmAUnHjp&lang=en-US")
        }

        checkPermissions()
    }

    private var currentPermissionRequest: PermissionRequest? = null

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Camera permission not granted", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            currentPermissionRequest?.grant(currentPermissionRequest?.resources)
            openWebViewButton.isEnabled = true
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Camera permission is required to use this feature", Toast.LENGTH_SHORT).show()
            currentPermissionRequest?.deny()
        }
        currentPermissionRequest = null
    }

    companion object {
        private const val CAMERA_REQUEST_CODE = 100
    }
}
