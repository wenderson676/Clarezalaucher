package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TimeDateHeader(
    showBattery: Boolean = false,
    isFocusMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val ptBr = remember { Locale("pt", "BR") }
    val timeFormat = remember { SimpleDateFormat("HH:mm", ptBr) }
    val dayOfWeekFormat = remember { SimpleDateFormat("EEEE", ptBr) }
    val dayMonthFormat = remember { SimpleDateFormat("dd 'DE' MMM", ptBr) }

    var currentTime by remember { mutableStateOf(Date()) }

    // Update time every 5 seconds
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = Date()
            delay(5000L)
        }
    }

    val dayOfWeek = remember(currentTime) {
        val raw = dayOfWeekFormat.format(currentTime)
        raw.uppercase(ptBr)
    }
    val dayMonth = remember(currentTime) { dayMonthFormat.format(currentTime).uppercase(ptBr) }

    val openClockApp = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallbackIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_CALCULATOR)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                // Try opening standard clock package
                val clockIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.deskclock")
                    ?: context.packageManager.getLaunchIntentForPackage("com.android.deskclock")
                    ?: Intent(AlarmClock.ACTION_SET_ALARM)
                clockIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(clockIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Aplicativo de Relógio não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val openCalendarApp = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time")
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = builder.build()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val calIntent = context.packageManager.getLaunchIntentForPackage("com.google.android.calendar")
                ?: Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_APP_CALENDAR)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
            try {
                calIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(calIntent)
            } catch (e2: Exception) {
                Toast.makeText(context, "Aplicativo de Calendário não encontrado", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Cyber Minimalist Clock Display (Clickable -> Opens Clock/Alarm)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(vertical = 2.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { openClockApp() }
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = timeFormat.format(currentTime),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 78.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = (-2).sp,
                    lineHeight = 80.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Futuristic Date Badge HUD (Clickable -> Opens Calendar)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { openCalendarApp() }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = dayOfWeek,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.1.sp,
                        fontSize = 11.sp
                    ),
                    color = NeonCyan
                )
                Text(
                    text = " // ",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Normal,
                        letterSpacing = 1.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                Text(
                    text = dayMonth,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.8.sp,
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

