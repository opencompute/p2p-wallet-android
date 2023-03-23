package org.p2p.wallet.newsend.statemachine

import java.math.BigDecimal
import org.p2p.wallet.newsend.statemachine.model.SendToken

sealed interface SendFeatureAction {

    object InitFeature : SendFeatureAction
    object RefreshFee : SendFeatureAction

    data class NewToken(
        val token: SendToken
    ) : SendFeatureAction

    data class AmountChange(
        val amount: BigDecimal
    ) : SendFeatureAction

    object ZeroAmount : SendFeatureAction
    object MaxAmount : SendFeatureAction
}