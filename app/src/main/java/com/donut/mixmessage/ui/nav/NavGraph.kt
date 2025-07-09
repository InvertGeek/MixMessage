package com.donut.mixmessage.ui.nav

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import com.donut.mixmessage.lastDestination
import com.donut.mixmessage.ui.routes.Home
import com.donut.mixmessage.ui.routes.password.Passwords
import com.donut.mixmessage.ui.routes.settings.Settings
import com.donut.mixmessage.ui.routes.settings.routes.AboutPage
import com.donut.mixmessage.ui.routes.settings.routes.AutoDecode
import com.donut.mixmessage.ui.routes.settings.routes.FastSend
import com.donut.mixmessage.ui.routes.settings.routes.FileUploadPage
import com.donut.mixmessage.ui.routes.settings.routes.OtherPage
import com.donut.mixmessage.ui.routes.settings.routes.PrefixPage
import com.donut.mixmessage.ui.routes.settings.routes.RSAPage


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun NavContent(innerPaddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPaddingValues)
            .fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        val controller = getNavController()
        LaunchedEffect(Unit) {
            if (lastDestination.isNotBlank()) {
                controller.navigate(lastDestination) {
                    anim {
                        enter = 0
                        exit = 0
                        popEnter = 0
                        popExit = 0
                    }
                }
            }
            controller.addOnDestinationChangedListener { controller, destination, _ ->
                destination.route?.let {
                    lastDestination = it
                }
            }
        }
        NavHost(
            navController = controller,
            startDestination = Home.name,
        ) {
            Home(this)
            Passwords(this)
            Settings(this).also {
                AutoDecode(this)
                FastSend(this)
                AboutPage(this)
                OtherPage(this)
                FileUploadPage(this)
                RSAPage(this)
                PrefixPage(this)
            }
        }
    }
}