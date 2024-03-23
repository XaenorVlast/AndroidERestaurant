package fr.isen.gomez.androiderestaurant

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.google.gson.Gson
import fr.isen.gomez.androiderestaurant.ui.theme.AndroidERestaurantTheme
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import java.io.FileNotFoundException


class DishDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Récupération de l'objet MenuItem depuis l'intent
        val menuItemJson = intent.getStringExtra("menuItem")
        val menuItem = Gson().fromJson(menuItemJson, MenuItem::class.java)


        setContent {
            AndroidERestaurantTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    DishDetailScreen(menuItem = menuItem)
                }
            }
        }
    }

}

@Composable
fun DishDetailScreen(menuItem: MenuItem) {
    var quantity by remember { mutableStateOf(1) }
    val pricePerUnit = menuItem.prices.firstOrNull()?.price?.toDouble() ?: 0.0
    val totalPrice = quantity * pricePerUnit
    val context = LocalContext.current // Obtention du contexte local

    Column(modifier = Modifier.padding(16.dp)) {
        if (menuItem.images.isNotEmpty()) {
            ImageCarousel(imageUrls = menuItem.images)
        }

        Text(
            text = menuItem.name_fr,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        Text(
            text = "Prix unitaire: ${menuItem.getFirstPriceFormatted()}",
            modifier = Modifier.padding(top = 8.dp)
        )

        menuItem.ingredients.forEach { ingredient ->
            Text(
                text = ingredient.name_fr,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        QuantitySelector(
            menuItem = menuItem,
            quantity = quantity,
            totalPrice = totalPrice,
            onQuantityChanged = { newQuantity -> quantity = newQuantity },
            context = context
        )
    }
}

@Composable
fun QuantitySelector(
    menuItem: MenuItem,
    quantity: Int,
    totalPrice: Double,
    onQuantityChanged: (Int) -> Unit,
    context: Context
) {
    Row {
        Button(onClick = { if (quantity > 1) onQuantityChanged(quantity - 1) }) {
            Text("-")
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(text = quantity.toString())

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = { onQuantityChanged(quantity + 1) }) {
            Text("+")
        }

        Spacer(modifier = Modifier.width(16.dp))

        val view = LocalView.current // Obtention de la vue pour la Snackbar

        Button(onClick = {
            addToCart(menuItem, quantity, totalPrice, view)
        }) {
            Text(text = String.format("Total: %.2f€", totalPrice))
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageCarousel(imageUrls: List<String>) {
    val pagerState = rememberPagerState(pageCount = { imageUrls.size })

    HorizontalPager(state = pagerState) { page ->
        Image(
            painter = rememberImagePainter(imageUrls[page]),
            contentDescription = "Image $page",
            modifier = Modifier.fillMaxWidth()
        )
    }
}



fun addToCart(menuItem: MenuItem, quantity: Int, totalPrice: Double, view: View) {
    val filename = "cart.json"
    val gson = Gson()
    val context = view.context
    var cart: MutableList<CartItem> = mutableListOf()

    Log.d("addToCart", "Début de la fonction addToCart")

    // Essayer de lire le fichier du panier existant
    try {
        context.openFileInput(filename).use { inputStream ->
            val existingCartJson = inputStream.bufferedReader().readText()
            Log.d("addToCart", "Fichier existant lu avec succès")
            try {
                val cartType = object : TypeToken<List<CartItem>>() {}.type
                cart = gson.fromJson(existingCartJson, cartType)
                Log.d("addToCart", "Désérialisation réussie")
            } catch (jsonException: JsonSyntaxException) {
                // Si le fichier ne contient pas une liste, initialiser cart comme une liste vide
                Log.e("addToCart", "Erreur de format JSON, initialisation d'un nouveau panier", jsonException)
                cart = mutableListOf()
            }
        }
    } catch (e: FileNotFoundException) {
        Log.e("addToCart", "Fichier non trouvé, création d'un nouveau fichier", e)
    }

    // Ajouter le nouvel article au panier
    val newCartItem = CartItem(menuItem, quantity, totalPrice)
    cart.add(newCartItem)
    Log.d("addToCart", "Article ajouté au panier")

    // Sauvegarder le panier mis à jour
    try {
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { outputStream ->
            val updatedCartJson = gson.toJson(cart)
            outputStream.write(updatedCartJson.toByteArray())
            Log.d("addToCart", "Panier sauvegardé avec succès")
        }
    } catch (e: Exception) {
        Log.e("addToCart", "Erreur lors de la sauvegarde du panier", e)
    }

    // Afficher une Snackbar pour informer l'utilisateur
    Snackbar.make(view, "Ajouté au panier", Snackbar.LENGTH_LONG).show()
    Log.d("addToCart", "Snackbar affichée")
}

data class CartItem(
    val menuItem: MenuItem,
    val quantity: Int,
    val totalPrice: Double
)

