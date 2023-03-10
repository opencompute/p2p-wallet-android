package org.p2p.ethereumkit.internal.api.jsonrpc

open class LongJsonRpc(
        method: String, params: List<Any>
) : JsonRpc<Long>(method, params) {
    @Transient
    override val typeOfResult = Long::class.java
}