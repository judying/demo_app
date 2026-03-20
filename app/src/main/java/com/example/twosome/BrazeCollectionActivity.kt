package com.example.twosome

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.braze.Braze
import com.braze.BrazeUser
import com.braze.events.IValueCallback
import com.braze.models.outgoing.BrazeProperties
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject

class BrazeCollectionActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BrazeCollectionActivity"
        private const val NOTIFICATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_braze_collection)

        // 상단 뒤로가기 버튼
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Braze 데이터 수집"

        // 알림 권한 요청
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_REQUEST_CODE
                )
            }
        }

        // FCM 토큰
        val tokenTextView = findViewById<TextView>(R.id.textView_token)
        val copyButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.button_copyToken)

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "FCM token fetch failed", task.exception)
                tokenTextView.text = "푸시 토큰 가져오기 실패"
                return@addOnCompleteListener
            }
            val token = task.result
            Log.d(TAG, "FCM Token: $token")
            tokenTextView.text = token
        }

        copyButton.setOnClickListener {
            val token = tokenTextView.text.toString()
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("FCM Token", token))
            Toast.makeText(this, "토큰이 복사됐어요!", Toast.LENGTH_SHORT).show()
        }

        // Login
        val externalIdEditText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_externalId)
        val loginButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.button_login)

        loginButton.setOnClickListener {
            val externalId = externalIdEditText.text.toString().trim()
            if (externalId.isNotEmpty()) {
                Braze.getInstance(this).changeUser(externalId)
                Toast.makeText(this, "User changed to $externalId", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "External ID를 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // Event
        val eventEditText = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_event)
        val prop1Key = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_event_property_1)
        val prop2Key = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_event_property_2)
        val prop3Key = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_event_property_3)
        val prop1Value = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_event_property_1_value)
        val prop2Value = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_event_property_2_value)
        val prop3Value = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_event_property_3_value)
        val sendEventButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.button_sendEvent)

        sendEventButton.setOnClickListener {
            val eventName = eventEditText.text.toString().trim()
            if (eventName.isNotEmpty()) {
                val json = JSONObject()
                if (prop1Key.text?.isNotEmpty() == true) json.put(prop1Key.text.toString(), prop1Value.text.toString())
                if (prop2Key.text?.isNotEmpty() == true) json.put(prop2Key.text.toString(), prop2Value.text.toString())
                if (prop3Key.text?.isNotEmpty() == true) json.put(prop3Key.text.toString(), prop3Value.text.toString())

                Braze.getInstance(this).logCustomEvent(eventName, BrazeProperties(json))
                Toast.makeText(this, "Event '$eventName' 전송!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "이벤트 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        // Attribute
        val attributeKey = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_attribute_key)
        val attributeValue = findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editText_attribute_value)
        val saveAttributeButton = findViewById<com.google.android.material.button.MaterialButton>(R.id.button_saveAttribute)

        saveAttributeButton.setOnClickListener {
            val key = attributeKey.text.toString().trim()
            val rawValue = attributeValue.text.toString().trim()

            if (key.isEmpty() || rawValue.isEmpty()) {
                Toast.makeText(this, "Key와 Value를 모두 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Braze.getInstance(this).getCurrentUser(object : IValueCallback<BrazeUser> {
                override fun onSuccess(user: BrazeUser) {
                    // [] 형식이면 array로 처리
                    if (rawValue.startsWith("[") && rawValue.endsWith("]")) {
                        val inner = rawValue.removePrefix("[").removeSuffix("]")
                        val array = inner.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toTypedArray<String?>()
                        user.setCustomAttributeArray(key, array)
                    } else {
                        user.setCustomAttribute(key, rawValue)
                    }

                    runOnUiThread {
                        Toast.makeText(this@BrazeCollectionActivity, "Attribute '$key' 저장!", Toast.LENGTH_SHORT).show()
                    }
                    Braze.getInstance(this@BrazeCollectionActivity).requestImmediateDataFlush()
                }

                override fun onError() {
                    runOnUiThread {
                        Toast.makeText(this@BrazeCollectionActivity, "Attribute 저장 실패", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    // 뒤로가기 버튼
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}