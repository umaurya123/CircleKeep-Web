package com.circlekeep.ui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        // Show placeholder text first (visible if offline or loading)
        Text(
            "CircleKeep - Keeping you connected",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                AdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = com.circlekeep.BuildConfig.BANNER_AD_UNIT_ID
                    adListener = object : AdListener() {
                        override fun onAdLoaded() {
                            Log.d("CircleKeepAd", "Banner Ad loaded successfully")
                        }
                        override fun onAdFailedToLoad(error: LoadAdError) {
                            Log.e("CircleKeepAd", "Banner Ad failed to load: ${error.message} (Code: ${error.code})")
                        }
                        override fun onAdOpened() {
                            Log.d("CircleKeepAd", "Banner Ad opened")
                        }
                    }
                    Log.d("CircleKeepAd", "Requesting Banner Ad: ${com.circlekeep.BuildConfig.BANNER_AD_UNIT_ID}")
                    loadAd(AdRequest.Builder().build())
                }
            },
            update = { 
                // No need to load ad here, it causes redundant requests on recomposition
            }
        )
    }
}
