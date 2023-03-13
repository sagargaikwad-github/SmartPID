package com.eits.smartpid;

import static android.content.ContentValues.TAG;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaMetadataRetriever;
import android.media.MediaRecorder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arthenica.mobileffmpeg.Config;
import com.arthenica.mobileffmpeg.FFmpeg;
import com.eits.smartpid.model.ComponentModel;
import com.eits.smartpid.model.FacilityModel;
import com.eits.smartpid.model.SQLiteModel;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class CameraActivity extends BaseClass {
    private TextView bluetoothCount, timeTV, dateTV, recordingTimeTV, viewFilesTV;
    private TextureView mTextureView;
    private ImageView flashBTN, mRecordImageButton;

    TextView latitudeTV, longitudeTV;

    Boolean mIsRecording = false;
    File mVideoFolder;
    String fileName, mVideoFileName, mTempVideoFileName, json, RecordingDate;

    ProgressDialog progressDialog;

    //CountDownTimer countDownTimer;
    CountDownTimer recordingTimer;
    String finalTime;
    ArrayList<Float> MinMaxAvgList;
    ArrayList<String> TimeArrayList;

    Handler handler;
    Handler bluetoothHandler;
    Handler locationHandler;
    Runnable runnable;
    Runnable bluetoothRunnable;
    Runnable locationRunnable;

    int ComponentSpinnerID, FacilitySpinnerID;


    String Notes, SiteLocation;
    int Facility, Component;

    int seconds = 0;
    MediaRecorder mMediaRecorder;
    int VideoBitrate;

    boolean isSurfaceAvailable = false;

    SendReceieve sendReceieve;


    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 100;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 200;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT_ABOVE11 = 300;
    private static final int REQUEST_LOCATION_PERMISSION = 400;


    private HandlerThread mBackgroundHandlerThread;
    private Handler mBackgroundHandler;

    String mCameraId;
    Size mPreviewSize;
    Size mVideoSize;

    View mdecorView;
    int mTotalRotation;

    CameraDevice mCameraDevice;
    CameraCaptureSession mPreviewCaptureSession;
    CameraCaptureSession mRecordCaptureSession;
    CaptureRequest.Builder mCaptureRequestBuilder;

    String[] ComponentArray;
    String[] FacilityArray;

    ArrayList<ComponentModel> Component_List = new ArrayList();
    ArrayList<FacilityModel> Facility_List = new ArrayList();

    SharedPreferences sharedPreferences;
    SQLiteModel sqLiteModel;

    BottomSheetDialog bottomSheetDialog;
    BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] paired_device_array;
    ArrayAdapter arrayAdapter;


    LocationRequest locationRequest;
    FusedLocationProviderClient mFusedLocationClient;

    BroadcastReceiver broadcastReceiver;

    ArrayList<String> LatitudeList;
    ArrayList<String> LongitudeList;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);



        //setContentView(R.layout.activity_camera);

        //Permissions for Camera:
        checkCameraPermissions();

        sqLiteModel = new SQLiteModel(this);

        sqLiteModel.clearCompFilter();
        sqLiteModel.clearFacFilter();

