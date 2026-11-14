package de.strix.marxversion.ui.ds

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.strix.marxversion.logic.CARD
import de.strix.marxversion.logic.StampCard

/**
 * 🎟️ Stempelkarte row — extracted fr TodayScreen's Hero (L0 @1114) so L1 (leaves Today) + L4 (lands i My Capital)
 * don't both edit it · look = unchanged: ☭ earned (gift stamps = same ☭) · red ! = today's stamp waiting · ○ to go
 * `ring` = colour o th empty slots (white on th hero gradient · `Ds.outline` on paper)
 */
@Composable
fun StampRow(card: StampCard, doneToday: Boolean, modifier: Modifier = Modifier, ring: Color = Ds.white.copy(alpha = 0.7f)) {
    Row(modifier, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        repeat(CARD) { i ->
            val waiting = !doneToday && i == card.filled
            Box(
                Modifier.size(24.dp).clip(CircleShape)
                    .then(when {
                        i < card.filled -> Modifier.background(Ds.gold)
                        waiting -> Modifier.background(Ds.red)
                        else -> Modifier.border(1.5.dp, ring, CircleShape)
                    }),
                contentAlignment = Alignment.Center,
            ) {
                when {
                    i < card.filled -> Text("☭", fontSize = 12.sp, color = Ds.red, fontWeight = FontWeight.Bold)
                    waiting -> Text("!", fontSize = 13.sp, color = Ds.white, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
