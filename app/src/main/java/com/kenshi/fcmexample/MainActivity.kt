package com.kenshi.fcmexample

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.kenshi.fcmexample.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// 해당 토픽을 구독하고 있는 모든 기기에게 알림이 옴
const val TOPIC = "/topics/myTopic2"

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        registration token
//        파이어베이스 유저의 token 을 SP 에 저장해서 해당 토큰을 통해 그 유저에게만 알림을 주는 방식을 자주 사용함
//         deprecated
//        FirebaseInstanceId.getInstance().instanceId.addOnSuccessListener {
//            FirebaseService.token = it.token
//            binding.etToken.setText(it.token)
//        }

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result ?: ""
                FirebaseService.token = token
                binding.etToken.setText(token)
            }
        }
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        binding.btnSend.setOnClickListener {
            val title = binding.etTitle.text.toString()
            val message = binding.etMessage.text.toString()
            val recipientToken = binding.etToken.text.toString()

            if (title.isNotEmpty() && message.isNotEmpty() && recipientToken.isNotEmpty()) {
                PushNotification(
                    data = NotificationData(title, message),
                    to = recipientToken
                    //to = TOPIC
                ).also {
                    sendNotification(it)
                }
            }
            // we need to create service to be able to receive them when our app is closed
            // runs in the background
        }
    }

    private fun sendNotification(notification: PushNotification) =
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.postNotification(notification)
                if (response.isSuccessful) {
                    //Log.d("success", "Response: ${Gson().toJson(response)}")
                    Log.d("success", "Response: $response")
                } else {
                    Log.e("error", response.errorBody().toString())
                }
            } catch (e: Exception) {
                Log.e("error", e.toString())
            }
        }
}