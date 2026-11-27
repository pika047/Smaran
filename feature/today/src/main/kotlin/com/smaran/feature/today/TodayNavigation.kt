package com.smaran.feature.today

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val TODAY_ROUTE = "today"

fun NavGraphBuilder.todayScreen(onAddNote: () -> Unit) {
    composable(TODAY_ROUTE) {
        TodayRoute(onAddNote = onAddNote)
    }
}
