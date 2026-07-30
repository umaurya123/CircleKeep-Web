package com.circlekeep

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class BillingManager(private val context: Context, private val onPurchaseSuccess: () -> Unit) {

    private val billingScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    
    private val _isBillingReady = MutableStateFlow(false)
    val isBillingReady: StateFlow<Boolean> = _isBillingReady

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
    }

    private var billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    init {
        startConnection()
    }

    private fun startConnection() {
        Log.d("CircleKeepBilling", "Starting BillingClient connection...")
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.d("CircleKeepBilling", "onBillingSetupFinished: ${billingResult.responseCode}, ${billingResult.debugMessage}")
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    _isBillingReady.value = true
                    queryPurchases()
                }
            }

            override fun onBillingServiceDisconnected() {
                Log.d("CircleKeepBilling", "onBillingServiceDisconnected")
                _isBillingReady.value = false
            }
        })
    }

    fun queryPurchases() {
        if (!billingClient.isReady) return

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingScope.launch {
            val result = billingClient.queryPurchasesAsync(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in result.purchasesList) {
                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        handlePurchase(purchase)
                    }
                }
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            
            billingScope.launch {
                val billingResult = billingClient.acknowledgePurchase(acknowledgePurchaseParams)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    onPurchaseSuccess()
                }
            }
        } else if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            onPurchaseSuccess()
        }
    }

    fun launchPurchaseFlow(activity: Activity, productId: String) {
        Log.d("CircleKeepBilling", "launchPurchaseFlow called for $productId")
        if (!_isBillingReady.value) {
            Log.d("CircleKeepBilling", "Billing not ready, attempting to reconnect")
            Toast.makeText(context, "Billing not ready. Check Internet/Play Store login.", Toast.LENGTH_SHORT).show()
            startConnection()
            return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingScope.launch {
            val result = billingClient.queryProductDetails(params)
            val billingResult = result.billingResult
            val productDetailsList = result.productDetailsList
            
            Log.d("CircleKeepBilling", "queryProductDetails result: ${billingResult.responseCode}, ${billingResult.debugMessage}")
            
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetails = productDetailsList?.firstOrNull()
                if (productDetails == null) {
                    Log.e("CircleKeepBilling", "Product '$productId' not found in Play Console list")
                    Toast.makeText(context, "Product '$productId' not found in Play Console", Toast.LENGTH_LONG).show()
                    return@launch
                }
                
                Log.d("CircleKeepBilling", "Launching billing flow...")
                val productDetailsParamsList = listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build()
                )

                val billingFlowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(productDetailsParamsList)
                    .build()

                billingClient.launchBillingFlow(activity, billingFlowParams)
            } else {
                Log.e("CircleKeepBilling", "Error querying product: ${billingResult.responseCode}, ${billingResult.debugMessage}")
                Toast.makeText(context, "Error querying product: ${billingResult.debugMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
