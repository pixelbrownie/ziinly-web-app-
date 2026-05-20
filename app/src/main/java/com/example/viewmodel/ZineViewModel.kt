package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ZineDatabase
import com.example.data.models.ZineCell
import com.example.data.models.ZineEntity
import com.example.data.repository.ZineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AestheticPreset(
    val name: String,
    val imageUrl: String,
    val description: String
)

class ZineViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: ZineRepository

    val allZines: StateFlow<List<ZineEntity>>
    val publicZines: StateFlow<List<ZineEntity>>

    // Editor States
    private val _editedZineId = MutableStateFlow<Int?>(null)
    val editedZineId = _editedZineId.asStateFlow()

    private val _zineTitle = MutableStateFlow("")
    val zineTitle = _zineTitle.asStateFlow()

    private val _zineDescription = MutableStateFlow("")
    val zineDescription = _zineDescription.asStateFlow()

    private val _zineThemeColor = MutableStateFlow("#F3B0C3")
    val zineThemeColor = _zineThemeColor.asStateFlow()

    private val _isZinePublic = MutableStateFlow(false)
    val isZinePublic = _isZinePublic.asStateFlow()

    private val _editorCells = MutableStateFlow<List<ZineCell>>(ZineEntity.createDefaultCells())
    val editorCells = _editorCells.asStateFlow()

    private val _selectedCellIndex = MutableStateFlow<Int?>(null)
    val selectedCellIndex = _selectedCellIndex.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    // Gorgeous direct Unsplash photo presets
    val photoPresets = listOf(
        AestheticPreset("Vinyl Record", "https://images.unsplash.com/photo-1539625319135-8d4f9c7be5cb?w=600", "Retro record player"),
        AestheticPreset("Retro Camera", "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=600", "Vintage film body"),
        AestheticPreset("Neon Street", "https://images.unsplash.com/photo-1563245372-f21724e3856d?w=600", "Tokyo midnight glow"),
        AestheticPreset("Coffee Mug", "https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=600", "Warm steam latte"),
        AestheticPreset("Concert Lights", "https://images.unsplash.com/photo-1506157786151-b8491531f063?w=600", "Front row energy"),
        AestheticPreset("Misty Pines", "https://images.unsplash.com/photo-1542718610-a1d656d1884c?w=600", "Cozy mountain forest"),
        AestheticPreset("Palm Breeze", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=600", "Golden coast sunrise"),
        AestheticPreset("Minimal Leaf", "https://images.unsplash.com/photo-1545241047-6083a3684587?w=600", "Fresh clean green")
    )

    // Warm colors for color palettes
    val colorPalette = listOf(
        "#FFF9E5", // Warm Cream
        "#F3B0C3", // Accent Pink
        "#FF9F1C", // Sunset Orange
        "#CBF3F0", // Soft Sage Mint
        "#2EC4B6", // Sky Turquoise
        "#E0B1CB", // Lavender Purple
        "#1A1A2E"  // Deep Charcoal
    )

    init {
        val database = ZineDatabase.getDatabase(application)
        repository = ZineRepository(database.zineDao())

        allZines = repository.allZines.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        publicZines = repository.publicZines.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        viewModelScope.launch {
            repository.checkAndSeedDatabase()
        }
    }

    fun selectCell(index: Int?) {
        _selectedCellIndex.value = index
    }

    fun startNewZine() {
        _editedZineId.value = null
        _zineTitle.value = "My Elite 8 Curation"
        _zineDescription.value = "Selected memories of my favorite weekend event."
        _zineThemeColor.value = "#F3B0C3"
        _isZinePublic.value = false
        _editorCells.value = ZineEntity.createDefaultCells()
        _selectedCellIndex.value = null
    }

    fun loadZineForEditing(zineId: Int) {
        viewModelScope.launch {
            val zine = repository.getZineByIdSync(zineId)
            if (zine != null) {
                _editedZineId.value = zine.id
                _zineTitle.value = zine.title
                _zineDescription.value = zine.description
                _zineThemeColor.value = zine.themeColorHex
                _isZinePublic.value = zine.isPublic
                _editorCells.value = zine.getCells()
                _selectedCellIndex.value = null
            }
        }
    }

    fun updateZineMetaData(title: String, description: String, themeColorHex: String, isPublic: Boolean) {
        _zineTitle.value = title
        _zineDescription.value = description
        _zineThemeColor.value = themeColorHex
        _isZinePublic.value = isPublic
    }

    fun updateZinePublicStatus(isPublic: Boolean) {
        _isZinePublic.value = isPublic
        saveCurrentZine()
    }

    fun updateSelectedCell(
        imagePath: String?,
        textOverlay: String,
        backgroundColorHex: String,
        textColorHex: String,
        fontSizeSp: Float,
        imageZoom: Float,
        focalX: Float,
        focalY: Float,
        isFlipped: Boolean
    ) {
        val index = _selectedCellIndex.value ?: return
        val currentList = _editorCells.value.toMutableList()
        val oldCell = currentList[index]
        currentList[index] = oldCell.copy(
            imagePath = imagePath,
            textOverlay = textOverlay,
            backgroundColorHex = backgroundColorHex,
            textColorHex = textColorHex,
            fontSizeSp = fontSizeSp,
            imageZoom = imageZoom,
            focalX = focalX,
            focalY = focalY,
            isFlipped = isFlipped
        )
        _editorCells.value = currentList
    }

    fun saveCurrentZine(onSuccess: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isSaving.value = true
            val titleText = _zineTitle.value.ifBlank { "Uncurated Zine" }
            val descText = _zineDescription.value.ifBlank { "Created on Ziinly" }
            val cellsJsonStr = ZineEntity.cellsToJson(_editorCells.value)

            val currentId = _editedZineId.value
            if (currentId == null) {
                // Insert new
                val newId = repository.insertZine(
                    ZineEntity(
                        title = titleText,
                        description = descText,
                        themeColorHex = _zineThemeColor.value,
                        isPublic = _isZinePublic.value,
                        cellsJson = cellsJsonStr
                    )
                )
                _editedZineId.value = newId.toInt()
            } else {
                // Update existing
                repository.updateZine(
                    ZineEntity(
                        id = currentId,
                        title = titleText,
                        description = descText,
                        themeColorHex = _zineThemeColor.value,
                        isPublic = _isZinePublic.value,
                        cellsJson = cellsJsonStr
                    )
                )
            }
            _isSaving.value = false
            onSuccess?.invoke()
        }
    }

    fun deleteZine(zine: ZineEntity) {
        viewModelScope.launch {
            repository.deleteZine(zine)
        }
    }
}
