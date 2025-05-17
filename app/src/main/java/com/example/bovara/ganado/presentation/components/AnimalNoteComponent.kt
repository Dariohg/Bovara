package com.example.bovara.ganado.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Note
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun AnimalNoteComponent(
    note: String,
    onNoteChange: (String) -> Unit,
    isEditable: Boolean = true
) {
    var isEditing by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf(note) }
    val focusManager = LocalFocusManager.current

    // Si la nota cambia externamente, actualizar el estado local
    LaunchedEffect(note) {
        noteText = note
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            NoteHeader(
                isEditing = isEditing,
                isEditable = isEditable,
                onEditClick = { isEditing = true },
                onSaveClick = {
                    isEditing = false
                    onNoteChange(noteText)
                    focusManager.clearFocus()
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isEditing && isEditable) {
                EditableNote(
                    noteText = noteText,
                    onNoteTextChanged = { noteText = it },
                    focusManager = focusManager
                )
            } else {
                DisplayNote(noteText = noteText)
            }

            // Agregar un indicador de estado si el animal no está activo
            if (!isEditable) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
                Text(
                    text = "Esta nota está en modo solo lectura porque el animal no está activo",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun NoteHeader(
    isEditing: Boolean,
    isEditable: Boolean,
    onEditClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Note,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Notas del Animal",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        if (isEditable) {
            if (isEditing) {
                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = Icons.Default.Save,
                        contentDescription = "Guardar nota",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar nota",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun EditableNote(
    noteText: String,
    onNoteTextChanged: (String) -> Unit,
    focusManager: FocusManager
) {
    OutlinedTextField(
        value = noteText,
        onValueChange = onNoteTextChanged,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp),
        placeholder = { Text("Añade notas o comentarios sobre el animal...") },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Default
        ),
        maxLines = 10,
        textStyle = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun DisplayNote(noteText: String) {
    if (noteText.isBlank()) {
        Text(
            text = "No hay notas para este animal. Haz clic en el icono de edición para añadir una nota.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    } else {
        Text(
            text = noteText,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}