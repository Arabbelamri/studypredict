package com.example.studypredict

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studypredict.controller.ResultController
import com.example.studypredict.model.AnalysisInput
import com.example.studypredict.model.AnalysisResult
import com.example.studypredict.navigation.Routes
import com.example.studypredict.network.ApiResult
import com.example.studypredict.network.BackendApi
import com.example.studypredict.network.SessionStore
import com.example.studypredict.localization.AppLocaleProvider
import com.example.studypredict.localization.localize
import com.example.studypredict.ui.theme.TrainingTheme
import com.example.studypredict.view.auth.AuthScreen
import com.example.studypredict.view.badges.BadgesScreen
import com.example.studypredict.view.history.HistoryScreen
import com.example.studypredict.view.home.StudyPredictHomeScreen
import com.example.studypredict.view.libraries.NearbyLibrariesScreen
import com.example.studypredict.view.notes.NotesScreen
import com.example.studypredict.view.profile.ProfileScreen
import com.example.studypredict.view.reminders.ReminderScreen
import com.example.studypredict.view.steps.AttendanceStepScreen
import com.example.studypredict.view.steps.ExercisesStepScreen
import com.example.studypredict.view.steps.FocusStepScreen
import com.example.studypredict.view.steps.PhysicalActivityStepScreen
import com.example.studypredict.view.steps.PreviousScoresStepScreen
import com.example.studypredict.view.steps.ResultScreen
import com.example.studypredict.view.steps.SleepStepScreen
import com.example.studypredict.view.steps.StudyHoursStepScreen
import com.example.studypredict.view.steps.TutoringSessionsStepScreen
import com.example.studypredict.view.tips.TipsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TrainingTheme {
                AppLocaleProvider {
                    AppNav()
                }
            }
        }
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var accessToken by remember { mutableStateOf(SessionStore.getAccessToken(context)) }
    var refreshToken by remember { mutableStateOf(SessionStore.getRefreshToken(context)) }
    var userEmail by remember { mutableStateOf(SessionStore.getEmail(context).orEmpty()) }
    var displayName by remember { mutableStateOf(SessionStore.getDisplayName(context).orEmpty()) }
    var isLoggedIn by remember { mutableStateOf(!accessToken.isNullOrBlank()) }

    var authSubmitting by remember { mutableStateOf(false) }
    var authServerError by remember { mutableStateOf<String?>(null) }

    var isPredicting by remember { mutableStateOf(false) }
    var analysisInput by remember { mutableStateOf(AnalysisInput()) }
    var latestResult by remember { mutableStateOf<AnalysisResult?>(null) }

    val resultController = remember { ResultController() }

    fun openLogin() {
        navController.navigate(Routes.AUTH_LOGIN)
    }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.AUTH_LOGIN) {
            AuthScreen(
                initialIsLoginMode = true,
                isSubmitting = authSubmitting,
                serverError = authServerError,
                onSubmit = { isLoginMode, email, password, inputDisplayName ->
                    scope.launch {
                        authSubmitting = true
                        authServerError = null

                        val loginResult = if (isLoginMode) {
                            BackendApi.login(email, password)
                        } else {
                            when (val registerResult = BackendApi.register(email, password, inputDisplayName)) {
                                is ApiResult.Success -> BackendApi.login(email, password)
                                is ApiResult.Failure -> {
                                    authSubmitting = false
                                    authServerError = registerResult.message
                                    return@launch
                                }
                            }
                        }

                        authSubmitting = false
                        when (loginResult) {
                            is ApiResult.Success -> {
                                val resolvedName = inputDisplayName
                                    ?: email.substringBefore("@").ifBlank { "Utilisateur" }
                                SessionStore.saveSession(
                                    context = context,
                                    accessToken = loginResult.data.accessToken,
                                    refreshToken = loginResult.data.refreshToken,
                                    email = email,
                                    displayName = resolvedName
                                )
                                accessToken = loginResult.data.accessToken
                                refreshToken = loginResult.data.refreshToken
                                userEmail = email
                                displayName = resolvedName
                                isLoggedIn = true
                                navController.navigate(Routes.HOME) {
                                    launchSingleTop = true
                                    popUpTo(Routes.HOME) { inclusive = false }
                                }
                            }

                            is ApiResult.Failure -> {
                                authServerError = loginResult.message
                            }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AUTH_SIGNUP) {
            AuthScreen(
                initialIsLoginMode = false,
                isSubmitting = authSubmitting,
                serverError = authServerError,
                onSubmit = { isLoginMode, email, password, inputDisplayName ->
                    scope.launch {
                        authSubmitting = true
                        authServerError = null

                        val loginResult = if (isLoginMode) {
                            BackendApi.login(email, password)
                        } else {
                            when (val registerResult = BackendApi.register(email, password, inputDisplayName)) {
                                is ApiResult.Success -> BackendApi.login(email, password)
                                is ApiResult.Failure -> {
                                    authSubmitting = false
                                    authServerError = registerResult.message
                                    return@launch
                                }
                            }
                        }

                        authSubmitting = false
                        when (loginResult) {
                            is ApiResult.Success -> {
                                val resolvedName = inputDisplayName
                                    ?: email.substringBefore("@").ifBlank { "Utilisateur" }
                                SessionStore.saveSession(
                                    context = context,
                                    accessToken = loginResult.data.accessToken,
                                    refreshToken = loginResult.data.refreshToken,
                                    email = email,
                                    displayName = resolvedName
                                )
                                accessToken = loginResult.data.accessToken
                                refreshToken = loginResult.data.refreshToken
                                userEmail = email
                                displayName = resolvedName
                                isLoggedIn = true
                                navController.navigate(Routes.HOME) {
                                    launchSingleTop = true
                                    popUpTo(Routes.HOME) { inclusive = false }
                                }
                            }

                            is ApiResult.Failure -> {
                                authServerError = loginResult.message
                            }
                        }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HOME) {
            StudyPredictHomeScreen(
                isLoggedIn = isLoggedIn,
                onStartAnalysis = { navController.navigate(Routes.STEP1) },
                onPrediction = { navController.navigate(Routes.LIBRARIES) },
                onBadges = { navController.navigate(Routes.BADGES) },
                onTips = { navController.navigate(Routes.TIPS) },
                onHistory = { navController.navigate(Routes.HISTORY) },
                onReminders = { navController.navigate(Routes.REMINDERS) },
                onNotes = { navController.navigate(Routes.NOTES) },
                onLogin = { openLogin() },
                onProfile = { navController.navigate(Routes.PROFILE) }
            )
        }

        composable(Routes.PROFILE) {
            if (!isLoggedIn) {
                Scaffold { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(localize("Vous devez vous connecter."))
                        Button(onClick = { openLogin() }) {
                            Text(localize("Aller au login"))
                        }
                    }
                }
            } else {
                ProfileScreen(
                    displayName = displayName.ifBlank { "Utilisateur" },
                    email = userEmail.ifBlank { "Email indisponible" },
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        scope.launch {
                            val storedRefresh = refreshToken
                            if (!storedRefresh.isNullOrBlank()) {
                                BackendApi.logout(storedRefresh)
                            }
                            SessionStore.clear(context)
                            accessToken = null
                            refreshToken = null
                            userEmail = ""
                            displayName = ""
                            isLoggedIn = false
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.HOME) { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }

        composable(Routes.STEP1) {
            StudyHoursStepScreen(
                stepIndex = 1,
                totalSteps = 8,
                onNext = { hours ->
                    analysisInput = analysisInput.copy(hoursPerWeek = hours)
                    navController.navigate(Routes.STEP2)
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STEP2) {
            AttendanceStepScreen(
                stepIndex = 2,
                totalSteps = 8,
                onBack = { navController.popBackStack() },
                onNext = { attendance ->
                    analysisInput = analysisInput.copy(attendancePercent = attendance)
                    navController.navigate(Routes.STEP3)
                }
            )
        }

        composable(Routes.STEP3) {
            ExercisesStepScreen(
                stepIndex = 3,
                totalSteps = 8,
                onBack = { navController.popBackStack() },
                onNext = { exercises ->
                    analysisInput = analysisInput.copy(exercisesPerMonth = exercises)
                    navController.navigate(Routes.STEP4)
                }
            )
        }

        composable(Routes.STEP4) {
            SleepStepScreen(
                stepIndex = 4,
                totalSteps = 8,
                onBack = { navController.popBackStack() },
                onNext = { sleep ->
                    analysisInput = analysisInput.copy(sleepHours = sleep)
                    navController.navigate(Routes.STEP5)
                }
            )
        }

        composable(Routes.STEP5) {
            PreviousScoresStepScreen(
                stepIndex = 5,
                totalSteps = 8,
                onBack = { navController.popBackStack() },
                onNext = { previous ->
                    analysisInput = analysisInput.copy(previousScores = previous)
                    navController.navigate(Routes.STEP6)
                }
            )
        }

        composable(Routes.STEP6) {
            TutoringSessionsStepScreen(
                stepIndex = 6,
                totalSteps = 8,
                onBack = { navController.popBackStack() },
                onNext = { sessions ->
                    analysisInput = analysisInput.copy(tutoringSessions = sessions)
                    navController.navigate(Routes.STEP7)
                }
            )
        }

        composable(Routes.STEP7) {
            PhysicalActivityStepScreen(
                stepIndex = 7,
                totalSteps = 8,
                onBack = { navController.popBackStack() },
                onNext = { physical, extracurricular ->
                    analysisInput = analysisInput.copy(
                        physicalActivityHours = physical,
                        extracurricularActivities = extracurricular
                    )
                    navController.navigate(Routes.STEP8)
                }
            )
        }

        composable(Routes.STEP8) {
            FocusStepScreen(
                stepIndex = 8,
                totalSteps = 8,
                isSubmitting = isPredicting,
                onBack = { navController.popBackStack() },
                onSeeResult = { focus ->
                    val token = accessToken
                    if (token.isNullOrBlank()) {
                        openLogin()
                        return@FocusStepScreen
                    }

                    analysisInput = analysisInput.copy(focusLevel = focus)
                    scope.launch {
                        isPredicting = true
                        latestResult = null

                        val predictionResult = BackendApi.predictSuccess(
                            token = token,
                            periodDays = 30,
                            hoursWorked = (analysisInput.hoursPerWeek * 4).toDouble(),
                            exercisesDone = analysisInput.exercisesPerMonth,
                            sleepHoursAvg = analysisInput.sleepHours.toDouble(),
                            attendance = analysisInput.attendancePercent.toDouble(),
                            previousScores = analysisInput.previousScores.toDouble(),
                            tutoringSessions = analysisInput.tutoringSessions,
                            physicalActivity = analysisInput.physicalActivityHours.toDouble(),
                            extracurricularActivities = analysisInput.extracurricularActivities
                        )

                        isPredicting = false
                        when (predictionResult) {
                            is ApiResult.Success -> {
                                latestResult = resultController.buildResultFromScore(
                                    input = analysisInput,
                                    scorePercent = predictionResult.data
                                )
                                navController.navigate(Routes.RESULT)
                            }

                            is ApiResult.Failure -> {
                                if (predictionResult.unauthorized) {
                                    SessionStore.clear(context)
                                    accessToken = null
                                    refreshToken = null
                                    userEmail = ""
                                    displayName = ""
                                    isLoggedIn = false
                                    openLogin()
                                }
                                Toast.makeText(context, predictionResult.message, Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                }
            )
        }

        composable(Routes.RESULT) {
            val result = latestResult
            if (result == null) {
                Scaffold { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(localize("Aucun resultat disponible."))
                        Button(onClick = { navController.popBackStack() }) {
                            Text(localize("Retour"))
                        }
                    }
                }
            } else {
                ResultScreen(
                    result = result,
                    onShare = { },
                    onNewAnalysis = {
                        analysisInput = AnalysisInput()
                        latestResult = null
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        composable(Routes.BADGES) {
            BadgesScreen(
                onBack = { navController.popBackStack() },
                token = accessToken,
                onUnauthorized = {
                    SessionStore.clear(context)
                    accessToken = null
                    refreshToken = null
                    userEmail = ""
                    displayName = ""
                    isLoggedIn = false
                    openLogin()
                }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                token = accessToken,
                onBack = { navController.popBackStack() },
                onStartAnalysis = { navController.navigate(Routes.STEP1) },
                onUnauthorized = {
                    SessionStore.clear(context)
                    accessToken = null
                    refreshToken = null
                    userEmail = ""
                    displayName = ""
                    isLoggedIn = false
                    openLogin()
                }
            )
        }

        composable(Routes.LIBRARIES) {
            NearbyLibrariesScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.TIPS) {
            TipsScreen(
                token = accessToken,
                onBack = { navController.popBackStack() },
                onUnauthorized = {
                    SessionStore.clear(context)
                    accessToken = null
                    refreshToken = null
                    userEmail = ""
                    displayName = ""
                    isLoggedIn = false
                    openLogin()
                }
            )
        }

        composable(Routes.REMINDERS) {
            ReminderScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.NOTES) {
            NotesScreen(
                token = accessToken,
                onBack = { navController.popBackStack() },
                onUnauthorized = {
                    SessionStore.clear(context)
                    accessToken = null
                    refreshToken = null
                    userEmail = ""
                    displayName = ""
                    isLoggedIn = false
                    openLogin()
                }
            )
        }
    }
}
