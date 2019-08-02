package com.magcomm.factorytest.item;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.magcomm.factorytest.R;

/**
 * Created by zhangziran on 2018/1/16.
 */

public class LEDFragment extends BaseFragment {
    private static final String TAG = "zhangziran";
    private NotificationManager notificationManager;
    private String channelID = "factorytest";
    private String channelName = "channel_name";
    private Notification notification;
    private int[] colors = new int[]{Color.RED, Color.GREEN, Color.BLUE};
    private int i = 0;
    private boolean isNeedRunning = true;

    @Override
    protected int getCurrentView() {
        return R.layout.led_test;
    }

    @SuppressLint("ServiceCast")
    @Override
    protected void onFragmentCreat() {
        TextView tvNotice = view.findViewById(R.id.tv_led_test);
        tvNotice.setText("LED");
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(channelID, channelName, NotificationManager.IMPORTANCE_HIGH);
        notificationManager.createNotificationChannel(channel);
        notification = new Notification.Builder(context, channelID)
                .setAutoCancel(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setWhen(System.currentTimeMillis())
                .build();
        notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        notification.ledOnMS = 1000;
        notification.ledOffMS = 1000;
        notification.ledARGB = colors[i];
        notificationManager.notify(111, notification);
        handler.postDelayed(runnable, 3000);
    }

    @Override
    protected String getTotalName() {
        return this.getClass().getSimpleName();
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (isNeedRunning) {
                i++;
                if(i > 2) {
                    i = 0;
                }
                notification.ledARGB = colors[i];
                notification.ledOnMS = 1000;
                notification.ledOffMS = 1000;
                notificationManager.notify(111, notification);
                handler.postDelayed(this::run, 3000);
            }
        }
    };

    @Override
    public void destroy() {
        notification.ledOnMS = 0;
        notification.ledOffMS = 0;
        notification.ledARGB = Color.TRANSPARENT;
        notificationManager.notify(111, notification);
        notificationManager.deleteNotificationChannel(channelID);
        notificationManager.cancel(111);
        isNeedRunning = false;
        if (runnable != null) {
            handler.removeCallbacks(runnable);
            runnable = null;
        }
    }

}
