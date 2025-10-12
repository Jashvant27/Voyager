package com.jashvantsewmangal.voyager.ui.components

import android.content.res.Configuration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationBottomSheet(
    locations: List<String>?,
    onDismissRequest: () -> Unit,
    addFunction: (String) -> Unit,
    removeFunction: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismissRequest) {
        LocationInput(locations ?: emptyList(), addFunction, removeFunction)
    }
}

@Preview(
    name = "LocationDialog (Empty) - Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "LocationDialog (Empty) - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)

@Composable
fun LocationDialogPreview() {
    VoyagerTheme {
        Surface {
            LocationBottomSheet(
                null,
                {},
                { _ -> },
                { _ -> }
            )
        }
    }
}

@Preview(
    name = "LocationDialog (Filled) - Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "LocationDialog (Filled) - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun LocationDialogPreviewFilled() {
    val list = listOf("Aruba", "Bonaire", "Curacao")
    VoyagerTheme {
        Surface {
            LocationBottomSheet(
                list,
                {},
                { _ -> },
                { _ -> }
            )
        }
    }
}
