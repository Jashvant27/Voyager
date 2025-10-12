package com.jashvantsewmangal.voyager.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jashvantsewmangal.voyager.enums.WhenEnum
import com.jashvantsewmangal.voyager.models.DayActivity
import com.jashvantsewmangal.voyager.models.NoDateActivity
import com.jashvantsewmangal.voyager.ui.theme.VoyagerTheme
import java.time.LocalDate
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewActivityBottomSheet(activity: NoDateActivity? = null, onDismissRequest: () -> Unit) {
    ModalBottomSheet(
        onDismissRequest = { onDismissRequest() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "Activity: ${activity?.what}",
                textAlign = TextAlign.Center,
            )
            TextButton(onClick = {
                //Save action
            }) {
                Text("Save")
            }
        }
    }
}

@Preview(
    name = "NewActivityDialog - Light Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
)
@Preview(
    name = "NewActivityDialog - Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
fun NewActivityDialogPreview() {
    val activity = DayActivity(
        id = "250691_349020omdker",
        date = LocalDate.of(2025, 12, 18),
        location = "Samui 82 1, Tambon Ang Thong",
        whenType = WhenEnum.CUSTOM,
        specific = LocalTime.of(11, 30),
        what = "Tree Bridge Jungle Zipline and Waterfall Adventure"
    )

    val noDateActivity =
        NoDateActivity(activity.location, activity.whenType, activity.specific, activity.what)

    VoyagerTheme {
        Surface {
            NewActivityBottomSheet(
                activity = noDateActivity,
                onDismissRequest = {}
            )
        }
    }
}