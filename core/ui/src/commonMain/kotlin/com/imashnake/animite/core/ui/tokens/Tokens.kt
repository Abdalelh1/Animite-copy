package com.imashnake.animite.core.ui.tokens

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

object RoleColors {
    fun owner(colors: ColorScheme): Color = colors.primary
    fun moderator(colors: ColorScheme): Color = colors.secondary
    fun premium(colors: ColorScheme): Color = colors.tertiary
}

object SemanticColors {
    fun anilistCardSurface(colors: ColorScheme): Color = colors.surfaceContainer
    fun statsCardSurface(colors: ColorScheme): Color = colors.surfaceContainer
}

object StatusColors {
    fun watching(colors: ColorScheme): Color = colors.primary
    fun completed(colors: ColorScheme): Color = colors.secondary
}
