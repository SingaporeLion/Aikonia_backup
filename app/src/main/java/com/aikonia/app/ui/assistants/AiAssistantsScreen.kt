package com.aikonia.app.ui.assistants

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aikonia.app.common.Constants
import com.aikonia.app.common.components.AppBar
import com.aikonia.app.common.components.AssistantCard
import com.aikonia.app.common.components.ChipItem
import com.aikonia.app.data.model.AiAssistantModel
import com.aikonia.app.data.model.AiAssistantsModel
import com.aikonia.app.ui.theme.*
import  com.aikonia.app.R
import com.aikonia.app.ui.color.PastelAqua
import com.aikonia.app.ui.color.PastelBlue
import com.aikonia.app.ui.color.PastelCoral
import com.aikonia.app.ui.color.PastelGreen
import com.aikonia.app.ui.color.PastelLavender
import com.aikonia.app.ui.color.PastelLilac
import com.aikonia.app.ui.color.PastelOrange
import com.aikonia.app.ui.color.PastelPink
import com.aikonia.app.ui.color.PastelPurple
import com.aikonia.app.ui.color.PastelRed
import com.aikonia.app.ui.color.PastelTeal
import com.aikonia.app.ui.color.PastelYellow

