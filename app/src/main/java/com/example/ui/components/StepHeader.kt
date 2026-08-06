package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.RegistrationStep

@Composable
fun StepHeader(
    currentStep: RegistrationStep,
    onStepClick: (RegistrationStep) -> Unit
) {
    val steps = listOf(
        StepData(RegistrationStep.STEP_1_SCAN_MANUFACTURER, "1. סריקת ברקוד/SN", Icons.Default.QrCodeScanner),
        StepData(RegistrationStep.STEP_2_ASSIGN_INVENTORY, "2. שיוך אינוונטר", Icons.Default.ConfirmationNumber),
        StepData(RegistrationStep.STEP_3_VALIDATE_PAIR, "3. אישור וקליטה", Icons.Default.VerifiedUser),
        StepData(RegistrationStep.STEP_4_LABEL_PRINT_PREVIEW, "4. תווית (אופציונלי)", Icons.Default.Print)
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, step ->
                val isCompleted = currentStep.ordinal > step.step.ordinal
                val isCurrent = currentStep == step.step

                StepItem(
                    stepNumber = index + 1,
                    title = step.title,
                    icon = step.icon,
                    isCompleted = isCompleted,
                    isCurrent = isCurrent,
                    onClick = { onStepClick(step.step) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

private data class StepData(
    val step: RegistrationStep,
    val title: String,
    val icon: ImageVector
)

@Composable
private fun StepItem(
    stepNumber: Int,
    title: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isCurrent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val circleColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.tertiary
            isCurrent -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        },
        label = "circleColor"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.onTertiary
            isCurrent -> MaterialTheme.colorScheme.onPrimary
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        },
        label = "contentColor"
    )

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp, horizontal = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(circleColor)
                .then(
                    if (isCurrent) Modifier.border(2.5.dp, MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "הושלם",
                    tint = contentColor,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = contentColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = 12.sp,
                fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Medium
            ),
            color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp)
        )
    }
}
