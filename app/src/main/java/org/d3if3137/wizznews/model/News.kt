package org.d3if3137.wizznews.model

data class News(
    val judul: String,
    val deskripsi: String,
    val imageId: String,
    val mine: Int = 0
)