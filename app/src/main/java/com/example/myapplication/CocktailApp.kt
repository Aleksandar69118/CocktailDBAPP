package com.example.myapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue

@Composable
fun CocktailApp() {
    val navController = rememberNavController()
    val viewModel = viewModel<CocktailViewModel>()

    NavHost(
        navController = navController,
        startDestination = "mainMenu"
    ) {
        // Schermata Menu Principale
        composable("mainMenu") {
            MainMenuScreen(
                onSearchSelected = { navController.navigate("search") },
                onRandomSelected = { navController.navigate("random") },
                onFavoritesSelected = { navController.navigate("favorites") }
            )
        }

        // Schermata Ricerca
        composable("search") {
            CocktailListScreen(
                viewModel = viewModel,
                onCocktailSelected = { cocktailId ->
                    navController.navigate("cocktailDetail/$cocktailId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Schermata Cocktail Random
        composable("random") {
            RandomCocktailScreen(
                viewModel = viewModel,
                onCocktailSelected = { cocktailId ->
                    navController.navigate("cocktailDetail/$cocktailId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Schermata Preferiti (placeholder)
        composable("favorites") {
            FavoritesScreen(
                viewModel = viewModel,
                onCocktailSelected = { cocktailId ->
                    navController.navigate("cocktailDetail/$cocktailId")
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Schermata Dettaglio (esistente)
        composable(
            "cocktailDetail/{cocktailId}",
            arguments = listOf(navArgument("cocktailId") { type = NavType.StringType })
        ) { backStackEntry ->
            val cocktailId = backStackEntry.arguments?.getString("cocktailId") ?: ""
            CocktailDetailScreen(
                viewModel = viewModel,
                cocktailId = cocktailId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainMenuScreen(
    onSearchSelected: () -> Unit,
    onRandomSelected: () -> Unit,
    onFavoritesSelected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Cocktail App", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onSearchSelected,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Search Cocktails")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRandomSelected,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Random Cocktail")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onFavoritesSelected,
            modifier = Modifier.fillMaxWidth(0.8f)
        ) {
            Text("Favorites")
        }
    }
}

@Composable
fun RandomCocktailScreen(
    viewModel: CocktailViewModel,
    onCocktailSelected: (String) -> Unit,
    onBack: () -> Unit
) {
    val isLoading by viewModel.isLoading
    val errorMessage by viewModel.errorMessage
    val cocktail by viewModel.selectedCocktail

    LaunchedEffect(Unit) {
        viewModel.getRandomCocktail()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CocktailTopBar(
            title = "Random Cocktail",
            onBack = onBack,
            actions = {
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
        )

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            errorMessage != null -> {
                ErrorMessage(
                    message = errorMessage!!,
                    onRetry = { viewModel.getRandomCocktail() }
                )
            }

            cocktail != null -> {
                RandomCocktailContent(
                    cocktail = cocktail!!,
                    onTryAnother = { viewModel.getRandomCocktail() },
                    onCocktailSelected = onCocktailSelected
                )
            }
        }
    }
}

@Composable
private fun RandomCocktailContent(
    cocktail: Cocktail,
    onTryAnother: () -> Unit,
    onCocktailSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AsyncImage(
            model = cocktail.strDrinkThumb,
            contentDescription = "Cocktail image",
            modifier = Modifier
                .height(300.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = cocktail.strDrink,
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "${cocktail.strCategory} • ${cocktail.strAlcoholic}",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onTryAnother,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Try Another Random Cocktail")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Instructions:",
            style = MaterialTheme.typography.titleLarge
        )

        Text(
            text = cocktail.strInstructions,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Ingredients:",
            style = MaterialTheme.typography.titleLarge
        )

        IngredientsList(cocktail = cocktail)

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onCocktailSelected(cocktail.idDrink) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("View Full Details")
        }
    }
}

@Composable
fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onRetry) {
            Text("Retry")
        }
    }
}