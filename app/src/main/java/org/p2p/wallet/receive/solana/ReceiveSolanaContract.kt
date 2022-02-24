package org.p2p.wallet.receive.solana

import android.graphics.Bitmap
import org.p2p.wallet.auth.model.Username
import org.p2p.wallet.common.mvp.MvpPresenter
import org.p2p.wallet.common.mvp.MvpView
import org.p2p.wallet.home.model.Token

interface ReceiveSolanaContract {

    interface View : MvpView {
        fun renderQr(qrBitmap: Bitmap?)
        fun showReceiveToken(token: Token.Active)
        fun showUserData(userPublicKey: String, username: Username?)
        fun showFullScreenLoading(isLoading: Boolean)
        fun showQrLoading(isLoading: Boolean)
        fun showToastMessage(resId: Int)
        fun showNetwork()
        fun showBrowser(url: String)
    }

    interface Presenter : MvpPresenter<View> {
        fun loadData()
        fun saveQr(name: String, bitmap: Bitmap)
        fun onNetworkClicked()
        fun onBrowserClicked(publicKey: String)
        fun onSaveClicked()
    }
}