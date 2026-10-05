package com.example.currencyconverter

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        setContent {
            CurrencyConverterTheme {
                CurrencyConverterApp()
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencyConverterApp() {

    // Amount entered by user
    var amount by remember {
        mutableStateOf("")
    }

    // Selected currencies
    var fromCurrency by remember {
        mutableStateOf("USD")
    }

    var toCurrency by remember {
        mutableStateOf("PKR")
    }

    // Result
    var result by remember {
        mutableStateOf("Result will appear here")
    }

    // Rate date
    var rateDate by remember {
        mutableStateOf("")
    }

    // Loading state
    var isLoading by remember {
        mutableStateOf(false)
    }

    // Dropdown states
    var fromExpanded by remember {
        mutableStateOf(false)
    }

    var toExpanded by remember {
        mutableStateOf(false)
    }

    // Coroutine
    val scope = rememberCoroutineScope()

    // Supported currencies
    val currencies = listOf(
        "USD",
        "PKR",
        "EUR",
        "GBP",
        "SAR",
        "AED",
        "INR"
    )


    Scaffold(

        topBar = {

            TopAppBar(

                title = {

                    Text(
                        text = "Currency Converter",
                        fontWeight = FontWeight.Bold
                    )

                }

            )

        }

    ) { paddingValues ->


        Column(

            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(
                    horizontal = 20.dp,
                    vertical = 16.dp
                ),

            horizontalAlignment = Alignment.CenterHorizontally

        ) {


            // Subtitle

            Text(

                text = "Convert currencies using live rates",

                fontSize = 16.sp,

                color = MaterialTheme.colorScheme.primary

            )


            Spacer(
                modifier = Modifier.height(22.dp)
            )


            // Amount

            OutlinedTextField(

                value = amount,

                onValueChange = {
                    amount = it
                },

                label = {
                    Text("Amount")
                },

                placeholder = {
                    Text("Enter amount")
                },

                singleLine = true,

                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),

                modifier = Modifier.fillMaxWidth()

            )


            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // FROM CURRENCY

            Text(

                text = "From Currency",

                modifier = Modifier.fillMaxWidth(),

                fontWeight = FontWeight.Bold,

                fontSize = 15.sp

            )


            Spacer(
                modifier = Modifier.height(7.dp)
            )


            Button(

                onClick = {
                    fromExpanded = true
                },

                modifier = Modifier.fillMaxWidth()

            ) {

                Text(

                    text = fromCurrency,

                    fontSize = 16.sp,

                    fontWeight = FontWeight.Bold

                )

            }


            DropdownMenu(

                expanded = fromExpanded,

                onDismissRequest = {
                    fromExpanded = false
                }

            ) {

                currencies.forEach { currency ->

                    DropdownMenuItem(

                        text = {
                            Text(currency)
                        },

                        onClick = {

                            fromCurrency = currency

                            fromExpanded = false

                        }

                    )

                }

            }


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            // SWAP BUTTON

            Button(

                onClick = {

                    val temporary = fromCurrency

                    fromCurrency = toCurrency

                    toCurrency = temporary

                },

                modifier = Modifier.height(48.dp)

            ) {

                Text(

                    text = "⇅",

                    fontSize = 24.sp

                )

            }


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            // TO CURRENCY

            Text(

                text = "To Currency",

                modifier = Modifier.fillMaxWidth(),

                fontWeight = FontWeight.Bold,

                fontSize = 15.sp

            )


            Spacer(
                modifier = Modifier.height(7.dp)
            )


            Button(

                onClick = {
                    toExpanded = true
                },

                modifier = Modifier.fillMaxWidth()

            ) {

                Text(

                    text = toCurrency,

                    fontSize = 16.sp,

                    fontWeight = FontWeight.Bold

                )

            }


            DropdownMenu(

                expanded = toExpanded,

                onDismissRequest = {
                    toExpanded = false
                }

            ) {

                currencies.forEach { currency ->

                    DropdownMenuItem(

                        text = {
                            Text(currency)
                        },

                        onClick = {

                            toCurrency = currency

                            toExpanded = false

                        }

                    )

                }

            }


            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // CONVERT BUTTON

            Button(

                onClick = {

                    val inputAmount =
                        amount.toDoubleOrNull()

                    if (
                        inputAmount == null ||
                        inputAmount < 0
                    ) {

                        result =
                            "Please enter a valid amount"

                        return@Button
                    }


                    // Show loading

                    isLoading = true

                    result = "Getting exchange rate..."


                    // Call API in background

                    scope.launch {

                        val response = getExchangeRate(

                            fromCurrency,

                            toCurrency

                        )


                        isLoading = false


                        if (response.error != null) {

                            result =
                                response.error

                            rateDate = ""

                        } else {

                            val convertedAmount =
                                inputAmount * response.rate


                            result = String.format(

                                Locale.US,

                                "%.2f %s = %.2f %s",

                                inputAmount,

                                fromCurrency,

                                convertedAmount,

                                toCurrency

                            )


                            rateDate =
                                "Rate date: ${response.date}"

                        }

                    }

                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)

            ) {

                Text(

                    text = if (isLoading)
                        "LOADING..."
                    else
                        "CONVERT",

                    fontSize = 16.sp,

                    fontWeight = FontWeight.Bold

                )

            }


            Spacer(
                modifier = Modifier.height(10.dp)
            )


            // CLEAR BUTTON

            Button(

                onClick = {

                    amount = ""

                    fromCurrency = "USD"

                    toCurrency = "PKR"

                    result =
                        "Result will appear here"

                    rateDate = ""

                },

                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)

            ) {

                Text(

                    text = "CLEAR",

                    fontSize = 16.sp,

                    fontWeight = FontWeight.Bold

                )

            }


            Spacer(
                modifier = Modifier.height(20.dp)
            )


            // RESULT CARD

            Card(

                modifier = Modifier.fillMaxWidth(),

                colors = CardDefaults.cardColors(

                    containerColor =
                        MaterialTheme
                            .colorScheme
                            .surfaceVariant

                )

            ) {

                Column(

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),

                    horizontalAlignment =
                        Alignment.CenterHorizontally

                ) {


                    Text(

                        text = "Conversion Result",

                        fontSize = 16.sp,

                        fontWeight = FontWeight.Bold

                    )


                    Spacer(
                        modifier = Modifier.height(10.dp)
                    )


                    Text(

                        text = result,

                        fontSize = 20.sp,

                        fontWeight = FontWeight.Bold,

                        lineHeight = 28.sp

                    )


                    // Show API date

                    if (rateDate.isNotEmpty()) {

                        Spacer(
                            modifier =
                                Modifier.height(8.dp)
                        )


                        Text(

                            text = rateDate,

                            fontSize = 13.sp,

                            color =
                                MaterialTheme
                                    .colorScheme
                                    .onSurfaceVariant

                        )

                    }

                }

            }


            Spacer(
                modifier = Modifier.height(20.dp)
            )

        }

    }

}


