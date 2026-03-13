package com.example.studypredict.history

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object HistoryStore {
    private val _records = MutableStateFlow<List<AnalysisRecord>>(emptyList())
    val records: StateFlow<List<AnalysisRecord>> = _records.asStateFlow()

    fun add(record: AnalysisRecord) {
        // on met le plus récent en premier
        _records.value = listOf(record) + _records.value
    }

    fun delete(id: String) {
        _records.value = _records.value.filterNot { it.id == id }
    }

    fun clear() {
        _records.value = emptyList()
    }
}