package com.donut.mixmessage.ui.component.nav

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
import com.donut.mixmessage.LocalDestination
import com.donut.mixmessage.ui.component.routes.Home
import com.donut.mixmessage.ui.component.routes.password.Passwords
import com.donut.mixmessage.ui.component.routes.settings.Settings
import com.donut.mixmessage.ui.component.routes.settings.routes.AboutPage
import com.donut.mixmessage.ui.component.routes.settings.routes.AutoDecode
import com.donut.mixmessage.ui.component.routes.settings.routes.FastSend
import com.donut.mixmessage.ui.component.routes.settings.routes.ImagePage
import com.donut.mixmessage.ui.component.routes.settings.routes.OtherPage
import com.donut.mixmessage.ui.component.routes.settings.routes.PrefixPage
import com.donut.mixmessage.ui.component.routes.settings.routes.RSAPage


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
        val lastDestination = LocalDestination.current
        LaunchedEffect(Unit) {
            if (lastDestination.value.isNotBlank()) {
                controller.navigate(lastDestination.value) {
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
                    lastDestination.value = it
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
                ImagePage(this)
                RSAPage(this)
                PrefixPage(this)
            }
        }
    }
}