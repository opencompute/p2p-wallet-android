package org.p2p.wallet.solend.ui.earn.bottomsheet

import kotlinx.coroutines.launch
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.home.model.Token
import org.p2p.wallet.solend.model.SolendDepositToken
import org.p2p.wallet.user.interactor.UserInteractor

class SolendTopUpBottomSheetPresenter(
    private val deposit: SolendDepositToken,
    private val userInteractor: UserInteractor
) : BasePresenter<SolendTopUpBottomSheetContract.View>(),
    SolendTopUpBottomSheetContract.Presenter {

    private var currentToken: Token? = null
    private var solToken: Token? = null

    override fun attach(view: SolendTopUpBottomSheetContract.View) {
        super.attach(view)
        launch {
            currentToken = userInteractor.getTokensForBuy(listOf(deposit.tokenSymbol)).firstOrNull()
            solToken = userInteractor.getUserSolToken()
        }
    }

    override fun onBuyClicked() {
        val tokenToBuy = currentToken?.takeIf { it.isUSDC } ?: solToken
        tokenToBuy?.let { token ->
            view?.showBuyScreen(token)
        }
    }

    override fun onReceiveClicked() {
        currentToken?.let { token ->
            view?.showReceiveScreen(token)
        }
    }
}