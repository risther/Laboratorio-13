package com.example.mynotification.util;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;

import androidx.core.app.NotificationCompat;

import com.example.mynotification.R;
import com.example.mynotification.app.config;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotificationUtils {

    private static String TAG = NotificationUtils.class.getSimpleName();
    private Context context;
    private static final int REQUEST_NOTIFICATION = 0;
    private Bitmap bitmap;

    public NotificationUtils(Context context){
        this.context = context;
    }

    public void showNotificationMessage(final String title,final String message
            , final String timeStamp, Intent intent){
        showNotificationMessage(title,message,timeStamp,intent,null);
    }

    public void showNotificationMessage(final String title, final String message
            , final String timeStamp, Intent intent, String imageUrl){
        if(TextUtils.isEmpty(message)) return;

        final int icon = R.mipmap.ic_launcher_round;
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent resultPendingIntent = PendingIntent.getActivity(context
                ,REQUEST_NOTIFICATION, intent,PendingIntent.FLAG_CANCEL_CURRENT);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context
                , config.CHANNEL_ID);

        final Uri alarm = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"
                +context.getPackageName()+"raw/tono");

        if(TextUtils.isEmpty(imageUrl)){
            if(imageUrl != null && imageUrl.length() > 4
                    && Patterns.WEB_URL.matcher(imageUrl).matches()){
                try {
                    URL url = new URL(imageUrl);
                    bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(bitmap != null){
                    showBigNotification(bitmap,builder,icon,title,message,timeStamp
                            ,resultPendingIntent,alarm);
                    playNotificationAlarm();
                }else{
                    showSmallNotification(builder,icon,title,message,timeStamp
                            ,resultPendingIntent,alarm);
                    playNotificationAlarm();
                }
            }else{
                showSmallNotification(builder,icon,title,message,timeStamp
                        ,resultPendingIntent,alarm);
                playNotificationAlarm();
            }
        }
    }

    public static boolean isAppIsInBackground(Context context){
        boolean isInBackground = true;
        ActivityManager activityManager = (ActivityManager)context
                .getSystemService(Context.ACTIVITY_SERVICE);

        List<ActivityManager.RunningAppProcessInfo> runningAppProcessInfos =
                activityManager.getRunningAppProcesses();
        for(ActivityManager.RunningAppProcessInfo processInfo : runningAppProcessInfos){
            if(processInfo.importance ==
                    ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                for(String activeProcess : processInfo.pkgList){
                    if(activeProcess.equals(context.getPackageName())){
                        isInBackground = false;
                    }
                }
            }
        }
        return isInBackground;
    }

    private void showBigNotification(Bitmap bitmap, NotificationCompat.Builder builder
            ,int icon, String title, String message,String timeStamp
            ,PendingIntent resultPendingIntent, Uri alarm){
        NotificationCompat.BigPictureStyle bigPictureStyle =
                new NotificationCompat.BigPictureStyle();

        bigPictureStyle.setBigContentTitle(title);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            bigPictureStyle.setSummaryText(Html.fromHtml(message,Html.FROM_HTML_MODE_LEGACY))
                    .toString();
        }else{
            bigPictureStyle.setSummaryText(Html.fromHtml(message).toString());
        }

        bigPictureStyle.bigPicture(bitmap);
        Notification notification = builder
                .setSmallIcon(icon)
                .setTicker(title)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarm)
                .setStyle(bigPictureStyle)
                .setWhen(getTimeMillisec(timeStamp))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),icon))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(config.NOTIFICATION_ID_BIG_IMAGE,notification);
    }

    private void showSmallNotification(NotificationCompat.Builder builder
            ,int icon, String title, String message,String timeStamp
            ,PendingIntent resultPendingIntent, Uri alarm){
        NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
        inboxStyle.addLine(message);

        Notification notification = builder
                .setTicker(title)
                .setAutoCancel(true)
                .setContentTitle(title)
                .setContentIntent(resultPendingIntent)
                .setSound(alarm)
                .setStyle(inboxStyle)
                .setWhen(getTimeMillisec(timeStamp))
                .setSmallIcon(icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(),icon))
                .setContentText(message)
                .build();

        NotificationManager notificationManager = (NotificationManager)context
                .getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(config.NOTIFICATION_ID,notification);
    }

    public static long getTimeMillisec(String timeStamp){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            Date date = (format.parse(timeStamp));
            return date.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public void playNotificationAlarm(){
        Uri alarm = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+"://"
                +context.getPackageName()+"/raw/tono");
        Ringtone ringtone = RingtoneManager.getRingtone(context,alarm);
        ringtone.play();
    }

}
