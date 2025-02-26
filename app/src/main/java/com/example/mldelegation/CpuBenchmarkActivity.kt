package com.example.mldelegation

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class CpuBenchmarkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cpu_benchmark)

        // Receive CPU info from main activity
        val cpuInfo = intent.getStringExtra("CPU_INFO") ?: "No CPU information available"
        findViewById<TextView>(R.id.cpuInfoText).text = cpuInfo

        findViewById<MaterialButton>(R.id.btnRunModel).setOnClickListener {
            runBenchmark()
        }
    }

    private fun runBenchmark() {
        // Your benchmark logic here
        val results = """
            Benchmark Results:
            Average Inference Time: 42ms
            Min Time: 38ms
            Max Time: 45ms
            Memory Usage: 128MB
        """.trimIndent()

        findViewById<TextView>(R.id.benchmarkResults).text = results
    }
}