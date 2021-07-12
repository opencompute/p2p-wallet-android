package com.p2p.wallet.user.model

import com.p2p.wallet.utils.valueOrZero
import com.p2p.wallet.main.api.SinglePriceResponse
import com.p2p.wallet.main.model.TokenPrice
import java.math.BigDecimal

object UserConverter {

    fun fromNetwork(tokenSymbol: String, response: SinglePriceResponse): TokenPrice = when (tokenSymbol) {
        "USD" -> TokenPrice(tokenSymbol, response.usdValue.valueOrZero())
        "SOL" -> TokenPrice(tokenSymbol, response.SOL.valueOrZero())
        "BTC" -> TokenPrice(tokenSymbol, response.BTC.valueOrZero())
        "SRM" -> TokenPrice(tokenSymbol, response.SRM.valueOrZero())
        "MSRM" -> TokenPrice(tokenSymbol, response.MSRM.valueOrZero())
        "ETH" -> TokenPrice(tokenSymbol, response.ETH.valueOrZero())
        "FTT" -> TokenPrice(tokenSymbol, response.FTT.valueOrZero())
        "YFI" -> TokenPrice(tokenSymbol, response.YFI.valueOrZero())
        "LINK" -> TokenPrice(tokenSymbol, response.LINK.valueOrZero())
        "XRP" -> TokenPrice(tokenSymbol, response.XRP.valueOrZero())
        "USDT" -> TokenPrice(tokenSymbol, response.USDT.valueOrZero())
        "USDC" -> TokenPrice(tokenSymbol, response.USDC.valueOrZero())
        "WUSDC" -> TokenPrice(tokenSymbol, response.WUSDC.valueOrZero())
        "SUSHI" -> TokenPrice(tokenSymbol, response.SUSHI.valueOrZero())
        "ALEPH" -> TokenPrice(tokenSymbol, response.ALEPH.valueOrZero())
        "SXP" -> TokenPrice(tokenSymbol, response.SXP.valueOrZero())
        "HGET" -> TokenPrice(tokenSymbol, response.HGET.valueOrZero())
        "CREAM" -> TokenPrice(tokenSymbol, response.CREAM.valueOrZero())
        "UBXT" -> TokenPrice(tokenSymbol, response.UBXT.valueOrZero())
        "HNT" -> TokenPrice(tokenSymbol, response.HNT.valueOrZero())
        "FRONT" -> TokenPrice(tokenSymbol, response.FRONT.valueOrZero())
        "AKRO" -> TokenPrice(tokenSymbol, response.AKRO.valueOrZero())
        "HXRO" -> TokenPrice(tokenSymbol, response.HXRO.valueOrZero())
        "UNI" -> TokenPrice(tokenSymbol, response.UNI.valueOrZero())
        "MATH" -> TokenPrice(tokenSymbol, response.MATH.valueOrZero())
        "TOMO" -> TokenPrice(tokenSymbol, response.TOMO.valueOrZero())
        "LUA" -> TokenPrice(tokenSymbol, response.LUA.valueOrZero())
        "KARMA" -> TokenPrice(tokenSymbol, response.KARMA.valueOrZero())
        "KEEP" -> TokenPrice(tokenSymbol, response.KEEP.valueOrZero())
        "SWAG" -> TokenPrice(tokenSymbol, response.SWAG.valueOrZero())
        "CEL" -> TokenPrice(tokenSymbol, response.CEL.valueOrZero())
        "RSR" -> TokenPrice(tokenSymbol, response.RSR.valueOrZero())
        "1INCH" -> TokenPrice(tokenSymbol, response.`1INCH`.valueOrZero())
        "GRT" -> TokenPrice(tokenSymbol, response.GRT.valueOrZero())
        "COMP" -> TokenPrice(tokenSymbol, response.COMP.valueOrZero())
        "PAXG" -> TokenPrice(tokenSymbol, response.PAXG.valueOrZero())
        "STRONG" -> TokenPrice(tokenSymbol, response.STRONG.valueOrZero())
        "FIDA" -> TokenPrice(tokenSymbol, response.FIDA.valueOrZero())
        "KIN" -> TokenPrice(tokenSymbol, response.KIN.valueOrZero())
        "MAPS" -> TokenPrice(tokenSymbol, response.MAPS.valueOrZero())
        "OXY" -> TokenPrice(tokenSymbol, response.OXY.valueOrZero())
        "BRZ" -> TokenPrice(tokenSymbol, response.BRZ.valueOrZero())
        "RAY" -> TokenPrice(tokenSymbol, response.RAY.valueOrZero())
        "PERK" -> TokenPrice(tokenSymbol, response.PERK.valueOrZero())
        "BTSG" -> TokenPrice(tokenSymbol, response.BTSG.valueOrZero())
        "BVOL" -> TokenPrice(tokenSymbol, response.BVOL.valueOrZero())
        "IBVOL" -> TokenPrice(tokenSymbol, response.IBVOL.valueOrZero())
        "AAVE" -> TokenPrice(tokenSymbol, response.AAVE.valueOrZero())
        "SECO" -> TokenPrice(tokenSymbol, response.SECO.valueOrZero())
        "SDOGE" -> TokenPrice(tokenSymbol, response.SDOGE.valueOrZero())
        "SAMO" -> TokenPrice(tokenSymbol, response.SAMO.valueOrZero())
        "ISA" -> TokenPrice(tokenSymbol, response.ISA.valueOrZero())
        "RECO" -> TokenPrice(tokenSymbol, response.RECO.valueOrZero())
        "NINJA" -> TokenPrice(tokenSymbol, response.NINJA.valueOrZero())
        "SLIM" -> TokenPrice(tokenSymbol, response.SLIM.valueOrZero())
        "QUEST" -> TokenPrice(tokenSymbol, response.QUEST.valueOrZero())
        "SPD" -> TokenPrice(tokenSymbol, response.SPD.valueOrZero())
        "STEP" -> TokenPrice(tokenSymbol, response.STEP.valueOrZero())
        "MEDIA" -> TokenPrice(tokenSymbol, response.MEDIA.valueOrZero())
        "SLOCK" -> TokenPrice(tokenSymbol, response.SLOCK.valueOrZero())
        "ROPE" -> TokenPrice(tokenSymbol, response.ROPE.valueOrZero())
        "DOCE" -> TokenPrice(tokenSymbol, response.DOCE.valueOrZero())
        "MCAPS" -> TokenPrice(tokenSymbol, response.MCAPS.valueOrZero())
        "COPE" -> TokenPrice(tokenSymbol, response.COPE.valueOrZero())
        "XCOPE" -> TokenPrice(tokenSymbol, response.XCOPE.valueOrZero())
        "AAPE" -> TokenPrice(tokenSymbol, response.AAPE.valueOrZero())
        "oDOP" -> TokenPrice(tokenSymbol, response.oDOP.valueOrZero())
        "RAYPOOL" -> TokenPrice(tokenSymbol, response.RAYPOOL.valueOrZero())
        "PERP" -> TokenPrice(tokenSymbol, response.PERP.valueOrZero())
        "OXYPOOL" -> TokenPrice(tokenSymbol, response.OXYPOOL.valueOrZero())
        "MAPSPOOL" -> TokenPrice(tokenSymbol, response.MAPSPOOL.valueOrZero())
        "LQID" -> TokenPrice(tokenSymbol, response.LQID.valueOrZero())
        "TRYB" -> TokenPrice(tokenSymbol, response.TRYB.valueOrZero())
        "HOLY" -> TokenPrice(tokenSymbol, response.HOLY.valueOrZero())
        "ENTROPPP" -> TokenPrice(tokenSymbol, response.ENTROPPP.valueOrZero())
        "FARM" -> TokenPrice(tokenSymbol, response.FARM.valueOrZero())
        "NOPE" -> TokenPrice(tokenSymbol, response.NOPE.valueOrZero())
        "STNK" -> TokenPrice(tokenSymbol, response.STNK.valueOrZero())
        "MEAL" -> TokenPrice(tokenSymbol, response.MEAL.valueOrZero())
        "SNY" -> TokenPrice(tokenSymbol, response.SNY.valueOrZero())
        "FROG" -> TokenPrice(tokenSymbol, response.FROG.valueOrZero())
        "CRT" -> TokenPrice(tokenSymbol, response.CRT.valueOrZero())
        "SKEM" -> TokenPrice(tokenSymbol, response.SKEM.valueOrZero())
        "SOLAPE" -> TokenPrice(tokenSymbol, response.SOLAPE.valueOrZero())
        "WOOF" -> TokenPrice(tokenSymbol, response.WOOF.valueOrZero())
        "MER" -> TokenPrice(tokenSymbol, response.MER.valueOrZero())
        "ACMN" -> TokenPrice(tokenSymbol, response.ACMN.valueOrZero())
        "MUDLEY" -> TokenPrice(tokenSymbol, response.MUDLEY.valueOrZero())
        "LOTTO" -> TokenPrice(tokenSymbol, response.LOTTO.valueOrZero())
        "BOLE" -> TokenPrice(tokenSymbol, response.BOLE.valueOrZero())
        "mBRZ" -> TokenPrice(tokenSymbol, response.mBRZ.valueOrZero())
        "mPLAT" -> TokenPrice(tokenSymbol, response.mPLAT.valueOrZero())
        "mDIAM" -> TokenPrice(tokenSymbol, response.mDIAM.valueOrZero())
        "APYS" -> TokenPrice(tokenSymbol, response.APYS.valueOrZero())
        "MIT" -> TokenPrice(tokenSymbol, response.MIT.valueOrZero())
        "PAD" -> TokenPrice(tokenSymbol, response.PAD.valueOrZero())
        "SHBL" -> TokenPrice(tokenSymbol, response.SHBL.valueOrZero())
        "AUSS" -> TokenPrice(tokenSymbol, response.AUSS.valueOrZero())
        "TULIP" -> TokenPrice(tokenSymbol, response.TULIP.valueOrZero())
        "JPYC" -> TokenPrice(tokenSymbol, response.JPYC.valueOrZero())
        "TYNA" -> TokenPrice(tokenSymbol, response.TYNA.valueOrZero())
        "ARDX" -> TokenPrice(tokenSymbol, response.ARDX.valueOrZero())
        "SSHIB" -> TokenPrice(tokenSymbol, response.SSHIB.valueOrZero())
        "SGI" -> TokenPrice(tokenSymbol, response.SGI.valueOrZero())
        "SOLT" -> TokenPrice(tokenSymbol, response.SOLT.valueOrZero())
        "KEKW" -> TokenPrice(tokenSymbol, response.KEKW.valueOrZero())
        "LOOP" -> TokenPrice(tokenSymbol, response.LOOP.valueOrZero())
        "BDE" -> TokenPrice(tokenSymbol, response.BDE.valueOrZero())
        "DWT" -> TokenPrice(tokenSymbol, response.DWT.valueOrZero())
        "DOGA" -> TokenPrice(tokenSymbol, response.DOGA.valueOrZero())
        "CHEEMS" -> TokenPrice(tokenSymbol, response.CHEEMS.valueOrZero())
        "SBFC" -> TokenPrice(tokenSymbol, response.SBFC.valueOrZero())
        "ECOP" -> TokenPrice(tokenSymbol, response.ECOP.valueOrZero())
        "CATO" -> TokenPrice(tokenSymbol, response.CATO.valueOrZero())
        "TOM" -> TokenPrice(tokenSymbol, response.TOM.valueOrZero())
        "FABLE" -> TokenPrice(tokenSymbol, response.FABLE.valueOrZero())
        "LZD" -> TokenPrice(tokenSymbol, response.LZD.valueOrZero())
        "FELON" -> TokenPrice(tokenSymbol, response.FELON.valueOrZero())
        "SLNDN" -> TokenPrice(tokenSymbol, response.SLNDN.valueOrZero())
        "SOLA" -> TokenPrice(tokenSymbol, response.SOLA.valueOrZero())
        "MPAD" -> TokenPrice(tokenSymbol, response.MPAD.valueOrZero())
        "SGT" -> TokenPrice(tokenSymbol, response.SGT.valueOrZero())
        "SOLDOG" -> TokenPrice(tokenSymbol, response.SOLDOG.valueOrZero())
        "LLAMA" -> TokenPrice(tokenSymbol, response.LLAMA.valueOrZero())
        "BOP" -> TokenPrice(tokenSymbol, response.BOP.valueOrZero())
        "MOLAMON" -> TokenPrice(tokenSymbol, response.MOLAMON.valueOrZero())
        "STUD" -> TokenPrice(tokenSymbol, response.STUD.valueOrZero())
        "RESP" -> TokenPrice(tokenSymbol, response.RESP.valueOrZero())
        "CHAD" -> TokenPrice(tokenSymbol, response.CHAD.valueOrZero())
        "DXL" -> TokenPrice(tokenSymbol, response.DXL.valueOrZero())
        "FUZ" -> TokenPrice(tokenSymbol, response.FUZ.valueOrZero())
        "STRANGE" -> TokenPrice(tokenSymbol, response.STRANGE.valueOrZero())
        "GRAPE" -> TokenPrice(tokenSymbol, response.GRAPE.valueOrZero())
        "KERMIT" -> TokenPrice(tokenSymbol, response.KERMIT.valueOrZero())
        "PIPANA" -> TokenPrice(tokenSymbol, response.PIPANA.valueOrZero())
        "CKC" -> TokenPrice(tokenSymbol, response.CKC.valueOrZero())
        "CHANGPENGUIN" -> TokenPrice(tokenSymbol, response.CHANGPENGUIN.valueOrZero())
        "KLB" -> TokenPrice(tokenSymbol, response.KLB.valueOrZero())
        else -> throw IllegalStateException("Unknown token symbol: $tokenSymbol")
    }

    private fun usdOrZero(response: SinglePriceResponse?): BigDecimal = response?.usdValue.valueOrZero()
}