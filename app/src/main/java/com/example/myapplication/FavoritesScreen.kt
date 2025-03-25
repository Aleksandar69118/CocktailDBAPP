package com.example.myapplication

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage

@Composable
fun FavoritesScreen(
    viewModel: CocktailViewModel,
    onCocktailSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val favorites by remember { derivedStateOf { viewModel.favorites } }
    val state = rememberDragDropListState(
        items = favorites,
        onMove = { from, to -> viewModel.moveFavorite(from, to) }
    )
    LaunchedEffect(favorites) {
        state.resetState()
    }

    Scaffold(
        topBar = {
            CocktailTopBar(
                title = "Favorites",
                onBack = onBack,
                actions = {
                    if (favorites.isNotEmpty()) {
                        IconButton(
                            onClick = { viewModel.clear() },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Clear All"
                            )
                        }
                    }
                }
            )
        }
    ) {  padding ->
        if (favorites.isEmpty()) {
            EmptyFavoritesPlaceholder(onExploreClick = onBack)
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = favorites,
                    key = { it.idDrink }
                ) { cocktail ->
                    val index = favorites.indexOfFirst { it.idDrink == cocktail.idDrink }

                    if (index != -1) {
                        DragDropListItem(
                            state = state,
                            index = index,
                            item = cocktail
                        ) { isDragging ->
                            FavoriteCocktailCard(
                                cocktail = cocktail,
                                onRemove = { viewModel.toggleFavorite(cocktail) },
                                onClick = { onCocktailSelected(cocktail.idDrink) },
                                isDragging = isDragging
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesPlaceholder(onExploreClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "No Favorites Yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Start saving your favorite cocktails by tapping the heart icon in the details screen",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onExploreClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        ) {
            Text("Explore Cocktails")
        }
    }
}

@Composable
fun <T> DragDropListItem(
    state: DragDropListState<T>,
    index: Int,
    item: T,
    content: @Composable (Boolean) -> Unit
) {
    val currentPosition by remember { derivedStateOf { state.currentPosition } }
    val draggedIndex by remember { derivedStateOf { state.draggedIndex } }
    val isDragging = index == draggedIndex

    val offset = if (currentPosition == index && currentPosition != draggedIndex) 50.dp else 0.dp
    val verticalOffset by animateDpAsState(offset, label = "dragOffset")

    Box(
        modifier = Modifier
            .offset(y = verticalOffset)
            .draggable(
                orientation = androidx.compose.foundation.gestures.Orientation.Vertical,
                state = rememberDraggableState { delta ->
                    state.currentPosition = index
                },
                onDragStarted = {
                    state.draggedItem = item
                    state.draggedIndex = index
                },
                onDragStopped = {
                    state.draggedIndex?.let { dragged ->
                        state.currentPosition?.let { target ->
                            if (dragged != target) {
                                state.onMove(dragged, target)
                            }
                        }
                    }
                    state.draggedItem = null
                    state.draggedIndex = null
                    state.currentPosition = null
                }
            )
    ) {
        content(isDragging)
    }
}

fun <T> DragDropListState<T>.resetState() {
    draggedItem = null
    draggedIndex = null
    currentPosition = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteCocktailCard(
    cocktail: Cocktail,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    isDragging: Boolean
) {
    val elevation = animateDpAsState(
        targetValue = if (isDragging) 16.dp else 2.dp,
        label = "cardElevation"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .alpha(if (isDragging) 0.8f else 1f),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.value),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "Drag handle",
                modifier = Modifier
                    .size(24.dp)
                    .alpha(0.6f)
            )

            AsyncImage(
                model = cocktail.strDrinkThumb,
                contentDescription = "Cocktail image",
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = cocktail.strDrink,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = cocktail.strCategory,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Remove from favorites",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}