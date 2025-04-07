// File: app/src/main/java/com/example/bovara/medicamento/presentation/RegisterMedicamentoScreen.kt

package com.example.bovara.medicamento.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bovara.di.AppModule
import com.example.bovara.medicamento.data.model.MedicamentoEntity
import com.example.bovara.ui.theme.AccentGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterMedicamentoScreen(
    onNavigateBack: () -> Unit,
    onMedicamentoRegistered: (Int) -> Unit
) {
    val context = LocalContext.current
    val medicamentoUseCase = AppModule.provideMedicamentoUseCase(context)

    val viewModel: RegisterMedicamentoViewModel = viewModel(
        factory = RegisterMedicamentoViewModel.Factory(medicamentoUseCase)
    )

    val state by viewModel.state.collectAsState()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    // Efecto para navegar de regreso con el medicamento registrado
    LaunchedEffect(state.savedMedicamento) {
        state.savedMedicamento?.let { medicamento ->
            onMedicamentoRegistered(medicamento.id)  // Solo pasar el ID
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Medicamento") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Rounded.ArrowBack,
                            contentDescription = "Regresar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Nombre del medicamento
            OutlinedTextField(
                value = state.nombre,
                onValueChange = { viewModel.onEvent(RegisterMedicamentoEvent.NombreChanged(it)) },
                label = { Text("Nombre del Medicamento*") },
                placeholder = { Text("Ej: Ivermectina, Vacuna Viral, etc.") },
                singleLine = true,
                isError = state.nombreError != null,
                supportingText = state.nombreError?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.HealthAndSafety,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Descripción
            OutlinedTextField(
                value = state.descripcion,
                onValueChange = { viewModel.onEvent(RegisterMedicamentoEvent.DescripcionChanged(it)) },
                label = { Text("Descripción o Propósito*") },
                placeholder = { Text("Ej: Prevención de fiebre aftosa") },
                isError = state.descripcionError != null,
                supportingText = state.descripcionError?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Tipo de medicamento
            Text(
                text = "Tipo de Medicamento",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Selector de tipo (puedes reutilizar el componente de MedicamentoTypeOption)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MedicamentoTypeOption(
                    selected = state.tipo == "vacuna",
                    title = "Vacuna",
                    icon = Icons.Default.HealthAndSafety,
                    onClick = { viewModel.onEvent(RegisterMedicamentoEvent.TipoChanged("vacuna")) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                MedicamentoTypeOption(
                    selected = state.tipo == "desparasitante",
                    title = "Desparasitante",
                    icon = Icons.Default.Healing,
                    onClick = { viewModel.onEvent(RegisterMedicamentoEvent.TipoChanged("desparasitante")) },
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MedicamentoTypeOption(
                    selected = state.tipo == "vitamina",
                    title = "Vitamina",
                    icon = Icons.Default.LocalPharmacy,
                    onClick = { viewModel.onEvent(RegisterMedicamentoEvent.TipoChanged("vitamina")) },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(8.dp))

                MedicamentoTypeOption(
                    selected = state.tipo == "antibiótico",
                    title = "Antibiótico",
                    icon = Icons.Default.Biotech,
                    onClick = { viewModel.onEvent(RegisterMedicamentoEvent.TipoChanged("antibiótico")) },
                    modifier = Modifier.weight(1f)
                )
            }

            // Dosis estándar
            OutlinedTextField(
                value = state.dosisML,
                onValueChange = { viewModel.onEvent(RegisterMedicamentoEvent.DosisMLChanged(it)) },
                label = { Text("Dosis Estándar (ml)*") },
                placeholder = { Text("Ej: 5.5") },
                singleLine = true,
                isError = state.dosisMLError != null,
                supportingText = state.dosisMLError?.let {
                    { Text(text = it, color = MaterialTheme.colorScheme.error) }
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Science,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            // Notas adicionales (opcional)
            OutlinedTextField(
                value = state.notas,
                onValueChange = { viewModel.onEvent(RegisterMedicamentoEvent.NotasChanged(it)) },
                label = { Text("Notas adicionales (opcional)") },
                placeholder = { Text("Agregue cualquier información adicional") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )

            // Botón para guardar
            Button(
                onClick = { viewModel.onEvent(RegisterMedicamentoEvent.SaveMedicamento) },
                enabled = !state.isLoading && state.canSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentGreen
                )
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Text(
                        text = "Guardar Medicamento",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Mostrar error si existe
    if (state.error != null) {
        AlertDialog(
            onDismissRequest = { viewModel.onEvent(RegisterMedicamentoEvent.ClearError) },
            title = { Text("Error") },
            text = { Text(state.error!!) },
            confirmButton = {
                Button(onClick = { viewModel.onEvent(RegisterMedicamentoEvent.ClearError) }) {
                    Text("Aceptar")
                }
            }
        )
    }
}