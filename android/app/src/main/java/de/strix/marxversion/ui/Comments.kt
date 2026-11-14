package de.strix.marxversion.ui

import android.text.format.DateUtils
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.strix.marxversion.data.Matrix
import de.strix.marxversion.data.f
import de.strix.marxversion.ui.ds.Ds
import de.strix.marxversion.ui.ds.MeBubble
import de.strix.marxversion.ui.ds.OtherBubble
import de.strix.marxversion.ui.ds.Primary
import de.strix.marxversion.ui.ds.Space

/**
 * 💬 comments on 1 book page (bottom sheet) · Matrix room o th chapter, filtered 2 this MEW page.
 * No account yet ➡ 1st thing = "how do u want 2 be called?" ➡ th account is made w th 1st comment.
 * Hold ur own comment = delete.
 */
/** 👤 th public name label · QA r5: just "Name" (r3 was "Lesesaal-Name", D-016) */
@Composable
fun nameLabel() = tr("Name", "Name")

/** 👤 "du schreibst als Rosa 1871 · ändern: ⚙️" — th 1 name field lives i Settings (QA r5 #8) · label-s muted, above th composer */
@Composable
fun WritesAs(name: String) = Text(tr("👤 du schreibst als %s · ändern: ⚙️ Einstellungen", "👤 you write as %s · change: ⚙️ Settings").f(name),
    style = MaterialTheme.typography.labelSmall, color = Ds.inkMuted)

/** 🎲 default name: a socialist thinker's 1st name + a year o th movement (no Lenin/Stalin/Mao — D-014: lens, not regime) */
fun comradeName(rnd: kotlin.random.Random = kotlin.random.Random): String =
    listOf("Rosa", "Clara", "Jenny", "Eleanor", "Friedrich", "Antonio", "Alexandra", "Walter", "Bertolt", "Angela", "Ernst", "Louise", "Paul", "Flora")
        .random(rnd) + " " + listOf(1848, 1867, 1871, 1918, 1968).random(rnd)

@OptIn(ExperimentalMaterial3Api::class)
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

    // sheet = paperHi · r24 top · drag handle (M3 dflts via Theme roles + MarxShapes)
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Ds.paperHi) {
        CommentsPanel(
            page = page, list = list, me = me, writer = name ?: newName, draft = draft, onDraft = { draft = it },
            canSend = draft.isNotBlank() && (name != null || newName.isNotBlank()), busy = busy, failed = failed,
            onSend = {
                busy = true; failed = false
                vm.postComment(kap, page, draft, name ?: newName) { ok -> busy = false; if (ok) draft = "" else failed = true }
            },
            onDelete = { vm.deleteComment(kap, it) }, onClose = onDismiss,
        )
    }
}

/**
 * th sheet's content, stateless (comments shot renders it wo Matrix) · design_system.md § 7 comments sheet:
 * head title-m "Reading room · p. N" + ★ · hint body-m only while empty · others = OtherBubble (start) · own = blue MeBubble (end)
 * composer: "you write as X" label-s · field r12 + Primary-S "Send" · legal ➡ ⓘ ➡ dialog (was a 10sp wall)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentsPanel(
    page: Int?, list: List<Matrix.Comment>, me: String?, writer: String, draft: String, onDraft: (String) -> Unit,
    canSend: Boolean, busy: Boolean, failed: Boolean, onSend: () -> Unit, onDelete: (String) -> Unit, onClose: () -> Unit,
) {
    val typo = MaterialTheme.typography
    var legal by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxWidth().padding(horizontal = Space.gutter).imePadding().navigationBarsPadding()) {
        // ★ close top-end, like evy window (MarxDialog)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(tr("Lesesaal · S. %s", "Reading room · p. %s").f(page?.toString() ?: "—"), style = typo.titleMedium, color = Ds.ink,
                modifier = Modifier.weight(1f))
            InfoButton(tr("Hinweise zum Lesesaal", "About the reading room")) { legal = true }
            CloseButton(onClose)
        }
        if (list.isEmpty()) Text(tr("Was denkst du? Frag, widersprich, erzähl aus deinem Alltag.", "What do you think? Ask, disagree, tell from your own life."),
            style = typo.bodyMedium, color = Ds.inkMuted, modifier = Modifier.padding(top = Space.xs))
        LazyColumn(Modifier.fillMaxWidth().heightIn(max = 420.dp).padding(vertical = Space.m), verticalArrangement = Arrangement.spacedBy(Space.s)) {
            items(list, key = { it.id }) { c ->
                val mine = c.sender == me
                val time = DateUtils.getRelativeTimeSpanString(c.ts).toString()
                // hold ur own = delete · own = end-aligned blue ("me" voice, same as margin notes) · others = paperDeep bubble
                val hold = Modifier.combinedClickable(onClick = {}, onLongClick = { if (mine) onDelete(c.id) })
                Row(Modifier.fillMaxWidth(), horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start) {
                    if (mine) Spacer(Modifier.width(Space.xxxl))
                    if (mine) MeBubble(Modifier.weight(1f, fill = false).then(hold)) {
                        Text(time, style = typo.labelSmall, color = Ds.inkMuted)
                        Text(c.text)
                    } else OtherBubble(c.name, time, Modifier.weight(1f, fill = false).then(hold)) { Text(c.text) }
                    if (!mine) Spacer(Modifier.width(Space.xxxl))
                }
            }
        }
        WritesAs(writer)   // QA r5 (#8): name field only i ⚙️ · no account yet = th prefilled comrade name
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = Space.xs, bottom = Space.s)) {
            OutlinedTextField(draft, onDraft, modifier = Modifier.weight(1f), minLines = 1, maxLines = 4,
                shape = MaterialTheme.shapes.medium, textStyle = typo.bodyMedium,
                placeholder = { Text(tr("✍ Schreib eine Randnotiz …", "✍ Write a margin note …"), style = typo.bodyMedium) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Ds.blue, unfocusedBorderColor = Ds.outline, cursorColor = Ds.blue,
                    focusedContainerColor = Ds.paperHi, unfocusedContainerColor = Ds.paperHi,
                    focusedPlaceholderColor = Ds.inkMuted, unfocusedPlaceholderColor = Ds.inkMuted),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
            Primary(tr("Senden", "Send"), onSend, Modifier.padding(start = Space.s), large = false, enabled = canSend && !busy, loading = busy)
        }
        if (failed) Text(tr("⚠️ Ging nicht — Internet? Nochmal versuchen.", "⚠️ Didn't work — internet? Try again."),
            style = typo.bodyMedium, color = Ds.redDeep, modifier = Modifier.padding(bottom = Space.s))
    }
    if (legal) MarxDialog({ legal = false }, title = tr("Lesesaal", "Reading room")) {
        Text(tr("Öffentlich für alle Mitlesenden.", "Public to everyone reading along."), style = typo.bodyMedium, color = Ds.ink)
        Text(tr("Eigenen Kommentar lange drücken = löschen.", "Hold your own comment = delete."), style = typo.bodyMedium, color = Ds.ink,
            modifier = Modifier.padding(top = Space.s))
    }
}

/** ⓘ icon-only btn · 48 target · desc 4 TalkBack */
@Composable
private fun InfoButton(desc: String, onClick: () -> Unit) =
    Box(Modifier.size(Space.tap).clip(CircleShape).clickable(role = Role.Button, onClick = onClick).semantics { contentDescription = desc },
        contentAlignment = Alignment.Center) {
        Text("ⓘ", style = MaterialTheme.typography.titleMedium, color = Ds.inkMuted)
    }
