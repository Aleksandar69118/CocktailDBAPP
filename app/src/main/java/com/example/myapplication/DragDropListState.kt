package com.example.myapplication

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

class DragDropListState<T>(
    val items: List<T>,
    val onMove: (Int, Int) -> Unit
) {
    var draggedItem by mutableStateOf<T?>(null)
    var draggedIndex by mutableStateOf<Int?>(null)
    var currentPosition by mutableStateOf<Int?>(null)
}

@Composable
fun <T> rememberDragDropListState(
    items: List<T>,
    onMove: (Int, Int) -> Unit
): DragDropListState<T> = remember {
    DragDropListState(items = items, onMove = onMove)
}