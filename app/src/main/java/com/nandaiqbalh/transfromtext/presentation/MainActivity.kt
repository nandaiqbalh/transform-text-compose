package com.nandaiqbalh.transfromtext.presentation

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Card
import androidx.compose.material.IconButton
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.nandaiqbalh.transfromtext.presentation.ui.theme.TransformTextTheme
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : ComponentActivity() {
	private val viewModel: TransformationViewModel by viewModels()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			TransformTextTheme {
				Surface(modifier = Modifier.fillMaxSize()) {
					TextTransformerApp(viewModel)
				}
			}
		}
	}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextTransformerApp(viewModel: TransformationViewModel) {
	var transformationFileContent by remember { mutableStateOf("") }
	var textFileContent by remember { mutableStateOf("") }
	var transformedText by remember { mutableStateOf("") }
	var transformationFileName by remember { mutableStateOf("") }
	var textFileName by remember { mutableStateOf("") }
	val context = LocalContext.current
	val scaffoldState = rememberScaffoldState()
	val scope = rememberCoroutineScope()

	val selectTransformationFileLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.GetContent()
	) { uri: Uri? ->
		uri?.let {
			transformationFileContent = readFileContent(uri, context)
			transformationFileName = getFileName(uri, context)
		}
	}

	val selectTextFileLauncher = rememberLauncherForActivityResult(
		contract = ActivityResultContracts.GetContent()
	) { uri: Uri? ->
		uri?.let {
			textFileContent = readFileContent(uri, context)
			textFileName = getFileName(uri, context)
		}
	}

	Scaffold(
		scaffoldState = scaffoldState,
		topBar = {
			TopAppBar(
				title = { Text("Text Transformer") },
				actions = {
					IconButton(onClick = {
						transformationFileContent = ""
						textFileContent = ""
						transformationFileName = ""
						textFileName = ""
						transformedText = ""
					}) {
						Icon(Icons.Filled.Delete, contentDescription = "Clear")
					}
				}
			)
		}
	) { paddingValues ->
		Column(
			modifier = Modifier
				.fillMaxSize()
				.padding(paddingValues)
				.verticalScroll(rememberScrollState())
				.background(MaterialTheme.colorScheme.surface), // Menggunakan warna surface dari MaterialTheme
			horizontalAlignment = Alignment.CenterHorizontally,
			verticalArrangement = Arrangement.Top
		) {
			Card(
				modifier = Modifier.fillMaxWidth().padding(16.dp),
				elevation = 4.dp,
				backgroundColor = MaterialTheme.colorScheme.background, // Menggunakan warna background dari MaterialTheme
			) {
				Column(
					modifier = Modifier.padding(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					Button(onClick = { selectTransformationFileLauncher.launch("text/plain") }) {
						Text("Select Transformation File")
					}
					if (transformationFileName.isNotEmpty()) {
						Text("Selected: $transformationFileName")
					}
					Spacer(modifier = Modifier.height(8.dp))
					Button(onClick = { selectTextFileLauncher.launch("text/plain") }) {
						Text("Select Text File")
					}
					if (textFileName.isNotEmpty()) {
						Text("Selected: $textFileName")
					}
					Spacer(modifier = Modifier.height(8.dp))
					Button(onClick = {
						if (transformationFileContent.isNotEmpty() && textFileContent.isNotEmpty()) {
							transformedText = viewModel.transformText(transformationFileContent, textFileContent)
						} else {
							scope.launch {
								scaffoldState.snackbarHostState.showSnackbar("Please upload both files")
							}
						}
					}) {
						Text("Transform")
					}
				}
			}
			Spacer(modifier = Modifier.height(16.dp))
			Card(
				modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
				elevation = 4.dp,
				backgroundColor = MaterialTheme.colorScheme.background, // Menggunakan warna background dari MaterialTheme
			) {
				Column(
					modifier = Modifier.padding(16.dp),
					horizontalAlignment = Alignment.CenterHorizontally,
					verticalArrangement = Arrangement.Center
				) {
					if (transformedText.isNotEmpty()) {
						Text(text = transformedText)
					} else {
						Text(
							"Result",
							style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface),
							// Menggunakan warna onSurface dari MaterialTheme
						)
					}
				}
			}
		}
	}
}

fun readFileContent(uri: Uri, context: Context): String {
	val inputStream = context.contentResolver.openInputStream(uri)
	val bufferedReader = BufferedReader(InputStreamReader(inputStream))
	return bufferedReader.use { it.readText() }
}

fun getFileName(uri: Uri, context: Context): String {
	var result: String? = null
	if (uri.scheme == "content") {
		val cursor = context.contentResolver.query(uri, null, null, null, null)
		cursor.use {
			if (it != null && it.moveToFirst()) {
				result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
			}
		}
	}
	if (result == null) {
		result = uri.path
		val cut = result?.lastIndexOf('/') ?: -1
		if (cut != -1) {
			result = result?.substring(cut + 1)
		}
	}
	return result ?: "unknown"
}
