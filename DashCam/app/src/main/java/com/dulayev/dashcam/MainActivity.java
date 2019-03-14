package com.dulayev.dashcam;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Surface;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    final String TAG = "DashCam";
    private CameraDevice camera;
    private MediaRecorder recorder;
    private Surface surface;
    private CaptureRequest.Builder builder;
    private CameraCaptureSession session;
    private Timer timer;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate");

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
                public void onOpened(@androidx.annotation.NonNull CameraDevice camera) {
                    Log.d(TAG, "onOpened");
                        OnCameraOpened(camera);
                }

                @Override
                public void onDisconnected(@androidx.annotation.NonNull CameraDevice camera) {
                    Log.d(TAG, "onDisconnected");
                }

                @Override
                public void onError(@androidx.annotation.NonNull CameraDevice camera, int error) {
                    Log.d(TAG, "onError");
                }
            };
            try {
                cameraManager.openCamera(cameraId, stateCallback, null/*current thread looper*/);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
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
        this.timer = new Timer();
        final Handler handler = new Handler();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        OnTimerEnd();
                    }
                });
            }
        }, 6000/*ms*/);
    }

    private void OnTimerEnd() {
        try {
            this.session.stopRepeating();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        recorder.stop();
    }
}
