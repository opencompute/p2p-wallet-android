package org.p2p.wallet.swap.interactor.orca

import org.p2p.solanaj.core.PublicKey
import org.p2p.solanaj.core.TransactionInstruction
import org.p2p.solanaj.programs.TokenProgram
import org.p2p.wallet.swap.model.OrcaInstructionsData

class OrcaInstructionsInteractor(
    private val orcaAddressInteractor: OrcaAddressInteractor,
) {

    suspend fun buildDestinationInstructions(
        owner: PublicKey,
        destination: PublicKey?,
        destinationMint: PublicKey,
        feePayer: PublicKey,
        closeAfterward: Boolean
    ): OrcaInstructionsData {
        val instructions = mutableListOf<TransactionInstruction>()

        // if destination is a registered non-native token account
        if (destination != null && !destination.equals(owner)) {
            return OrcaInstructionsData(destination, instructions)
        }

        // if destination is a native account or is nil
        val addressData = orcaAddressInteractor.findAssociatedAddress(owner, destinationMint.toBase58())

        if (addressData.shouldCreateAssociatedInstruction) {
            val createAccount = TokenProgram.createAssociatedTokenAccountInstruction(
                TokenProgram.ASSOCIATED_TOKEN_PROGRAM_ID,
                TokenProgram.PROGRAM_ID,
                destinationMint,
                addressData.associatedAddress,
                feePayer,
                feePayer
            )

            instructions.add(createAccount)
        }

        val closeInstructions = mutableListOf<TransactionInstruction>()
        if (closeAfterward) {
            closeInstructions += TokenProgram.closeAccountInstruction(
                TokenProgram.PROGRAM_ID,
                addressData.associatedAddress,
                owner,
                owner
            )
        }

        return OrcaInstructionsData(addressData.associatedAddress, instructions)
    }
}