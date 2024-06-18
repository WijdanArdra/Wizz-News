package org.d3if3137.wizznews.ui.screen

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.d3if3137.wizznews.BuildConfig
import org.d3if3137.wizznews.R
import org.d3if3137.wizznews.model.MainViewModel
import org.d3if3137.wizznews.model.News
import org.d3if3137.wizznews.model.User
import org.d3if3137.wizznews.network.ApiStatus
import org.d3if3137.wizznews.network.NewsApi
import org.d3if3137.wizznews.network.UserDataStore
import org.d3if3137.wizznews.ui.theme.BasicColor
import org.d3if3137.wizznews.ui.theme.LightGrey
import org.d3if3137.wizznews.ui.theme.TextColor
import org.d3if3137.wizznews.ui.theme.WizzNewsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    val viewModel: MainViewModel = viewModel()
    val errorMessage by viewModel.errorMessage

    var showDialog by remember { mutableStateOf(false) }
    var showNewsDialog by remember { mutableStateOf(false) }

    var bitmap: Bitmap? by remember { mutableStateOf(null) }
    val launcher = rememberLauncherForActivityResult(CropImageContract()) {
        bitmap = getCroppedImage(context.contentResolver, it)
        if (bitmap != null) showNewsDialog = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Wizz News", fontWeight = FontWeight.SemiBold)
                },
                colors = TopAppBarDefaults.mediumTopAppBarColors(
                    containerColor = BasicColor,
                    titleContentColor = TextColor
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                        } else {
                            showDialog = true
                        }
                    }) {
                        Icon(
                            modifier = Modifier.size(30.dp),
                            painter = painterResource(id = R.drawable.account_circle),
                            contentDescription = stringResource(R.string.profil),
                            tint = TextColor
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            IconButton(
                modifier = Modifier
                    .size(57.dp)
                    .clip(RoundedCornerShape(40))
                    .background(BasicColor),
                onClick = {
                    val option = CropImageContractOptions(
                        null, CropImageOptions(
                            imageSourceIncludeGallery = true,
                            imageSourceIncludeCamera = true,
                            fixAspectRatio = true
                        )
                    )
                    launcher.launch(option)
                }) {
                Icon(
                    modifier = Modifier.size(43.dp),
                    imageVector = Icons.Filled.Add,
                    contentDescription = "fab",
                    tint = TextColor
                )
            }
        }
    ) { padding ->
        ScreenContent(viewModel, user.email, Modifier.padding(padding))

        if (showDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showDialog = false }) {
                CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                showDialog = false
            }
        }

        if (showNewsDialog) {
            NewsDialog(
                bitmap = bitmap,
                onDismissRequest = { showNewsDialog = false }) { id, judul, deskripsi, _ ->
                viewModel.saveData(user.email, id, judul, deskripsi, 1, bitmap!!)
                showNewsDialog = false
            }
        }

        if (errorMessage != null) {
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }
}

@Composable
fun ScreenContent(viewModel: MainViewModel, userId: String, modifier: Modifier) {
    val data by viewModel.data
    val status by viewModel.status.collectAsState()
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        viewModel.retrieveData(userId)
    }

    Column(modifier = modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "Aplikasi ini adalah aplikasi untuk melihat berita yang anda simpan untuk dibaca di lain waktu. Tersedia juga berita yang ditambahkan oleh Admin.")
        Divider()
        Text(
            text = "Berita tersimpan:", fontWeight = FontWeight.Bold, fontSize = 30.sp,
            textAlign = TextAlign.Justify
        )

        when (status) {
            ApiStatus.LOADING -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Loading", fontSize = 30.sp, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(progress = 1f)
                }
            }

            ApiStatus.SUCCESS -> {
                SwipeRefresh(
                    state = rememberSwipeRefreshState(isRefreshing = refreshing),
                    onRefresh = {
                        refreshing = true
                        viewModel.retrieveData(userId)
                        refreshing = false
                    },
                ) {
                    LazyVerticalGrid(
                        contentPadding = PaddingValues(bottom = 50.dp),
                        columns = GridCells.Fixed(2)
                    ) {
                        items(data) { news ->
                            ItemsGrid(berita = news, onDelete = { beritaId ->
                                Log.d("ScreenContent", "Deleting data with ID: $beritaId")
                                viewModel.deleteData(userId, beritaId)
                            })
                        }
                    }
                }
            }

            ApiStatus.FAILED -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = stringResource(R.string.error))
                    Button(
                        onClick = { viewModel.retrieveData(userId) },
                        modifier = Modifier.padding(top = 16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.coba_lagi))
                    }
                }
            }
        }
    }
}

@Composable
fun ItemsGrid(berita: News, onDelete: (String) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    DisplayAlertDialog(
        openDialog = showDialog,
        onDismissRequest = { showDialog = false },
        onConfirmation = {
            onDelete(berita.id)
            showDialog = false
        }
    )

    Column(
        modifier = Modifier
            .padding(4.dp)
            .clip(shape = RoundedCornerShape(17.dp))
            .border(1.dp, LightGrey, RoundedCornerShape(17.dp)),
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                contentAlignment = Alignment.TopEnd
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(NewsApi.getNewsUrl(berita.imageId))
                        .crossfade(true)
                        .build(),
                    modifier = Modifier.fillMaxWidth(),
                    contentDescription = berita.judul,
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(id = R.drawable.loading_img),
                    error = painterResource(id = R.drawable.broken_img),
                )
                if (berita.mine == 1) {
                    IconButton(onClick = {
                        showDialog = true
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(id = R.string.hapus),
                            tint = Color.White
                        )
                    }
                }
            }
            Text(
                modifier = Modifier
                    .background(Color(0f, 0f, 0f, 0.5f))
                    .fillMaxWidth()
                    .padding(4.dp),
                text = berita.judul,
                color = Color.White,
                maxLines = 6,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            text = berita.deskripsi
        )
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error:${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val nama = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri.toString()
            dataStore.saveData(User(nama, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    WizzNewsTheme {
        MainScreen()
    }
}