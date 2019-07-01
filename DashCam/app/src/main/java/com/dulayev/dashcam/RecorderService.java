package com.dulayev.dashcam;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.content.Intent.FLAG_ACTIVITY_SINGLE_TOP;

public class RecorderService extends IntentService {

    private static final int NOTIFICATION_ID = R.string.notification_text;
    final String TAG = getClass().getSimpleName();
    private CameraDevice camera;
    private MediaRecorder recorder;
    private Surface surface;
    private CaptureRequest.Builder builder;
    private CameraCaptureSession session;

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public RecorderService(String name) {
        super(name);
    }
    public RecorderService() {
        super("default-service-name");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStart");

        //Notification notification = new Notification(R.drawable.ic_media_stop, getText(R.string.notification_text),
        //        System.currentTimeMillis());
        Intent notificationIntent = new Intent(this, this.getClass());
        notificationIntent.addFlags(FLAG_ACTIVITY_SINGLE_TOP);
        //PendingIntent.
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, notificationIntent, 0);

        Notification.Builder builder = new Notification.Builder(this);
        builder.setSmallIcon(R.drawable.ic_media_stop)
            .setContentTitle(getString(R.string.notification_text))
            .setContentIntent(pendingIntent);

        Notification notification = builder.getNotification();

//        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);

//        notificationManager.notify(R.drawable.ic_media_stop, notification);

        //notification.setLatestEventInfo(this, getText(R.string.notification_title),
        //        getText(R.string.notification_message), pendingIntent);
        startForeground(NOTIFICATION_ID, notification);

        CameraManager cameraManager = getSystemService(CameraManager.class);
        String cameraId = null;
        try {
            String[] idList = cameraManager.getCameraIdList();

            for (String id : idList) {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);
                if (characteristics.get(CameraCharacteristics.LENS_FACING).equals(CameraMetadata.LENS_FACING_BACK)) {
                    cameraId = id;
                    Log.d(TAG, "Back camera is: " + id);
                    break;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "Camera selected: " + cameraId);

        if (cameraId != null) {
            final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    Log.d(TAG, "onOpened");
                    OnCameraOpened(camera);
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    Log.d(TAG, "onDisconnected");
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    Log.d(TAG, "onError");
                }
            };
            try {
                cameraManager.openCamera(cameraId, stateCallback, null/*current thread looper*/);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }


        return Service.START_NOT_STICKY;
    }

    private void OnCameraOpened(CameraDevice camera) {
        this.camera = camera;
        try {
            this.recorder = new MediaRecorder();
            recorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            File picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            recorder.setOutputFile(picturesDir.getPath() + "/dc.mp4");
            recorder.setVideoSize(1920, 1080);
            recorder.setVideoFrameRate(30);
            try {
                recorder.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //recorder.start();
            this.surface = recorder.getSurface();

            this.builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
            builder.addTarget(surface);


            camera.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, "onConfigured");
                    OnCaptureSessionConfigured(session);
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.d(TAG, "onConfigureFailed");
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void OnCaptureSessionConfigured(CameraCaptureSession session) {
        this.session = session;
        recorder.start();
        try {
            session.setRepeatingRequest(builder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureStarted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, long timestamp, long frameNumber) {
                    super.onCaptureStarted(session, request, timestamp, frameNumber);
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        Log.d(TAG, "onHandleIntent");

        try {
            this.session.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        recorder.stop();

        this.stopForeground(true);
        this.stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
