package com.example.mldelegation

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.android.gms.tflite.java.TfLite
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.gpu.GpuDelegateFactory
import org.tensorflow.lite.support.common.FileUtil

class MainActivity : AppCompatActivity() {
    private val initializeTask by lazy { TfLite.initialize(this) }
    private lateinit var interpreter: InterpreterApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        startTfLiteInitialization()
    }

    private fun startTfLiteInitialization() {
        initializeTask.addOnSuccessListener {
            // Initialization successful, create interpreter
            createInterpreter()
        }.addOnFailureListener { e ->
            Log.e("TFLite", "Initialization failed", e)
        }
    }
    private fun createInterpreter() {
        try {
            val options = InterpreterApi.Options().apply {
                setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY)
            }

            // Load your model
            val modelBuffer = FileUtil.loadMappedFile(this, "your_model.tflite")

            interpreter = InterpreterApi.create(modelBuffer, options)
            // Now you can run inferences
        } catch (e: Exception) {
            Log.e("TFLite", "Interpreter creation failed", e)
        }
    }

    private fun checkGpuAvailability() {
        TfLiteGpu.isGpuDelegateAvailable(this).addOnSuccessListener { available ->
            if(available) {
                setupGpuDelegate()
            } else {}
        }
    }

    private fun setupGpuDelegate() {
        // Initialize with GPU support
        TfLite.initialize(
            this,
            TfLiteInitializationOptions.builder()
                .setEnableGpuDelegateSupport(true)
                .build()
        ).addOnSuccessListener {
            createGpuInterpreter()
        }
    }

    private fun createGpuInterpreter() {
        val options = InterpreterApi.Options().apply {
            setRuntime(InterpreterApi.Options.TfLiteRuntime.FROM_SYSTEM_ONLY)
            addDelegateFactory(GpuDelegateFactory())
        }

        val modelBuffer = FileUtil.loadMappedFile(this, "your_model.tflite")
        interpreter = InterpreterApi.create(modelBuffer, options)
    }
}