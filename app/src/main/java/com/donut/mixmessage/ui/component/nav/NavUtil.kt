package com.donut.mixmessage.ui.component.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.donut.mixmessage.util.common.genRandomString
import com.donut.mixmessage.util.common.isNotNull
import java.lang.ref.WeakReference

@Suppress("MemberVisibilityCanBePrivate")
class MixNavPage(
    val name: String = genRandomString(),
    private val displayNavBar: Boolean = true,
    private val gap: Dp = 0.dp,
    val modifier: Modifier = Modifier,
    val useTransition: Boolean = false,
    val content: @Composable (NavBackStackEntry) -> Unit,
) {

    operator fun invoke(builder: NavGraphBuilder) {
        register(builder)
    }

    fun register(builder: NavGraphBuilder) {
        builder.apply {
            composable(
                name,
                enterTransition = { if (useTransition) slideInHorizontally(tween()) { it } else null },
                exitTransition = { if (useTransition) slideOutHorizontally(tween()) { it } else null },
            ) {

                if (getCurrentRoute().contentEquals(name)) {
                    showNavBar = displayNavBar
                }
                Column(
                    modifier = modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(gap),
                ) {
                    content(it)
                }
            }
        }
    }
}

var navControllerCache = WeakReference<NavHostController>(null)
var showNavBar by mutableStateOf(true)

@Composable
fun NavTitle(showBackIcon: Boolean = false, title: String) {
    val controller = getNavController()
    Row(
        modifier = Modifier.padding(0.dp, 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showBackIcon) {
            IconButton(onClick = {
                controller.popBackStack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "back",
                )
            }
        }
        Text(text = title, fontSize = 25.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun getNavController(): NavHostController {
    val cache = navControllerCache.get()
    if (cache.isNotNull()) {
        return cache!!
    }
    val navController = rememberNavController()
    navControllerCache = WeakReference(navController)
    return navController
}

@Composable
fun getCurrentRoute(): String? {
    val navController = getNavController()
    val stackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = stackEntry?.destination?.route
    return currentRoute
}