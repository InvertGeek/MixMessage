package com.donut.mixmessage.ui.component.nav

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.navigation
import com.donut.mixmessage.ui.component.routes.Home
import com.donut.mixmessage.ui.component.routes.password.Passwords
import com.donut.mixmessage.ui.component.routes.settings.Settings
import com.donut.mixmessage.ui.component.routes.settings.routes.AboutPage
import com.donut.mixmessage.ui.component.routes.settings.routes.AutoDecode
import com.donut.mixmessage.ui.component.routes.settings.routes.FastSend
import com.donut.mixmessage.ui.component.routes.settings.routes.OtherPage

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavContent(innerPaddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPaddingValues)
            .fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        NavHost(
            navController = getNavController(),
            startDestination = Home.name,
        ) {
            Home(this)
            Passwords(this)
            navigation(startDestination = Settings.name, route = "settings") {
                Settings(this)
                AutoDecode(this)
                FastSend(this)
                AboutPage(this)
                OtherPage(this)
            }
        }
    }
}