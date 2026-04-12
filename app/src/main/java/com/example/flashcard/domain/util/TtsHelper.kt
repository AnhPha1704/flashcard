package com.example.flashcard.domain.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsHelper(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context, this)
    private var pendingText: String? = null
    private val mainHandler = android.os.Handler(android.os.Looper.getMainLooper())

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.US)
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "Ngôn ngữ không được hỗ trợ!")
                Toast.makeText(context, "Thiếu dữ liệu giọng nói tiếng Anh!", Toast.LENGTH_LONG).show()
            } else {
                // Kỹ thuật "Silent Priming": kích hoạt pipeline âm thanh bằng một
                // câu lệnh im lặng (1ms), để lần phát âm thật sự đầu tiên không bị lag.
                tts?.playSilentUtterance(1L, TextToSpeech.QUEUE_FLUSH, "prime")

                // Đợi pipeline âm thanh khởi động xong rồi mới đánh dấu sẵn sàng
                mainHandler.postDelayed({
                    _isReady.value = true
                    // Phát âm nội dung đang chờ (nếu có)
                    pendingText?.let {
                        tts?.speak(it, TextToSpeech.QUEUE_FLUSH, null, null)
                        pendingText = null
                    }
                }, 600)
            }
        } else {
            Log.e("TTS", "Khởi tạo TTS thất bại!")
        }
    }

    fun speak(text: String) {
        if (_isReady.value) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        } else {
            pendingText = text
        }
    }

    fun stop() {
        tts?.stop()
    }

    fun shutdown() {
        tts?.shutdown()
        tts = null
    }
}
