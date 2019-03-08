package co.jp.kddi_tech

import android.Manifest
import android.app.Application
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class SpeechRecognizerViewModel(application: Application) : AndroidViewModel(application), RecognitionListener {

    // 3 arguments constructor
    data class ViewState(
        val spokenText: String,
        val isListening: Boolean,
        val error: String?
    )


    private var viewState: MutableLiveData<ViewState>? = null

    private val speechRecognizer: SpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(application.applicationContext).apply {
        setRecognitionListener(this@SpeechRecognizerViewModel)
    }

    // triggering intent for showing microphone UI for speech recognition and return back the result
    private val recognizerIntent: Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ja")
        putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, application.packageName)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    var isListening = false
        get() = viewState?.value?.isListening ?: false

    var permissionToRecordAudio = checkAudioRecordingPermission(context = application)

    fun getViewState(): LiveData<ViewState> {
        if (viewState == null) {
            viewState = MutableLiveData()
            viewState?.value = initViewState()
        }

        return viewState as MutableLiveData<ViewState>
    }

    // init method for initializtion initViewState object
    private fun initViewState() = ViewState(
        spokenText = "",
        isListening = false,
        error = null
    )

    fun startListening() {
        speechRecognizer.startListening(recognizerIntent)
        notifyListening(isRecording = true)
    }

    fun stopListening() {
        speechRecognizer.stopListening()
        notifyListening(isRecording = false)
        viewState?.value = viewState?.value?.copy(spokenText = "")
    }

    private fun notifyListening(isRecording: Boolean) {
        viewState?.value = viewState?.value?.copy(isListening = isRecording)
    }

    private fun updateResults(speechBundle: Bundle?) {
        val userSaid = speechBundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        viewState?.value = viewState?.value?.copy(spokenText = userSaid?.get(0) ?: "")
    }

    private fun checkAudioRecordingPermission(context: Application) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED

    // for updating the ui view whenever there is spoken word
    override fun onPartialResults(results: Bundle?) = updateResults(speechBundle = results)
    override fun onResults(results: Bundle?) = updateResults(speechBundle = results)
    override fun onEndOfSpeech() = notifyListening(isRecording = false)

    override fun onError(errorCode: Int) {
        viewState?.value = viewState?.value?.copy(error = when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "error_audio_error"
            SpeechRecognizer.ERROR_CLIENT -> "error_client"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "error_permission"
            SpeechRecognizer.ERROR_NETWORK -> "error_network"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "error_timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "error_no_match"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "error_busy"
            SpeechRecognizer.ERROR_SERVER -> "error_server"
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "error_timeout"
            else -> "error_unknown"
        })
    }

    override fun onReadyForSpeech(p0: Bundle?) {}
    override fun onRmsChanged(p0: Float) {}
    override fun onBufferReceived(p0: ByteArray?) {}
    override fun onEvent(p0: Int, p1: Bundle?) {}
    override fun onBeginningOfSpeech() {}
}