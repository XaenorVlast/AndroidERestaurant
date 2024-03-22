package fr.isen.gomez.androiderestaurant

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment


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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            "Bienvenue chez\nDroidRestaurant",
            textAlign = TextAlign.Center,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF57C00), // Couleur orange personnalisée
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Inclure l'icône ici
        Image(
            painter = painterResource(id = R.drawable.androidcook),
            contentDescription = "Icône du Chef",
            modifier = Modifier.size(100.dp) // Modifiez ceci selon la taille souhaitée
        )

        // Affichage des catégories avec séparateurs
        val categories = listOf("Entrées", "Plats", "Desserts")
        categories.forEach { category ->
            CategoryItem(name = category, onClick = { categoryName ->
                val intent = Intent(context, CategoryActivity::class.java).apply {
                    putExtra("categoryName", categoryName)
                }
                context.startActivity(intent)
            })
            Divider(
                color = Color(0xFFF57C00), // Couleur orange personnalisée
                modifier = Modifier.padding(vertical = 8.dp)
            )
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
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        color = Color.Black
    )
}

