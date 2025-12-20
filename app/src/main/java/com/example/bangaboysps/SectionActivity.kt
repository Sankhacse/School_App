package com.example.bangaboysps

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

// -------------------------------------------------
// MODEL
// -------------------------------------------------
data class CsvItem(
    val timestamp: String,
    val title: String = "",
    val className: String = "",
    val date: String = "",
    val url: String = "",
    val kind: String = ""
)

// -------------------------------------------------
// NETWORK
// -------------------------------------------------
suspend fun fetchCsvText(url: String): String = withContext(Dispatchers.IO) {
    URL(url).readText(Charsets.UTF_8)
}

// -------------------------------------------------
// CSV PARSER
// -------------------------------------------------
fun parseCsvSafe(csv: String): List<List<String>> {
    val rows = mutableListOf<MutableList<String>>()
    var row = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false

    var i = 0
    while (i < csv.length) {
        val c = csv[i]
        when (c) {
            '"' -> {
                if (i + 1 < csv.length && csv[i + 1] == '"') {
                    sb.append('"')
                    i++
                } else inQuotes = !inQuotes
            }
            ',' -> if (!inQuotes) {
                row.add(sb.toString().trim())
                sb.clear()
            } else sb.append(c)
            '\n', '\r' -> {
                if (!inQuotes) {
                    row.add(sb.toString().trim())
                    sb.clear()
                    rows.add(row)
                    row = mutableListOf()
                    if (c == '\r' && i + 1 < csv.length && csv[i + 1] == '\n') i++
                } else sb.append(c)
            }
            else -> sb.append(c)
        }
        i++
    }

    if (sb.isNotEmpty() || row.isNotEmpty()) {
        row.add(sb.toString().trim())
        rows.add(row)
    }

    return rows.map { r -> r.map { it.trim('"') } }
}

// -------------------------------------------------
// DRIVE LINK FIXERS
// -------------------------------------------------
fun fixDriveLink(raw: String): String {
    val url = raw.trim()
    return when {
        url.contains("open?id=") -> {
            val id = url.substringAfter("open?id=").substringBefore("&")
            "https://drive.google.com/file/d/$id/view?usp=drivesdk"
        }
        url.contains("/file/d/") -> {
            val id = url.substringAfter("/file/d/").substringBefore("/")
            "https://drive.google.com/file/d/$id/view?usp=drivesdk"
        }
        else -> url
    }
}

// ✅ ONLY for images (direct render)
fun fixDriveImageLink(raw: String): String {
    val url = raw.trim()
    val id = when {
        url.contains("open?id=") -> url.substringAfter("open?id=").substringBefore("&")
        url.contains("/file/d/") -> url.substringAfter("/file/d/").substringBefore("/")
        else -> ""
    }
    return if (id.isNotEmpty())
        "https://drive.google.com/uc?export=view&id=$id"
    else url
}

// -------------------------------------------------
// TIMESTAMP PARSER
// -------------------------------------------------
fun parseTimestamp(raw: String): Long {
    val formats = listOf(
        "M/d/yyyy H:mm:ss",
        "M/d/yyyy h:mm:ss a",
        "MM/dd/yyyy H:mm:ss",
        "MM/dd/yyyy h:mm:ss a",
        "d/M/yyyy H:mm:ss",
        "d/M/yyyy h:mm:ss a",
        "dd/MM/yyyy H:mm:ss",
        "dd/MM/yyyy h:mm:ss a"
    )

    for (fmt in formats) {
        try {
            val sdf = SimpleDateFormat(fmt, Locale.getDefault())
            sdf.isLenient = false
            val date = sdf.parse(raw)
            if (date != null) return date.time
        } catch (_: Exception) {}
    }
    return 0L
}

