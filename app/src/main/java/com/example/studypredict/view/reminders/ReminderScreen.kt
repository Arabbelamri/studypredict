package com.example.studypredict.view.reminders

import android.Manifest
import android.app.AlarmManager
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddAlert
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationManagerCompat
import com.example.studypredict.controller.ReminderController
import com.example.studypredict.controller.ReminderDataProvider
import com.example.studypredict.model.ReminderItem
import com.example.studypredict.model.ReminderUiState
import com.example.studypredict.reminders.createReminderNotificationChannel
import kotlinx.coroutines.launch

@Composable
fun ReminderScreen(
    onBack: () -> Unit
) {
    val ctx = LocalContext.current
    val reminderController = remember { ReminderController() }

    val bg = Color(0xFFF2F6FF)
    val card = Color(0xFFF7FAFF)
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)
    val purple = Color(0xFF6D41FF)
    val primaryGrad = Brush.horizontalGradient(
        listOf(Color(0xFF4B3CFF), Color(0xFFB400FF))
    )

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var uiState by remember { mutableStateOf(ReminderUiState()) }

    val reminders = remember {
        mutableStateListOf<ReminderItem>().apply {
            addAll(ReminderDataProvider.defaultReminders())
        }
    }

    val enabledCount = reminders.count { it.enabled }
    val progressTarget =
        if (reminders.isEmpty()) 0f else enabledCount.toFloat() / reminders.size.toFloat()

    val progress by animateFloatAsState(
        targetValue = progressTarget,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "reminder_progress"
    )

    val notifPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        scope.launch {
            snackbarHostState.showSnackbar(
                if (granted) "Notifications activées ✅" else "Permission refusée."
            )
        }
    }

    fun ensureNotifPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < 33) return

        val granted = ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!granted) {
            notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    fun notificationsActuallyEnabled(): Boolean {
        val appEnabled = NotificationManagerCompat.from(ctx).areNotificationsEnabled()
        if (!appEnabled) return false
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(
            ctx,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun exactAlarmsActuallyEnabled(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        val alarmManager = ctx.getSystemService(AlarmManager::class.java)
        return alarmManager?.canScheduleExactAlarms() == true
    }

    LaunchedEffect(Unit) {
        createReminderNotificationChannel(ctx)
    }

    Scaffold(
        containerColor = bg,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 18.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Retour",
                            tint = dark
                        )
                    }
                    Text(
                        text = "Retour",
                        color = dark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(10.dp, RoundedCornerShape(16.dp), clip = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(primaryGrad),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            text = "Rappels",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = dark
                        )
                        Text(
                            text = "Planifie des notifications sur ton téléphone",
                            color = gray
                        )
                    }
                }
            }

            item {
                val shape = RoundedCornerShape(22.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, shape, clip = false),
                    shape = shape,
                    color = card
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Progression",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = dark
                                )
                                Text(
                                    text = "$enabledCount / ${reminders.size} rappels actifs",
                                    color = gray
                                )
                            }

                            Text(
                                text = "${(progress * 100).toInt()}%",
                                color = purple,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 22.sp
                            )
                        }

                        Spacer(Modifier.height(10.dp))

                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = Color(0xFF111827),
                            trackColor = Color(0xFFD1D5DB)
                        )
                    }
                }
            }

            item {
                val shape = RoundedCornerShape(22.dp)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(12.dp, shape, clip = false),
                    shape = shape,
                    color = Color.White
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Outlined.Alarm,
                                contentDescription = null,
                                tint = Color(0xFF5B55FF)
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(
                                text = "Créer un rappel",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp,
                                color = dark
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        val notificationsEnabled = notificationsActuallyEnabled()
                        val exactAlarmsEnabled = exactAlarmsActuallyEnabled()
                        val reminderAuthorized = notificationsEnabled && exactAlarmsEnabled

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (reminderAuthorized) Color(0xFFEFFCF5) else Color(0xFFFFF4F4),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                                Text(
                                    text = if (reminderAuthorized) "Etat: autorise" else "Etat: non autorise",
                                    color = if (reminderAuthorized) Color(0xFF047857) else Color(0xFFB91C1C),
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "Notifications: ${if (notificationsEnabled) "OK" else "OFF"}  |  Alarmes exactes: ${if (exactAlarmsEnabled) "OK" else "OFF"}",
                                    color = gray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        OutlinedTextField(
                            value = uiState.title,
                            onValueChange = { uiState = uiState.copy(title = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Titre") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp)
                        )

                        Spacer(Modifier.height(10.dp))

                        OutlinedTextField(
                            value = uiState.message,
                            onValueChange = { uiState = uiState.copy(message = it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Message") },
                            shape = RoundedCornerShape(16.dp),
                            minLines = 2
                        )

                        Spacer(Modifier.height(12.dp))

                        TimePickerRow(
                            hour = uiState.pickedHour,
                            minute = uiState.pickedMinute,
                            onMinusHour = {
                                uiState = uiState.copy(
                                    pickedHour = (uiState.pickedHour + 23) % 24
                                )
                            },
                            onPlusHour = {
                                uiState = uiState.copy(
                                    pickedHour = (uiState.pickedHour + 1) % 24
                                )
                            },
                            onMinusMinute = {
                                uiState = uiState.copy(
                                    pickedMinute = (uiState.pickedMinute + 59) % 60
                                )
                            },
                            onPlusMinute = {
                                uiState = uiState.copy(
                                    pickedMinute = (uiState.pickedMinute + 1) % 60
                                )
                            },
                            accent = purple
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { ensureNotifPermissionIfNeeded() },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Notifications,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Autoriser")
                            }

                            Button(
                                onClick = {
                                    if (uiState.title.isBlank()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Ajoute un titre.")
                                        }
                                        return@Button
                                    }

                                    ensureNotifPermissionIfNeeded()
                                    if (!notificationsActuallyEnabled()) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Active d'abord les notifications pour voir les rappels."
                                            )
                                        }
                                        return@Button
                                    }
                                    createReminderNotificationChannel(ctx)

                                    val item = reminderController.buildReminderItem(
                                        title = uiState.title,
                                        message = uiState.message,
                                        hour = uiState.pickedHour,
                                        minute = uiState.pickedMinute
                                    )

                                    val success = reminderController.scheduleDailyReminder(ctx, item)

                                    if (success) {
                                        reminders.add(0, item)

                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Rappel programmé à ${reminderController.formatTime(item.hour, item.minute)} ✅"
                                            )
                                        }

                                        uiState = ReminderUiState()
                                    } else {
                                        reminderController.requestExactAlarmPermission(ctx)

                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                "Autorise les alarmes exactes pour activer ce rappel."
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = purple,
                                    contentColor = Color.White
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.AddAlert,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Programmer", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Mes rappels",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = dark
                )
            }

            if (reminders.isEmpty()) {
                item {
                    val shape = RoundedCornerShape(22.dp)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(10.dp, shape, clip = false),
                        shape = shape,
                        color = Color.White
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.NotificationsNone,
                                contentDescription = null,
                                tint = gray,
                                modifier = Modifier.size(52.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            Text(
                                text = "Aucun rappel",
                                fontWeight = FontWeight.ExtraBold,
                                color = dark
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Crée un rappel pour tes révisions ou tes habitudes.",
                                color = gray
                            )
                        }
                    }
                }
            } else {
                items(reminders, key = { it.id }) { item ->
                    ReminderRow(
                        item = item,
                        onToggle = { enabled ->
                            val index = reminders.indexOfFirst { it.id == item.id }
                            if (index == -1) return@ReminderRow

                            reminders[index] = item.copy(enabled = enabled)

                            if (enabled) {
                                ensureNotifPermissionIfNeeded()
                                if (!notificationsActuallyEnabled()) {
                                    reminders[index] = item.copy(enabled = false)
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            "Notifications non autorisées. Rappel non activé."
                                        )
                                    }
                                    return@ReminderRow
                                }
                                createReminderNotificationChannel(ctx)

                                val success = reminderController.scheduleDailyReminder(
                                    ctx,
                                    reminders[index]
                                )

                                scope.launch {
                                    snackbarHostState.showSnackbar(if (success) "Rappel réactivé ✅"
                                    else "Autorise les alarmes exactes."
                                    )
                                }

                                if (!success) {
                                    reminderController.requestExactAlarmPermission(ctx)
                                }
                            } else {
                                reminderController.cancelReminder(ctx, item.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Rappel désactivé")
                                }
                            }
                        },
                        onDelete = {
                            reminderController.cancelReminder(ctx, item.id)
                            reminders.removeAll { it.id == item.id }

                            scope.launch {
                                snackbarHostState.showSnackbar("Rappel supprimé")
                            }
                        },
                        formattedTime = reminderController.formatTime(item.hour, item.minute)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimePickerRow(
    hour: Int,
    minute: Int,
    onMinusHour: () -> Unit,
    onPlusHour: () -> Unit,
    onMinusMinute: () -> Unit,
    onPlusMinute: () -> Unit,
    accent: Color
) {
    val shape = RoundedCornerShape(18.dp)

    Surface(
        shape = shape,
        color = Color(0xFFF7FAFF),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TimeStepper(
                label = "Heure",
                value = hour.toString().padStart(2, '0'),
                onMinus = onMinusHour,
                onPlus = onPlusHour,
                accent = accent
            )

            Text(
                text = ":",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 22.sp,
                color = Color(0xFF111827)
            )

            TimeStepper(
                label = "Minute",
                value = minute.toString().padStart(2, '0'),
                onMinus = onMinusMinute,
                onPlus = onPlusMinute,
                accent = accent
            )
        }
    }
}

@Composable
private fun TimeStepper(
    label: String,
    value: String,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    accent: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color(0xFF6B7280),
            fontWeight = FontWeight.Medium
        )

        Spacer(Modifier.height(6.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(
                onClick = onMinus,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("−")
            }

            Spacer(Modifier.width(10.dp))

            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )

            Spacer(Modifier.width(10.dp))

            OutlinedButton(
                onClick = onPlus,
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+")
            }
        }
    }
}

@Composable
private fun ReminderRow(
    item: ReminderItem,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit,
    formattedTime: String
) {
    val shape = RoundedCornerShape(22.dp)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp, shape, clip = false),
        shape = shape,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF0B1220)
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = item.message,
                    color = Color(0xFF6B7280),
                    fontSize = 13.sp
                )

                Spacer(Modifier.height(6.dp))

                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "⏰ $formattedTime",
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = Color(0xFFEEF2FF),
                        labelColor = Color(0xFF111827)
                    )
                )
            }

            Switch(
                checked = item.enabled,
                onCheckedChange = onToggle
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "Supprimer",
                    tint = Color(0xFFEF4444)
                )
            }
        }
    }
}
