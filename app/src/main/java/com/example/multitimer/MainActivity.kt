package com.example.multitimer

import android.content.Context
import android.media.Ringtone
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import android.media.RingtoneManager
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TimerApp()
        }
    }
}

@Composable
fun TimerApp() {
    val context = LocalContext.current

    // Saver для TextFieldValue
    val textFieldSaver = Saver<TextFieldValue, String>(
        save = { it.text }, // Сохраняем только текст
        restore = { TextFieldValue(it) } // Восстанавливаем из текста
    )

    // Используем кастомный Saver в rememberSaveable
    var timerTitle by rememberSaveable(stateSaver = textFieldSaver) { mutableStateOf(TextFieldValue("")) }
    var inputTime by rememberSaveable(stateSaver = textFieldSaver) { mutableStateOf(TextFieldValue("")) }
    var remainingTime by rememberSaveable { mutableStateOf(0) }
    var isRunning by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(isRunning) {
        if (isRunning) {
            while (remainingTime > 0 && isRunning) {
                delay(1000L)
                remainingTime -= 1
            }
            if (remainingTime == 0) {
                isRunning = false
                playNotificationSound(context)
            }
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = timerTitle,
            onValueChange = { timerTitle = it },
            label = { Text("Название таймера") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning
        )

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = inputTime,
            onValueChange = { inputTime = it },
            label = { Text("Время для отсчета (секунды)") },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isRunning
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = formatTime(remainingTime),
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val parsedTime = inputTime.text.toIntOrNull()
                    if (parsedTime != null && parsedTime > 0) {
                        remainingTime = parsedTime
                        isRunning = true
                    }
                },
                enabled = !isRunning && inputTime.text.toIntOrNull() != null
            ) {
                Text("Запустить")
            }

            Button(
                onClick = { isRunning = false },
                enabled = isRunning
            ) {
                Text("Пауза")
            }

            Button(
                onClick = {
                    isRunning = false
                    remainingTime = 0
                    inputTime = TextFieldValue("")
                    timerTitle = TextFieldValue("")
                }
            ) {
                Text("Сброс")
            }
        }
    }
}

fun playNotificationSound(context: Context) {
    val notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
    val ringtone: Ringtone = RingtoneManager.getRingtone(context, notificationUri)
    ringtone.play()
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val remainingSeconds = seconds % 60
    return String.format("%02d:%02d", minutes, remainingSeconds)
}