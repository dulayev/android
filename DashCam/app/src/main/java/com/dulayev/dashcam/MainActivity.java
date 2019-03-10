package com.dulayev.dashcam;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    final String TAG = "DashCam";

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
}
