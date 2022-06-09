package org.p2p.wallet.deeplinks

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import androidx.fragment.app.FragmentManager
import org.p2p.wallet.R
import org.p2p.wallet.home.MainFragment
import org.p2p.wallet.notification.NotificationType
import org.p2p.wallet.root.RootActivity
import org.p2p.wallet.utils.toStringMap

class AppDeeplinksManager(private val context: Context) {

    companion object {
        const val NOTIFICATION_TYPE = "eventType"
        const val DEEPLINK_MAIN_SCREEN_EXTRA = "DEEPLINK_SCREEN_EXTRA"
    }

    var mainTabsSwitcher: MainTabsSwitcher? = null
    var mainFragmentManager: FragmentManager? = null

    private var pendingIntent: Intent? = null

    fun buildIntent(notificationType: NotificationType): Intent {
        val activityManager = context.getSystemService<ActivityManager>()
        val openedScreenOrRoot = activityManager?.appTasks
            ?.firstOrNull()
            ?.taskInfo
            ?.baseIntent
            ?: Intent(context, RootActivity::class.java)
        return openedScreenOrRoot.apply {
            addDeeplinkDataToIntent(notificationType)
        }
    }

    private fun Intent.addDeeplinkDataToIntent(notificationType: NotificationType) {
        if (notificationType == NotificationType.RECEIVE) {
            putExtra(DEEPLINK_MAIN_SCREEN_EXTRA, R.id.itemHistory)
        }
    }

    fun handleDeeplinkIntent(intent: Intent) {
        val extras = intent.extras ?: return
        // additional parsing when app been opened with notification from background
        if (extras.containsKey(NOTIFICATION_TYPE)) {
            val values = extras.toStringMap()
            val notificationType = NotificationType.fromValue(
                values[NOTIFICATION_TYPE].orEmpty()
            )
            intent.addDeeplinkDataToIntent(notificationType)
        }
        handleOrSaveDeeplinkIntent(intent)
    }

    private fun handleOrSaveDeeplinkIntent(intent: Intent) {
        intent.extras?.apply {
            if (containsKey(DEEPLINK_MAIN_SCREEN_EXTRA)) {
                popToMainScreen()
                mainTabsSwitcher?.navigate(getInt(DEEPLINK_MAIN_SCREEN_EXTRA)) ?: savePendingIntent(intent)
            }
        }
    }

    private fun popToMainScreen() {
        mainFragmentManager?.apply {
            if (backStackEntryCount > 1) {
                popBackStackImmediate(
                    MainFragment::class.java.name,
                    0
                )
            }
        }
    }

    fun handleSavedDeeplinkIntent() {
        pendingIntent?.let {
            handleOrSaveDeeplinkIntent(it)
            pendingIntent = null
        }
    }

    private fun savePendingIntent(intent: Intent) {
        pendingIntent = intent
    }
}