package com.health.clayful.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.health.clayful.R
import com.health.clayful.model.NotificationReponseModel
import com.health.clayful.ui.home.HomeActivity
import com.health.clayful.utils.Constants
import com.health.clayful.utils.PrefData
import io.customer.messagingpush.CustomerIOFirebaseMessagingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zendesk.logger.Logger
import zendesk.messaging.android.push.PushNotifications
import zendesk.messaging.android.push.PushResponsibility

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val LOG_TAG = "DefaultMessagingService"
    private val REQUEST_CODE = 0
    private var notification: Notification ?= null
    private var notificationId = 0

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Log.e("mynotifications", remoteMessage.data.toString())

        if(remoteMessage.data.containsKey("CIO-Delivery-Token")) {
            CustomerIOFirebaseMessagingService.onMessageReceived(remoteMessage)
        }
        else {
            when (PushNotifications.shouldBeDisplayed(remoteMessage.data)) {
                PushResponsibility.MESSAGING_SHOULD_DISPLAY -> {
                    val model = Gson().fromJson(remoteMessage.data.toString(), NotificationReponseModel::class.java )
                    val bundle = Bundle()
                    if(model.message?.type.equals("text")) {
                        bundle.putString(NotificationModel.message, "${model.message?.name}: ${model.message?.text}")
                        bundle.putBoolean(Constants.HAS_NOTIFICATION, true)
                        sendIntentToBroadCastReceiver(bundle)
                        showNotification(bundle, "${model.message?.name}: ${model.message?.text}")
                    }
                    else if(model?.message?.type?.equals("image")!!){
                        bundle.putString(NotificationModel.message, "${model.message?.name}: ${model.message?.text}")
                        sendIntentToBroadCastReceiver(bundle)
                        bundle.putBoolean(Constants.HAS_NOTIFICATION, true)
                        showNotification(bundle, "${model.message?.name}: ${model.message?.altText}")
                    }
                }
                PushResponsibility.MESSAGING_SHOULD_NOT_DISPLAY -> {
                    Logger.d(LOG_TAG, "Notification received from Messaging but shouldn't be displayed")
                }
                PushResponsibility.NOT_FROM_MESSAGING -> {
                    Logger.d(LOG_TAG, "Notification received but doesn't belong to Messaging")
                }
            }
        }
    }

    private fun sendIntentToBroadCastReceiver(bundle: Bundle) {
        val intent = Intent()
        intent.action = Constants.BROADCAST
        intent.putExtra(Constants.NOTIFICATION_DATA, bundle)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)
    }

    private fun showNotification(bundle: Bundle, messageBody: String) {

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val resultIntent = Intent(this, HomeActivity::class.java).apply {
            putExtra(NotificationModel.notification_bundle, bundle)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        var resultPendingIntent : PendingIntent
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            resultPendingIntent = PendingIntent.getActivity(
                this,
                0,
                resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } else {
            resultPendingIntent = PendingIntent.getActivity(
                this,
                0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        /*val resultPendingIntent = PendingIntent.getActivity(
            this,
            REQUEST_CODE,
            resultIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )*/

        if (Build.VERSION.SDK_INT >= 26) {
            //When sdk version is larger than26
            val id = resources.getString(R.string.default_notification_channel_id)
            val description = "143"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(id, description, importance)
            manager.createNotificationChannel(channel)

            notification = NotificationCompat.Builder(this, id)
                .setCategory(Notification.CATEGORY_MESSAGE)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(resultPendingIntent)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .build()
            manager.notify(notificationId++, notification)
        } else {
            //When sdk version is less than26
            notification = NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.app_icon)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setContentIntent(resultPendingIntent)
                .setDefaults(Notification.DEFAULT_SOUND or Notification.DEFAULT_VIBRATE)
                .setStyle(NotificationCompat.BigTextStyle().bigText(messageBody))
                .build()
            manager.notify(notificationId++, notification)
        }
    }

    override fun onNewToken(token: String) {
        PushNotifications.updatePushNotificationToken(token)
        CustomerIOFirebaseMessagingService.onNewToken(token)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                PrefData(applicationContext).storeFirebaseToken(token)
            }
            catch (ex : Exception) {}
        }
    }
}