package com.p2p.wallet.main.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.R
import com.p2p.wallet.databinding.ItemTokenGroupBinding
import com.p2p.wallet.token.model.Token

class TokenGroupViewHolder(
    binding: ItemTokenGroupBinding,
    private val isZerosHidden: Boolean,
    private val onItemClicked: (Token) -> Unit,
    private val onEditClicked: (Token) -> Unit,
    private val onHideClicked: (Token) -> Unit
) : RecyclerView.ViewHolder(binding.root) {

    constructor(
        parent: ViewGroup,
        isZerosHidden: Boolean,
        onItemClicked: (Token) -> Unit,
        onEditClicked: (Token) -> Unit,
        onDeleteClicked: (Token) -> Unit
    ) : this(
        binding = ItemTokenGroupBinding.inflate(LayoutInflater.from(parent.context), parent, false),
        isZerosHidden = isZerosHidden,
        onItemClicked = onItemClicked,
        onEditClicked = onEditClicked,
        onHideClicked = onDeleteClicked
    )

    private val shownView = binding.shownView
    private val hiddenView = binding.hiddenView
    private val groupTextView = binding.groupTextView
    private val hiddenRecyclerView = binding.hiddenRecyclerView

    private val tokenAdapter: TokenHiddenAdapter by lazy {
        TokenHiddenAdapter(
            isZerosHidden = isZerosHidden,
            onItemClicked = { onItemClicked(it) },
            onEditClicked = { onEditClicked(it) },
            onDeleteClicked = { onHideClicked(it) }
        )
    }

    private val tokenLayoutManager = LinearLayoutManager(itemView.context)

    fun onBind(group: TokenAdapter.Companion.TokenAdapterItem.HiddenGroup) {
        val tokens = group.tokens
        val resources = itemView.context.resources
        groupTextView.text = resources.getQuantityString(R.plurals.hidden_wallets, tokens.size, tokens.size)

        with(hiddenRecyclerView) {
            layoutManager = tokenLayoutManager
            adapter = tokenAdapter
        }

        tokenAdapter.setItems(tokens)

        hiddenView.setOnClickListener {
            shownView.isVisible = true
            hiddenRecyclerView.isVisible = true
            hiddenView.isVisible = false
        }

        shownView.setOnClickListener {
            hiddenView.isVisible = true
            hiddenRecyclerView.isVisible = false
            shownView.isVisible = false
        }
    }

    fun onViewRecycled() {
        hiddenRecyclerView.layoutManager = null
        hiddenRecyclerView.adapter = null
    }
}