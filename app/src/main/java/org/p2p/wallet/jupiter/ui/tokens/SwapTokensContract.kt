package org.p2p.wallet.jupiter.ui.tokens

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.jupiter.interactor.model.SwapTokenModel

interface SwapTokensContract {
    interface View : MvpView {
        fun setTokenItems(items: List<AnyCellItem>)
        fun showEmptyState(isEmpty: Boolean)
        fun close()
    }

    interface Presenter : MvpPresenter<View> {
        fun onSearchTokenQueryChanged(newQuery: String)
        fun onTokenClicked(clickedToken: SwapTokenModel)
    }
}
