package com.example.data.repository

import com.example.data.local.ZineDao
import com.example.data.models.ZineCell
import com.example.data.models.ZineEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class ZineRepository(private val zineDao: ZineDao) {
    val allZines: Flow<List<ZineEntity>> = zineDao.getAllZines()
    val publicZines: Flow<List<ZineEntity>> = zineDao.getPublicZines()

    fun getZineById(id: Int): Flow<ZineEntity?> = zineDao.getZineById(id)

    suspend fun getZineByIdSync(id: Int): ZineEntity? = zineDao.getZineByIdSync(id)

    suspend fun insertZine(zine: ZineEntity): Long = zineDao.insertZine(zine)

    suspend fun updateZine(zine: ZineEntity) = zineDao.updateZine(zine)

    suspend fun deleteZine(zine: ZineEntity) = zineDao.deleteZine(zine)

    suspend fun checkAndSeedDatabase() {
        val count = allZines.first().size
        if (count == 0) {
            // Seed sample zines with creative themes and standard colors
            seedSamples()
        }
    }

    private suspend fun seedSamples() {
        // Sample 1: The Concert Dump
        val concertCells = listOf(
            ZineCell(0, "Page 4", null, "🍕 Parking Lot Pre-Show Slices", "#FDE2E4", "#1A1A2E"),
            ZineCell(1, "Page 3", null, "🎟️ Best Seats in the House!", "#E2ECE9", "#1A1A2E"),
            ZineCell(2, "Page 2", null, "🎸 Opening Act Blew Us Away!", "#FFF1E6", "#1A1A2E"),
            ZineCell(3, "Page 1", null, "✨ Main Stage Core Memory unlocked", "#DFE7FD", "#1A1A2E"),
            ZineCell(4, "Page 5", null, "🤳 Quick Aesthetic Blur in Hallway", "#FFD166", "#1A1A2E"),
            ZineCell(5, "Page 6", null, "🎆 Confetti Rain on the Finale", "#C5D3E8", "#1A1A2E"),
            ZineCell(6, "Back", null, "See you next tour • Made via Ziinly", "#1A1A2E", "#FFF9E5"),
            ZineCell(7, "Cover", null, "🎸 REVERB: A Concert Dump", "#F3B0C3", "#1A1A2E")
        )
        zineDao.insertZine(
            ZineEntity(
                title = "REVERB 2026",
                description = "8 electric frames from the indie concert of the summer.",
                themeColorHex = "#F3B0C3",
                isPublic = true,
                cellsJson = ZineEntity.cellsToJson(concertCells)
            )
        )

        // Sample 2: Weekend Coffee & Books
        val coffeeCells = listOf(
            ZineCell(0, "Page 4", null, "📚 Dust Jackets & Bookstores", "#F1F5F9", "#1A1A2E"),
            ZineCell(1, "Page 3", null, "🌧️ Rainy Window Symphony", "#E2E8F0", "#1A1A2E"),
            ZineCell(2, "Page 2", null, "🔌 Freshly Ground Coffee Aroma", "#FEF3C7", "#1A1A2E"),
            ZineCell(3, "Page 1", null, "☕ First Sip of Hot Vanilla Latte", "#FDE68A", "#1A1A2E"),
            ZineCell(4, "Page 5", null, "🧣 Snuggled under a Wool Blanket", "#F1F5F9", "#1A1A2E"),
            ZineCell(5, "Page 6", null, "📝 Journaling random thoughts", "#E2E8F0", "#1A1A2E"),
            ZineCell(6, "Back", null, "Slow Sundays • Pocket Booklet", "#1A1A2E", "#FFF9E5"),
            ZineCell(7, "Cover", null, "☕ Slow Brew: Rain & Writing", "#FDE68A", "#1A1A2E")
        )
        zineDao.insertZine(
            ZineEntity(
                title = "Slow Sunday",
                description = "Warm mugs and dog-eared pages on a rainy afternoon.",
                themeColorHex = "#FDE68A",
                isPublic = false,
                cellsJson = ZineEntity.cellsToJson(coffeeCells)
            )
        )

        // Sample 3: Roadtrip Memories
        val tripCells = listOf(
            ZineCell(0, "Page 4", null, "🛣️ Empty Highway, Loud Radio", "#E0F2FE", "#1A1A2E"),
            ZineCell(1, "Page 3", null, "☕ Diner Pancakes, Extra Syrup", "#FEF5E7", "#1A1A2E"),
            ZineCell(2, "Page 2", null, "🏔️ First Glimpse of the Pines", "#D1FAE5", "#1A1A2E"),
            ZineCell(3, "Page 1", null, "🌲 Misty Forest Cabin Morning", "#A7F3D0", "#1A1A2E"),
            ZineCell(4, "Page 5", null, "🪵 Crackling Campfire Stories", "#FEE2E2", "#1A1A2E"),
            ZineCell(5, "Page 6", null, "🌌 Stargazing from the Truck Bed", "#312E81", "#FFF9E5"),
            ZineCell(6, "Back", null, "The pines call • Made in Ziinly", "#1A1A2E", "#FFF9E5"),
            ZineCell(7, "Cover", null, "🌲 Wanderlust: Cascade Range", "#A7F3D0", "#1A1A2E")
        )
        zineDao.insertZine(
            ZineEntity(
                title = "Cascade Pines",
                description = "Escaping the screen for 48 hours under the giant evergreens.",
                themeColorHex = "#A7F3D0",
                isPublic = true,
                cellsJson = ZineEntity.cellsToJson(tripCells)
            )
        )
    }
}
