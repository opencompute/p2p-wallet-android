package org.p2p.wallet.history.ui.historylist

import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import android.content.Context
import android.util.AttributeSet
import com.google.android.material.snackbar.Snackbar
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.p2p.core.common.TextContainer
import org.p2p.core.glide.GlideManager
import org.p2p.uikit.utils.attachAdapter
import org.p2p.uikit.utils.inflateViewBinding
import org.p2p.uikit.utils.recycler.RoundedDecoration
import org.p2p.uikit.utils.toPx
import org.p2p.wallet.common.ui.recycler.EndlessScrollListener
import org.p2p.wallet.common.ui.recycler.PagingState
import org.p2p.wallet.databinding.LayoutHistoryListBinding
import org.p2p.wallet.history.ui.model.HistoryItem
import org.p2p.wallet.history.ui.token.adapter.HistoryAdapter
import org.p2p.wallet.jupiter.model.SwapOpenedFrom
import org.p2p.wallet.utils.unsafeLazy

class HistoryListView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ConstraintLayout(context, attrs, defStyleAttr), KoinComponent, HistoryListViewContract.View {

    private val binding = inflateViewBinding<LayoutHistoryListBinding>()

    private var listViewType: HistoryListViewType = HistoryListViewType.AllHistory

    private val glideManager: GlideManager by inject()

    private val historyAdapter: HistoryAdapter by unsafeLazy {
        HistoryAdapter(
            glideManager = glideManager,
            onHistoryItemClicked = presenter::onItemClicked,
            onRetryClicked = { presenter.loadHistory(listViewType) },
        )
    }

    private lateinit var presenter: HistoryListViewContract.Presenter

    private var clickListener: HistoryListViewClickListener? = null

    fun bind(
        presenter: HistoryListViewContract.Presenter,
        clickListener: HistoryListViewClickListener,
        listType: HistoryListViewType,
    ) {
        this.presenter = presenter
        this.listViewType = listType
        this.clickListener = clickListener

        presenter.attach(this)
        bindView()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        presenter.detach()
    }

    private fun bindView() = with(binding) {
        presenter.attach(listViewType)
        errorStateLayout.buttonRetry.setOnClickListener {
            presenter.refreshHistory(listViewType)
        }
        val scrollListener = EndlessScrollListener(
            layoutManager = historyRecyclerView.layoutManager as LinearLayoutManager,
            loadNextPage = { presenter.loadNextHistoryPage(listViewType) }
        )
        with(historyRecyclerView) {
            addOnScrollListener(scrollListener)
            attachAdapter(historyAdapter)
            addItemDecoration(RoundedDecoration(16f.toPx()))
        }
        refreshLayout.setOnRefreshListener {
            presenter.refreshHistory(listViewType)
            scrollListener.reset()
        }
        loadHistory()
    }

    fun loadHistory() {
        presenter.loadHistory(listViewType)
    }

    override fun showPagingState(state: PagingState) = with(binding) {
        historyAdapter.setPagingState(state)
        val isHistoryEmpty = historyAdapter.isEmpty()

        when (state) {
            is PagingState.InitialLoading -> {
                shimmerView.root.isVisible = true
                refreshLayout.isVisible = false
            }

            is PagingState.Idle -> {
                shimmerView.root.isVisible = false
                refreshLayout.isVisible = true
                errorStateLayout.root.isVisible = false
                emptyStateLayout.root.isVisible = isHistoryEmpty
                historyRecyclerView.isVisible = !isHistoryEmpty
            }

            is PagingState.Loading -> {
                shimmerView.root.isVisible = isHistoryEmpty
                refreshLayout.isVisible = true
                errorStateLayout.root.isVisible = false
                emptyStateLayout.root.isVisible = false
                historyRecyclerView.isVisible = !isHistoryEmpty
            }

            is PagingState.Error -> {
                shimmerView.root.isVisible = false
                refreshLayout.isVisible = true
                errorStateLayout.root.isVisible = isHistoryEmpty
                emptyStateLayout.root.isVisible = false
                historyRecyclerView.isVisible = !isHistoryEmpty
            }
        }
    }

    override fun showHistory(history: List<HistoryItem>) {
        with(binding) {
            historyAdapter.submitList(history)
            historyRecyclerView.invalidateItemDecorations()

            val isHistoryEmpty = historyAdapter.isEmpty()
            emptyStateLayout.root.isVisible = isHistoryEmpty
            historyRecyclerView.isVisible = !isHistoryEmpty
        }
    }

    override fun onTransactionClicked(transactionId: String) {
        clickListener?.onTransactionClicked(transactionId)
    }

    override fun onBridgeClaimClicked(transactionId: String) {
        clickListener?.onBridgeClaimClicked(transactionId)
    }

    override fun onBridgeSendClicked(transactionId: String) {
        clickListener?.onBridgeSendClicked(transactionId)
    }

    override fun onSellTransactionClicked(transactionId: String) {
        clickListener?.onSellTransactionClicked(transactionId)
    }

    override fun onSwapBannerItemClicked(
        sourceTokenMint: String,
        destinationTokenMint: String,
        sourceSymbol: String,
        destinationSymbol: String,
        openedFrom: SwapOpenedFrom
    ) {
        clickListener?.onSwapBannerClicked(
            sourceTokenMint = sourceTokenMint,
            destinationTokenMint = destinationTokenMint,
            sourceSymbol = sourceSymbol,
            destinationSymbol = destinationSymbol,
            openedFrom = openedFrom
        )
    }

    override fun onUserSendLinksClicked() {
        clickListener?.onUserSendLinksClicked()
    }

    override fun showRefreshing(isRefreshing: Boolean) = with(binding) {
        refreshLayout.isRefreshing = isRefreshing
        refreshLayoutProgressPlaceholder.isVisible = isRefreshing
    }

    override fun scrollToTop() {
        binding.historyRecyclerView.smoothScrollToPosition(0)
    }

    //region Not Needed Base Methods
    override fun showErrorMessage(e: Throwable?) = Unit
    override fun showErrorMessage(messageResId: Int) = Unit

    override fun showToast(message: TextContainer) = Unit
    override fun showUiKitSnackBar(
        message: String?,
        messageResId: Int?,
        onDismissed: () -> Unit,
        actionButtonResId: Int?,
        actionBlock: ((Snackbar) -> Unit)?,
    ) = Unit
    //endregion
}
