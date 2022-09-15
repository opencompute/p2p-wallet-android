package org.p2p.wallet.infrastructure.account

import android.content.SharedPreferences
import androidx.core.content.edit
import com.google.gson.Gson
import org.p2p.wallet.common.crypto.keystore.KeyStoreWrapper
import kotlin.reflect.KClass

class AccountStorage(
    private val keyStoreWrapper: KeyStoreWrapper,
    private val sharedPreferences: SharedPreferences,
    private val gson: Gson
) : AccountStorageContract {

    override fun <T> saveObject(key: String, data: T) {
        val objectAsJson = gson.toJson(data)
        val encodedData = keyStoreWrapper.encode(key, objectAsJson)
        sharedPreferences.edit { putString(key, encodedData) }
    }

    @Throws(IllegalArgumentException::class)
    override fun <T : Any> getObject(key: String, type: KClass<T>): T? {
        return sharedPreferences.getString(key, null)
            ?.let { keyStoreWrapper.decode(key, it) }
            ?.let { gson.fromJson(it, type.java) }
    }

    override fun contains(key: String): Boolean = sharedPreferences.contains(key)
}
