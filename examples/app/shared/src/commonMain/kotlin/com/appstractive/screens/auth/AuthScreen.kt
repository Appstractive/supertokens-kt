package com.appstractive.screens.auth

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.tab.CurrentTab
import cafe.adriel.voyager.navigator.tab.LocalTabNavigator
import cafe.adriel.voyager.navigator.tab.Tab
import cafe.adriel.voyager.navigator.tab.TabNavigator
import cafe.adriel.voyager.navigator.tab.TabOptions

object AuthScreen : Screen {

    @Composable
    override fun Content() {
        TabNavigator(EMailPasswordTab) {
            Scaffold(content = {
                Box(modifier = Modifier.padding(it), content = {
                    CurrentTab()
                })
            }, bottomBar = {
                BottomNavigation {
                    TabNavigationItem(EMailPasswordTab)
                    TabNavigationItem(PasswordlessTab)
                    TabNavigationItem(ThirdPartyTab)
                }
            })
        }
    }

    @Composable
    private fun RowScope.TabNavigationItem(tab: Tab) {
        val tabNavigator = LocalTabNavigator.current

        BottomNavigationItem(
            selected = tabNavigator.current == tab,
            onClick = { tabNavigator.current = tab },
            icon = {
                Icon(
                    painter = checkNotNull(tab.options.icon),
                    contentDescription = tab.options.title,
                )
            },
        )
    }
}

object EMailPasswordTab : Tab {

    override val options: TabOptions
        @Composable get() {
            val title = "EMail/Password"
            val icon = rememberVectorPainter(Icons.Default.Home)

            return remember {
                TabOptions(
                    index = 0u, title = title, icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        EmailPassword()
    }


}

object PasswordlessTab : Tab {

    override val options: TabOptions
        @Composable get() {
            val title = "Passwordless"
            val icon = rememberVectorPainter(Icons.Default.Email)

            return remember {
                TabOptions(
                    index = 1u, title = title, icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // ...
    }
}

object ThirdPartyTab : Tab {

    override val options: TabOptions
        @Composable get() {
            val title = "Passwordless"
            val icon = rememberVectorPainter(Icons.Default.AccountBox)

            return remember {
                TabOptions(
                    index = 2u, title = title, icon = icon
                )
            }
        }

    @Composable
    override fun Content() {
        // ...
    }
}