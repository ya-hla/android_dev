package co.jp.kddi_tech

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.Observer

class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizerViewModel: SpeechRecognizerViewModel
    private lateinit var user_word_result_textField: TextView
    private lateinit var micButton: Button
    private val REQUEST_RECORD_AUDIO_PERMISSION = 200
    private val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // bind button with action
        micButton = findViewById<Button>(R.id.mic_button).apply {
            setOnClickListener(micClickListener)
        }
        user_word_result_textField = findViewById<TextView>(R.id.txtUserWord)
        setupSpeechViewModel()
    }

    private val micClickListener = View.OnClickListener {
        if (!speechRecognizerViewModel.permissionToRecordAudio) {

            // show UI to "Allow the Application to record Audio?"
            ActivityCompat.requestPermissions(this, permissions, REQUEST_RECORD_AUDIO_PERMISSION)
            return@OnClickListener
        }
        if (speechRecognizerViewModel.isListening) {

            speechRecognizerViewModel.stopListening()
        } else {
            speechRecognizerViewModel.startListening()
        }
    }

    private fun setupSpeechViewModel() {
        speechRecognizerViewModel = ViewModelProviders.of(this).get(SpeechRecognizerViewModel::class.java)
        speechRecognizerViewModel.getViewState().observe(this, Observer<SpeechRecognizerViewModel.ViewState> { viewState ->
            render(viewState)
        })
    }

    private fun render(uiOutput: SpeechRecognizerViewModel.ViewState?) {
        user_word_result_textField.text = ""
        if (uiOutput == null) {
            return
        }
        // to check as if the recognized text is spoken by user or system
        if (uiOutput.spokenText != "") {
                user_word_result_textField.text = uiOutput.spokenText

        }
        if (uiOutput.isListening) {
            user_word_result_textField.text = ""
            micButton.background  =  ContextCompat.getDrawable(this, R.drawable.microphone_active_64)
            // only the condition where there is no internet connection
            if (uiOutput.error == "error_timeout") {
                micButton.background  = ContextCompat.getDrawable(this, R.drawable.microphone_inactive_64)
                speechRecognizerViewModel.stopListening()
                return
            }
        } else {
            micButton.background  = ContextCompat.getDrawable(this, R.drawable.microphone_inactive_64)
        }

    }
    // asking permission of audio recording

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // when user allow the permission
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            speechRecognizerViewModel.permissionToRecordAudio = grantResults[0] == PackageManager.PERMISSION_GRANTED
        }
        // when user allow the permission
        if (speechRecognizerViewModel.permissionToRecordAudio) {
            micButton.performClick()
        }
    }

}
