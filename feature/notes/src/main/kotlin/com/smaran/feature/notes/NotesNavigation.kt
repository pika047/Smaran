package com.smaran.feature.notes

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val ADD_NOTE_ROUTE = "notes/add"

fun NavGraphBuilder.addNoteScreen(onFinished: () -> Unit) {
    composable(ADD_NOTE_ROUTE) {
        AddNoteRoute(onFinished = onFinished)
    }
}
