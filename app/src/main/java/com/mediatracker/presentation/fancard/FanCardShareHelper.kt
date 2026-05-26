package com.mediatracker.presentation.fancard

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.core.content.FileProvider
import com.mediatracker.R
import java.io.File

object FanCardShareHelper {

    suspend fun shareBitmap(
        context: Context,
        graphicsLayer: GraphicsLayer,
    ) {
        val bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
        shareBitmap(context, bitmap)
    }

    private fun shareBitmap(context: Context, bitmap: Bitmap) {
        val file = File(context.cacheDir, "fancard_${System.currentTimeMillis()}.png")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
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
}
