package fr.isen.gomez.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.isen.gomez.androiderestaurant.ui.theme.AndroidERestaurantTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier



class HomeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AndroidERestaurantTheme {
                // Un container surface utilisant la couleur 'background' du thème
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val context = LocalContext.current // Récupérez le contexte ici
                    MenuScreen(context = context)
                }
            }
        }

    }
    override fun onDestroy() {
        super.onDestroy()
        Log.d("HomeActivity", "L'activité Home est détruite.")
    }
}

@Composable
fun MenuScreen(context: Context) {
    Column {
        val categories = listOf("Entrées", "Plats", "Desserts")
        Text(
            "Bienvenue chez DroidRestaurant",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textAlign = TextAlign.Center,
            fontSize = 24.sp
        )
        // Affichage des catégories
        categories.forEach { category ->
            CategoryItem(name = category, onClick = { categoryName ->
                // Intention pour démarrer fr.isen.gomez.androiderestaurant.CategoryActivity avec le nom de la catégorie
                val intent = Intent(context, CategoryActivity::class.java).apply {
                    putExtra("categoryName", categoryName)
                }
                context.startActivity(intent)
            })
        }
    }
}


@Composable
fun CategoryItem(name: String, onClick: (String) -> Unit) {
    Text(
        text = name,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(name) }
            .padding(16.dp),
        textAlign = TextAlign.Center,
        fontSize = 18.sp
    )
}