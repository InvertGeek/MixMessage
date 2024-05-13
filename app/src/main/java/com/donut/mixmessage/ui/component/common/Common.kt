package com.donut.mixmessage.ui.component.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Done
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.donut.mixmessage.ui.theme.LightColorScheme
import com.donut.mixmessage.util.common.truncate

@Composable
fun CommonColumn(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.HorizontalOrVertical = Arrangement.spacedBy(8.dp),
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier,
//            .padding(8.dp),
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
        content = content
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CommonSwitch(
    checked: Boolean,
    text: String,
    onCheckedChangeListener: (Boolean) -> Unit,
    description: String = ""
) {

    FlowRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = text, modifier = Modifier.align(Alignment.CenterVertically))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChangeListener,
        )
        Text(
            text = description,
            modifier = Modifier.align(Alignment.CenterVertically),
            color = Color(0xFF9E9E9E),
            fontSize = 14.sp
        )
    }
}

@Composable
fun ClearableTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
    label: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {


        TextField(
            value = value,
            maxLines = maxLines,
            onValueChange = {
                onValueChange(it)
            },
            label = label,
//            textStyle = TextStyle(color = Color.Black), // 可以根据需要设置文本样式
            modifier = Modifier.weight(1f) // 占据剩余空间
        )

        if (value.text.isNotEmpty()) {
            Icon(
                Icons.Filled.Clear,
                contentDescription = "Clear text",
                tint = LightColorScheme.primary,
                modifier = Modifier.clickable {
                    onValueChange(TextFieldValue())
                }.padding(4.dp)
            )
        }
    }
}

@Composable
fun SingleSelectItemList(items: List<String>, currentOption: String, onSelect: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .heightIn(0.dp, 400.dp),
    ) {
        items(items.size) { item ->
            val currentItem = items[item]
            val selected = currentOption == currentItem
            FilterChip(
                label = { Text(text = currentItem.truncate(13)) },
                onClick = {
                    onSelect(currentItem)
                },
                selected = selected,
                leadingIcon = if (selected) {
                    {
                        Icon(
                            Icons.Outlined.Done,
                            contentDescription = "selected",
                            tint = Color(
                                0xFF24a0ed
                            )
                        )
                    }
                } else {
                    null
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}



