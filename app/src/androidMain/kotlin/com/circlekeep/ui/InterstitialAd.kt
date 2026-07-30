package com.circlekeep.ui

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

fun loadInterstitialAd(context: Context, onAdResult: (InterstitialAd?) -> Unit) {
    Log.d("CircleKeepAd", "Requesting Interstitial Ad: ${com.circlekeep.BuildConfig.INTERSTITIAL_AD_UNIT_ID}")
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        context,
        com.circlekeep.BuildConfig.INTERSTITIAL_AD_UNIT_ID,
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.e("CircleKeepAd", "Interstitial Ad failed to load: ${adError.message} (Code: ${adError.code})")
                onAdResult(null)
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d("CircleKeepAd", "Interstitial Ad loaded successfully")
                onAdResult(interstitialAd)
            }
        }
    )
}

fun showInterstitialAd(activity: Activity, interstitialAd: InterstitialAd, onAdDismissed: () -> Unit) {
    Log.d("CircleKeepAd", "Showing Interstitial Ad")
    interstitialAd.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            Log.d("CircleKeepAd", "Interstitial Ad dismissed")
            onAdDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
            Log.e("CircleKeepAd", "Interstitial Ad failed to show: ${adError.message}")
            onAdDismissed()
        }
        
        override fun onAdShowedFullScreenContent() {
            Log.d("CircleKeepAd", "Interstitial Ad showed full screen content")
        }
    }
    interstitialAd.show(activity)
}
