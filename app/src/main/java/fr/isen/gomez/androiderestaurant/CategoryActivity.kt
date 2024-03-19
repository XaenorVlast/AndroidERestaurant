package fr.isen.gomez.androiderestaurant

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import fr.isen.gomez.androiderestaurant.ui.theme.AndroidERestaurantTheme



class CategoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val categoryName = intent.getStringExtra("categoryName") ?: "Catégorie"
        val menuItems = mutableStateOf<List<MenuItem>>(listOf())

        fetchMenuItems(categoryName) {
            menuItems.value = it
        }

        setContent {
            AndroidERestaurantTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    MenuScreen(categoryName = categoryName, items = menuItems.value)
                }
            }
        }
    }

    private fun fetchMenuItems(categoryName: String, onResult: (List<MenuItem>) -> Unit) {
        val queue = Volley.newRequestQueue(this)
        val url = "http://test.api.catering.bluecodegames.com/menu"

        val stringRequest = object: StringRequest(Request.Method.POST, url,
            Response.Listener<String> { response ->
                val menuResponse = Gson().fromJson(response, MenuResponse::class.java)
                val filteredItems = menuResponse.data.firstOrNull { it.name == categoryName }?.items ?: emptyList()
                onResult(filteredItems)
            },
            Response.ErrorListener { error ->
                // Log de base avec le message d'erreur
                Log.e("CategoryActivity", "Erreur de la requête Volley: ${error.message}")

                // Détails supplémentaires sur l'erreur
                val statusCode = error.networkResponse?.statusCode ?: "Code d'état non disponible"
                val errorData = String(error.networkResponse?.data ?: "Données d'erreur non disponibles".toByteArray())

                // Log des détails supplémentaires
                Log.e("CategoryActivity", "Code d'état HTTP: $statusCode")
                Log.e("CategoryActivity", "Données d'erreur: $errorData")

                // Affichage d'un Toast plus détaillé
                runOnUiThread {
                    Toast.makeText(this@CategoryActivity, "Erreur lors du chargement des données: $statusCode - Voir log pour plus de détails", Toast.LENGTH_LONG).show()
                }

                    if (error.networkResponse?.statusCode == 400) {
                        Log.e("CategoryActivity", "Requête Incorrecte (Erreur 400). Message: ${error.message}")
                        runOnUiThread {
                            Toast.makeText(this@CategoryActivity, "Requête Incorrecte (Erreur 400)", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Log.e("CategoryActivity", "Erreur lors du chargement des données: ${error.message}")
                        runOnUiThread {
                            Toast.makeText(this@CategoryActivity, "Erreur lors du chargement des données", Toast.LENGTH_LONG).show()
                        }
                    }


            }

        ) {
            override fun getParams(): Map<String, String> = hashMapOf("id_shop" to "1")
        }

        queue.add(stringRequest)
    }
}

@Composable
fun MenuScreen(categoryName: String, items: List<MenuItem>) {
    Column {
        Text(
            text = categoryName,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn {
            items(items) { item ->
                MenuItemComposable(item)
            }
        }
    }
}

@Composable
fun MenuItemComposable(item: MenuItem) {
    Text(
        text = item.name,
        modifier = Modifier
            .padding(16.dp)

    )
    // Vous pouvez ajouter plus d'informations sur le plat ici
}

// Classes pour la réponse et les données
data class MenuResponse(val data: List<Category>)
data class Category(val name: String, val items: List<MenuItem>)
data class MenuItem(val name: String, // et autres propriétés que vous souhaitez utiliser
)
