package com.example.mldelegation
import android.opengl.GLES20
import android.opengl.GLES32
import android.opengl.GLSurfaceView
import android.os.Build
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.tflite.client.TfLiteInitializationOptions
import com.google.android.gms.tflite.gpu.support.TfLiteGpu
import com.google.android.gms.tflite.java.TfLite
import org.tensorflow.lite.InterpreterApi
import org.tensorflow.lite.gpu.GpuDelegateFactory
import org.tensorflow.lite.support.common.FileUtil
import java.io.File

class MainActivity : AppCompatActivity() {
    private val initializeTask by lazy { TfLite.initialize(this) }
    private lateinit var interpreter: InterpreterApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        setupButtons()
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

    private fun setupButtons() {
        findViewById<Button>(R.id.btnRunGpu).setOnClickListener {
            showGpuInfoDialog()
        }
        findViewById<Button>(R.id.btnRunCpu).setOnClickListener {
            showCpuInfoDialog()
        }
    }

    private fun getCpuInfo(): String {
        return try {
            // Get basic CPU information
            val arch = Build.SUPPORTED_ABIS.joinToString(", ")
            val cores = Runtime.getRuntime().availableProcessors()
            val hardware = Build.HARDWARE
            val model = try {
                File("/proc/cpuinfo").readLines()
                    .firstOrNull { it.contains("Hardware") }
                    ?.split(":")?.lastOrNull()?.trim()
                    ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }

            """
        CPU Information:
        Model: $model
        Cores: $cores
        Architecture: $arch
        Hardware: $hardware
        """.trimIndent()
        } catch (e: Exception) {
            "Could not retrieve CPU information: ${e.message}"
        }
    }

    private fun showCpuInfoDialog() {
        val cpuInfo = getCpuInfo()
        AlertDialog.Builder(this)
            .setTitle("CPU Information")
            .setMessage(cpuInfo)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun showGpuInfoDialog() {
        val gpuInfo = getGpuInfo()
        AlertDialog.Builder(this)
            .setTitle("GPU Information")
            .setMessage(gpuInfo)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    private fun getGpuInfo(): String {
        return try {
            val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
            val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
            val version = GLES20.glGetString(GLES20.GL_VERSION) ?: "Unknown"

            """
        GPU Information:
        Renderer: $renderer
        Vendor: $vendor
        OpenGL Version: $version
        """.trimIndent()
        } catch (e: Exception) {
            "Could not retrieve GPU information: ${e.message}"
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