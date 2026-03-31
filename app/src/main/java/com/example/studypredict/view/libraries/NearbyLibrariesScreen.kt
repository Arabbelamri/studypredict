package com.example.studypredict.view.libraries

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.MyLocation
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.studypredict.libraries.LibraryPlace
import com.example.studypredict.libraries.fetchLibrariesOverpass
import com.example.studypredict.localization.localize
import com.example.studypredict.ui.components.NoInternetDialog
import com.example.studypredict.utils.NetworkUtils
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import kotlin.coroutines.resume

private class MapHolder {
    var map: MapView? = null
}

@Composable
fun NearbyLibrariesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val bg = Color(0xFFF2F6FF)
    val card = Color(0xFFF7FAFF)
    val dark = Color(0xFF0B1220)
    val gray = Color(0xFF6B7280)
    val purple = Color(0xFF6D41FF)
    val primaryGrad = Brush.horizontalGradient(
        listOf(Color(0xFF4B3CFF), Color(0xFFB400FF))
    )

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var hasPermission by remember { mutableStateOf(false) }
    var userLoc by remember { mutableStateOf<Location?>(null) }
    var libs by remember { mutableStateOf<List<LibraryPlace>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var showNoInternetDialog by remember { mutableStateOf(false) }

    var radiusMeters by remember { mutableStateOf(2000) }

    val holder = remember { MapHolder() }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        hasPermission =
            (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                    (res[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    fun centerOn(lat: Double, lon: Double, zoom: Double = 15.0) {
        val map = holder.map ?: return
        map.controller.setZoom(zoom)
        map.controller.setCenter(GeoPoint(lat, lon))
        map.invalidate()
    }

    fun renderMarkers(user: Location?, libraries: List<LibraryPlace>) {
        val map = holder.map ?: return

        val keep = map.overlays.filterNot { it is Marker }
        map.overlays.clear()
        map.overlays.addAll(keep)

        user?.let {
            Marker(map).apply {
                position = GeoPoint(it.latitude, it.longitude)
                title = "Vous"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.overlays.add(this)
            }
        }

        libraries.forEach { lib ->
            Marker(map).apply {
                position = GeoPoint(lib.lat, lib.lon)
                title = lib.name
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                map.overlays.add(this)
            }
        }

        map.invalidate()
    }

    suspend fun refresh() {
        if (!NetworkUtils.isInternetAvailable(context)) {
            loading = false
            error = null
            showNoInternetDialog = true
            return
        }

        error = null
        loading = true

        val loc = getLastKnownLocation(context)
        userLoc = loc

        if (loc == null) {
            loading = false
            error = "Impossible d'obtenir ta position."
            return
        }

        try {
            val results = fetchLibrariesOverpass(loc.latitude, loc.longitude, radiusMeters)
            libs = results
            centerOn(loc.latitude, loc.longitude, zoom = 14.5)
            renderMarkers(loc, results)

            if (results.isEmpty()) {
                error = null
            }
        } catch (_: Exception) {
            error = "Impossible de charger les bibliothèques. Vérifie ta connexion et réessaie."
        } finally {
            loading = false
        }
    }

    var showList by remember { mutableStateOf(false) }
    LaunchedEffect(libs.size) {
        showList = libs.isNotEmpty()
    }

    val headerAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing),
        label = "headerAlpha"
    )

    Scaffold(containerColor = bg) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(horizontal = 18.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Outlined.ArrowBack,
                        contentDescription = localize("Retour"),
                        tint = dark
                    )
                }
                Text(
                    localize("Retour"),
                    color = dark,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(primaryGrad),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Outlined.LocationOn,
                        contentDescription = null,
                        tint = Color.White
                    )
                }

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        text = localize("Bibliothèques proches"),
                        fontSize = 28.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                        color = dark
                    )
                    Text(
                        text = localize("Trouve un spot pour réviser autour de toi"),
                        color = gray
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            val actionShape = RoundedCornerShape(22.dp)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(10.dp, actionShape, clip = false),
                shape = actionShape,
                color = card
            ) {
                Column(Modifier.padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Search,
                            contentDescription = null,
                            tint = Color(0xFF5B55FF)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            localize("Rayon de recherche"),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                            color = dark
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = radiusMeters == 2000,
                            onClick = { radiusMeters = 2000 },
                            label = {
                                Text(
                                    "2 km",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF5B55FF),
                                selectedLabelColor = Color.White
                            )
                        )

                        FilterChip(
                            selected = radiusMeters == 5000,
                            onClick = { radiusMeters = 5000 },
                            label = {
                                Text(
                                    "5 km",
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF5B55FF),
                                selectedLabelColor = Color.White
                            )
                        )

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = {
                                if (!NetworkUtils.isInternetAvailable(context)) {
                                    showNoInternetDialog = true
                                    return@Button
                                }
                                scope.launch { refresh() }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = purple,
                                contentColor = Color.White
                            )
                        ) {
                            Text(if (loading) "..." else localize("Rechercher"))
                        }
                    }

                    if (loading) {
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    error?.let {
                        Spacer(Modifier.height(10.dp))
                        Text(localize(it), color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            val mapShape = RoundedCornerShape(26.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .shadow(14.dp, mapShape, clip = false)
                    .clip(mapShape)
                    .background(Color.White)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(5.0)
                            controller.setCenter(GeoPoint(48.8566, 2.3522))
                            holder.map = this
                        }
                    },
                    update = {
                        renderMarkers(userLoc, libs)
                    }
                )

                FloatingActionButton(
                    onClick = {
                        userLoc?.let { centerOn(it.latitude, it.longitude, 15.5) }
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(14.dp),
                    shape = CircleShape,
                    containerColor = Color.White,
                    contentColor = Color(0xFF111827)
                ) {
                    Icon(Icons.Outlined.MyLocation, contentDescription = localize("Recentrer"))
                }
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = localize("Résultats (%d)", libs.size),
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = dark
            )

            Spacer(Modifier.height(8.dp))

            if (!hasPermission) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            localize("Autorise la localisation pour afficher les bibliothèques."),
                            color = dark
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Text(localize("Autoriser"))
                        }
                    }
                }
                return@Column
            }

            if (!loading && libs.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(
                            localize("Aucune bibliothèque chargée pour le moment."),
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            localize("Appuie sur “Rechercher” pour trouver des bibliothèques autour de toi."),
                            color = gray
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            AnimatedVisibility(
                visible = showList,
                enter = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 6 }
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 18.dp)
                ) {
                    items(libs) { lib ->
                        val itemShape = RoundedCornerShape(22.dp)
                        Surface(
                            onClick = { centerOn(lib.lat, lib.lon, 17.0) },
                            shape = itemShape,
                            color = Color.White,
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(10.dp, itemShape, clip = false)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(Color(0xFFEEF2FF)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.LocationOn,
                                        contentDescription = null,
                                        tint = Color(0xFF5B55FF)
                                    )
                                }

                                Spacer(Modifier.width(12.dp))

                                Column(Modifier.weight(1f)) {
                                    Text(
                                        lib.name,
                                        fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                                        color = dark
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        "📍 ${"%.5f".format(lib.lat)}, ${"%.5f".format(lib.lon)}",
                                        color = gray,
                                        fontSize = 13.sp
                                    )
                                }

                                AssistChip(
                                    onClick = { centerOn(lib.lat, lib.lon, 17.0) },
                                    label = {
                                        Text(
                                            localize("Voir"),
                                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = Color(0xFF5B55FF),
                                        labelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showNoInternetDialog) {
            NoInternetDialog(
                onDismiss = { showNoInternetDialog = false },
                onOpenSettings = {
                    showNoInternetDialog = false
                    NetworkUtils.openInternetSettings(context)
                }
            )
        }
    }
}

@SuppressLint("MissingPermission")
private suspend fun getLastKnownLocation(context: Context): Location? =
    withContext(Dispatchers.IO) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        try {
            val task = client.lastLocation
            suspendCancellableCoroutine { cont ->
                task.addOnSuccessListener { loc -> cont.resume(loc) }
                task.addOnFailureListener { cont.resume(null) }
            }
        } catch (_: Exception) {
            null
        }
    }