//        locationHandle();
//        mFusedLocationClient.getLastLocation()
//                .addOnSuccessListener(new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                       if(location!=null)
//                       {
//                           Toast.makeText(CameraActivity.this, "Hii", Toast.LENGTH_SHORT).show();
//                       }else
//                       {
//                           Toast.makeText(CameraActivity.this, "Null_HI", Toast.LENGTH_SHORT).show();
//                       }
//                    }
//                });


        broadcastReceiver = new BroadcastReceiver() {
            @SuppressLint("MissingPermission")
            @Override
            public void onReceive(Context context, Intent intent) {
                if (isGPSEnabled()) {
                    Toast.makeText(context, "Gps is Turned on", Toast.LENGTH_SHORT).show();
                    locationHandle();

                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(locationRequest);
                    builder.setAlwaysShow(true);

                    Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(context)
                            .checkLocationSettings(builder.build());
                    result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
                        @SuppressLint("MissingPermission")
                        @Override
                        public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                            try {
                                LocationSettingsResponse response = task.getResult(ApiException.class);
                                LocationCallback locationCallback = new LocationCallback() {
                                    @Override
                                    public void onLocationResult(LocationResult locationResult) {
                                        if (locationResult != null) {
                                            for (Location location : locationResult.getLocations()) {
                                                double latitude = location.getLatitude();
                                                double longitude = location.getLongitude();

                                                String lat = String.format("%.5f", latitude);
                                                String lon = String.format("%.5f", longitude);

                                                latitudeTV.setText("LAT-" + lat);
                                                longitudeTV.setText("LON-" + lon);
                                            }
                                        } else {
                                            Toast.makeText(context, "Hi", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                };

                                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

                            } catch (ApiException e) {
                                switch (e.getStatusCode()) {
                                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                        try {
                                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                            resolvableApiException.startResolutionForResult((Activity) context, 2);
                                        } catch (IntentSender.SendIntentException ex) {
                                            ex.printStackTrace();
                                        }
                                        break;

                                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                        break;
                                }
                            }
                        }
                    });

                } else {
                    Toast.makeText(context, "Gps is turned off", Toast.LENGTH_SHORT).show();
                }
            }
        };
        registerReceiver(broadcastReceiver, new IntentFilter(LocationManager.MODE_CHANGED_ACTION));


        //FindID's:
        findID();


        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //AllOnClicks in One Function:
        setOnClick();


        progressDialog = new ProgressDialog(this);

        //Making Video Folder
        createVideoFolder();

        //Initialize Media Recorder
        mMediaRecorder = new MediaRecorder();

        //For Displaying Date and Time;
        Time();

        countDown();

        sqLiteModel = new SQLiteModel(CameraActivity.this);

    }

    private boolean isOnline(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            //should check null because in airplane mode it will be null
            return (netInfo != null && netInfo.isConnected());
        } catch (NullPointerException e) {
            e.printStackTrace();
            return false;
        }
    }


    private void locationHandle() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(CameraActivity.this);
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(50 / 2);
    }

    private void setOnClick() {
        bluetoothCount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkBluetoothIsOn();
            }
        });

        //ViewFiles Currently TopLeft on CameraActivityScreen:
        viewFilesTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(CameraActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        latitudeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocation();
            }
        });
        longitudeTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startLocation();
            }
        });

        //Recording Button Handle:
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
                                        Toast.makeText(CameraActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
                                        Log.e("STOP_RECORD_ERROR", e.getMessage());
                                        Log.e("STOP_RECORD_ERROR", e.toString());
                                    }
                                    startPreview();
                                }
                            }, 500);
                        } else {
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    //  Toast.makeText(CameraActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();

                                    ComponentArray = new String[Component_List.size()];
                                    for (int i = 0; i < Component_List.size(); i++) {
                                        ComponentArray[i] = Component_List.get(i).getCompName();
                                    }


                                    FacilityArray = new String[Facility_List.size()];
                                    for (int i = 0; i < Facility_List.size(); i++) {
                                        FacilityArray[i] = Facility_List.get(i).getFacName();
                                    }

                                    BottomSheet(R.layout.bottomsheet_metadata);

                                    Spinner FacilitySpinner = bottomSheetDialog.findViewById(R.id.bottomSheet_metadata_facility_spinner);
                                    Spinner ComponentSpinner = bottomSheetDialog.findViewById(R.id.bottomSheet_metadata_component_spinner);
                                    TextInputEditText siteLocation = bottomSheetDialog.findViewById(R.id.bottomSheet_metadata_siteLocation_ET);
                                    TextInputEditText notes = bottomSheetDialog.findViewById(R.id.bottomSheet_metadata_Notes_ET);


                                    LinearLayout MetaData_BottomSheet_LL = bottomSheetDialog.findViewById(R.id.MetaData_BottomSheet_LL);
                                    MetaData_BottomSheet_LL.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            hidekeyboard(MetaData_BottomSheet_LL);
                                        }
                                    });

                                    Button SaveBTN = bottomSheetDialog.findViewById(R.id.bottomSheet_metadata_save_BTN);
                                    Button CancelBTN = bottomSheetDialog.findViewById(R.id.bottomSheet_metadata_cancel_BTN);


                                    ArrayAdapter facilityAdapter = new ArrayAdapter(CameraActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, FacilityArray);
                                    FacilitySpinner.setAdapter(facilityAdapter);

                                    ArrayAdapter componentAdapter = new ArrayAdapter(CameraActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, ComponentArray);
                                    ComponentSpinner.setAdapter(componentAdapter);


                                    sharedPreferences = getSharedPreferences("MetaData", MODE_PRIVATE);
                                    int SP_component = 0, SP_facility = 0;
                                    String SP_sitelocation = "", SP_Notes = "";
                                    try {
                                        SP_component = sharedPreferences.getInt("Component", 0);
                                        SP_facility = sharedPreferences.getInt("Facility", 0);
                                        SP_sitelocation = sharedPreferences.getString("SiteLocation", "");
                                        SP_Notes = sharedPreferences.getString("Notes", "");
                                    } catch (Exception e) {
                                    }

                                    FacilitySpinner.setSelection(SP_facility);
                                    ComponentSpinner.setSelection(SP_component);

                                    FacilitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                            mdecorView = bottomSheetDialog.getWindow().getDecorView();
                                            hideSystemUI();
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {
                                            mdecorView = bottomSheetDialog.getWindow().getDecorView();
                                            hideSystemUI();
                                        }
                                    });
                                    ComponentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                        @Override
                                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                            mdecorView = bottomSheetDialog.getWindow().getDecorView();
                                            hideSystemUI();
                                        }

                                        @Override
                                        public void onNothingSelected(AdapterView<?> adapterView) {
                                            mdecorView = bottomSheetDialog.getWindow().getDecorView();
                                            hideSystemUI();
                                        }
                                    });
                                    FacilitySpinner.setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View view, MotionEvent motionEvent) {
                                            //FacilitySpinner.clearFocus();

                                            hidekeyboard(MetaData_BottomSheet_LL);
                                            //mdecorView =  bottomSheetDialog.getWindow().getDecorView();
                                            //hideSystemUI();
                                            return false;
                                        }
                                    });
                                    ComponentSpinner.setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View view, MotionEvent motionEvent) {
                                            //ComponentSpinner.clearFocus();
                                            hidekeyboard(MetaData_BottomSheet_LL);
                                            // mdecorView =  bottomSheetDialog.getWindow().getDecorView();
                                            //hideSystemUI();
                                            return false;
                                        }
                                    });

                                    siteLocation.setText(SP_sitelocation);
                                    notes.setText(SP_Notes);

                                    SaveBTN.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {

                                            SharedPreferences.Editor editor = sharedPreferences.edit();

                                            // int ComponentID = ComponentSpinner.getSelectedItemPosition();
                                            int ComponentID = ComponentSpinner.getSelectedItemPosition();
                                            int FacilityID = FacilitySpinner.getSelectedItemPosition();


//                                            int ComponentID = Component_List.get(Component).getCompId();
                                            //   String ComponentName = Component_List.get(Component).getCompName();
//                                            int FacilityID = Facility_List.get(Facility).getFacID();
                                            // String FacilityName = Facility_List.get(Facility).getFacName();


                                            SiteLocation = siteLocation.getText().toString();
                                            Notes = notes.getText().toString();

                                            if (ComponentID != 0 && FacilityID != 0) {
                                                bottomSheetDialog.dismiss();
                                                mIsRecording = true;
                                                createVideoFolder();
                                                startRecord();

                                                Component = Component_List.get(ComponentID).getCompId();
                                                Facility = Facility_List.get(FacilityID).getFacID();

                                                editor.putInt("Component", ComponentID);
                                                editor.putInt("Facility", FacilityID);
                                                editor.putString("SiteLocation", SiteLocation);
                                                editor.putString("Notes", Notes);
                                                editor.commit();
                                                Toast.makeText(CameraActivity.this, "Recording Started", Toast.LENGTH_SHORT).show();
                                            } else {
                                                if (FacilityID == 0) {
                                                    Toast.makeText(CameraActivity.this, "Please Select Facility", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(CameraActivity.this, "Please Select Component", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                        }
                                    });
                                    CancelBTN.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            bottomSheetDialog.dismiss();
                                        }
                                    });
                                }
                            }, 500);

                        }
                    } else {
                        checkWriteStoragePermission();
                    }
                }
