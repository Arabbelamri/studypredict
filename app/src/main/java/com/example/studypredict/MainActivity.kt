package com.example.studypredict

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.studypredict.controller.ResultController
import com.example.studypredict.model.AnalysisInput
import com.example.studypredict.navigation.Routes
import com.example.studypredict.ui.theme.TrainingTheme
import com.example.studypredict.view.badges.BadgesScreen
import com.example.studypredict.view.history.HistoryScreen
import com.example.studypredict.view.home.StudyPredictHomeScreen
import com.example.studypredict.view.libraries.NearbyLibrariesScreen
import com.example.studypredict.view.notes.NotesScreen
import com.example.studypredict.view.reminders.ReminderScreen
import com.example.studypredict.view.steps.AttendanceStepScreen
import com.example.studypredict.view.steps.ExercisesStepScreen
import com.example.studypredict.view.steps.FocusStepScreen
import com.example.studypredict.view.steps.ResultScreen
import com.example.studypredict.view.steps.SleepStepScreen
import com.example.studypredict.view.steps.StudyHoursStepScreen
import com.example.studypredict.view.tips.TipsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TrainingTheme {
                AppNav()
            }
        }
    }
}

@Composable
fun AppNav() {
    val navController = rememberNavController()

    var analysisInput by remember { mutableStateOf(AnalysisInput()) }
    val resultController = remember { ResultController() }

    NavHost(
        navController = navController,
        startDestination = Routes.HOME
    ) {
        composable(Routes.HOME) {
            StudyPredictHomeScreen(
                onStartAnalysis = { navController.navigate(Routes.STEP1) },
                onPrediction = { navController.navigate(Routes.LIBRARIES) },
                onBadges = { navController.navigate(Routes.BADGES) },
                onTips = { navController.navigate(Routes.TIPS) },
                onHistory = { navController.navigate(Routes.HISTORY) },
                onReminders = { navController.navigate(Routes.REMINDERS) },
                onNotes = { navController.navigate(Routes.NOTES) }
            )
        }

        composable(Routes.STEP1) {
            StudyHoursStepScreen(
                stepIndex = 1,
                totalSteps = 5,
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
                totalSteps = 5,
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
                totalSteps = 5,
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
                totalSteps = 5,
                onBack = { navController.popBackStack() },
                onNext = { sleep ->
                    analysisInput = analysisInput.copy(sleepHours = sleep)
                    navController.navigate(Routes.STEP5)
                }
            )
        }

        composable(Routes.STEP5) {
            FocusStepScreen(
                stepIndex = 5,
                totalSteps = 5,
                onBack = { navController.popBackStack() },
                onSeeResult = { focus ->
                    analysisInput = analysisInput.copy(focusLevel = focus)
                    navController.navigate(Routes.RESULT)
                }
            )
        }

        composable(Routes.RESULT) {
            val result = remember(analysisInput) {
                resultController.buildResult(analysisInput)
            }

            ResultScreen(
                result = result,
                onShare = { },
                onNewAnalysis = {
                    analysisInput = AnalysisInput()
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.BADGES) {
            BadgesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            HistoryScreen(
                onBack = { navController.popBackStack() },
                onStartAnalysis = { navController.navigate(Routes.STEP1) }
            )
        }

        composable(Routes.LIBRARIES) {
            NearbyLibrariesScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.TIPS) {
            TipsScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.REMINDERS) {
            ReminderScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTES) {
            NotesScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}