package com.ashelyakin.libadb.sample.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TextFieldWithFilling(title: String, value: String, fillingColor: Color, onNewValue: (String) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        TextField(
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                disabledTextColor = Color.Black,
                errorTextColor = Color.Black,
                focusedPlaceholderColor = Color.Black,
                unfocusedPlaceholderColor = Color.Black,
                disabledPlaceholderColor = Color.Black,
                errorPlaceholderColor = Color.Black,
                focusedLabelColor = Color.Black,
                unfocusedLabelColor = Color.Black,
                disabledLabelColor = Color.Black,
                errorLabelColor = Color.Black,
                focusedContainerColor = fillingColor,
                unfocusedContainerColor = fillingColor
            ),
            value = value,
            onValueChange = { onNewValue(it) },
            readOnly = true,
            label = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                )
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}