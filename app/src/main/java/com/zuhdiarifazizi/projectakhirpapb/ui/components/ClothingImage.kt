package com.zuhdiarifazizi.projectakhirpapb.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.zuhdiarifazizi.projectakhirpapb.R
import com.zuhdiarifazizi.projectakhirpapb.domain.model.ClothingItem
import com.zuhdiarifazizi.projectakhirpapb.domain.model.OutfitCandidate
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraCardBorder
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraShapes
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraSpacing
import com.zuhdiarifazizi.projectakhirpapb.ui.theme.AuraTextSecondary

@Composable
fun ClothingImage(
    item: ClothingItem,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 132.dp,
    contentDescription: String = "${item.category.displayName}: ${item.name}, warna ${item.color.colorName}"
) {
    val resource = imageResource(item.imageUri)
    if (resource != null) {
        Image(
            painter = painterResource(resource),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .fillMaxWidth()
                .height(imageHeight)
                .clip(AuraShapes.small)
                .background(MaterialTheme.colorScheme.surface)
        )
    } else if (!item.imageUri.isNullOrBlank()) {
        LocalClothingPhoto(
            imageUri = item.imageUri,
            contentDescription = contentDescription,
            modifier = modifier,
            imageHeight = imageHeight
        )
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(imageHeight)
                .semantics {
                    this.contentDescription = "${item.name}. Foto pakaian belum tersedia."
                },
            shape = AuraShapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, AuraCardBorder)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clothes),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Foto belum tersedia",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSecondary
                )
            }
        }
    }
}

@Composable
fun LocalClothingPhoto(
    imageUri: String,
    contentDescription: String,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 180.dp
) {
    val resource = imageResource(imageUri)
    if (resource != null) {
        Image(
            painter = painterResource(resource),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = modifier.fillMaxWidth().height(imageHeight).clip(AuraShapes.small)
                .background(MaterialTheme.colorScheme.surface)
        )
        return
    }
    val context = LocalContext.current
    val decoded = produceState(initialValue = DecodedPhoto(), imageUri) {
        value = withContext(Dispatchers.IO) {
            DecodedPhoto(bitmap = decodeLocalImage(context, imageUri)?.asImageBitmap(), complete = true)
        }
    }.value

    if (decoded.bitmap != null) {
        Image(
            painter = BitmapPainter(decoded.bitmap),
            contentDescription = contentDescription,
            contentScale = ContentScale.Fit,
            modifier = modifier
                .fillMaxWidth()
                .height(imageHeight)
                .clip(AuraShapes.small)
                .background(MaterialTheme.colorScheme.surface)
        )
    } else {
        Surface(
            modifier = modifier
                .fillMaxWidth()
                .height(imageHeight)
                .semantics { this.contentDescription = "$contentDescription. Foto belum dapat ditampilkan." },
            shape = AuraShapes.small,
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, AuraCardBorder)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clothes),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = if (decoded.complete) "Foto belum dapat ditampilkan" else "Memuat foto pakaian",
                    style = MaterialTheme.typography.bodySmall,
                    color = AuraTextSecondary
                )
            }
        }
    }
}

private data class DecodedPhoto(val bitmap: ImageBitmap? = null, val complete: Boolean = false)

private fun decodeLocalImage(context: android.content.Context, imageUri: String): Bitmap? {
    return runCatching {
        val uri = Uri.parse(imageUri)
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        val sampleSize = generateSequence(1) { it * 2 }
            .takeWhile { bounds.outWidth / it > MAX_IMAGE_DIMENSION || bounds.outHeight / it > MAX_IMAGE_DIMENSION }
            .lastOrNull() ?: 1
        val bitmap = context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sampleSize })
        } ?: return null
        val orientation = context.contentResolver.openInputStream(uri)?.use { input ->
            ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
        } ?: ExifInterface.ORIENTATION_NORMAL
        val rotation = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            else -> 0f
        }
        if (rotation == 0f) bitmap else {
            val matrix = Matrix().apply { postRotate(rotation) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true).also { bitmap.recycle() }
        }
    }.getOrNull()
}

private const val MAX_IMAGE_DIMENSION = 1280

@Composable
fun OutfitGarmentGallery(
    candidate: OutfitCandidate,
    modifier: Modifier = Modifier,
    imageHeight: Dp = 124.dp
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AuraSpacing.sm)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(AuraSpacing.sm)) {
            candidate.topWear?.let { item ->
                GarmentImageItem(item, Modifier.weight(1f), imageHeight)
            }
            candidate.bottomWear?.let { item ->
                GarmentImageItem(item, Modifier.weight(1f), imageHeight)
            }
        }
        candidate.outerWear?.let { item ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                GarmentImageItem(item, Modifier.fillMaxWidth(0.62f), imageHeight)
            }
        }
    }
}

@Composable
private fun GarmentImageItem(item: ClothingItem, modifier: Modifier, imageHeight: Dp) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ClothingImage(item = item, imageHeight = imageHeight)
        Text(
            text = "${item.category.displayName}: ${item.name}, warna ${item.color.colorName}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun imageResource(assetName: String?): Int? = when (assetName) {
    "demo_top_shirt_white" -> R.drawable.demo_top_shirt_white
    "demo_top_shirt_navy" -> R.drawable.demo_top_shirt_navy
    "demo_top_tshirt_black" -> R.drawable.demo_top_tshirt_black
    "demo_top_tshirt_white" -> R.drawable.demo_top_tshirt_white
    "demo_top_hoodie_gray" -> R.drawable.demo_top_hoodie_gray
    "demo_top_blazer_black" -> R.drawable.demo_top_blazer_black
    "demo_bottom_chinos_cream" -> R.drawable.demo_bottom_chinos_cream
    "demo_bottom_jeans_blue" -> R.drawable.demo_bottom_jeans_blue
    "demo_bottom_formal_black" -> R.drawable.demo_bottom_formal_black
    "demo_bottom_cargo_olive" -> R.drawable.demo_bottom_cargo_olive
    "demo_outer_denim" -> R.drawable.demo_outer_denim
    "demo_outer_cardigan_cream" -> R.drawable.demo_outer_cardigan_cream
    "demo_outer_bomber_black" -> R.drawable.demo_outer_bomber_black
    "demo_sport_jersey_blue" -> R.drawable.demo_sport_jersey_blue
    "demo_sport_jogger_black" -> R.drawable.demo_sport_jogger_black
    "demo_sport_windbreaker_sage" -> R.drawable.demo_sport_windbreaker_sage
    "demo_other_batik_brown" -> R.drawable.demo_other_batik_brown
    "demo_other_trousers_charcoal" -> R.drawable.demo_other_trousers_charcoal
    "demo_work_striped_lightblue" -> R.drawable.demo_work_striped_lightblue
    "demo_hangout_flannel_maroon" -> R.drawable.demo_hangout_flannel_maroon
    "demo_street_graphic_mustard" -> R.drawable.demo_street_graphic_mustard
    "demo_hangout_chino_terracotta" -> R.drawable.demo_hangout_chino_terracotta
    else -> null
}
