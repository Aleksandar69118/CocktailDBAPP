package com.example.myapplication

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

data class CocktailResponse(val drinks: List<Cocktail>)

data class Cocktail(
    val idDrink: String,
    val strDrink: String,
    val strCategory: String,
    val strAlcoholic: String,
    val strGlass: String,
    val strInstructions: String,
    val strDrinkThumb: String,
    val strIngredient1: String?,
    val strIngredient2: String?,
    val strIngredient3: String?,
    val strIngredient4: String?,
    val strIngredient5: String?,
    val strIngredient6: String?,
    val strIngredient7: String?,
    val strIngredient8: String?
)

interface CocktailApiService {
    @GET("search.php")
    suspend fun searchCocktails(@Query("s") query: String): CocktailResponse

    @GET("lookup.php")
    suspend fun getCocktailDetails(@Query("i") id: String): CocktailResponse

    @GET("random.php")
    suspend fun getRandomCocktail(): CocktailResponse
}

object RetrofitInstance {
    private const val BASE_URL = "https://www.thecocktaildb.com/api/json/v1/1/"

    val api: CocktailApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CocktailApiService::class.java)
    }
}

class CocktailViewModel : ViewModel() {
    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _cocktailList = MutableStateFlow<List<Cocktail>>(emptyList())
    val cocktailList: StateFlow<List<Cocktail>> = _cocktailList.asStateFlow()

    private val _selectedCocktail = mutableStateOf<Cocktail?>(null)
    val selectedCocktail: State<Cocktail?> = _selectedCocktail

    private val _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _isSearching = mutableStateOf(false)
    val isSearching: State<Boolean> = _isSearching

    private val _errorMessage = mutableStateOf<String?>(null)
    val errorMessage: State<String?> = _errorMessage

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun getRandomCocktail() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitInstance.api.getRandomCocktail()
                _selectedCocktail.value = response.drinks.firstOrNull()
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Failed to get random cocktail: ${e.localizedMessage}"
                _selectedCocktail.value = null
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Aggiungi logica per i preferiti
    private val _favorites = mutableStateOf<List<Cocktail>>(emptyList())
    val favorites: State<List<Cocktail>> = _favorites

    fun addToFavorites(cocktail: Cocktail) {
        _favorites.value = _favorites.value + cocktail
    }

    fun getCocktailDetails(id: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val response = RetrofitInstance.api.getCocktailDetails(id)
                _selectedCocktail.value = response.drinks.firstOrNull()
            } catch (e: Exception) {
                _errorMessage.value = "Error loading details: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchCocktails(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                val response = RetrofitInstance.api.searchCocktails(query)
                if (response.drinks.isNullOrEmpty()) {
                    _errorMessage.value = "No cocktails found"
                    _cocktailList.value = emptyList()
                } else {
                    _cocktailList.value = response.drinks
                    _errorMessage.value = null
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error: ${e.localizedMessage}"
                _cocktailList.value = emptyList()
            } finally {
                _isSearching.value = false
            }
        }
    }
}