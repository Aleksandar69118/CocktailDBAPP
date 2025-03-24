package com.example.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope

@Composable
fun CocktailListScreen(
    viewModel: CocktailViewModel,
    onCocktailSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val cocktailList by viewModel.cocktailList.collectAsState()
    val searchQuery by viewModel.searchQuery
    val isSearching by viewModel.isSearching
    val errorMessage by viewModel.errorMessage
    val isLoading by viewModel.isLoading

    Column(modifier = Modifier.fillMaxSize()) {
        CocktailTopBar(title = "Search Cocktails", onBack = onBack) {
            IconButton(
                onClick = { viewModel.getRandomCocktail() },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh"
                )
            }
        }
        SearchBar(viewModel)

        if (isSearching) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        }

        if (cocktailList.isEmpty() && searchQuery.isEmpty()) {
            Text(
                text = "Start typing to search cocktails",
                modifier = Modifier.padding(16.dp)
            )
        }

        LazyColumn {
            items(cocktailList) { cocktail ->
                CocktailListItem(
                    cocktail = cocktail,
                    onClick = { onCocktailSelected(cocktail.idDrink) }
                )
            }
        }
    }
}

@Composable
fun SearchBar(viewModel: CocktailViewModel) {
    var debounceJob by remember { mutableStateOf<Job?>(null) }
    val coroutineScope = rememberCoroutineScope()

    TextField(
        value = viewModel.searchQuery.value,
        onValueChange = { query ->
            viewModel.onSearchQueryChanged(query)
            debounceJob?.cancel()
            debounceJob = coroutineScope.launch {
                delay(500)
                if (query.isNotEmpty()) {
                    viewModel.searchCocktails(query)
                }
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        placeholder = { Text("Search cocktails...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search icon") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                coroutineScope.launch {
                    viewModel.searchCocktails(viewModel.searchQuery.value)
                }
            }
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CocktailTopBar(
    title: String,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = actions
    )
}