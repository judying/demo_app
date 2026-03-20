package com.example.twosome  // 본인 패키지명으로 변경

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.braze.Braze
import com.braze.BrazeActivityLifecycleCallbackListener
import com.braze.support.BrazeLogger
import android.content.Intent
import android.widget.ImageButton
import com.braze.models.outgoing.BrazeProperties
import org.json.JSONObject
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = true  // true = 아이콘 검정
        }
        setupBottomNavigation()
        registerActivityLifecycleCallbacks(BrazeActivityLifecycleCallbackListener())
        BrazeLogger.logLevel = Log.VERBOSE
        Braze.getInstance(this).changeUser("juryeol");
        requestNotificationPermission()

        // 하루 토막 상식 담기
        findViewById<ImageButton>(R.id.btnCartGift1).setOnClickListener {
            Braze.getInstance(this).logCustomEvent(
                "add_to_cart",
                BrazeProperties(JSONObject().put("id", "1").put("product_name", "하루 토막 상식").put("price", 16800))
            )
            showAddToCartDialog("하루 토막 상식") {
                Toast.makeText(this, "장바구니로 이동!", Toast.LENGTH_SHORT).show()
            }
        }

        // 프로젝트 헤일메리 담기
        findViewById<ImageButton>(R.id.btnCartGift2).setOnClickListener {
            Braze.getInstance(this).logCustomEvent(
                "add_to_cart",
                BrazeProperties(JSONObject().put("id","2").put("product_name", "프로젝트 헤일메리 (영화 특별판)").put("price", 17100))
            )
            showAddToCartDialog("프로젝트 헤일메리") {
                Toast.makeText(this, "장바구니로 이동!", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    "android.permission.POST_NOTIFICATIONS"
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf("android.permission.POST_NOTIFICATIONS"),
                    1001
                )
            }
        }
    }

    private fun showAddToCartDialog(productName: String, onGoCart: () -> Unit) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_to_cart)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        dialog.findViewById<ImageButton>(R.id.btnDialogClose).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.btnContinueShopping).setOnClickListener {
            dialog.dismiss()
        }
        dialog.findViewById<Button>(R.id.btnGoCart).setOnClickListener {
            dialog.dismiss()
            onGoCart()
        }

        dialog.show()
    }


    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    // 홈 화면 (현재 화면)
                    true
                }
                R.id.nav_search_kyobo -> {
                    // TODO: 검색 화면 전환
                    true
                }
                R.id.nav_my -> {
                    // TODO: MY 화면 전환
                    true
                }
                R.id.nav_history -> {
                    // 히스토리 탭 → Braze 설정 화면
                    startActivity(Intent(this, BrazeCollectionActivity::class.java))
                    true
                }
                R.id.nav_category -> {
                    // TODO: 카테고리 화면 전환
                    true
                }
                else -> false
            }
        }
    }
}
