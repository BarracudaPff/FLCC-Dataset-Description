package org.jetbrains.completion.full.line.settings

import com.intellij.ui.layout.CellBuilder
import com.intellij.ui.layout.ComponentPredicate
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.toBinding
import javax.swing.JTextField
import kotlin.reflect.KMutableProperty0

// For some reason method intTextField() in com/intellij/ui/layout/Cell.kt
// throws java.lang.LinkageError: loader constraint violation: when resolving method,
// But it's copy works fine :/
fun Row.intTextFieldFixed(prop: KMutableProperty0<Int>, columns: Int? = null, range: IntRange? = null): CellBuilder<JTextField> {
    val binding = prop.toBinding()
    return textField(
            { binding.get().toString() },
            { value -> value.toIntOrNull()?.let { intValue -> binding.set(range?.let { intValue.coerceIn(it.first, it.last) } ?: intValue) } },
            columns
    ).withValidationOnInput {
        val value = it.text.toIntOrNull()
        if (value == null)
            error("Please enter a number")
        else if (range != null && value !in range)
            error("Please enter a number from ${range.first} to ${range.last}")
        else null
    }
}

fun Row.doubleTextField(prop: KMutableProperty0<Double>, columns: Int? = null, range: IntRange? = null): CellBuilder<JTextField> {
    val binding = prop.toBinding()
    return textField(
            { prop.get().toString() },
            { value -> value.toDoubleOrNull()?.let { intValue -> binding.set(range?.let { intValue.coerceIn(it.first.toDouble(), it.last.toDouble()) } ?: intValue) } },
            columns
    ).withValidationOnInput {
        val value = it.text.toDoubleOrNull()
        if (value == null)
            error("Please enter a valid double number (ex. 3.14)")
        else if (range != null && value < range.first && value > range.last)
            error("Please enter a number from ${range.first} to ${range.last}")
        else null
    }
}

fun Row.enableSubRowsIf(predicate: ComponentPredicate) {
    subRowsEnabled = predicate()
    predicate.addListener { subRowsEnabled = it }
}
