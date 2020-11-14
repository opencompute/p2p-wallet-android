package com.p2p.wowlet.fragment.sendcoins.dialog

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.p2p.wowlet.R
import com.p2p.wowlet.databinding.DialogSendCoinDoneBinding
import com.p2p.wowlet.fragment.sendcoins.viewmodel.SendCoinsViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class SendCoinDoneDialog : DialogFragment() {

    private val sendCoinsViewModel: SendCoinsViewModel by viewModel()
    lateinit var binding: DialogSendCoinDoneBinding

    companion object {
        fun newInstance(): SendCoinDoneDialog = SendCoinDoneDialog()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.dialog_send_coin_done, container, false
        )
        binding.viewModel = sendCoinsViewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sendCoinsViewModel.getCoinList()
    }

    override fun onResume() {
        super.onResume()
        dialog?.run {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }
}