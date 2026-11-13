package de.strix.marxversion.ui

import android.text.format.DateUtils
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.f

/**
 * 💬 comments on 1 book page (bottom sheet) · Matrix room o th chapter, filtered 2 this MEW page.
 * No account yet ➡ 1st thing = "how do u want 2 be called?" ➡ th account is made w th 1st comment.
 * Hold ur own comment = delete.
 */
/** 👤 th public name label · QA r5: just "Name" (r3 was "Lesesaal-Name", D-016) */
@Composable
fun nameLabel() = tr("Name", "Name")

/** 👤 "du schreibst als Rosa 1871 · ändern: ⚙️" — th 1 name field lives i Settings (QA r5 #8) */
@Composable
fun WritesAs(name: String) = Text(tr("👤 du schreibst als %s · ändern: ⚙️ Einstellungen", "👤 you write as %s · change: ⚙️ Settings").f(name),
    fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

/** 🎲 default name: a socialist thinker's 1st name + a year o th movement (no Lenin/Stalin/Mao — D-014: lens, not regime) */
fun comradeName(rnd: kotlin.random.Random = kotlin.random.Random): String =
    listOf("Rosa", "Clara", "Jenny", "Eleanor", "Friedrich", "Antonio", "Alexandra", "Walter", "Bertolt", "Angela", "Ernst", "Louise", "Paul", "Flora")
        .random(rnd) + " " + listOf(1848, 1867, 1871, 1918, 1968).random(rnd)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun CommentsSheet(vm: AppVm, kap: String, page: Int?, onDismiss: () -> Unit) {
    val all by vm.comments.collectAsStateWithLifecycle()
    val name by vm.mxName.collectAsStateWithLifecycle()
    val me by produceState<String?>(null, name) { value = vm.myUserId() }
    val list = all[kap].orEmpty().filter { it.page == page }
    var draft by remember { mutableStateOf("") }
    val newName = vm.suggestedName
    var busy by remember { mutableStateOf(false) }
    var failed by remember { mutableStateOf(false) }
    LaunchedEffect(kap) { vm.loadComments(kap) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp).imePadding().navigationBarsPadding()) {
            // ☭ close top-right, like evy window (MarxDialog) @1107
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(tr("💬 Seite %s", "💬 Page %s").f(page?.toString() ?: "—") + "  ·  " + tr("Kap. %s", "Ch. %s").f(kap),
                    fontWeight = FontWeight.Bold, fontSize = 17.sp, modifier = Modifier.weight(1f))
                CloseButton(onDismiss)
            }
            Text(tr("Was denkst du? Frag, widersprich, erzähl aus deinem Alltag.", "What do you think? Ask, disagree, tell from your own life."),
                fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (list.isEmpty()) item {
                    Text(tr("🌱 Noch nichts hier. Sei die/der Erste!", "🌱 Nothing here yet. Be the first!"),
                        color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(vertical = 16.dp))
                }
                items(list, key = { it.id }) { c ->
                    val mine = c.sender == me
                    Column(Modifier.fillMaxWidth()
                        .background(if (mine) Gold.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(10.dp))
                        .combinedClickable(onClick = {}, onLongClick = { if (mine) vm.deleteComment(kap, c.id) })
                        .padding(10.dp)) {
                        Row {
                            Text(c.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                            Text(DateUtils.getRelativeTimeSpanString(c.ts).toString(), fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                        Text(c.text, fontSize = 15.sp)
                    }
                }
            }
            WritesAs(name ?: newName)   // QA r5 (#8): name field only i ⚙️ · no account yet = th prefilled comrade name
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(draft, { draft = it }, modifier = Modifier.weight(1f).padding(vertical = 6.dp), minLines = 1, maxLines = 4,
                    placeholder = { Text(tr("Kommentar …", "Comment …")) },
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
                val canSend = draft.isNotBlank() && (name != null || newName.isNotBlank()) && !busy
                if (busy) CircularProgressIndicator(Modifier.padding(start = 8.dp))
                else Button(onClick = {
                    busy = true; failed = false
                    vm.postComment(kap, page, draft, name ?: newName) { ok -> busy = false; if (ok) draft = "" else failed = true }
                }, enabled = canSend, modifier = Modifier.padding(start = 8.dp)) { Text(tr("Senden", "Send")) }  // ☭ = close only (2× ☭ was confusing @1107)
            }
            if (failed) Text(tr("⚠️ Ging nicht — Internet? Nochmal versuchen.", "⚠️ Didn't work — internet? Try again."), color = Red, fontSize = 12.sp)
            Text(tr("Eigenen Kommentar lange drücken = löschen · öffentlich für alle Mitlesenden", "Hold your own comment = delete · public to everyone reading along"),
                fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(bottom = 8.dp))
        }
    }
}
