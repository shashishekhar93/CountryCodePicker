package com.smcoding.countrycodesdk

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.smcoding.countrycodepicker.CountryCodePicker
import com.smcoding.countrycodepicker.enableGithubCountryRequests
import com.smcoding.countrycodesdk.ui.theme.CountryCodePickerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CountryCodePickerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CountryCodePickerScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun CountryCodePickerScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    
    // Create an instance of CountryCodePicker to use its dialog methods
    val countryCodePicker = remember { 
        CountryCodePicker(context).apply {
            // Optional configuration
            showNameCode = true
        }
    }

    countryCodePicker.enableGithubCountryRequests()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Country Code Picker Sample")
        
        Spacer(modifier = Modifier.height(16.dp))

        // This is the visible picker component that opens dialog on its own click
        AndroidView(
            modifier = Modifier.wrapContentSize(),
            factory = { ctx ->
                CountryCodePicker(ctx).apply {
                    onCountryChangeListener = object : CountryCodePicker.OnCountryChangeListener {
                        override fun onCountrySelected() {
                            Toast.makeText(ctx, "Selected: $selectedCountryName ($selectedCountryCodeWithPlus)", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Manual trigger button as requested
        Button(onClick = {
            countryCodePicker.launchCountrySelectionDialog()
        }) {
            Text(text = "Open Country Picker Dialog")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CountryCodePickerTheme {
        CountryCodePickerScreen()
    }
}
