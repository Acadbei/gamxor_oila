package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.ui.theme.screen.MapWithStoryFromServer
import org.osmdroid.config.Configuration
import androidx.compose.material3.Scaffold
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material.icons.filled.LocationOn

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Osmdroid configuration initialization
        Configuration.getInstance().userAgentValue = packageName
        
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(navController)
                    }
                    composable("home") {
                        Home1()
                        MyApp()
                    }
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun StatikProfil() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Ism: Azamat")
        Text(text = "Kasbi: Android Dasturchi")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun ProfilKartasi1() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profil rasmi",
                modifier = Modifier
                    .size(60.dp)
                    .background(Color.LightGray, shape = CircleShape)
                    .padding(8.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Azamat",
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    text = "Android Dasturchi",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OddiyNavbar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xF55555F5))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Icon(Icons.Default.Home, contentDescription = "Home")
        Icon(Icons.Default.Search, contentDescription = "Search")
        Icon(Icons.Default.Settings, contentDescription = "Settings")
    }
}

@Preview(showBackground = true)
@Composable
fun Ozim1() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(16.dp),
    ) {
        Text(text = "1-text")
        Text(text = "2-text")
        Text(text = "3-text")
    }
}

@Preview(showBackground = true)
@Composable
fun Tf() {
    var kiritilganMatn by remember { mutableStateOf("") }

    OutlinedTextField(
        value = kiritilganMatn,
        onValueChange = { yangiHarf ->
            kiritilganMatn = yangiHarf
        },
        label = { Text("Ma'lumot kiriting") },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(200.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.Blue,   // Bosilgandagi ramka rangi
            unfocusedBorderColor = Color.Gray  // Oddiy holatdagi ramka rangi
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Ikonka"
            )
        }
    )
}

@Preview(showBackground = true)
@Composable
fun Ekran() {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.Bottom
        ) {
            Tf()
            //OddiyNavbar()
        }
    }
}

@Composable
fun LoginScreen(navController: NavController) {
    var phone by remember { mutableStateOf("") }
    var sms by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {

        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text(text = "Telefon") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = sms,
            onValueChange = { sms = it },
            label = { Text(text = "SMS kod") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                navController.navigate("home")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kirish")
        }
    }
}

@Composable
fun Home1() {
    MapWithStoryFromServer()
    Ekran()

}


@Composable
fun MyApp() {
    var selectedItem by remember { mutableStateOf(0) }

    val items = listOf("Home", "Map", "Profile")

    Scaffold(
        bottomBar = {
            NavigationBar(modifier = Modifier
                .fillMaxWidth()
                //.height(60.dp)
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = {
                            when (item) {
                                "Home" -> Icon(Icons.Default.Home, contentDescription = "")
                                "Map" -> Icon(Icons.Default.LocationOn, contentDescription = "")
                                "Profile" -> Icon(Icons.Default.Person, contentDescription = "")
                            }
                        },
                        label = { Text(item) }
                    )
                }
            }
        }
    ) { padding ->

        // Sahifa almashtirish
        Box(modifier = Modifier.padding(padding)) {
            when (selectedItem) {
                0 -> Home1()
                1 -> Text("65464")
                2 -> Text("Profile Screen")
            }
        }
    }
}