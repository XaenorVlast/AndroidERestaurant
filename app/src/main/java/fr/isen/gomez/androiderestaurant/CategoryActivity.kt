package fr.isen.gomez.androiderestaurant

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import fr.isen.gomez.androiderestaurant.ui.theme.AndroidERestaurantTheme
import org.json.JSONObject
import coil.compose.rememberImagePainter



class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categoryName = intent.getStringExtra("categoryName") ?: "Catégorie"

        setContent {
            val isLoading = remember { mutableStateOf(true) } // Ajout de l'indicateur de chargement
            val menuItems = remember { mutableStateOf<List<MenuItem>>(listOf()) }
            val context = LocalContext.current // Obtenez le contexte pour l'utiliser dans la navigation

            AndroidERestaurantTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    if (isLoading.value) {
                        CircularProgressIndicator(modifier = Modifier.padding(16.dp))
                    } else {
                        // Ajoutez ici la gestion du clic en passant une fonction lambda à MenuScreen
                        MenuScreen(categoryName = categoryName, items = menuItems.value, onClick = { menuItem ->
                            // Créez et démarrez l'intent ici
                            val intent = Intent(context, DishDetailActivity::class.java).apply {
                                val gson = Gson()
                                val menuItemJson = gson.toJson(menuItem)
                                putExtra("menuItem", menuItemJson)
                            }
                            context.startActivity(intent)
                        })
                    }
                }
            }

            // Appel asynchrone pour récupérer les items du menu
            fetchMenuItems(categoryName) { items ->
                menuItems.value = items
                isLoading.value = false // Mise à jour de l'état de chargement
            }
        }
    }


    private fun fetchMenuItems(categoryName: String, onResult: (List<MenuItem>) -> Unit) {
        val queue = Volley.newRequestQueue(this)
        val url = "http://test.api.catering.bluecodegames.com/menu"
        val params = JSONObject()
        params.put("id_shop", "1")

        val jsonObjectRequest = JsonObjectRequest(Request.Method.POST, url, params,
            { response ->
                Log.d("CategoryActivity", "Réponse de l'API: $response")
                try {
                    val gson = Gson()
                    val menuResponse = gson.fromJson(response.toString(), MenuResponse::class.java)
                    val filteredItems =
                        menuResponse.data.firstOrNull { it.name_fr == categoryName }?.items
                            ?: emptyList()
                    onResult(filteredItems) // Utilisez onResult pour passer les items filtrés
                } catch (e: Exception) {
                    Log.e("CategoryActivity", "Parsing error", e)
                    onResult(emptyList()) // En cas d'erreur, passez une liste vide
                }
            },
            { error ->
                error.printStackTrace()
                Log.e("CategoryActivity", "Volley error: ${error.message}")
                runOnUiThread {
                    Toast.makeText(this, "Failed to load data: ${error.message}", Toast.LENGTH_LONG).show()
                }
                onResult(emptyList()) // En cas d'erreur de réseau, passez aussi une liste vide
            })

        queue.add(jsonObjectRequest)
    }


}

@Composable
fun MenuScreen(categoryName: String, items: List<MenuItem>, onClick: (MenuItem) -> Unit) {
    Column {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), // Crée 2 colonnes
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp)
        ) {
            items(items) { item ->
                MenuItemComposable(item = item, onClick = onClick)
            }
        }
    }
}

@Composable
fun MenuItemComposable(item: MenuItem, onClick: (MenuItem) -> Unit) {
    Column(modifier = Modifier
        .padding(8.dp)
        .clickable { onClick(item) }) { // Ajoutez la gestion de clic ici
        if (item.images.isNotEmpty()) {
            ImageFromUrls(urls = item.images)
        } else {
            // Afficher une image par défaut si aucune image n'est disponible
        }
        Text(
            text = item.name_fr,
            modifier = Modifier.padding(top = 8.dp)
        )
        Text(
            text = item.getFirstPriceFormatted(),
            modifier = Modifier.padding(top = 4.dp)
        )
        // Vous pouvez ajouter d'autres détails sur l'item ici si nécessaire.
    }
}


@Composable
fun ImageFromUrls(urls: List<String>) {
    var currentUrlIndex by remember { mutableStateOf(0) }

    val painter = rememberImagePainter(
        data = urls.getOrNull(currentUrlIndex),
        builder = {
            crossfade(true)
            error(android.R.drawable.ic_dialog_alert) // Utilisez une icône d'erreur par défaut d'Android
            listener(
                onError = { _, throwable ->
                    Log.d("ImageFromUrls", "Erreur de chargement pour l'URL : ${urls.getOrNull(currentUrlIndex)} avec l'erreur: ${throwable.message}")
                    if (currentUrlIndex < urls.size - 1) {
                        // Essayez la prochaine URL si celle actuelle échoue
                        currentUrlIndex++
                    }
                }
            )
        }
    )

    Image(
        painter = painter,
        contentDescription = null,
        modifier = Modifier
            .aspectRatio(1f) // Assurez-vous que l'image est carrée
            .fillMaxWidth(), // Remplissez la largeur maximale disponible
        contentScale = ContentScale.Crop // Gère comment l'image doit être redimensionnée ou déplacée pour remplir les dimensions données.
    )
}



// Classes pour la réponse et les données
data class MenuResponse(
    val data: List<Category> // Assurez-vous que cela correspond au champ "data" du JSON
)

data class Category(
    val name_fr: String, // Nom français de la catégorie
    val items: List<MenuItem> // Liste des items dans cette catégorie
)

data class MenuItem(
    val id: String,
    val name_fr: String,
    val id_category: String,
    val categ_name_fr: String,
    val images: List<String>,
    val ingredients: List<Ingredient>,
    val prices: List<Price>
) {
    // Ajoutez cette fonction pour obtenir le premier prix disponible
    // Supposons que le prix soit stocké sous forme de chaîne de caractères et qu'il représente un prix en euros
    fun getFirstPriceFormatted(): String {
        return if (prices.isNotEmpty()) {
            "${prices.first().price}€"
        } else {
            "N/A"
        }
    }
}

data class Ingredient(
    val id: String, // Identifiant de l'ingrédient
    val id_shop: String, // Identifiant du magasin/shop
    val name_fr: String, // Nom français de l'ingrédient
    val create_date: String, // Date de création de l'ingrédient
    val update_date: String, // Date de mise à jour de l'ingrédient
    val id_pizza: String? // Identifiant de la pizza (si applicable, peut ne pas être présent pour tous les ingrédients, donc nullable)
)

data class Price(
    val id: String, // Identifiant du prix
    val id_pizza: String, // Identifiant de la pizza
    val id_size: String, // Identifiant de la taille
    val price: String, // Valeur du prix
    val create_date: String, // Date de création du prix
    val update_date: String, // Date de mise à jour du prix
    val size: String // Taille correspondante au prix
)