//                else {
//                    if (mIsRecording) {
//                        mIsRecording = false;
//                        mRecordImageButton.setImageResource(R.drawable.record_paused);
//
//                        //toggleFlashModeRecord(flashStatus);
//
//                        mMediaRecorder.stop();
//                        mMediaRecorder.reset();
//                        startPreview();
//                    } else {
//                        mIsRecording = true;
//                        mRecordImageButton.setImageResource(R.drawable.record_resumed);
//                        createVideoFolder();
//
//                        startRecord();
//                    }
//                }
            }
        });

        //Flash ON/OFF Handling:
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

                //If Recording is started and We Click flashLight Button so ,
                //FlashStatus going are ON or OFF.

                // If Recording is ON, we can handle FlashLight using toggleFlashModeRecord(flashStatus) function;
                if (mIsRecording) {
                    try {
                        toggleFlashModeRecord(flashStatus);
                    } catch (Exception e) {
                    }
                }
                // If Recording is off , we cam off handle FlashLight usinng toggleFlashMode(flashStatus) function;
                else {
                    toggleFlashMode(flashStatus);
                }
            }
        });
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

    private void hidekeyboard(LinearLayout linearLayout) {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(linearLayout.getWindowToken(), 0);
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
                startBluetooth();
                startLocation();
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

    private void startLocation() {
        if (checkLocationPermission()) {
            if (isOnline(getApplicationContext())) {
                turnOnGPS();
            } else {
                Toast.makeText(this, "Need Internet Connection", Toast.LENGTH_SHORT).show();
            }
        } else {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_PERMISSION);
        }
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean isGPSEnabled() {
        if (isOnline(getApplicationContext())) {
            LocationManager locationManager = null;
            boolean isEnabled = false;
            if (locationManager == null) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            }
            isEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            return isEnabled;
        } else {
            Toast.makeText(this, "Need Internet Connection", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    private void turnOnGPS() {
        locationHandle();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(this)
                .checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @SuppressLint("MissingPermission")
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    LocationCallback locationCallback = new LocationCallback() {
                        @Override
                        public void onLocationResult(LocationResult locationResult) {
                            if (locationResult != null) {
                                for (Location location : locationResult.getLocations()) {

                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();

                                    String lat = String.format("%.5f", latitude);
                                    String lon = String.format("%.5f", longitude);

                                    latitudeTV.setText("LAT-" + lat);
                                    longitudeTV.setText("LON-" + lon);

//                                    latitudeTV.setText("LAT-" + String.valueOf(location.getLatitude()));
//                                    longitudeTV.setText("LONG-" + String.valueOf(location.getLongitude()));
                                }
                            } else {
                                Log.e("TurnOnGps", "LocationCallbackCatch");
                            }
                        }
                    };
                    mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
                } catch (ApiException e) {
                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult((Activity) CameraActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            break;
                    }
                }
            }
        });

    }

    private void startBluetooth() {
        checkBluetoothIsOn();
    }

    @SuppressLint("MissingPermission")
    private void checkBluetoothIsOn() {
        if (!bluetoothAdapter.isEnabled()) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 999);
        } else {
            bluetoothData();
        }
    }

    @SuppressLint("MissingPermission")
    private void bluetoothData() {
        @SuppressLint("MissingPermission")
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        String[] strings = new String[pairedDevices.size()];
        paired_device_array = new BluetoothDevice[pairedDevices.size()];
        int i = 0;

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                paired_device_array[i] = device;
                strings[i] = device.getName();
                i++;
            }
            arrayAdapter = new ArrayAdapter(CameraActivity.this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, strings);

            AlertDialog.Builder builder = new AlertDialog.Builder(CameraActivity.this);


            builder.setTitle("Choose Bluetooth Device");
            builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int item) {
                    ClientDevice clientDevice = new ClientDevice(paired_device_array[item]);
                    clientDevice.start();
                }
            });
            AlertDialog alert = builder.create();
            alert.show();

            alert.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
            alert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
        }
    }

    @SuppressLint("MissingPermission")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999 && resultCode == RESULT_OK) {
            Toast.makeText(CameraActivity.this, "Bluetooth is Turned on", Toast.LENGTH_SHORT).show();
            bluetoothData();
        }
        if (requestCode == 2 && resultCode == RESULT_OK) {
               turnOnGPS(); //Null Work
        } else {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(CameraActivity.this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {

                                double latitude = location.getLatitude();
                                double longitude = location.getLongitude();

                                String lat = String.format("%.5f", latitude);
                                String lon = String.format("%.5f", longitude);

                                latitudeTV.setText("LAT-" + lat);
                                longitudeTV.setText("LON-" + lon);

//                                latitudeTV.setText("LAT-"+String.valueOf(location.getLatitude()));
//                                longitudeTV.setText("LONG-"+String.valueOf(location.getLongitude()));
                            } else {
                                latitudeTV.setText("LAT-" + "Null");
                                longitudeTV.setText("LON-" + "Null");
                            }
                        }
                    });

