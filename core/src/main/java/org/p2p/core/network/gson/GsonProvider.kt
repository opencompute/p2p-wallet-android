package org.p2p.core.network.gson

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import org.p2p.core.wrapper.eth.EthAddress
import java.math.BigInteger
import org.p2p.core.token.SolAddress
import org.p2p.core.wrapper.HexString

class GsonProvider {

    var gson: Gson? = null

    fun provide(): Gson {
        return gson ?: buildGson()
    }

    private fun buildGson(): Gson {
        return GsonBuilder().apply {
            setLenient()
            registerTypeAdapter(BigInteger::class.java, BigIntegerTypeAdapter())
            registerTypeAdapter(object : TypeToken<BigInteger?>() {}.type, BigIntegerTypeAdapter())
            registerTypeAdapter(Long::class.java, LongTypeAdapter())
            registerTypeAdapter(Int::class.java, IntTypeAdapter())
            registerTypeAdapter(ByteArray::class.java, ByteArrayTypeAdapter())
            registerTypeAdapter(EthAddress::class.java, AddressTypeAdapter())
            registerTypeAdapter(SolAddress::class.java, SolAddressTypeAdapter())
            registerTypeAdapter(HexString::class.java, HexStringTypeAdapter())
        }.create().also {
            gson = it
        }
    }
}

