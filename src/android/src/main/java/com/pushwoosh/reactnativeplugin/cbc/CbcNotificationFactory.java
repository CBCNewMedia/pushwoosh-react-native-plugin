package com.pushwoosh.reactnativeplugin.cbc;

import android.app.Notification;
import android.app.PendingIntent;
import android.media.RingtoneManager;
import android.net.Uri;
import androidx.core.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.pushwoosh.reactnativeplugin.R;
import com.pushwoosh.notification.PushMessage;
import com.pushwoosh.notification.PushwooshNotificationFactory;

import android.os.Build;

public class CbcNotificationFactory extends PushwooshNotificationFactory {
    private static final String LCAT = "CbcNotificationFactory";

    private RemoteViews buildBigContentView(PushMessage pushData) {
        RemoteViews contentView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.custom_notification);
        if (pushData.getHeader() != null && !pushData.getHeader().isEmpty()) {
            contentView.setTextViewText(R.id.notification_title, getContentFromHtml(pushData.getHeader()));
        }
        if (pushData.getMessage() != null && !pushData.getMessage().isEmpty()) {
            contentView.setTextViewText(R.id.notification_msg, getContentFromHtml(pushData.getMessage()));
        }
        if (pushData.getBigPictureUrl() != null && !pushData.getBigPictureUrl().isEmpty()) {
            contentView.setImageViewBitmap(R.id.notification_img, getBigPicture(pushData));
        }

        return contentView;
    }

    @Override
    public Notification onGenerateNotification(PushMessage pushData) {
        Uri sound = null;
        try {
            sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        } catch (Exception e) {
            Log.e(LCAT, "Failed to get sound resource for push" + e.getMessage());
        }

        int iconResId = getApplicationContext().getResources().getIdentifier("notification_icon", "drawable", getApplicationContext().getPackageName());
        if (iconResId == 0) {
            iconResId = getApplicationContext().getApplicationInfo().icon;
        }

        NotificationCompat.Builder notificationBuilder = null;

        // Android >=26 requires a notification channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String channel_id = this.addChannel(pushData);
            notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), channel_id);
        }
        else {
            notificationBuilder = new NotificationCompat.Builder(getApplicationContext());
        }

        notificationBuilder.setContentText(getContentFromHtml(pushData.getMessage()))
                .setSmallIcon(iconResId)
                .setTicker(getContentFromHtml(pushData.getTicker()))
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(true)
                .setSound(sound)
                .setPriority(pushData.getPriority())
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setCustomBigContentView(buildBigContentView(pushData))
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(), 0, getNotificationIntent(pushData), PendingIntent.FLAG_CANCEL_CURRENT));

        if (pushData.getHeader() != null && !pushData.getHeader().isEmpty()) {
            notificationBuilder.setContentTitle(getContentFromHtml(pushData.getHeader()));
        }

        Notification notification = notificationBuilder.build();

        if (pushData.getVibration()) {
            addVibration(notification, pushData.getVibration());
        }

        return notification;
    }
}
