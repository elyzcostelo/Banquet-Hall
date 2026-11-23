package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Anchor
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.LiveDeckStatus
import com.example.domain.LiveHallCondition
import com.example.ui.theme.AdmiralNavy
import com.example.ui.theme.BrassGold
import com.example.ui.theme.BrassGoldBright
import com.example.ui.theme.CrispWhite
import com.example.ui.theme.DeepBridgeNavy
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusCrimson
import com.example.ui.theme.StatusEmerald

@Composable
fun LiveDeckHeader(
    liveStatus: LiveDeckStatus,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse_alpha",
    )

    val dotColor by animateColorAsState(
        targetValue = when (liveStatus.condition) {
            LiveHallCondition.OPEN_NOW -> StatusEmerald
            LiveHallCondition.OCCUPIED_NOW -> StatusCrimson
            LiveHallCondition.MAINTENANCE_NOW -> StatusAmber
            LiveHallCondition.AFTER_HOURS -> BrassGoldBright
        },
        label = "live_dot_color",
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_deck_header"),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, BrassGold.copy(alpha = 0.45f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
        ) {
            Image(
                painter = painterResource(id = R.drawable.img_banquet_hero),
                contentDescription = stringResource(R.string.hero_banner_cd),
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                DeepBridgeNavy.copy(alpha = 0.72f),
                                AdmiralNavy.copy(alpha = 0.62f),
                                DeepBridgeNavy.copy(alpha = 0.94f),
                            ),
                        ),
                    ),
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp),
                verticalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = BrassGold.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, BrassGoldBright.copy(alpha = 0.75f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Anchor,
                                contentDescription = null,
                                tint = BrassGoldBright,
                                modifier = Modifier.size(14.dp),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "AIMS • JUNE V • 5TH DECK",
                                style = MaterialTheme.typography.labelSmall,
                                color = CrispWhite,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                    }

                    Surface(
                        color = DeepBridgeNavy.copy(alpha = 0.85f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.20f)),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .alpha(pulseAlpha)
                                    .clip(CircleShape)
                                    .background(dotColor),
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Filled.Schedule,
                                contentDescription = null,
                                tint = CrispWhite.copy(alpha = 0.85f),
                                modifier = Modifier.size(13.dp),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = liveStatus.currentTimeFormatted,
                                style = MaterialTheme.typography.labelSmall,
                                color = CrispWhite,
                                fontWeight = FontWeight.SemiBold,
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Banquet Hall Schedule",
                        style = MaterialTheme.typography.headlineMedium,
                        color = CrispWhite,
                    )
                    Text(
                        text = liveStatus.headline,
                        style = MaterialTheme.typography.titleSmall,
                        color = BrassGoldBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = liveStatus.detail,
                        style = MaterialTheme.typography.bodySmall,
                        color = CrispWhite.copy(alpha = 0.86f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}
