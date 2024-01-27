package com.aikonia.app.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.aikonia.app.R


val FlowerFontFamily = FontFamily(Font(R.font.indieflower))

val Typography = Typography(
    body1 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        color = TextColor // Ersetzen Sie TextColor durch die normale Schriftfarbe
    ),

    caption = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.W100,
        textAlign = TextAlign.Center
    ),

    h1 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp
    ),

    h2 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp
    ),

    h3 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),

    h4 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),

    h5 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    h6 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
)


val TypographyDark = Typography(
    body1 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.W600,
        fontSize = 16.sp,
        lineHeight = 25.sp,
        color = TextColorDark
    ),
    caption = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.W100,
        textAlign = TextAlign.Center
    ),

    h1 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp
    ),

    h2 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp
    ),

    h3 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 24.sp
    ),

    h4 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),

    h5 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    h6 = TextStyle(
        fontFamily = FlowerFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
)
