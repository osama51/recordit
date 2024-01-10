package com.toddler.recordit.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.res.fontResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.toddler.recordit.R


val provider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = R.array.com_google_android_gms_fonts_certs
)

//val fontName = GoogleFont("Lobster Two")
//val fontName = GoogleFont("Fjalla One")
//val fontName = GoogleFont("Grandiflora One")
val fontName = GoogleFont("Rubik Doodle Triangles")

val fontFamily = FontFamily(
    Font(googleFont = fontName, fontProvider = provider)
)

val Russo = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.russo_one_local, FontWeight.Normal),
    androidx.compose.ui.text.font.Font(R.font.russo_one_local, FontWeight.Bold)
)

val Abel = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.abel, FontWeight.Normal),
    androidx.compose.ui.text.font.Font(R.font.abel, FontWeight.Bold)
)

val RobotoMedium = FontFamily(
    androidx.compose.ui.text.font.Font(R.font.roboto_medium, FontWeight.Normal),
    androidx.compose.ui.text.font.Font(R.font.roboto_medium, FontWeight.Bold)
)


// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),

    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)