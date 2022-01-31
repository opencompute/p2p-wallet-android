package org.p2p.wallet.swap.ui.settings

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import org.p2p.wallet.R
import org.p2p.wallet.databinding.ItemFeePaymentTokenBinding
import org.p2p.wallet.main.model.Token

class SwapSettingsTokensAdapter(
    private val selectedSymbol: String,
    private val onTokenSelected: (Token.Active) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val data = mutableListOf<Token.Active>()

    private var selectedIndex: Int = -1

    @SuppressLint("NotifyDataSetChanged")
    fun setItems(new: List<Token.Active>) {
        data.clear()
        data.addAll(new)

        selectedIndex = new.indexOfFirst { it.tokenSymbol == selectedSymbol }

        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        ViewHolder(parent)

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolder).onBind(
            item = data[position],
            onTokenSelected = {
                notifyDataSetChanged()
                onTokenSelected.invoke(it)
            }
        )
    }

    inner class ViewHolder(
        binding: ItemFeePaymentTokenBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        constructor(parent: ViewGroup) : this(
            ItemFeePaymentTokenBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )

        private val radioButton = binding.radioButton
        private val titleTextView = binding.titleTextView
        private val subTitleTextView = binding.subTitleTextView

        fun onBind(item: Token.Active, onTokenSelected: (Token.Active) -> Unit) {
            radioButton.post { radioButton.isChecked = adapterPosition == selectedIndex }
            titleTextView.text = item.tokenSymbol
            if (item.isSOL) {
                val available = itemView.context.getString(R.string.swap_available_format, item.total.toPlainString())
                subTitleTextView.text = available
            } else {
                subTitleTextView.setText(R.string.swap_outcomes_from_the_transaction)
            }

            itemView.setOnClickListener {
                selectedIndex = adapterPosition
                onTokenSelected.invoke(item)
            }
        }
    }
}