package com.voxable.core_ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * Mavi \u0131\u015f\u0131k filtresi overlay'i.
 * Ekran\u0131n \u00fczerine yar\u0131 saydam s\u0131cak renk tonu (amber) kaplama uygulayarak
 * mavi \u0131\u015f\u0131k emisyonunu azalt\u0131r.
 *
 * @param enabled Filtre a\u00e7\u0131k m\u0131
 * @param intensity Yo\u011funluk (0f..1f), 0 = hi\u00e7 etki yok, 1 = maksimum s\u0131cak ton
 * @param modifier Modifier
 */
@Composable
fun BlueLightFilter(
    enabled: Boolean,
    intensity: Float = 0.3f,
    modifier: Modifier = Modifier
) {
    val clampedIntensity = intensity.coerceIn(0f, 1f)

    val targetColor = if (enabled) {
        Color(
            red = 1f,
            green = 0.85f - (clampedIntensity * 0.25f),
            blue = 0.2f - (clampedIntensity * 0.15f),
            alpha = clampedIntensity * 0.35f
        )
    } else {
        Color.Transparent
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 500),
        label = "blue_light_filter_color"
    )

    if (animatedColor != Color.Transparent) {
        Canvas(
            modifier = modifier.fillMaxSize()
        ) {
            drawRect(color = animatedColor)
        }
    }
}
