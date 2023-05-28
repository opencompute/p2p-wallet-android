package org.p2p.wallet.striga.model

sealed interface StrigaDataLayerResult<out T> {
    data class Success<T>(val value: T) : StrigaDataLayerResult<T>
    class Failure<T>(val error: StrigaDataLayerError) : StrigaDataLayerResult<T>

    @Throws(StrigaDataLayerError::class)
    fun unwrap(): T = when (this) {
        is Success -> value
        is Failure -> throw error
    }
}

fun <T, E : StrigaDataLayerError> E.toFailureResult(): StrigaDataLayerResult.Failure<T> =
    StrigaDataLayerResult.Failure(this)

fun <T> T.toSuccessResult(): StrigaDataLayerResult.Success<T> =
    StrigaDataLayerResult.Success(this)