package com.prismaqf.callblocker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

/**
 * Call detect service
 * @author Moskvichev Andrey V.
 * @see 'www.codeproject.com/Articles/548416/Detecting-incoming-and-outgoing-phone-calls-on-And'
 */public class CallDetectService extends Service {

    private static final String TAG = CallDetectService.class.getCanonicalName();



    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        CallDetectService getService() {
            // Return this instance of LocalService so clients can call public methods
            return CallDetectService.this;
        }
    }

    private final CallHelper myCallHelper;
    private final IBinder myBinder = new LocalBinder();
    
    public CallDetectService() {
        myCallHelper = CallHelper.GetHelper(this);
    }

    public int getNumReceived() {return myCallHelper.getNumReceived();}

    public int getNumTriggered() {return myCallHelper.getNumTriggered();}
    

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        sendNotification();
        myCallHelper.start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (myCallHelper) {
                    myCallHelper.recordServiceStart();
                }
            }
        }).start();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.w(TAG,"The service has been killed");
/*        new Thread(new Runnable() {

            @Override
            public void run() {
                synchronized (this) {
                    myCallHelper.recordServiceStop();
                }
            }
        }).start();*/
        myCallHelper.recordServiceStop();
        myCallHelper.stop();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }


    private void sendNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.police)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.tx_notification));
        Intent resultIntent = new Intent(this, CallBlockerManager.class);
        //artificial back stack for the navigation to go back to the app
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(CallBlockerManager.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(R.integer.notification_id, mBuilder.build());
    }
}
