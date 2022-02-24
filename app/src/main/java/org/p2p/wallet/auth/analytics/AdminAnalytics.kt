package org.p2p.wallet.auth.analytics

import org.p2p.wallet.common.analytics.Events.ADMIN_APP_CLOSED
import org.p2p.wallet.common.analytics.Events.ADMIN_APP_OPENED
import org.p2p.wallet.common.analytics.Events.ADMIN_PASSWORD_CREATED
import org.p2p.wallet.common.analytics.Events.ADMIN_PIN_CREATED
import org.p2p.wallet.common.analytics.Events.ADMIN_PIN_REJECTED
import org.p2p.wallet.common.analytics.Events.ADMIN_PUSH_RECEIVED
import org.p2p.wallet.common.analytics.Events.ADMIN_SIGNED_OUT
import org.p2p.wallet.common.analytics.Events.ADMIN_SIGN_OUT
import org.p2p.wallet.common.analytics.Events.ADMIN_SNACKBAR_RECEIVED
import org.p2p.wallet.common.analytics.TrackerContract

class AdminAnalytics(
    private val tracker: TrackerContract
) {

    fun logAppOpened(source: AppOpenSource) {
        tracker.logEvent(ADMIN_APP_OPENED, arrayOf(Pair("Source_Open", source.title)))
    }

    fun logAppClosed(lastScreenName: String) {
        tracker.logEvent(ADMIN_APP_CLOSED, arrayOf(Pair("Last_Screen", lastScreenName)))
    }

    fun logPushReceived(campaignName: String) {
        tracker.logEvent(ADMIN_PUSH_RECEIVED, arrayOf(Pair("Push_Campaign", campaignName)))
    }

    fun logSnackBarReceived(message: String) {
        tracker.logEvent(ADMIN_SNACKBAR_RECEIVED, arrayOf(Pair("Snackbar_Type", message)))
    }

    fun logSignOut(backupState: BackupState = BackupState.OFF) {
        tracker.logEvent(
            ADMIN_SIGN_OUT,
            arrayOf(
                Pair("Backup_State", backupState.title)
            )
        )
    }

    fun logSignedOut(backupState: BackupState = BackupState.OFF) {
        tracker.logEvent(ADMIN_SIGNED_OUT, arrayOf(Pair("Backup_State", backupState.title)))
    }

    // TODO determine about pin complex calculation
    fun logPinCreated(isPinComplex: Boolean = false, currentScreenName: String) {
        tracker.logEvent(
            ADMIN_PIN_CREATED,
            arrayOf(
                Pair("Pin_Complexity", isPinComplex),
                Pair("Current_Screen", currentScreenName)
            )
        )
    }

    fun logPinRejected(currentScreenName: String) {
        tracker.logEvent(
            ADMIN_PIN_REJECTED,
            arrayOf(
                Pair("Current_Screen", currentScreenName)
            )
        )
    }

    fun logPasswordCreated() {
        tracker.logEvent(ADMIN_PASSWORD_CREATED)
    }

    enum class AppOpenSource(val title: String) {
        DIRECT("Direct"),
        DEEPLINK("Deeplink"),
        PUSH("Push")
    }

    enum class BackupState(val title: String) {
        ON("On"),
        OFF("Off"),
        DISCARDED("Discarded"),
        NO_NEED("No_Need")
    }
}