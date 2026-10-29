package com.imashnake.animite.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.unit.sp
import com.animeta.app.R



/**
 * [Manrope](https://fonts.google.com/specimen/Manrope).
 * Bundled locally for instant, offline, zero-latency font loading.
 */
val manropeFontFamily = FontFamily(
    Font(R.font.manrope, FontWeight.ExtraBold),
    Font(R.font.manrope, FontWeight.Bold),
    Font(R.font.manrope, FontWeight.SemiBold),
    Font(R.font.manrope, FontWeight.Medium),
    Font(R.font.manrope, FontWeight.Normal),
    Font(R.font.manrope, FontWeight.Light),
    Font(R.font.manrope, FontWeight.ExtraLight),
    Font(R.font.manrope_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.manrope_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.manrope_extra_bold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.manrope_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.manrope_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.manrope_extra_light_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.manrope_semi_bold_italic, FontWeight.SemiBold, FontStyle.Italic),
)

/**
 * [Cairo](https://fonts.google.com/specimen/Cairo).
 * Bundled locally for instant, offline, zero-latency font loading.
 */
val cairoFontFamily = FontFamily(
    Font(R.font.cairo, FontWeight.ExtraBold),
    Font(R.font.cairo, FontWeight.Bold),
    Font(R.font.cairo, FontWeight.SemiBold),
    Font(R.font.cairo, FontWeight.Medium),
    Font(R.font.cairo, FontWeight.Normal),
    Font(R.font.cairo, FontWeight.Light),
    Font(R.font.cairo, FontWeight.ExtraLight),
)

const val BASELINE_SHIFT = 0.2f

fun createAnimiteTypography(
    fontFamily: FontFamily,
    baselineShift: Float = BASELINE_SHIFT,
    isArabic: Boolean = false,
) = Typography(
    // - [AnimeScreen, MangaScreen]: Media list headings.
    // - [MediaPage]: Media section headings.
    titleMedium = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        letterSpacing = if (isArabic) 0.sp else 0.2.sp,
        lineHeight = if (isArabic) 22.sp else 20.sp,
        baselineShift = BaselineShift(baselineShift)
    ),
    // - [AnimeScreen, MangaScreen]: Media list item labels.
    // - [MediaPage]: Character names.
    labelLarge = TextStyle(
        fontSize = 12.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight.SemiBold,
        lineHeight = if (isArabic) 20.sp else 18.sp,
        baselineShift = BaselineShift(baselineShift)
    ),
    // - [MediaPage]: Media title.
    titleLarge = TextStyle(
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        letterSpacing = if (isArabic) 0.sp else 0.2.sp,
        lineHeight = if (isArabic) 24.sp else 22.sp,
        baselineShift = BaselineShift(baselineShift)
    ),
    // - [MediaPage]: Media description.
    // - [SearchItem]: Search list item titles.
    // - [SearchItem]: Search list item studios.
    // - [SearchItem]: Search list item footer.
    bodyMedium = TextStyle(
        fontSize = 14.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        lineHeight = if (isArabic) 24.sp else 22.sp,
        baselineShift = BaselineShift(baselineShift)
    ),
    // - [MediaPage]: Stat label.
    // - [SearchItem]: Search list item season and year.
    labelSmall = TextStyle(
        fontSize = 12.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Medium,
        lineHeight = if (isArabic) 18.sp else 16.sp,
        baselineShift = BaselineShift(baselineShift)
    ),
    // - [MediaPage]: Stat score.
    displaySmall = TextStyle(
        fontSize = 24.sp,
        fontFamily = fontFamily,
        fontWeight = FontWeight.Bold,
        lineHeight = if (isArabic) 32.sp else 28.sp,
        baselineShift = BaselineShift(baselineShift)
    ),
)

val AnimiteTypography = createAnimiteTypography(manropeFontFamily, BASELINE_SHIFT, isArabic = false)
val CairoTypography = createAnimiteTypography(cairoFontFamily, 0.0f, isArabic = true)
