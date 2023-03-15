package org.p2p.wallet.bridge.repository

import com.google.gson.Gson
import java.net.URI
import org.p2p.core.rpc.JsonRpc
import org.p2p.core.rpc.RpcApi
import org.p2p.wallet.bridge.api.mapper.BridgeServiceErrorMapper
import org.p2p.wallet.bridge.model.BridgeResult
import org.p2p.wallet.infrastructure.network.environment.NetworkServicesUrlProvider

class BridgeRemoteRepository(
    private val api: RpcApi,
    private val gson: Gson,
    private val errorMapper: BridgeServiceErrorMapper,
    private val urlProvider: NetworkServicesUrlProvider,
) : BridgeRepository {
    private val bridgeStringUrl = urlProvider.loadBridgesServiceEnvironment().baseUrl
    private val bridgeUrl = URI(bridgeStringUrl)

    override suspend fun <P, T> launch(request: JsonRpc<P, T>): BridgeResult.Success<T> {
        try {
            val requestGson = gson.toJson(request)
            val response = api.launch(bridgeUrl, jsonRpc = requestGson)
            val result = request.parseResponse(response, gson)
            return BridgeResult.Success(result)
        } catch (e: JsonRpc.ResponseError.RpcError) {
            val errorCode = e.error.code
            throw errorMapper.parseError(errorCode)
        } catch (e: Throwable) {
            throw BridgeResult.Error.InternalServiceError
        }
    }
}
