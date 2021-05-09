package com.p2p.wallet.dashboard.ui.dialog.swap.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.databinding.ItemSwapTokenBinding
import com.p2p.wallet.token.model.Token
import com.p2p.wallet.utils.bindadapter.imageSourceRadiusDp

class SelectTokenToSwapAdapter(
    private val selectedWalletItem: Token
) : RecyclerView.Adapter<SelectTokenToSwapAdapter.ViewHolder>() {

    private val swapToTokenItems: MutableList<Token> = mutableListOf()
    private val swapToTokenItemsInitial: MutableList<Token> = mutableListOf()
    private var onItemClick: ((selectedWalletItem: Token) -> Unit)? = null

    inner class ViewHolder(private val binding: ItemSwapTokenBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position: Int) {
            val item = swapToTokenItems[position]
            binding.apply {
                txtTokenName.text = item.tokenName
                txtTokenSymbol.text = item.tokenSymbol
                imgTokenSwap.imageSourceRadiusDp(item.iconUrl, 12)
                root.setOnClickListener { onItemClick?.invoke(swapToTokenItems[position]) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemSwapTokenBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return swapToTokenItems.size
    }

    private fun filterItemsByName(name: String) {
        if (name.isNotEmpty()) {
            val swapToTokenItemsFiltered: MutableList<Token> = ArrayList()
            swapToTokenItemsInitial.forEach {
                if (it.tokenName.contains(name, true)) {
                    swapToTokenItemsFiltered.add(it)
                }
            }
            updateItems(swapToTokenItemsFiltered)
        } else {
            updateItems(swapToTokenItemsInitial)
        }
    }

    private fun updateItems(walletItems: Collection<Token>) {
        val walletItemsWithoutFrom = walletItems.toMutableList()
        walletItemsWithoutFrom.removeAll { it.mintAddress == selectedWalletItem.mintAddress }
        swapToTokenItems.clear()
        swapToTokenItems.addAll(walletItemsWithoutFrom)
        notifyDataSetChanged()
    }

    fun setSearchBarEditText(editText: AppCompatEditText) {
        editText.doAfterTextChanged {
            filterItemsByName(it.toString())
        }
    }

    fun initList(walletItems: Collection<Token>) {
        swapToTokenItemsInitial.apply {
            clear()
            addAll(walletItems)
        }
        updateItems(swapToTokenItemsInitial)
    }

    fun setOnItemClickListener(clickEvent: (selectedWalletItem: Token) -> Unit) {
        onItemClick = clickEvent
    }
}