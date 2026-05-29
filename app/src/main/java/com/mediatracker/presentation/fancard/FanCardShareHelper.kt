package com.mediatracker.presentation.fancard

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.core.content.FileProvider
import com.mediatracker.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object FanCardShareHelper {

    suspend fun shareBitmap(
        context: Context,
        graphicsLayer: GraphicsLayer,
    ) {
        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
        shareBitmap(context, bitmap)
    }

    private suspend fun shareBitmap(context: Context, bitmap: Bitmap) {
        val file = withContext(Dispatchers.IO) {
            cleanOldCache(context)
            val f = File(context.cacheDir, "fancard_${System.currentTimeMillis()}.png")
            f.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            f
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file,
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(
            Intent.createChooser(shareIntent, context.getString(R.string.fancard_share_title))
        )
    }

    private fun cleanOldCache(context: Context) {
        val cacheDir = context.cacheDir
        val oneHourAgo = System.currentTimeMillis() - 3600_000
        cacheDir.listFiles()
            ?.filter { it.isFile && it.name.startsWith("fancard_") && it.lastModified() < oneHourAgo }
            ?.forEach { it.delete() }
    }
}
