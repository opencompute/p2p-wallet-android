package org.p2p.wallet.moonpay.clientsideapi

import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayBuyCurrencyResponse
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayCurrencyResponse
import org.p2p.wallet.moonpay.clientsideapi.response.MoonpayIpAddressResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MoonpayClientSideApi {

    @GET("v3/currencies/{currencyCode}/buy_quote/")
    suspend fun getBuyCurrency(
        @Path("currencyCode") quoteCurrencyCode: String,
        @Query("apiKey") apiKey: String,
        @Query("baseCurrencyAmount") baseCurrencyAmount: String?,
        @Query("quoteCurrencyAmount") quoteCurrencyAmount: String?,
        @Query("baseCurrencyCode") baseCurrencyCode: String,
        @Query("paymentMethod") paymentMethod: String,
        @Query("fixed") fixed: String = "true",
        @Query("regionalPricing") regionalPricing: String = "true",
        @Query("areFeesIncluded") areFeesIncluded: String = "true"
    ): MoonpayBuyCurrencyResponse

    @GET("v3/currencies/{currencyCode}/ask_price/")
    suspend fun getCurrencyAskPrice(
        @Path("currencyCode") quoteCurrencyCode: String,
        @Query("apiKey") apiKey: String
    ): MoonpayCurrencyResponse

    @GET("v4/ip_address/")
    suspend fun getIpAddress(
        @Query("apiKey") apiKey: String
    ): MoonpayIpAddressResponse
}