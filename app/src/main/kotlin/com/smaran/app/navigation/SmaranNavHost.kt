package com.smaran.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.smaran.feature.notes.ADD_NOTE_ROUTE
import com.smaran.feature.notes.addNoteScreen
import com.smaran.feature.today.TODAY_ROUTE
import com.smaran.feature.today.todayScreen

/** Wires feature screens together. Features never depend on each other, only on routes passed in here. */
@Composable
fun SmaranNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = TODAY_ROUTE) {
        todayScreen(onAddNote = { navController.navigate(ADD_NOTE_ROUTE) })
        addNoteScreen(onFinished = { navController.popBackStack() })
    }
}