//            LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
//            List<String> providers = locationManager.getProviders(true);
//            Location bestLocation = null;
//            for (String provider : providers) {
//                Location l = locationManager.getLastKnownLocation(provider);
//                if (l == null) {
//                    continue;
//                }
//                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
//                    latitudeTV.setText("LAT-" + String.valueOf(bestLocation.getLatitude()));
//                    longitudeTV.setText("LONG-" + String.valueOf(bestLocation.getLongitude()));
//                }
//            }
        }
    }

    private void BottomSheet(int bottomSheet) {
        bottomSheetDialog = new BottomSheetDialog(CameraActivity.this);

        //mdecorView=bottomSheetDialog.getWindow().getDecorView();
        // hideSystemUI();


        BottomSheetBehavior<View> bottomSheetBehavior;
        View bottomSheetView = LayoutInflater.from(CameraActivity.this).inflate(bottomSheet, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        bottomSheetBehavior = BottomSheetBehavior.from((View) bottomSheetView.getParent());
        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        bottomSheetBehavior.setMaxHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        bottomSheetBehavior.setMaxWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        bottomSheetBehavior.setPeekHeight(100);
        bottomSheetBehavior.setDraggable(false);

        bottomSheetDialog.show();


        bottomSheetDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        bottomSheetDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);


        // hideSystemUI();

    }

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

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            getWindow().getDecorView().getWindowInsetsController().hide(WindowInsets.Type.systemBars());
//        } else {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN);
//        }

