package com.donut.mixmessage.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.donut.mixmessage.ui.component.routes.HomePage
import com.donut.mixmessage.ui.component.routes.password.Passwords
import com.donut.mixmessage.ui.component.routes.settings.Settings
import com.donut.mixmessage.util.common.performHapticFeedBack


fun NavGraphBuilder.navPage(name: String, content: @Composable () -> Unit) {
    composable(name) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            content()
        }
    }
}

@Composable
fun NavContent(navController: NavHostController, innerPaddingValues: PaddingValues) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPaddingValues)
            .background(Color(0xFFE6DFEB))
            .fillMaxHeight(), verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        NavHost(navController = navController, startDestination = "home") {
            navPage("home") {
                HomePage()
            }
            navPage("settings") {
                Settings()
            }
            navPage("passwords") {
                Passwords()
            }
        }
    }
}


@Composable
fun NavComponent() {
    val navController = rememberNavController()


    val stackEntry by navController.currentBackStackEntryAsState()

    val currentRoute = stackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
//                .background(Color(0xFFED6666)),
                verticalArrangement = Arrangement.Bottom
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xADB6D9D9))
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                ) {
                    @Composable
                    fun NavButton(text: String, icon: ImageVector, jumpTo: String) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(15.dp))
                                .clickable {
                                    performHapticFeedBack()
                                    navController.navigate(jumpTo) {
                                    }
                                }
                                .background(
                                    if (jumpTo == currentRoute) Color(0x4324A0ED) else Color.Transparent
                                ).padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(icon, contentDescription = text)
                            Text(text = text, fontSize = 12.sp)
                        }
                    }
                    NavButton("主页", Icons.Outlined.Home, "home")
                    NavButton("密钥", Icons.Outlined.Lock, "passwords")
                    NavButton("设置", Icons.Outlined.Settings, "settings")
                }
            }
        },
        content = { innerPadding ->
            NavContent(navController, innerPadding)
        }
    )


}