package com.jashvantsewmangal.voyager.ui.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp

@Composable
fun ActivityItemAnnotatedText(displayTime: String, headline: String?) {
    val textMeasurer = rememberTextMeasurer()
    val textStyle = MaterialTheme.typography.titleMedium
    val density = LocalDensity.current

    val textLayoutResult = remember(displayTime, textStyle) {
        textMeasurer.measure(
            text = AnnotatedString(displayTime),
            style = textStyle
        )
    }

    val placeholderWidth = with(density) { textLayoutResult.size.width.toDp() + 12.dp }
    val placeholderHeight = with(density) { textLayoutResult.size.height.toDp() }

    val annotatedText = buildAnnotatedString {
        headline?.let {
            append(it)
            append("  ")
        }
        appendInlineContent("timeTag", "[time]")
    }

    val inlineContent = mapOf(
        "timeTag" to InlineTextContent(
            Placeholder(
                width = with(density) { placeholderWidth.toSp() },
                height = with(density) { placeholderHeight.toSp() },
                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
            )
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = displayTime,
                    color = MaterialTheme.colorScheme.primary,
                    style = textStyle,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 0.dp)
                )
            }
        }
    )

    Text(
        text = annotatedText,
        inlineContent = inlineContent,
        style = textStyle,
        modifier = Modifier
            .fillMaxWidth()
    )
}