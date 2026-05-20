package com.example.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "zines")
data class ZineEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val createdAt: Long = System.currentTimeMillis(),
    val themeColorHex: String = "#F3B0C3", // Ziinly accent pink default
    val isPublic: Boolean = false,
    val cellsJson: String
) {
    // Safely deserialize cells from JSON
    fun getCells(): List<ZineCell> {
        val list = mutableListOf<ZineCell>()
        try {
            val arr = JSONArray(cellsJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    ZineCell(
                        positionIndex = obj.getInt("positionIndex"),
                        pageName = obj.getString("pageName"),
                        imagePath = if (obj.isNull("imagePath")) null else obj.getString("imagePath"),
                        textOverlay = obj.optString("textOverlay", ""),
                        backgroundColorHex = obj.optString("backgroundColorHex", "#FFF9E5"),
                        textColorHex = obj.optString("textColorHex", "#1A1A2E"),
                        fontSizeSp = obj.optDouble("fontSizeSp", 14.0).toFloat(),
                        imageZoom = obj.optDouble("imageZoom", 1.0).toFloat(),
                        focalX = obj.optDouble("focalX", 0.5).toFloat(),
                        focalY = obj.optDouble("focalY", 0.5).toFloat(),
                        isFlipped = obj.optBoolean("isFlipped", false)
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return createDefaultCells()
        }
        return list
    }

    companion object {
        fun cellsToJson(cells: List<ZineCell>): String {
            val arr = JSONArray()
            for (cell in cells) {
                val obj = JSONObject().apply {
                    put("positionIndex", cell.positionIndex)
                    put("pageName", cell.pageName)
                    if (cell.imagePath != null) {
                        put("imagePath", cell.imagePath)
                    } else {
                        put("imagePath", JSONObject.NULL)
                    }
                    put("textOverlay", cell.textOverlay)
                    put("backgroundColorHex", cell.backgroundColorHex)
                    put("textColorHex", cell.textColorHex)
                    put("fontSizeSp", cell.fontSizeSp.toDouble())
                    put("imageZoom", cell.imageZoom.toDouble())
                    put("focalX", cell.focalX.toDouble())
                    put("focalY", cell.focalY.toDouble())
                    put("isFlipped", cell.isFlipped)
                }
                arr.put(obj)
            }
            return arr.toString()
        }

        fun createDefaultCells(): List<ZineCell> {
            val names = listOf(
                "Page 4", "Page 3", "Page 2", "Page 1", // Row 0 (Rotated 180° in physical folding sheets)
                "Page 5", "Page 6", "Back", "Cover"      // Row 1 (Normal 0° alignment)
            )
            return List(8) { i ->
                ZineCell(
                    positionIndex = i,
                    pageName = names[i],
                    backgroundColorHex = if (i == 7) "#F3B0C3" else "#FFF9E5", // Cover is pink, others warm cream by default
                    textColorHex = "#1A1A2E"
                )
            }
        }
    }
}
