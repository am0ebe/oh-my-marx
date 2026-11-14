package de.strix.marxversion.ui.ds

import androidx.compose.ui.graphics.Color

/**
 * 🎨 1 accent per plan (by list position, design_system.md § 2 plan accents) · Parteibuch-red · Insight-Timer dusk tones · night blue · factory green
 * job = stripe · emoji disc · Tag bg only — nvr txt (3.0-4.3 on white) · moved fr PlansScreen.kt (L3 @1114)
 */
val PlanAccents = listOf(
    Color(0xFFB3261E), Color(0xFF6A3093), Color(0xFF1E88E5), Color(0xFF00897B), Color(0xFFE07A2E),
    Color(0xFFD6577A), Color(0xFF9C7A12), Color(0xFF1F2F4F), Color(0xFF2E7D32),
)

fun planAccent(i: Int) = PlanAccents[Math.floorMod(i, PlanAccents.size)]
