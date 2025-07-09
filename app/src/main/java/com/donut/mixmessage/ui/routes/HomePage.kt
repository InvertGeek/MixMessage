package com.donut.mixmessage.ui.routes

import androidx.compose.ui.unit.dp
import com.donut.mixmessage.ui.component.encoder.DecodeComponent
import com.donut.mixmessage.ui.component.encoder.EncodeComponent
import com.donut.mixmessage.ui.nav.MixNavPage


val Home = MixNavPage(gap = 20.dp) {
    EncodeComponent()
    DecodeComponent()
}