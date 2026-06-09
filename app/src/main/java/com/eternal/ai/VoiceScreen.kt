package com.eternal.ai

import android.Manifest
import android.media.MediaRecorder
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.IOException

@Composable
fun VoiceScreen() {
    val context = LocalContext.current
    var hasPermission by remember { mutableStateOf(false) }
    var isRecording by remember { mutableStateOf(false) }
    var mediaRecorder by remember { mutableStateOf<MediaRecorder?>(null) }
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted -> hasPermission = isGranted }

    Box(modifier = Modifier.fillMaxSize().background(NeoChineseColors.InkBlack), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("🎤 神谕聆听", color = NeoChineseColors.Gold, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            if (!hasPermission) {
                Text("需录音权限以聆听神谕", color = NeoChineseColors.RicePaper, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(Manifest.permission.RECORD_AUDIO) }) { Text("授予权限") }
            } else {
                if (isRecording) {
                    Text("🔴 录音中...", color = NeoChineseColors.JadeGreen, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        mediaRecorder?.stop()
                        mediaRecorder?.release()
                        mediaRecorder = null
                        isRecording = false
                    }) { Text("停止录音") }
                } else {
                    Button(onClick = {
                        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_MUSIC), "eternal_voice.3gp")
                        try {
                            mediaRecorder = MediaRecorder().apply {
                                setAudioSource(MediaRecorder.AudioSource.MIC)
                                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                                setOutputFile(file.absolutePath)
                                prepare()
                                start()
                            }
                            isRecording = true
                        } catch (e: IOException) {
                            e.printStackTrace()
                        }
                    }) { Text("开始录音") }
                }
            }
        }
    }
}
