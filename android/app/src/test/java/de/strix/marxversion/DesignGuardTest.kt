package de.strix.marxversion

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * 🛡️ design guard = ratchet (design_system.md § 11 · D-029): `ui/ds/` = th only place w raw colours, sizes, shapes, M3 btns
 * counts each pattern i src/main/java OUTSIDE ui/ds/ ⬄ baseline (src/test/resources/design_guard_baseline.txt)
 *   count > baseline ➡ ❌ (new raw value: use a ds token / component)
 *   count < baseline ➡ ❌ too, w th new nr (lower th baseline i th same commit ➡ it can nvr creep back up) · 0 at th end
 * exempt: Share.kt card bitmap · Fx.kt canvas · notify/ RemoteViews · Feast.kt gradients
 */
class DesignGuardTest {
    private val root = File("src/main/java/de/strix/marxversion")
    private val baselineFile = File("src/test/resources/design_guard_baseline.txt")
    private val exempt = listOf("ui/ds/", "ui/Share.kt", "ui/Fx.kt", "notify/", "logic/Feast.kt")

    private val patterns = linkedMapOf(
        "Color(0x" to Regex("""Color\(0x"""),
        ".sp" to Regex("""\.sp\b"""),
        "RoundedCornerShape(" to Regex("""RoundedCornerShape\("""),
        "Button(" to Regex("""(?<![A-Za-z])Button\("""),              // M3 Button (not IconButton / TextButton / CloseButton)
        "OutlinedButton(" to Regex("""OutlinedButton\("""),
        "TextButton(" to Regex("""TextButton\("""),
        "AssistChip(" to Regex("""AssistChip\("""),
        "FilterChip(" to Regex("""FilterChip\("""),
    )

    private fun counts(): Map<String, Int> {
        val files = root.walkTopDown().filter { f -> f.isFile && f.extension == "kt" }
            .filter { f -> exempt.none { f.relativeTo(root).invariantSeparatorsPath.startsWith(it) } }.toList()
        assertTrue("no sources found at ${root.absolutePath}", files.isNotEmpty())
        val code = files.joinToString("\n") { f -> f.readLines().filterNot { it.trimStart().startsWith("import ") }.joinToString("\n") }
        return patterns.mapValues { (_, re) -> re.findAll(code).count() }
    }

    private fun baseline(): Map<String, Int> = baselineFile.readLines()
        .map { it.substringBefore("#").trim() }.filter { it.isNotEmpty() }
        .associate { it.substringBeforeLast("=").trim() to it.substringAfterLast("=").trim().toInt() }

    @Test fun ratchet() {
        val now = counts()
        val base = baseline()
        val report = now.entries.joinToString("\n") { (k, n) -> "  %-22s %4d  (baseline %s)".format(k, n, base[k] ?: "–") }
        println("🛡️ design guard (outside ui/ds/):\n$report")
        val up = now.filter { (k, n) -> n > (base[k] ?: 0) }
        val down = now.filter { (k, n) -> base[k] != null && n < base[k]!! }
        assertTrue("🛡️ raw design values went UP (use ui/ds tokens / components):\n" +
            up.entries.joinToString("\n") { (k, n) -> "  $k: $n > ${base[k] ?: 0}" }, up.isEmpty())
        assertTrue("🛡️ nice, fewer raw values ➡ lower th baseline i ${baselineFile.path}:\n" +
            down.entries.joinToString("\n") { (k, n) -> "  $k = $n" }, down.isEmpty())
    }
}