// -------------------------------------------------
// DATE FIX
// -------------------------------------------------
fun fixDate(raw: String): String {
    val s = raw.trim()
    if (s.isEmpty()) return s

    val fmts = listOf("M/d/yyyy", "MM/dd/yyyy", "yyyy-MM-dd")
    for (f in fmts) {
        try {
            val sdf = SimpleDateFormat(f, Locale.getDefault())
            val d = sdf.parse(s)
            if (d != null) {
                return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(d)
            }
        } catch (_: Exception) {}
    }
    return s
}

// -------------------------------------------------
// ACTIVITY
// -------------------------------------------------
class SectionActivity : ComponentActivity() {

    companion object {
        const val IMAGE_CSV =
            "https://docs.google.com/spreadsheets/d/19t6NEDUWEyL9iImJpvjGyWMbVS_cw5vZ3DmdnIa4LgY/export?format=csv&gid=491397586"
        const val VIDEO_CSV =
            "https://docs.google.com/spreadsheets/d/19t6NEDUWEyL9iImJpvjGyWMbVS_cw5vZ3DmdnIa4LgY/export?format=csv&gid=1942431404"
        const val HOMEWORK_CSV =
            "https://docs.google.com/spreadsheets/d/19t6NEDUWEyL9iImJpvjGyWMbVS_cw5vZ3DmdnIa4LgY/export?format=csv&gid=561711561"
        const val MARKSHEET_CSV =
            "https://docs.google.com/spreadsheets/d/19t6NEDUWEyL9iImJpvjGyWMbVS_cw5vZ3DmdnIa4LgY/export?format=csv&gid=248471653"
        const val ANNOUNCEMENT_CSV =
            "https://docs.google.com/spreadsheets/d/19t6NEDUWEyL9iImJpvjGyWMbVS_cw5vZ3DmdnIa4LgY/export?format=csv&gid=1020539710"
        const val MAGAZINE_CSV =
            "https://docs.google.com/spreadsheets/d/19t6NEDUWEyL9iImJpvjGyWMbVS_cw5vZ3DmdnIa4LgY/export?format=csv&gid=1250438507"
        const val HOLIDAY_CSV =
            "https://docs.google.com/spreadsheets/d/19t6NEDUWEyL9iImJpvjGyWMbVS_cw5vZ3DmdnIa4LgY/export?format=csv&gid=879057643"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sec = intent.getStringExtra("sectionType") ?: "images"

        val url = when (sec) {
            "images" -> IMAGE_CSV
            "videos" -> VIDEO_CSV
            "announcement" -> ANNOUNCEMENT_CSV
            "magazine" -> MAGAZINE_CSV
            "holiday" -> HOLIDAY_CSV
            in listOf(
                "homework_pp", "homework_class1", "homework_class2",
                "homework_class3", "homework_class4", "homework_class5"
            ) -> HOMEWORK_CSV
            in listOf(
                "marksheet_pp", "marksheet_class1", "marksheet_class2",
                "marksheet_class3", "marksheet_class4", "marksheet_class5"
            ) -> MARKSHEET_CSV
            else -> IMAGE_CSV
        }

        setContent { MaterialTheme { SectionScreen(sec, url) } }
    }

    private fun classNameOf(sec: String) = when (sec) {
        "homework_pp", "marksheet_pp" -> "PP"
        "homework_class1", "marksheet_class1" -> "1"
        "homework_class2", "marksheet_class2" -> "2"
        "homework_class3", "marksheet_class3" -> "3"
        "homework_class4", "marksheet_class4" -> "4"
        "homework_class5", "marksheet_class5" -> "5"
        else -> ""
    }

