package com.voxable.core_accessibility.semantics

import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.paneTitle
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

/**
 * TalkBack uyumlu erişilebilirlik semantik modifier'ıları.
 */

fun Modifier.accessibleButton(
    label: String,
    stateDesc: String? = null
): Modifier = this.semantics {
    contentDescription = label
    role = Role.Button
    if (stateDesc != null) {
        stateDescription = stateDesc
    }
}

fun Modifier.accessibleHeading(
    label: String
): Modifier = this.semantics {
    contentDescription = label
    heading()
}

fun Modifier.accessibleLiveRegion(
    label: String,
    polite: Boolean = true
): Modifier = this.semantics {
    contentDescription = label
    liveRegion = if (polite) LiveRegionMode.Polite else LiveRegionMode.Assertive
}

fun Modifier.accessiblePane(
    title: String
): Modifier = this.semantics {
    paneTitle = title
}

fun Modifier.accessibleImage(
    description: String
): Modifier = this.semantics {
    contentDescription = description
    role = Role.Image
}