/*
 * =========================================================
 * API FUNCTION
 * =========================================================
 *
 * Frankfurter API:
 *
 * https://api.frankfurter.dev/v2/rate/usd/pkr
 *
 */

suspend fun getExchangeRate(

    fromCurrency: String,

    toCurrency: String

): ExchangeResponse = withContext(Dispatchers.IO) {

    try {

        // Create API URL

        val urlString =
            "https://api.frankfurter.dev/v2/rate/" +
                    "${fromCurrency.lowercase()}/" +
                    "${toCurrency.lowercase()}"


        val url =
            URL(urlString)


        // Open connection

        val connection =
            url.openConnection()
                    as HttpURLConnection


        connection.requestMethod = "GET"

        connection.connectTimeout = 10000

        connection.readTimeout = 10000


        // Check HTTP response

        val responseCode =
            connection.responseCode


        if (responseCode != 200) {

            connection.disconnect()

            return@withContext ExchangeResponse(

                rate = 0.0,

                date = "",

                error =
                    "Unable to get exchange rate"

            )

        }


        // Read response

        val responseText =
            connection.inputStream
                .bufferedReader()
                .use {
                    it.readText()
                }


        connection.disconnect()


        // Convert JSON

        val json =
            JSONObject(responseText)


        val rate =
            json.getDouble("rate")


        val date =
            json.getString("date")


        ExchangeResponse(

            rate = rate,

            date = date,

            error = null

        )


    } catch (e: Exception) {

        ExchangeResponse(

            rate = 0.0,

            date = "",

            error =
                "Network error. Check your internet connection."

        )

    }

}


/*
 * =========================================================
 * API RESPONSE
 * =========================================================
 */

data class ExchangeResponse(

    val rate: Double,

    val date: String,

    val error: String?

)


/*
 * =========================================================
 * THEME
 * =========================================================
 */

@Composable
fun CurrencyConverterTheme(

    content: @Composable () -> Unit

) {

    MaterialTheme(

        content = content

    )

}