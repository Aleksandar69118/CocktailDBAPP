package com.example.myapplication

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import coil.compose.AsyncImage
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

@Composable
fun CocktailDetailScreen(
    viewModel: CocktailViewModel,
    cocktailId: String,
    onBack: () -> Boolean
) {
    val cocktail by viewModel.selectedCocktail

    LaunchedEffect(cocktailId) {
        viewModel.getCocktailDetails(cocktailId)
    }

    cocktail?.let {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            AsyncImage(
                model = it.strDrinkThumb,
                contentDescription = null,
                modifier = Modifier
                    .height(200.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = it.strDrink, style = MaterialTheme.typography.headlineMedium)
            Text(text = it.strCategory, style = MaterialTheme.typography.titleMedium)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Ingredients:", style = MaterialTheme.typography.titleMedium)
            IngredientsList(cocktail = it)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Instructions:", style = MaterialTheme.typography.titleMedium)
            Text(text = it.strInstructions)
        }
    }
}

@Composable
fun IngredientsList(cocktail: Cocktail) {
    val ingredients = listOfNotNull(
        cocktail.strIngredient1,
        cocktail.strIngredient2,
        cocktail.strIngredient3,
        cocktail.strIngredient4,
        cocktail.strIngredient5,
        cocktail.strIngredient6,
        cocktail.strIngredient7,
        cocktail.strIngredient8,
    )

    Column {
        ingredients.forEach { ingredient ->
            Text(text = "- $ingredient")
        }
    }
}