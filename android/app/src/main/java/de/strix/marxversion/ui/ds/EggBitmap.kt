package de.strix.marxversion.ui.ds

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.withClip
import de.strix.marxversion.logic.Feast

/**
 * 🥚 D-066 egg as a round badge on a plain Canvas (quote card · 📱 widget = no Compose there)
 * = EggBadge's look: bg + fg, th 108 canvas cropped 2 its inner 72 (bounds 1.5× th circle), white ring
 */
fun drawEgg(ctx: Context, c: Canvas, feast: Feast, cx: Int, cy: Int, r: Int, ring: Float = r / 20f) {
    c.withClip(Path().apply { addCircle(cx.toFloat(), cy.toFloat(), r.toFloat(), Path.Direction.CW) }) {
        listOf(EggRes.bg(feast), EggRes.fg(feast)).forEach { id ->
            ContextCompat.getDrawable(ctx, id)?.apply { setBounds(cx - r * 3 / 2, cy - r * 3 / 2, cx + r * 3 / 2, cy + r * 3 / 2); draw(this@withClip) }
        }
    }
    if (ring > 0) c.drawCircle(cx.toFloat(), cy.toFloat(), r - ring / 2, Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = ring; color = 0xE6FFFFFF.toInt() })
}

/** square px × px bitmap o th badge (📱 widget: RemoteViews takes a Bitmap, not a layered vector) */
fun eggBitmap(ctx: Context, feast: Feast, px: Int): Bitmap =
    createBitmap(px, px).also { drawEgg(ctx, Canvas(it), feast, px / 2, px / 2, px / 2) }
