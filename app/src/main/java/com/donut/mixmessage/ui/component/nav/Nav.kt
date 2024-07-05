package com.donut.mixmessage.ui.component.nav

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.donut.mixmessage.ui.component.routes.Home
import com.donut.mixmessage.ui.component.routes.password.Passwords
import com.donut.mixmessage.ui.component.routes.settings.Settings
import com.donut.mixmessage.util.common.OnDispose
import com.donut.mixmessage.util.common.performHapticFeedBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavComponent() {

    OnDispose {
        navControllerCache.clear()
    }

    val currentRoute = getCurrentRoute()
    val controller = getNavController()


    Scaffold(
        bottomBar = {
            AnimatedVisibility(
                visible = showNavBar,
                enter = slideInVertically { it } + fadeIn(tween()),
                exit = slideOutVertically { it }
            ) {
                NavigationBar {
                    @Composable
                    fun NavButton(text: String, icon: ImageVector, jumpTo: String) {
                        NavigationBarItem(
                            selected = jumpTo == currentRoute,
                            onClick = {
                                performHapticFeedBack()
                                controller.navigate(jumpTo) {
                                    launchSingleTop = true
                                }
                            },
                            label = {
                                Text(text = text)
                            },
                            icon = {
                                Icon(icon, contentDescription = text)
                            }
                        )
                        return
                    }
                    NavButton("主页", Icons.Outlined.Home, Home.name)
                    NavButton("密钥", Icons.Outlined.Lock, Passwords.name)
                    NavButton("设置", Icons.Outlined.Settings, Settings.name)
                }
            }
        },
        content = { innerPadding ->
            NavContent(innerPadding)
        }
    )


}