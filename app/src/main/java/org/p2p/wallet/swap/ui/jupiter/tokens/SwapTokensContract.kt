package org.p2p.wallet.swap.ui.jupiter.tokens

import org.p2p.uikit.model.AnyCellItem
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView

interface SwapTokensContract {
    interface View : MvpView {
        fun setTokenItems(items: List<AnyCellItem>)
    }

    interface Presenter : MvpPresenter<View> {
        fun onSearchTokenQueryChanged(newQuery: String)
        fun onTokenClicked()
    }
}