//        View decorView = getWindow().getDecorView();
//        if (hasFocus) {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }else
//        {
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                    | View.SYSTEM_UI_FLAG_FULLSCREEN
//                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        }
//
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);


            mdecorView = getWindow().getDecorView();
            mdecorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);

    }


    private void stopRecord() {
        progressDialog.setMessage("Processing Please Wait");
        progressDialog.setCancelable(false);
        //mdecorView = progressDialog.getWindow().getDecorView();
        progressDialog.show();

        progressDialog.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        progressDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);

        recordingTimer.cancel();
        seconds = 0;

        //hideSystemUI();
        //countDownTimer.cancel();

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


        // String a = "-y -i " + fileSavePath + " -metadata comment=" + json + " -framerate 60 -vf [in]" + text() + "[out] " + mVideoFileName;
        String a = "-y -i " + fileSavePath + " -metadata comment=" + json + " -vf [in]" + text(fileSavePath) + "[out] -r 18 -qscale 0 -preset ultrafast " + mVideoFileName;
        //  String a = "-y -i " + fileSavePath + " -metadata comment=" + json + " -vf [in]" + text() + "[out] -crf 18 -vf format=yuv420p -r 25 -qscale 0 " + mVideoFileName;
        Log.e("text Value", a);

        long executionId = FFmpeg.executeAsync(a, (executionId1, returnCode) -> {
            if (returnCode == Config.RETURN_CODE_SUCCESS) {
                try {
                    VideoSavedSuccessFully();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
                MinMaxAvgList.clear();

                TimeArrayList.clear();

                LatitudeList.clear();
                LongitudeList.clear();

                mIsRecording = false;
                //countDownTimer.start();
                recordingTimeTV.setText("00:00:00");
            } else if (returnCode == Config.RETURN_CODE_CANCEL) {
                progressDialog.dismiss();
            } else {
                progressDialog.dismiss();
            }
        });
    }

    private void VideoSavedSuccessFully() throws IOException {
        Toast.makeText(this, "Video Saved Successfully", Toast.LENGTH_SHORT).show();
        File file1 = new File(mTempVideoFileName);
        file1.delete();
    }

    private void createJSON() throws IOException {
        System.out.println("MinMaxSize : " + MinMaxAvgList.size());
        for (int i = 0; i < MinMaxAvgList.size(); i++) {
            System.out.println("MinMax : " + i + " : " + MinMaxAvgList.get(i));
        }


        System.out.println("TimeArrayList : " + TimeArrayList.size());
        for (int i = 0; i < TimeArrayList.size(); i++) {
            System.out.println("TimeArray : " + i + " : " + TimeArrayList.get(i));
        }

        float Min = 0;
        float Max = 0;
        float Average = 0;

        Min = MinMaxAvgList.get(0);
        for (int i = 0; i < MinMaxAvgList.size(); i++) {
            if (Min > MinMaxAvgList.get(i)) {
                Min = MinMaxAvgList.get(i);
            }
        }

        Max = MinMaxAvgList.get(0);
        for (int i = 0; i < MinMaxAvgList.size(); i++) {
            if (Max < MinMaxAvgList.get(i)) {
                Max = MinMaxAvgList.get(i);
            }
        }

        float Total = 0;
        for (int i = 0; i < MinMaxAvgList.size(); i++) {
            Total = Total + MinMaxAvgList.get(i);
        }
        Average = Total / MinMaxAvgList.size();


        try {
            // Create a new instance of a JSONObject
            final JSONObject object = new JSONObject();
            // With put you can add a name/value pair to the JSONObject
            object.put("videoTitle", fileName + ".mp4");
            object.put("component", Component);
            object.put("facility", Facility);
            object.put("sitelocation", SiteLocation);
            object.put("notes", Notes);
            object.put("min", String.valueOf(Min));
            object.put("max", String.valueOf(Max));
            object.put("average", String.valueOf(Average));

            // Calling toString() on the JSONObject returns the JSON in string format.

            json = object.toString();
            String sEncoded = URLEncoder.encode(json, "UTF-8");
            json = sEncoded;
            System.out.println("ENCODER : " + sEncoded);

//            String sDecoded = URLDecoder.decode(json, "UTF-8");
//            System.out.println(" : " + sDecoded);

        } catch (JSONException e) {
            Log.e(TAG, "Failed to create JSONObject", e);
        }
    }

    private String text(String fileSavePath) {

        String VideoHeight=VideoHeight(fileSavePath);
        String VideoWidth=VideoWidth(fileSavePath);

        int height=Integer.parseInt(VideoHeight);
        int width=Integer.parseInt(VideoWidth);


        int textSize;
        int datex,datey;
        int timex,timey;
        int latx,laty;
        int lonx,lony;


        if (width < 1080) {
           textSize=40;
           datex=width-220;
           datey=30;

           timex=width-440;
           timey=30;

           latx=width/2;
           latx=latx/2-100;
           laty=height-100;

           lonx=width/2;
           lonx=lonx+100;
           lony=height-100;

        } else if (width <= 1280) {
            textSize=50;
            datex=width-250;
            datey=40;

            timex=width-500;
            timey=40;

            latx=width/2-150;
            laty=height-150;

            lonx=width/2+150;
            lony=height-150;

        } else {
            textSize=60;
            datex=width-350;
            datey=50;

            timex=width-700;
            timey=50;

            latx=width/2-700;
            laty=height-200;

            lonx=width/2+350;
            lony=height-200;

        }


        String text = "";
        for (int i = 0; i < MinMaxAvgList.size(); i++) {

            String LAT = "LAT-" + i;
            String LON = "LON-" + i;

            if (i < MinMaxAvgList.size() - 1) {
                text = text + "drawtext=text=" + MinMaxAvgList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=80:y=(h-text_h)/2:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + RecordingDate + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x="+datex+":y="+datey+":enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + TimeArrayList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x="+timex+":y="+timey+":enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + LatitudeList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x="+latx+":y="+laty+":enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + LongitudeList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x="+lonx+":y="+lony+":enable='between(t\\," + i + "\\," + (i + 1) + ")',";

            } else {
                text = text + "drawtext=text=" + MinMaxAvgList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=80:y=(h-text_h)/2:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + RecordingDate + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x="+datex+":y="+datey+":enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + TimeArrayList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x="+timex+":y="+timey+":enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + LatitudeList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x="+latx+":y="+laty+":enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
                        "drawtext=text=" + LongitudeList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize="+textSize+":fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x="+lonx+":y="+lony+":enable='between(t\\," + i + "\\," + (i + 1) + ")'";

            }
            System.out.println(text);
        }
        return text;
    }


