package org.p2p.wallet.swap.ui.jupiter.tokens.presenter

import kotlinx.coroutines.launch
import org.p2p.wallet.R
import org.p2p.wallet.common.mvp.BasePresenter
import org.p2p.wallet.swap.jupiter.interactor.SwapTokensInteractor
import org.p2p.wallet.swap.jupiter.interactor.model.SwapTokenModel
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensContract
import org.p2p.wallet.swap.ui.jupiter.tokens.SwapTokensListMode

class SwapTokensPresenter(
    private val tokenToChange: SwapTokensListMode,
    private val mapper: SwapTokensMapper,
    private val searchResultMapper: SearchSwapTokensMapper,
    private val interactor: SwapTokensInteractor,
) : BasePresenter<SwapTokensContract.View>(), SwapTokensContract.Presenter {

    private var allTokens: List<SwapTokenModel> = emptyList()
    private var currentToken: SwapTokenModel? = null

    override fun attach(view: SwapTokensContract.View) {
        super.attach(view)
        launch {
            try {
                initialLoad()
            } catch (error: Throwable) {
                view.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    private suspend fun initialLoad() {
        val currentTokenToSwapTokens = when (tokenToChange) {
            SwapTokensListMode.TOKEN_A -> interactor.getCurrentTokenA() to interactor.getAllTokensA()
            SwapTokensListMode.TOKEN_B -> interactor.getCurrentTokenB() to interactor.getAllAvailableTokensB()
        }
        currentToken = currentTokenToSwapTokens.first
        allTokens = currentTokenToSwapTokens.second
        renderAllSwapTokensList(allTokens)
    }

    private fun renderAllSwapTokensList(tokens: List<SwapTokenModel>) {
        val cellItems = mapper.toCellItems(
            chosenToken = currentToken ?: return,
            swapTokens = tokens
        )
        view?.setTokenItems(cellItems)
    }

    override fun onSearchTokenQueryChanged(newQuery: String) {
        if (newQuery.isBlank()) {
            renderAllSwapTokensList(allTokens)
        } else {
            renderSearchTokenList(newQuery)
        }
    }

    private fun renderSearchTokenList(newQuery: String) {
        launch {
            try {
                val searchResult = interactor.searchToken(tokenToChange, newQuery)
                val cellItems = searchResultMapper.toCellItems(searchResult)
                view?.setTokenItems(cellItems)
            } catch (error: Throwable) {
                view?.setTokenItems(emptyList())
                view?.showUiKitSnackBar(messageResId = R.string.error_general_message)
            }
        }
    }

    override fun onTokenClicked(clickedToken: SwapTokenModel) {
        view?.showUiKitSnackBar(clickedToken.tokenName)
    }
}
