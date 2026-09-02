package com.pws.primaragagym.ui.components.auth

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.theme.GreenPrimary
import com.pws.primaragagym.ui.theme.TextOnPrimary

@Composable
fun AuthPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    isSuccess: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(Dimens.button_height),
        enabled = enabled && !isLoading,
        shape = RoundedCornerShape(Dimens.button_corner_radius),
        colors = ButtonDefaults.buttonColors(
            containerColor = when {
                isSuccess -> GreenPrimary.copy(alpha = 0.8f)
                else -> GreenPrimary
            },
            disabledContainerColor = GreenPrimary.copy(alpha = 0.4f),
            contentColor = TextOnPrimary,
            disabledContentColor = TextOnPrimary.copy(alpha = 0.6f)
        ),
        contentPadding = PaddingValues(horizontal = Dimens.spacing_6)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = TextOnPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(Dimens.spacing_2))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}
