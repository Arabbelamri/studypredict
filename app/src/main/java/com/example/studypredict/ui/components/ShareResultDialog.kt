package com.example.studypredict.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import compose.icons.FontAwesomeIcons
import compose.icons.fontawesomeicons.Brands
import compose.icons.fontawesomeicons.brands.FacebookF
import compose.icons.fontawesomeicons.brands.LinkedinIn
import compose.icons.fontawesomeicons.brands.Twitter
import compose.icons.fontawesomeicons.brands.Whatsapp

@Composable
fun ShareResultDialog(
    link: String,
    selectedTarget: String?,
    onSelectTarget: (String) -> Unit,
    onOpenNetwork: (String) -> Unit,
    onDismiss: () -> Unit,
    onShare: () -> Unit,
    onSaveImage: () -> Unit,
    onSaveText: () -> Unit,
    onCopyLink: () -> Unit,
    onShareTo: (String) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(22.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Partager mon résultat",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Outlined.Close, contentDescription = "Fermer")
                    }
                }

                Spacer(Modifier.height(6.dp))

                Text(
                    text = "Partage tes résultats avec tes amis ou sauvegarde-les !",
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Start
                )

                Spacer(Modifier.height(14.dp))

                // Partager (action)
                Button(
                    enabled = (selectedTarget != null),
                    onClick = onShare,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6D41FF),
                        contentColor = Color.White
                    )
                ) {
                    Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Partager", fontWeight = FontWeight.Bold)
                }

                Spacer(Modifier.height(10.dp))

                // ✅ Deux boutons séparés : image + texte
                OutlinedButton(
                    onClick = onSaveImage,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.Download, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Sauvegarder l'image (Galerie)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(10.dp))

                OutlinedButton(
                    onClick = onSaveText,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text("Sauvegarder le texte (Fichiers)", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Selectionnez la plateforme de partage :",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(Modifier.height(12.dp))

                // Grille 3 + 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShareSquareButton(
                        label = "Facebook",
                        icon = FontAwesomeIcons.Brands.FacebookF,
                        bg = Color(0xFF1877F2),
                        modifier = Modifier.weight(1f),
                        onClick = { onShareTo("Facebook") }
                    )
                    ShareSquareButton(
                        label = "Twitter",
                        icon = FontAwesomeIcons.Brands.Twitter,
                        bg = Color(0xFF1DA1F2),
                        modifier = Modifier.weight(1f),
                        onClick = { onShareTo("Twitter") }
                    )
                    ShareSquareButton(
                        label = "LinkedIn",
                        icon = FontAwesomeIcons.Brands.LinkedinIn,
                        bg = Color(0xFF0A66C2),
                        modifier = Modifier.weight(1f),
                        onClick = { onShareTo("LinkedIn") }
                    )
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ShareSquareButton(
                        label = "WhatsApp",
                        icon = FontAwesomeIcons.Brands.Whatsapp,
                        bg = Color(0xFF25D366),
                        modifier = Modifier.weight(1f),
                        onClick = { onShareTo("WhatsApp") }
                    )
                    ShareSquareButton(
                        label = "Email",
                        icon = Icons.Outlined.Email,
                        bg = Color(0xFF4B5563),
                        modifier = Modifier.weight(1f),
                        onClick = { onShareTo("Email") }
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Copier le lien :",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF111827)
                )

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFFF3F4F6))
                        .padding(start = 14.dp, end = 10.dp, top = 12.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = link,
                        modifier = Modifier.weight(1f),
                        color = Color(0xFF111827)
                    )
                    IconButton(onClick = onCopyLink) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copier"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShareSquareButton(
    label: String,
    icon: ImageVector,
    bg: Color,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = bg,
        modifier = modifier.height(88.dp),
        border = if (selected) BorderStroke(3.dp, Color.White) else null
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = label,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    fontSize = 13.sp
                )
            }


            if (selected) {
                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(22.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("✓", color = bg, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}