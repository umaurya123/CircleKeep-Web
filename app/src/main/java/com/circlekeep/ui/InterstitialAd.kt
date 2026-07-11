package com.circlekeep.ui

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

fun loadInterstitialAd(context: Context, onAdLoaded: (InterstitialAd) -> Unit) {
    val adRequest = AdRequest.Builder().build()
    InterstitialAd.load(
        context,
        "ca-app-pub-3940256099942544/1033173712", // Sample interstitial ID
        adRequest,
        object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                // Handle failure
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                onAdLoaded(interstitialAd)
            }
        }
    )
}

fun showInterstitialAd(activity: Activity, interstitialAd: InterstitialAd, onAdDismissed: () -> Unit) {
    interstitialAd.fullScreenContentCallback = object : com.google.android.gms.ads.FullScreenContentCallback() {
        override fun onAdDismissedFullScreenContent() {
            onAdDismissed()
        }

        override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
            onAdDismissed()
        }
    }
    interstitialAd.show(activity)
}
