package com.p2p.wallet.history.ui.main.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.p2p.wallet.databinding.ItemHistoryEmptyBinding

class EmptyViewHolder(
    binding: ItemHistoryEmptyBinding
) : RecyclerView.ViewHolder(binding.root) {

    constructor(parent: ViewGroup) : this(
        ItemHistoryEmptyBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )
}