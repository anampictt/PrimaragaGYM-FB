package com.pws.primaragagym.ui.components.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import com.pws.primaragagym.R
import com.pws.primaragagym.ui.theme.Dimens
import com.pws.primaragagym.ui.theme.TextPrimaryDark
import com.pws.primaragagym.ui.theme.TextSecondaryDark

@Composable
fun AuthHeader(
    modifier: Modifier = Modifier,
    logoSize: Dp = Dimens.auth_logo_size,
    showTagline: Boolean = true
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.logogym),
            contentDescription = "Logo Primaraga Gym",
            modifier = Modifier.size(logoSize),
            contentScale = ContentScale.Fit
        )
        if (showTagline) {
            Spacer(modifier = Modifier.height(Dimens.spacing_2))
            Text(
                text = "Strong Body, Strong Mind",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center
            )
        }
    }
}
