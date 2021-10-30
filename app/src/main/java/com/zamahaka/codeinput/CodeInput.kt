package com.zamahaka.codeinput

import android.util.Log
import android.view.inputmethod.BaseInputConnection
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFontLoader
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalTextInputService
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.pasteText
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setText
import androidx.compose.ui.text.Paragraph
import androidx.compose.ui.text.input.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import arrow.core.rightPadZip

// TODO:
//  Notify about focus
@Composable
fun CodeInput(
    requiredLength: Int,
    code: String,
    onSymbolSelected: (String) -> Unit,
    onDelete: () -> Unit,
) {
//    BaseInputConnection
    var textFieldValueState by remember { mutableStateOf(TextFieldValue(text = code)) }
    val textFieldValue = textFieldValueState.copy(text = code)

    val textInputService = LocalTextInputService.current
    var textInputSession by remember { mutableStateOf<TextInputSession?>(null) }

    val focusRequester = FocusRequester()
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Log.d(
        "myLog",
        "CodeInput(requiredLength=$requiredLength, code=$code) { isFocused = $isFocused }",
    )

    val focusMod = Modifier
        .focusRequester(focusRequester)
        .onFocusChanged { focusState ->
            Log.d("myLog", "onFocusChanged: $focusState")
            if (focusState.isFocused) {
                textInputSession = textInputService?.startInput(
                    value = TextFieldValue(text = code),
                    imeOptions = ImeOptions(
                        singleLine = true,
                        autoCorrect = false,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                    ),
                    onEditCommand = {
                        Log.d("myLog", "onEditCommand: $it")
                    },
                    onImeActionPerformed = {
                        Log.d("myLog", "onImeActionPerformed: $it")
                    }
                )

                Log.d("myLog", "textInputSession.isOpen: ${textInputSession?.isOpen}")
                textInputSession?.showSoftwareKeyboard()
            } else {
                textInputSession?.hideSoftwareKeyboard()
                textInputSession?.dispose()
            }
        }
        .focusable(enabled = true, interactionSource = interactionSource)

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .wrapContentSize()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
            ) {
                Log.d("myLog", "clickable")
                focusRequester.requestFocus()
                if (isFocused) textInputSession?.showSoftwareKeyboard()
            }
            .then(focusMod)
            .onKeyEvent {
                Log.d("myLog", "onKeyEvent: $it")
                true
            }
    ) {

        (0 until requiredLength).rightPadZip(textFieldValue.text.toList()).forEach { (index, symbol) ->
            SymbolView(
                symbol = symbol,
                isSelected = isFocused && index == code.length,
            )
        }
    }
}

@Composable
fun SymbolView(
    symbol: Char?,
    isSelected: Boolean,
    strokeWidth: Dp = dimensionResource(R.dimen.dividerThickness),
) {
    val density = LocalDensity.current
    val resourceLoader = LocalFontLoader.current

    val strokeWidthPx = with(density) { strokeWidth.toPx() }
    val color by animateColorAsState(
        targetValue = colorResource(if (isSelected) R.color.divideDark else R.color.divideLight02),
    )

    val style = LocalTextStyle.current.copy(fontSize = 38.sp)

    val emWidth = remember(style) {
        with(density) {
            Paragraph(
                text = "M",
                style = style,
                width = Float.MAX_VALUE,
                density = density,
                resourceLoader = resourceLoader,
            ).minIntrinsicWidth.toDp()
        }
    }

    Box(
        modifier = Modifier
            .wrapContentHeight()
            .defaultMinSize(minWidth = emWidth)
            .drawBehind {
                val y = size.height - strokeWidthPx

                drawLine(
                    color = color,
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = strokeWidthPx,
                )
            }
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = symbol?.toString() ?: "",
            style = style,
        )
    }
}

@Preview("Empty")
@Composable
fun PreviewEmpty() {
    CodeInput(requiredLength = 4, code = "", onSymbolSelected = {}, onDelete = {})
}

@Preview("Some")
@Composable
fun PreviewSome() {
    CodeInput(requiredLength = 4, code = "12", onSymbolSelected = {}, onDelete = {})
}

@Preview("Full")
@Composable
fun PreviewFull() {
    CodeInput(requiredLength = 4, code = "1234", onSymbolSelected = {}, onDelete = {})
}