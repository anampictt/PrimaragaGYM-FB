package com.pws.primaragagym.ui.components.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.theme.InputBackgroundLight
import com.pws.primaragagym.ui.theme.InputBackgroundFocusedLight
import com.pws.primaragagym.ui.theme.InputBorderLight
import com.pws.primaragagym.ui.theme.InputBorderFocusedLight
import com.pws.primaragagym.ui.theme.InputCursorLight
import com.pws.primaragagym.ui.theme.InputHintLight
import com.pws.primaragagym.ui.theme.InputTextLight

@Composable
fun AuthTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    isError: Boolean = false,
    errorMessage: String? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    isFocused: Boolean = false,
    onFocusChange: (Boolean) -> Unit = {}
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = Dimens.spacing_2)
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(Dimens.input_height)
                .clip(RoundedCornerShape(Dimens.input_corner_radius))
                .background(if (isFocused) InputBackgroundFocusedLight else InputBackgroundLight)
                .border(
                    width = 1.dp,
                    color = when {
                        isError -> MaterialTheme.colorScheme.error
                        isFocused -> InputBorderFocusedLight
                        else -> InputBorderLight
                    },
                    shape = RoundedCornerShape(Dimens.input_corner_radius)
                )
                .padding(horizontal = Dimens.spacing_4),
            enabled = enabled,
            singleLine = singleLine,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            cursorBrush = SolidColor(InputCursorLight),
            textStyle = MaterialTheme.typography.bodyLarge.copy(color = InputTextLight),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                style = MaterialTheme.typography.bodyLarge,
                                color = InputHintLight
                            )
                        }

                        innerTextField()
                    }
                }
            }
        )
        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = Dimens.spacing_1, start = Dimens.spacing_1)
            )
        }
    }
}
