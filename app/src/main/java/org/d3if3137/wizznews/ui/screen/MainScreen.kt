package org.d3if3137.wizznews.ui.screen

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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.d3if3137.wizznews.R
import org.d3if3137.wizznews.model.MainViewModel
import org.d3if3137.wizznews.model.News
import org.d3if3137.wizznews.network.ApiStatus
import org.d3if3137.wizznews.network.NewsApi
import org.d3if3137.wizznews.ui.theme.BasicColor
import org.d3if3137.wizznews.ui.theme.LightGrey
import org.d3if3137.wizznews.ui.theme.TextColor
import org.d3if3137.wizznews.ui.theme.WizzNewsTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
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
                actions = {}
            )
        },
        floatingActionButton = {
            IconButton(
                modifier = Modifier.size(57.dp).clip(RoundedCornerShape(40)).background(BasicColor),
                onClick = {}) {
                Icon(
                    modifier = Modifier.size(43.dp),
                    imageVector = Icons.Filled.Add,
                    contentDescription = "fab",
                    tint = TextColor
                )
            }
        }
    ) { padding ->
        ScreenContent(Modifier.padding(padding))
    }
}

@Composable
fun ScreenContent(modifier: Modifier) {
    val viewModel: MainViewModel = viewModel()
    val data by viewModel.data
    val status by viewModel.status.collectAsState()

    Column(modifier = modifier.padding(24.dp)) {
        Text(text = "JUDUL", fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "About App",
            color = Color.Gray,
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
                LazyVerticalGrid(
                    contentPadding = PaddingValues(bottom = 50.dp),
                    modifier = Modifier.padding(top = 24.dp),
                    columns = GridCells.Fixed(2)
                ) {
                    items(data) {
                        ItemsGrid(berita = it)
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
                        onClick = { viewModel.retrieveData() },
                        modifier = Modifier.padding(top = 16.dp),
                        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                    ) {
                        Text(text = stringResource(R.string.try_again))
                    }
                }
            }
        }
    }
}

@Composable
fun ItemsGrid(berita : News) {
    Column(
        modifier = Modifier
            .padding(4.dp)
            .clip(shape = RoundedCornerShape(17.dp))
            .border(1.dp, LightGrey),
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(NewsApi.getNewsUrl(berita.imageId))
                    .crossfade(true)
                    .build(),
                modifier = Modifier.fillMaxWidth(),
                contentDescription = berita.judul,
                contentScale = ContentScale.Crop
            )
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

@Preview(showBackground = true)
@Composable
fun Preview() {
    WizzNewsTheme {
        MainScreen()
    }
}