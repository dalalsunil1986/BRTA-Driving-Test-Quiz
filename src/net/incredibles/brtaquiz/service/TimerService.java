package net.incredibles.brtaquiz.service;

/**
 * @author shaiekh
 * @Created 2/19/12 12:22 PM
 */

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.*;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import net.incredibles.brtaquiz.R;
import net.incredibles.brtaquiz.activity.QuestionActivity;
import net.incredibles.brtaquiz.util.ForeGroundServiceCompat;
import roboguice.inject.InjectResource;
import roboguice.service.RoboService;

@Singleton
public class TimerService extends RoboService {
    private static final int SERVICE_ID = 8271;
    private static final long TICK_INTERVAL_IN_MILLIS = 1000;

    public static final String KEY_TIME_PULSE = "KEY_TIME_PULSE";
    public static final int MSG_REGISTER_CLIENT = 1;
    public static final int MSG_UNREGISTER_CLIENT = 2;
    public static final int MSG_TIME_PULSE = 3;
    public static final int MSG_TIME_UP = 4;

    @InjectResource(R.string.ticker_text)
    private String notificationTickerText;
    @InjectResource(R.string.notification_title)
    private String notificationTitle;
    @InjectResource(R.string.notification_text)
    private String notificationText;
    @InjectResource(R.string.time_per_question_in_seconds)
    private String timePerQuestionInSeconds;
    @InjectResource(R.string.no_of_questions)
    private String noOfQuestions;

    @Inject
    private NotificationManager notificationManager;

    private Messenger clientMessenger;
    private Messenger serviceMessenger;
    private Notification notification;
    private PendingIntent pendingIntentForNotification;
    private ForeGroundServiceCompat foreGroundServiceCompat;
    private CountDownTimer countDownTimer;

    @Override
    public IBinder onBind(Intent intent) {
        return serviceMessenger.getBinder();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        serviceMessenger = new Messenger(new IncomingHandler());
        notification = new Notification(R.drawable.ic_launcher, notificationTickerText, System.currentTimeMillis());
        pendingIntentForNotification = PendingIntent.getActivity(this, 0, new Intent(this, QuestionActivity.class), 0);
        foreGroundServiceCompat = new ForeGroundServiceCompat(this);

        long testDuration = getTestDuration();
        countDownTimer = new RemainingTimeCount(testDuration, TICK_INTERVAL_IN_MILLIS);
        setLatestEventInfoNotification(getFormattedTime(testDuration));
        countDownTimer.start();

        foreGroundServiceCompat.startForeground(SERVICE_ID, notification);
    }

    private long getTestDuration() {
        long timePerQuestionInMillis = Long.parseLong(timePerQuestionInSeconds) * 1000L;
        long questions = Long.parseLong(noOfQuestions);
        return timePerQuestionInMillis * questions;
    }

    public static String getFormattedTime(long timeInMillis) {
        int hours   = (int) ((timeInMillis / (1000*60*60)) % 24);
        int minutes = (int) ((timeInMillis / (1000*60)) % 60);
        int seconds = (int) (timeInMillis / 1000) % 60 ;

        String paddedHours = (hours < 10 ? "0" : "") + hours;
        String paddedMinutes = (minutes < 10 ? "0" : "") + minutes;
        String paddedSeconds = (seconds < 10 ? "0" : "") + seconds;

        return paddedHours + ":" + paddedMinutes + ":" + paddedSeconds;
    }

    private void setLatestEventInfoNotification(String remainingTime) {
        notification.setLatestEventInfo(this, notificationTitle, notificationText + "   " + remainingTime,
                pendingIntentForNotification);
    }

    private void sendMessageToUI(long intValueToSend) {
        try {
            if (clientMessenger != null) {
                Bundle bundle = new Bundle();
                bundle.putLong(KEY_TIME_PULSE, intValueToSend);
                Message msg = Message.obtain();
                msg.what = MSG_TIME_PULSE;
                msg.setData(bundle);
                clientMessenger.send(msg);
            }
        } catch (RemoteException e) {
            clientMessenger = null;
        }
    }

    private class RemainingTimeCount extends CountDownTimer {

        public RemainingTimeCount(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            try {
                clientMessenger.send(Message.obtain(null, MSG_TIME_UP));
                foreGroundServiceCompat.stopForeground(SERVICE_ID);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onTick(long millisUntilFinished) {
            sendMessageToUI(millisUntilFinished);
            setLatestEventInfoNotification(getFormattedTime(millisUntilFinished));
            notificationManager.notify(SERVICE_ID, notification);
        }
    }


    private class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_REGISTER_CLIENT:
                    clientMessenger = msg.replyTo;
                    break;
                case MSG_UNREGISTER_CLIENT:
                    clientMessenger = null;
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

}