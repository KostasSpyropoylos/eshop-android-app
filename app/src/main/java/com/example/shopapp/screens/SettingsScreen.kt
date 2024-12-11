package com.example.shopapp.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.shopapp.R
import com.example.shopapp.ui.theme.AppThemeManager
import com.example.shopapp.viewmodels.AuthState
import com.example.shopapp.viewmodels.AuthViewModel

@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier,
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val authState = authViewModel.authState.observeAsState()
    LaunchedEffect(authState.value) {
        when (authState.value) {
            is AuthState.Unauthenticated -> navController.navigate("login")
            else -> Unit
        }
    }
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp, 10.dp, 20.dp, 10.dp)
    ) {

        item {
            Spacer(Modifier.height(5.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(1.dp, 20.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    Modifier
                        .padding(10.dp, 20.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_location_pin_24),
                        contentDescription = null,

                        )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Region",
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    Modifier
                        .padding(10.dp, 20.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                ) {
                    Text(
                        "GR",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(R.drawable.baseline_keyboard_arrow_right_24),
                        contentDescription = null,
                    )

                }
            }
        }

        item {
            Spacer(Modifier.height(5.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(1.dp, 10.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    Modifier
                        .padding(10.dp, 20.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_language_24),
                        contentDescription = null,

                        )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Language",
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    Modifier
                        .padding(10.dp, 20.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                ) {
                    Text(
                        "EN",
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(Modifier.width(10.dp))
                    Icon(
                        painter = painterResource(R.drawable.baseline_keyboard_arrow_right_24),
                        contentDescription = null,
                    )

                }
            }
        }


        item {
            Spacer(Modifier.height(5.dp))
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(1.dp, 10.dp)
                    .clip(shape = RoundedCornerShape(20.dp))
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    Modifier
                        .padding(10.dp, 20.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_dark_mode_24),
                        contentDescription = null,

                        )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Night Mode",
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    Modifier
                        .padding(10.dp, 10.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant
                        ),
                ) {
                    ToggleSwitch()
                }
            }
        }


        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = {
                    authViewModel.signout()
                }, colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                    disabledContainerColor = Color.Black,
                    disabledContentColor = Color.Black
                ),
                    shape = RoundedCornerShape(20.dp),

                    border = BorderStroke(width = 2.dp, color = MaterialTheme.colorScheme.primary),
                    content = {
                        Text(text = "Sign out")
                    })
            }
        }
    }
}

@Composable
fun ToggleSwitch() {
    val isDarkTheme = AppThemeManager.isDarkThemeEnabled
    var checked by remember { mutableStateOf(isDarkTheme) }

    Switch(
        checked = checked,
        onCheckedChange = { isChecked ->
            checked = isChecked
            AppThemeManager.isDarkThemeEnabled = isChecked
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.primary,
            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
        )
    )

}
