package com.aikonia.app.ui.startchat

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aikonia.app.R
import com.aikonia.app.common.components.AnimatedButton
import com.aikonia.app.common.components.AppBar
import com.aikonia.app.common.components.NoConnectionDialog
import com.aikonia.app.common.components.ThereIsUpdateDialog
import com.aikonia.app.ui.activity.isOnline
import com.aikonia.app.ui.theme.FlowerFontFamily
// import com.aikonia.app.ui.upgrade.PurchaseHelper
import java.util.Locale
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color
//import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import android.content.SharedPreferences
import android.util.Log
import com.aikonia.app.ui.theme.VibrantBlue
import com.aikonia.app.ui.theme.VibrantBlue2

import android.media.MediaPlayer
import androidx.compose.foundation.indication
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import android.widget.VideoView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.material.TextFieldDefaults

@Composable
fun StartChatScreen(
    navigateToMenu: () -> Unit,
    navigateToChat: (String, String, List<String>?) -> Unit,
    navigateToWelcome: () -> Unit,
    startChatViewModel: StartChatViewModel = hiltViewModel(),
    sharedPreferences: SharedPreferences,

) {

    val context = LocalContext.current
    var showDialog by remember { mutableStateOf(false) }
    var showUpdateDialog by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf("") }
    var birthYear by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val genderOptions = listOf("Mädchen", "Junge")
    val shouldStartInMenu = sharedPreferences.getBoolean("shouldStartInMenu", true)
    val isUserDataSaved = startChatViewModel.isUserDataSaved.collectAsState().value

    var videoView: VideoView? by remember { mutableStateOf(null) }
    val lifecycleOwner = LocalLifecycleOwner.current


    fun changeLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    LaunchedEffect(Unit) {
        Log.d("StarChatScreen", "Checking user data saved status")
        if (shouldStartInMenu) {
            navigateToMenu()
        } else {
            // Abrufen der Benutzer-ID
            val userId = sharedPreferences.getInt("userIdKey", -1).toLong()
            if (userId != -1L) {
                startChatViewModel.checkUserDataExists(userId.toInt())
            }

            // ... (Rest der Logik bleibt gleich)

            // Entscheidung, ob zum Chat oder zum Welcome-Screen navigiert werden soll
            if (isUserDataSaved) {
                Log.d("StartChatScreen", "User data is saved, navigating to ChatScreen")
                navigateToChat(name, birthYear, listOf(gender))
            } else {
                Log.d("StarChatScreen", "User data is saved, navigating to WelcomeScreen")
                navigateToWelcome()
            }
        }


        if (isOnline(context).not()) showDialog = true
        if (startChatViewModel.isThereUpdate.value) showUpdateDialog = true
    }



    if (showDialog) {
        NoConnectionDialog {
            showDialog = false
        }
    }

    if (showUpdateDialog) {
        ThereIsUpdateDialog {
            try {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=com.aikonia.app")
                    )
                )
            } catch (e: ActivityNotFoundException) {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.aikonia.app")
                    )
                )
            }
        }
    }

    // VideoView initialisieren und Video einrichten


    DisposableEffect(lifecycleOwner) {
        val videoView = VideoView(context).apply {
            setVideoPath("android.resource://${context.packageName}/${R.raw.start_chat_image_animation}")
            setOnPreparedListener { mp ->
                mp.isLooping = true
                start()
            }
        }

        onDispose {
            videoView.stopPlayback()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                VideoView(context).apply {
                    setVideoPath("android.resource://${context.packageName}/${R.raw.start_chat_image_animation}")
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        start()
                    }
                }
            },
            modifier = Modifier.matchParentSize()
        )



        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            //AppBar(
            //    onClickAction = {},
            //    image = R.drawable.app_icon,
            //    text = stringResource(R.string.app_name),
            //    tint = VibrantBlue

            //)

            Spacer(modifier = Modifier.height(320.dp)) // Vergrößern oder verkleinern Sie diesen Wert, um die Textfelder weiter nach unten zu verschieben

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top // Inhalte an den oberen Rand der Column ausrichten
            ) {

                // UI-Elemente für Eingabefelder
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            "Name",
                            color = Color.White, // Farbe des Labels
                            fontFamily = FlowerFontFamily, // Schriftart für das Label
                            fontSize = 16.sp // Größe des Labels
                        )
                    },
                    textStyle = TextStyle(
                        color = Color.White, // Textfarbe im Feld
                        fontSize = 16.sp, // Textgröße im Feld
                        fontFamily = FlowerFontFamily // Schriftart im Feld
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        backgroundColor = VibrantBlue2.copy(alpha = 0.5f), // Direkte Definition der Hintergrundfarbe mit Transparenz
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.6f) // Kontrolliert die Breite des Textfelds
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    // Weitere Eigenschaften...
                )

                Spacer(modifier = Modifier.height(8.dp)) // Verringern Sie diesen Wert, um den Abstand zu reduzieren

                OutlinedTextField(
                    value = birthYear,
                    onValueChange = { birthYear = it },
                    label = {
                        Text(
                            "Geburtsjahr",
                            color = Color.White, // Farbe des Labels
                            fontFamily = FlowerFontFamily, // Schriftart für das Label
                            fontSize = 16.sp // Größe des Labels
                        )
                    },
                    textStyle = TextStyle(
                        color = Color.White, // Textfarbe im Feld
                        fontSize = 16.sp, // Textgröße im Feld
                        fontFamily = FlowerFontFamily // Schriftart im Feld
                    ),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        textColor = Color.White,
                        backgroundColor = VibrantBlue2.copy(alpha = 0.5f), // Direkte Definition der Hintergrundfarbe mit Transparenz
                        cursorColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth(fraction = 0.6f) // Kontrolliert die Breite des Textfelds
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    // Weitere Eigenschaften...
                )

                Spacer(modifier = Modifier.height(8.dp)) // Verringern Sie diesen Wert, um den Abstand zu reduzieren

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp), // Stellt sicher, dass es ein Padding um die Column gibt
                        horizontalAlignment = Alignment.CenterHorizontally // Zentriert die Inhalte horizontal
                    ) {
                        OutlinedTextField(
                            value = gender,
                            onValueChange = { /* Nichts tun, da die Auswahl über das Dropdown-Menü erfolgt */ },
                            label = {
                                Text(
                                    "Geschlecht",
                                    color = Color.White,
                                    fontFamily = FlowerFontFamily,
                                    fontSize = 16.sp
                                )
                            },
                            textStyle = TextStyle(
                                color = Color.White,
                                fontSize = 16.sp,
                                fontFamily = FlowerFontFamily
                            ),
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    Icons.Filled.ArrowDropDown,
                                    "Dropdown-Icon",
                                    modifier = Modifier.clickable { expanded = !expanded }
                                )
                            },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                textColor = Color.White,
                                backgroundColor = VibrantBlue2.copy(alpha = 0.5f),
                                cursorColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth(fraction = 0.6f) // Breite des Textfelds kontrollieren
                                .padding(vertical = 8.dp) // Vertikaler Abstand
                        )

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier
                                .fillMaxWidth(fraction = 0.6f) // Kontrolliert die Breite des Dropdown-Menüs
                                .background(
                                    VibrantBlue2.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(4.dp)
                                ) // Hintergrundfarbe für das Dropdown-Menü
                        ) {
                            genderOptions.forEach { option ->
                                DropdownMenuItem(
                                    onClick = {
                                        gender = option
                                        expanded = false
                                    }
                                ) {
                                    Text(
                                        option,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }



                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter), // Dies richtet die innere Column unten im Box aus
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f)) // Dieser Spacer schiebt alles darunter an den unteren Rand
                        AnimatedButton(onClick = {
                            if (name.isNotEmpty() && birthYear.isNotEmpty() && gender.isNotEmpty()) {
                                startChatViewModel.saveUser(name, birthYear, gender)
                            } else {
                                // Warnung, dass alle Felder ausgefüllt werden müssen
                            }
                        }, text = stringResource(R.string.start_chat))

                        // Navigation zum Chat bei erfolgreicher Speicherung der Benutzerdaten
                        val isSaved = startChatViewModel.isUserDataSaved.collectAsState().value
                        if (isSaved) navigateToChat(name, birthYear, listOf(gender))

                    }
                        Spacer(modifier = Modifier.height(36.dp)) // Fügt am unteren Rand etwas Platz hinzu
                    }
                }
            }
        }
    }


