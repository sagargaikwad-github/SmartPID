package com.eits.smartpid;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.CamcorderProfile;
import android.media.Image;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.Settings;
import android.speech.SpeechRecognizer;
import android.util.JsonReader;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;



import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CameraActivity extends AppCompatActivity {
    private TextView bluetoothCount, timeTV, dateTV, latitudeTV, longitudeTV, recordingTimeTV, viewFilesTV;
    private TextureView mTextureView;
    private ImageView flashBTN, mRecordImageButton;

    Boolean mIsRecording = false;
    File mVideoFolder;
    String fileName,mVideoFileName, mTempVideoFileName, json;

    ProgressDialog progressDialog;

    CountDownTimer countDownTimer;
    CountDownTimer recordingTimer;
    String finalTime;
    ArrayList<Float> MinMaxAvgList;

    Handler handler;


    int seconds = 0;
    MediaRecorder mMediaRecorder;

    boolean isSurfaceAvailable = false;

    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 100;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 200;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT_ABOVE11 = 300;

    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    String mCameraId;
    Size mPreviewSize;
    Size mVideoSize;

    Size mImageSize;
    View mdecorView;
    int mTotalRotation;

    CameraDevice mCameraDevice;
    CameraCaptureSession mPreviewCaptureSession;
    CameraCaptureSession mRecordCaptureSession;
    CaptureRequest.Builder mCaptureRequestBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        checkCameraPermissions();
        MinMaxAvgList = new ArrayList<>();

        findID();
        setOnClick();

        handler = new Handler();

        progressDialog = new ProgressDialog(this);
        createVideoFolder();
        mMediaRecorder = new MediaRecorder();

    }

    private void setOnClick() {
        viewFilesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        mRecordImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(CameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_GRANTED) {
                        if (mIsRecording) {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        stopRecord();
                                    } catch (RuntimeException e) {
                                        Toast.makeText(CameraActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                    startPreview();
                                }
                            }, 500);
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //  Toast.makeText(CameraActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();
                                    mIsRecording = true;

                                    createVideoFolder();
                                    startRecord();
                                }
                            }, 500);

                        }
                    } else {
                        checkWriteStoragePermission();
                    }
                } else {
                    if (mIsRecording) {
                        mIsRecording = false;
                        mRecordImageButton.setImageResource(R.drawable.record_paused);

                        //toggleFlashModeRecord(flashStatus);

                        mMediaRecorder.stop();
                        mMediaRecorder.reset();
                        startPreview();
                    } else {
                        mIsRecording = true;
                        mRecordImageButton.setImageResource(R.drawable.record_resumed);
                        createVideoFolder();

                        startRecord();
                    }
                }
            }
        });
        flashBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flashStatus) {
                    flashStatus = false;
                    flashBTN.setImageResource(R.drawable.ic_flash_off);
                } else {
                    flashStatus = true;
                    flashBTN.setImageResource(R.drawable.ic_flash_on);
                }

                if (mIsRecording) {
                    try {
                        toggleFlashModeRecord(flashStatus);
                    } catch (Exception e) {
                        //   Toast.makeText(CameraActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    toggleFlashMode(flashStatus);
                }

            }
        });
    }

    private void findID() {
        viewFilesTV = findViewById(R.id.viewFilesTV);
        mTextureView = findViewById(R.id.camera_textureView);
        bluetoothCount = findViewById(R.id.bluetoothCountTV);
        timeTV = findViewById(R.id.timeTV);
        dateTV = findViewById(R.id.dateTV);
        latitudeTV = findViewById(R.id.latitudeTV);
        longitudeTV = findViewById(R.id.longitudeTV);
        recordingTimeTV = findViewById(R.id.recordingTimeTV);
        timeTV = findViewById(R.id.timeTV);
        mRecordImageButton = findViewById(R.id.recordBTN_IV);
        flashBTN = findViewById(R.id.flashBTN_IV);
    }

    TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            setUpCamera(i, i1);
            if (isSurfaceAvailable == true) {
                connectCamera();
            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture, int i, int i1) {
            configureTransform(i, i1);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

        }
    };

    CameraDevice.StateCallback mCameraDeviceStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDevice = camera;

            if (mIsRecording) {
                createVideoFolder();
                startRecord();

            }
            startPreview();

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            // Toast.makeText(CameraActivity.this, "Disconnected", Toast.LENGTH_SHORT).show();
            camera.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            mCameraDevice = null;
        }
    };

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private static class CompareSizeByArea implements Comparator<Size> {
        @Override
        public int compare(Size size, Size t1) {
            return Long.signum((long) size.getWidth() * size.getHeight() /
                    (long) t1.getWidth() * t1.getHeight());
        }
    }

    boolean flashStatus = false;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        View decorView = getWindow().getDecorView();
        if (hasFocus) {
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }

    private void hideSystemUI() {
        mdecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }

    private void stopRecord() {
        progressDialog.setMessage("Processing Please Wait");
        progressDialog.setCancelable(false);
        mdecorView = progressDialog.getWindow().getDecorView();
        progressDialog.show();

        recordingTimer.cancel();
        seconds = 0;

        hideSystemUI();
        countDownTimer.cancel();

        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
            mRecordImageButton.setImageResource(R.drawable.record_paused);
            mIsRecording = false;
        } catch (Exception e) {

        }

        String path = "/storage/emulated/0/Download/InspRec";
        String tempPath = null;
        File file = new File(path);
        File[] files = file.listFiles();

        if (files != null) {
            for (File file1 : files) {
                if (file1.getPath().contains(mTempVideoFileName)) {
                    tempPath = file1.getPath();
                    break;
                }
            }

            try {
                TextOnVideo(tempPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void TextOnVideo(String fileSavePath) throws IOException {
        createVideoFileName();
        createJSON();

        String a = "-y -i " + fileSavePath + " -metadata comment=" + json + " -framerate 60 -vf [in]" + text() + "[out] " + mVideoFileName;
        Log.e("text Value", a);

        long executionId = FFmpeg.executeAsync(a, (executionId1, returnCode) -> {
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                try {
                    addDataInSqlite();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                MinMaxAvgList.clear();
                mIsRecording = false;
                countDownTimer.start();
                recordingTimeTV.setText("00:00:00");
            } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
            }
        });
    }

    private void addDataInSqlite() throws IOException {
        Toast.makeText(this, "Video Saved Successfully", Toast.LENGTH_SHORT).show();
        File file1 = new File(mTempVideoFileName);
        file1.delete();

        String filePath = mVideoFolder + File.separator + "fileName1" + ".txt";
        File file = new File(filePath);
        String filepathTXT = file.getAbsolutePath();


     //   FileWriter writer = new FileWriter(filepathTXT);
//        writer.append("First string is here to be written.");
//        writer.flush();
//        writer.close();

        String a= " -y -i "+mVideoFileName+" -f ffmetadata "+filepathTXT;
        long executionId = FFmpeg.executeAsync(a, (executionId1, returnCode) -> {
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show();
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(filepathTXT));
                    String line = reader.readLine();
                    while (line != null) {
                        if(line.startsWith("comment")) {
                            String val=line.substring(8);
                            String sDecoded= URLDecoder.decode(val,"UTF-8");

                            try {
                               JSONObject jObj = new JSONObject(sDecoded);
                               String t = jObj.getString("videoTitle");
                               String c = jObj.getString("component");
                               String f = jObj.getString("facility");
                               String s = jObj.getString("sitelocation");
                               String notes= jObj.getString("notes");

                               System.out.println(t);
                               System.out.println(c);
                               System.out.println(f);
                               System.out.println(s);
                               System.out.println(notes);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                        line = reader.readLine();
                    }
                    reader.close();

                    File file2=new File(filepathTXT);
                    file2.delete();


                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                Toast.makeText(this, "error", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "error1", Toast.LENGTH_SHORT).show();
            }
        });



    }

    private void createJSON() throws IOException {
        try {
            // Create a new instance of a JSONObject
            final JSONObject object = new JSONObject();
            // With put you can add a name/value pair to the JSONObject
            object.put("videoTitle", fileName);
            object.put("component", "Flange");
            object.put("facility", "New facility");
            object.put("sitelocation", "Mumbai");
            object.put("notes", "NotesOO"+"kkk"+"KKK");
            object.put("min", 1);
            object.put("max", 50);
            object.put("average", 25);

            // Calling toString() on the JSONObject returns the JSON in string format.

            json = object.toString();
            String sEncoded = URLEncoder.encode(json, "UTF-8");
            json = sEncoded;
            System.out.println("ENCODER : "+sEncoded);

            String sDecoded= URLDecoder.decode(json,"UTF-8");
            System.out.println(" : "+sDecoded);

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSONObject", e);
        }
    }


    private String text() {
        String text = "";
        for (int i = 0; i < MinMaxAvgList.size(); i++) {
            String LAT = "LAT-" + i;
            String LON = "LON-" + i;

            if (i < MinMaxAvgList.size() - 1) {
                text = text + "drawtext=text=" + MinMaxAvgList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=80:y=(h-text_h)/2:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text='09/02/2022':fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-10:y=10:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text='Test Time':fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-500:y=10:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + LAT + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=300:y=h-th-100:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + LON + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-300:y=h-th-100:enable='between(t\\," + i + "\\," + (i + 1) + ")',";

            } else {
                text = text + "drawtext=text=" + MinMaxAvgList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=80:y=(h-text_h)/2:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text='09/02/2022':fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-10:y=10:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text='Test Time':fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-500:y=10:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + LAT + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=300:y=h-th-100:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + LON + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-300:y=h-th-100:enable='between(t\\," + i + "\\," + (i + 1) + ")'";
            }
        }
        System.out.println(text);
        return text;
    }

    private void checkCameraPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                isSurfaceAvailable = true;
            } else {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION_RESULT);
            }
        } else {
            isSurfaceAvailable = true;
        }
    }

    private void toggleFlashMode(boolean flashStatus) {

        try {
            if (flashStatus) {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }
            mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void toggleFlashModeRecord(boolean flashStatus) {
        try {
            if (flashStatus) {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }
            mRecordCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        if (mTextureView.isAvailable()) {
            setUpCamera(mTextureView.getWidth(), mTextureView.getHeight());
            if (isSurfaceAvailable == true) {
                connectCamera();
            }
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
        countDown();
        countDownTimer.start();

    }

    private void countDown() {
        countDownTimer = new CountDownTimer(50000, 1000) {
            @Override
            public void onTick(long l) {
                int seconds = (int) (l / 1000);
                bluetoothCount.setText(String.valueOf(seconds));

                if (mIsRecording) {
                    MinMaxAvgList.add(Float.valueOf(bluetoothCount.getText().toString()));
                }
            }

            @Override
            public void onFinish() {
                countDown();
                countDownTimer.start();
            }
        };
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT_ABOVE11) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isSurfaceAvailable = true;
            } else {
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                } else {
                    permissionDialog("Storage");
                }
            }
        }
        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                isSurfaceAvailable = true;
            } else {
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    finish();
                } else {
                    permissionDialog("Camera");
                }
            }

        }
        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                if (shouldShowRequestPermissionRationale(permissions[0])) {
                    finish();
                } else {
                    permissionDialog("Storage");
                }
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();

        countDownTimer.cancel();

        stopBackgroundThread();

        if (mIsRecording == true) {
            stopRecord();
        }

        mIsRecording = false;
        mRecordImageButton.setImageResource(R.drawable.record_paused);

        try {
            mMediaRecorder.stop();
            mMediaRecorder.reset();
        } catch (Exception e) {

        }

        flashStatus = false;
        if (flashStatus) {
            flashBTN.setImageResource(R.drawable.ic_flash_on);
        } else {
            flashBTN.setImageResource(R.drawable.ic_flash_off);
        }
        CloseCamera();
    }

    private void setUpCamera(int width, int height) {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            for (String cameraId : cameraManager.getCameraIdList()) {
                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);

                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();

                mTotalRotation = sensorToDeviceOrientation(cameraCharacteristics, deviceOrientation);

                int rotateWidth = width;
                int rotateHeight = height;

                boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
                if (swapRotation) {
                    rotateWidth = height;
                    rotateHeight = width;
                }

                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotateWidth, rotateHeight);
                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotateWidth, rotateHeight);

                mCameraId = cameraId;
                return;
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        Activity activity = CameraActivity.this;
        if (null == mTextureView || null == mPreviewSize || null == activity) {
            return;
        }
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();

        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);

        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }

        mTextureView.setTransform(matrix);

    }

    //CameraPermissions
    @SuppressLint("MissingPermission")
    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void permissionDialog(String name) {
        new AlertDialog.Builder(CameraActivity.this)
                .setTitle(name + " permission Denied ")
                .setMessage(name + " permission not granted. Please grant " + name + " permission from app settings.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setCancelable(false)
                .show();
    }

    private void closePreviewSession() {
        if (mPreviewCaptureSession != null) {
            mPreviewCaptureSession.close();
            mPreviewCaptureSession = null;
        }
    }

    private void startRecord() {
        try {
            closePreviewSession();
            setUpMediaRecorder();
        } catch (IOException e) {
            e.printStackTrace();
        }

        mRecordImageButton.setImageResource(R.drawable.record_resumed);

        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();

        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());

        Surface previewSurface = new Surface(surfaceTexture);
        Surface recordSurface = mMediaRecorder.getSurface();

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        mCaptureRequestBuilder.addTarget(previewSurface);
        mCaptureRequestBuilder.addTarget(recordSurface);

        if (flashStatus) {
            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
        } else {
            mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
        }

        try {
            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            mPreviewCaptureSession = cameraCaptureSession;
                            mRecordCaptureSession = cameraCaptureSession;
                            try {
                                RecordingTimer();
                                recordingTimer.start();
                                cameraCaptureSession.setRepeatingRequest(
                                        mCaptureRequestBuilder.build(), null, null);
                            } catch (CameraAccessException e) {
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                        }
                    }, null);
        } catch (CameraAccessException e) {
        }
    }

    public void RecordingTimer() {
        recordingTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long l) {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time = String.format(Locale.getDefault(),
                        "%02d:%02d:%02d", hours,
                        minutes, secs);

                if (mIsRecording) {
                    seconds++;
                    recordingTimeTV.setText(time);
                    finalTime = recordingTimeTV.getText().toString();
                } else {
                    // recordingTime.setText("00:00:00");
                    recordingTimeTV.setText(finalTime);
                }
            }

            @Override
            public void onFinish() {

            }
        };


    }


    private void startPreview() {
        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();

        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
        Surface previewSurface = new Surface(surfaceTexture);

        try {
            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCaptureRequestBuilder.addTarget(previewSurface);

            if (flashStatus) {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                mCaptureRequestBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }

            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                            mPreviewCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        null, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        }
                    }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void CloseCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void startBackgroundThread() {
        mBackgroundHandlerThread = new HandlerThread("Camera2API");
        mBackgroundHandlerThread.start();
        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
    }

    private void stopBackgroundThread() {
        mBackgroundHandlerThread.quitSafely();
        try {
            mBackgroundHandlerThread.join();
            mBackgroundHandlerThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


    }

    private static int sensorToDeviceOrientation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
        return (sensorOrientation + deviceOrientation * 1 + 360) % 360;

    }

    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<Size>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width &&
                    option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizeByArea());
        } else {
            return choices[0];
        }
    }

    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mVideoFolder = new File(movieFile, "InspRec");
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }


    private void createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("YYYYMMDD_HHmmss").format(new Date());
        fileName = "ECG" + timestamp;

        String filePath = mVideoFolder + File.separator + fileName + ".mp4";
        File file = new File(filePath);
        mVideoFileName = file.getAbsolutePath();
    }


    private void createTempFFmPEGVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("YYYYMMDD_HHmmss").format(new Date());
        String fileName = "ECG-ffmpeg" + timestamp;

        String filePath = mVideoFolder + File.separator + fileName + ".mp4";
        File file = new File(filePath);
        mTempVideoFileName = file.getAbsolutePath();
    }

    private void checkWriteStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {
            } else {
                requestPermissions(new String[]{Manifest.permission.MANAGE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT_ABOVE11);
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                    PackageManager.PERMISSION_GRANTED) {

            } else {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
            }
        } else {
        }
    }

    @SuppressLint("NewApi")
    private void setUpMediaRecorder() throws IOException {
        try {
            createTempFFmPEGVideoFileName();
        } catch (Exception e) {

        }

        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);

        CamcorderProfile camcorderProfile;

        camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
        int targetVideoBitRate = camcorderProfile.videoBitRate;

        mMediaRecorder.setOutputFile(mTempVideoFileName);

        mMediaRecorder.setVideoEncodingBitRate(targetVideoBitRate);

        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoFrameRate(camcorderProfile.videoFrameRate);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);


        System.out.println("Width : " + mVideoSize.getWidth() + " Height : " + mVideoSize.getHeight());
        System.out.println("Width : " + camcorderProfile.videoFrameWidth + " Height : " + camcorderProfile.videoFrameHeight);


        if (mVideoSize.getWidth() < 1080) {
            mMediaRecorder.setVideoSize(960, 720);
        } else if (mVideoSize.getWidth() <= 1280) {
            mMediaRecorder.setVideoSize(1280, 720);
        } else {
            mMediaRecorder.setVideoSize(1920, 1080);
        }


        mMediaRecorder.prepare();

        mMediaRecorder.start();


    }

    @Override
    public void onBackPressed() {
        if (progressDialog.isShowing()) {
            Toast.makeText(this, "Please Wait Video Processing needs to complete", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }

    }
}