//    text = text + "drawtext=text=" + MinMaxAvgList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=80:y=(h-text_h)/2:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
//            "drawtext=text=" + RecordingDate + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-10:y=10:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
//            "drawtext=text=" + TimeArrayList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-500:y=10:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
//            "drawtext=text=" + LatitudeList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=300:y=h-th-100:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
//            "drawtext=text=" + LongitudeList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-300:y=h-th-100:enable='between(t\\," + i + "\\," + (i + 1) + ")',";
//
//} else {
//        text = text + "drawtext=text=" + MinMaxAvgList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=80:y=(h-text_h)/2:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
//        "drawtext=text=" + RecordingDate + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-10:y=10:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
//        "drawtext=text=" + TimeArrayList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-500:y=10:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
//        "drawtext=text=" + LatitudeList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=300:y=h-th-100:enable='between(t\\," + i + "\\," + (i + 1) + ")'," +
//        "drawtext=text=" + LongitudeList.get(i) + ":fontfile=/system/fonts/Roboto-Regular.ttf:fontsize=60:fontcolor=yellow:box=1:boxcolor=black@0.5:boxborderw=5:x=w-tw-300:y=h-th-100:enable='between(t\\," + i + "\\," + (i + 1) + ")'";
//        }

    private String VideoHeight(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, Uri.parse(videoPath));
        } catch (Exception e) {
            Log.e("Error3", e.toString());

        }
        String Height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        retriever.release();
        return Height;
    }
    private String VideoWidth(String videoPath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(this, Uri.parse(videoPath));
        } catch (Exception e) {
            Log.e("Error3", e.toString());

        }
        String Width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH);
        retriever.release();
        return Width;
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

        Facility_List.clear();
        Component_List.clear();

        Facility_List = sqLiteModel.getFacilityListForCameraActivitySpinner();
        Component_List = sqLiteModel.getComponentListForCameraActivitySpinner();

        TimeArrayList = new ArrayList<>();
        MinMaxAvgList = new ArrayList<>();
        LatitudeList = new ArrayList<>();
        LongitudeList = new ArrayList<>();

        if (mTextureView.isAvailable()) {
            setUpCamera(mTextureView.getWidth(), mTextureView.getHeight());
            if (isSurfaceAvailable == true) {
                connectCamera();
            }
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }


        //countDownTimer.start();
    }


    //Need Time Continuously on Screen So we add this in a handler:
    private void Time() {
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                LocalDateTime myObj = LocalDateTime.now();
                //Date
                DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                String formattedDate = myObj.format(dateFormat);
                dateTV.setText(formattedDate);
                RecordingDate = formattedDate;

                //Time
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
                DateTimeFormatter timeFormatter1 = DateTimeFormatter.ofPattern("HH\\\\:mm\\\\:ss");

                String formattedTime = myObj.format(timeFormatter);
                String formattedTime1 = myObj.format(timeFormatter1);

                timeTV.setText(formattedTime);

                if (mIsRecording) {
                    TimeArrayList.add(formattedTime1);
                    LatitudeList.add(latitudeTV.getText().toString());
                    LongitudeList.add(longitudeTV.getText().toString());
                }
                handler.postDelayed(runnable, 1000);
            }
        };
        handler.post(runnable);
    }


    //This countDown Function used for Bluetooth Data:
    private void countDown() {
        bluetoothHandler = new Handler();
        bluetoothRunnable = new Runnable() {
            @Override
            public void run() {
                bluetoothCount.setText(String.valueOf(BluetoothText));
                if (mIsRecording) {
                    int val = -100;
                    try {
                        val = Integer.parseInt(bluetoothCount.getText().toString());
                    } catch (Exception e) {

                    }
                    MinMaxAvgList.add(Float.valueOf(val));
                }
                bluetoothHandler.postDelayed(bluetoothRunnable, 1000);
            }
        };
        bluetoothHandler.post(bluetoothRunnable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
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
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                turnOnGPS();
            } else {
                Toast.makeText(this, "REQUEST_LOCATION_PERMISSION", Toast.LENGTH_SHORT).show();

            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            sendReceieve.cancel();
        } catch (Exception e) {

        }
        //We will Stop VideoRecording when this lifecycle will appear.
        //so all components will stop like countdown, if recording is on then stop recording.

        //countDownTimer.cancel();
        stopBackgroundThread();

        //Stop Recording if On
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

        //Turn off Flash if ON:
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

    @SuppressLint("MissingPermission")
    private void connectCamera() {
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.openCamera(mCameraId, mCameraDeviceStateCallback, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }


    //If Rationale Permissions are needed at that Time this function is used we only pass Permission name in string
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

    //Recording Started
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
                                //   mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
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

    //Recording Timer Customization:
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

    //Camera PreView Launch:
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
                                //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);
                                //mCaptureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                                mPreviewCaptureSession.setRepeatingRequest(mCaptureRequestBuilder.build(),
                                        null, mBackgroundHandler); //Nackgroundasdd
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

    private static int sensorToDeviceOrientation(CameraCharacteristics
                                                         cameraCharacteristics, int deviceOrientation) {
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

    //Created Folder for Saving Files:
    private void createVideoFolder() {
        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        mVideoFolder = new File(movieFile, "InspRec");
        if (!mVideoFolder.exists()) {
            mVideoFolder.mkdirs();
        }
    }

    //Created this File for Saving a FFMpeg Edited video:
    //This are a Final VideoFile Output.
    private void createVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("YYYYMMdd_HHmmss").format(new Date());
        fileName = "ECG" + timestamp;

        String filePath = mVideoFolder + File.separator + fileName + ".mp4";
        File file = new File(filePath);
        mVideoFileName = file.getAbsolutePath();
    }

    //Created this File for Saving a Recorded video and using this file we can Operation a FFMPEG Commands:
    //Function Name are like this are a final output , but it's not it is only a CameraRecorded video file:
    private void createTempFFmPEGVideoFileName() throws IOException {
        String timestamp = new SimpleDateFormat("YYYYMMdd_HHmmss").format(new Date());

        String fileName = "ECG-ffmpeg" + timestamp;
        String filePath = mVideoFolder + File.separator + fileName + ".mp4";
        File file = new File(filePath);
        mTempVideoFileName = file.getAbsolutePath();
    }

    //Storage Permission:
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

    //MediaRecorder for Saving for Recorded Video
    @SuppressLint("NewApi")
    private void setUpMediaRecorder() throws IOException {

        //STEP 1: Created TempFile for Video Recording:
        try {
            createTempFFmPEGVideoFileName();
        } catch (Exception e) {

        }

        //STEP 2: Video Recording Surface Send as VideoSource:
        try {
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
        } catch (Exception e) {

        }


        //STEP 3: CamcorderProfile for getting Device Maximum Capacity
        CamcorderProfile camcorderProfile;
        //IF Quality HIGH MAX Capacity we can get
        camcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);

        //STEP 4: Set all other Functions to a mediaRecorder.
        VideoBitrate = camcorderProfile.videoBitRate;
        mMediaRecorder.setOutputFile(mTempVideoFileName);
        mMediaRecorder.setVideoEncodingBitRate(VideoBitrate);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setVideoFrameRate(camcorderProfile.videoFrameRate);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);


        System.out.println("Width : " + mVideoSize.getWidth() + " Height : " + mVideoSize.getHeight());
        System.out.println("Width : " + camcorderProfile.videoFrameWidth + " Height : " + camcorderProfile.videoFrameHeight);


        //STEP 5: VideoSize gives error because SomeDevice not recording its Full capacity, so we customize this.
        //Not all devices tested but some available devices are checked and its working Properly.
       if (mVideoSize.getWidth() < 1080) {
            mMediaRecorder.setVideoSize(960, 720);
        } else if (mVideoSize.getWidth() <= 1280) {
            mMediaRecorder.setVideoSize(1280, 720);
        } else {
            mMediaRecorder.setVideoSize(1920, 1080);
        }


        //STEP 6: If any error from mediaRecorder from STEP2, prepare are Unsuccessful.
        //After Successful Prepare we will start MediaRecorder that records our Screen.
        mMediaRecorder.prepare();
        mMediaRecorder.start();
    }

    @Override
    public void onBackPressed() {
        //This handled for is when VideoConverting is happened, and user will clicks on backButton.
        if (progressDialog.isShowing()) {
            Toast.makeText(this, "Please Wait Video Processing needs to complete", Toast.LENGTH_SHORT).show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}