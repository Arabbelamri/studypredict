package com.example.studypredict.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

@Composable
fun NoInternetDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Connexion absente") },
        text = {
            Text("Cette fonctionnalité a besoin d'une connexion internet. Activez le Wi-Fi ou les données mobiles avant de continuer.")
        },
        confirmButton = {
            TextButton(onClick = onOpenSettings) {
                Text("Ouvrir les réglages")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}