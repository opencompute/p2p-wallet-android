package org.p2p.wallet.send.ui.search.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import com.bumptech.glide.Glide
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemSearchInvalidResultBinding
import org.p2p.wallet.send.model.SearchResult
import org.p2p.wallet.utils.CUT_ADDRESS_SYMBOLS_COUNT
import org.p2p.wallet.utils.cutMiddle
import org.p2p.wallet.utils.toPx
import org.p2p.wallet.utils.viewbinding.getString
import org.p2p.wallet.utils.viewbinding.inflateViewBinding
import timber.log.Timber

class SearchErrorViewHolder(
    parent: ViewGroup,
    private val binding: ItemSearchInvalidResultBinding = parent.inflateViewBinding(attachToRoot = false),
) : RecyclerView.ViewHolder(binding.root) {

    private val iconPadding = 12.toPx()

    fun onBind(item: SearchResult) {
        with(binding) {
            textViewAddress.text = item.addressState.address.cutMiddle(CUT_ADDRESS_SYMBOLS_COUNT)

            when (item) {
                is SearchResult.OwnAddressError -> {
                    textViewDescription.setText(R.string.search_yourself_description)
                    textViewError.setText(R.string.search_yourself_error)
                    showWalletIcon()
                }
                is SearchResult.InvalidDirectAddress -> {
                    val description = getString(
                        R.string.search_no_other_tokens_description,
                        item.directToken.symbol
                    )
                    textViewDescription.text = description
                    textViewError.setText(R.string.search_no_other_tokens_error)
                    loadTokenIcon(item.directToken.iconUrl)
                }
                // We are expecting no to get any other types of results for now
                else -> {
                    Timber.w("Unexpected search result type: $item")
                }
            }
        }
    }

    private fun showWalletIcon() {
        binding.imageViewWallet.apply {
            setPadding(iconPadding, iconPadding, iconPadding, iconPadding)
            alpha = 0.3f
            setImageResource(R.drawable.ic_search_wallet)
        }
    }

    private fun loadTokenIcon(iconUrl: String?) {
        with(binding.imageViewWallet) {
            setPadding(0, 0, 0, 0)
            alpha = 1f

            Glide.with(this)
                .load(iconUrl)
                .circleCrop()
                .into(this)
        }
    }
}