    // -------------------------------------------------
    // MAIN SCREEN
    // -------------------------------------------------
    @Composable
    fun SectionScreen(type: String, url: String) {

        val skyBlue = Color(0xFFB3E5FC)

        var loading by remember { mutableStateOf(true) }
        var error by remember { mutableStateOf<String?>(null) }
        var list by remember { mutableStateOf<List<CsvItem>>(emptyList()) }

        LaunchedEffect(url, type) {
            try {
                loading = true

                val raw = fetchCsvText(url)
                val rows = parseCsvSafe(raw)
                val data = if (rows.size > 1) rows.drop(1) else emptyList()

                list = when {

                    type == "announcement" ->
                        data.map { CsvItem(it[0], it[1], kind = "announcement") }

                    type.startsWith("homework") -> {
                        val cls = classNameOf(type)
                        data.mapNotNull {
                            if (it[2] != cls) null
                            else CsvItem(it[0], it[3], it[2], fixDate(it[1]), kind = "homework")
                        }
                    }

                    type == "magazine" ->
                        data.map { CsvItem(it[0], it[1], url = fixDriveLink(it[2]), kind = "magazine") }

                    type == "holiday" ->
                        data.map { CsvItem(it[0], it[1], url = fixDriveLink(it[2]), kind = "holiday") }

                    type.startsWith("marksheet") -> {
                        val cls = classNameOf(type)
                        data.mapNotNull {
                            if (it[2] != cls) null
                            else CsvItem(it[0], it[1], it[2], url = fixDriveLink(it[3]), kind = "marksheet")
                        }
                    }

                    // 🔴 ONLY IMAGE LOGIC CHANGED
                    type == "images" ->
                        data.map {
                            CsvItem(it[0], url = fixDriveImageLink(it[1]), kind = "image")
                        }

                    else ->
                        data.map { CsvItem(it[0], it[1], url = fixDriveLink(it[2]), kind = "file") }
                }

                list = list.sortedByDescending { parseTimestamp(it.timestamp) }

            } catch (e: Exception) {
                error = e.message
            } finally {
                loading = false
            }
        }

        Box(
            Modifier.fillMaxSize().background(skyBlue).padding(16.dp)
        ) {
            when {
                loading -> LoadingBox()
                error != null -> ErrorBox(error!!)
                else -> ResultList(list)
            }
        }
    }

    // -------------------------------------------------
    // UI
    // -------------------------------------------------
    @Composable
    fun LoadingBox() {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(10.dp))
            Text("Loading…")
        }
    }

    @Composable
    fun ErrorBox(msg: String) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Error: $msg", color = Color.Red)
        }
    }

    @Composable
    fun ResultList(list: List<CsvItem>) {
        val ctx = LocalContext.current

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list) { item ->
                when (item.kind) {

                    "announcement" -> TextCard(item.title)

                    "homework" -> HomeworkCard(item)

                    "image" -> ImageCard(item.url) {
                        ctx.startActivity(
                            Intent(Intent.ACTION_VIEW, item.url.toUri())
                        )
                    }

                    "magazine", "holiday", "marksheet", "file" ->
                        FileCard(item) {
                            ctx.startActivity(
                                Intent(Intent.ACTION_VIEW, it.toUri())
                            )
                        }
                }
            }
        }
    }

    @Composable
    fun TextCard(text: String) {
        Card(colors = CardDefaults.cardColors(Color.White)) {
            Column(Modifier.padding(14.dp)) {
                Text(text)
            }
        }
    }

    @Composable
    fun HomeworkCard(item: CsvItem) {
        Card(colors = CardDefaults.cardColors(Color.White)) {
            Column(Modifier.padding(14.dp)) {
                Text(item.date)
                Spacer(Modifier.height(6.dp))
                Text(item.title)
            }
        }
    }

    @Composable
    fun FileCard(item: CsvItem, open: (String) -> Unit) {
        Card(
            modifier = Modifier.clickable { open(item.url) },
            colors = CardDefaults.cardColors(Color.White)
        ) {
            Column(Modifier.padding(14.dp)) {
                Text(item.title.ifBlank { "File" })
                if (item.className.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text("Class: ${item.className}", color = Color.Gray)
                }
            }
        }
    }
}

// -------------------------------------------------
// IMAGE CARD (VISIBLE IMAGE, NOT WHITE BOX)
// -------------------------------------------------
@Composable
fun ImageCard(url: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(Color.White)
    ) {
        AsyncImage(
            model = url,
            contentDescription = "Image",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
