package org.p2p.wallet.home.model

import org.p2p.core.token.Token
import org.p2p.core.utils.isMoreThan

/**
 * First element should always be USDC || USDT
 * Further, we sort by total in USD
 * If there is no USD rate for the token, we sort by its total amount
 * Other tokens are not user's, no sorting is applied
 * */
class TokenComparator : Comparator<Token> {

    override fun compare(o1: Token, o2: Token): Int = when {
        o1.isUSDC -> -1
        o2.isUSDC -> 1
        o1.isUSDT && !o2.isUSDC -> -1
        o2.isUSDT && !o2.isUSDC -> 1
        o1 is Token.Active && o2 is Token.Active -> compareActiveTokens(o1, o2)
        o1 is Token.Active -> -1
        o2 is Token.Active -> 1
        else -> 0
    }

    private fun compareActiveTokens(
        o1: Token.Active,
        o2: Token.Active
    ): Int = when {
        o1.totalInUsd != null && o2.totalInUsd != null -> if (o1.totalInUsd!!.isMoreThan(o2.totalInUsd!!)) -1 else 1
        o1.totalInUsd != null && o2.totalInUsd == null -> -1
        o1.totalInUsd == null && o2.totalInUsd != null -> 1
        else -> 0
    }
}
