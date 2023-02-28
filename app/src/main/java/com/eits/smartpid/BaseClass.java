package com.eits.smartpid;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class BaseClass extends AppCompatActivity {
    BluetoothAdapter bluetoothAdapter;
   // final String uuidID="fe964a9c-184c-11e6-b6ba-3e1d05defe78";
    final String uuidID="f00001101-0000-1000-8000-00805f9b34fb";

    String ThreadName = null;
    final int STATE_CONNECTING = 1;
    final int STATE_CONNECTED = 2;
    final int STATE_CONNECTION_FAILED = 3;
    final int STATE_BLUETOOTH_OFF = 4;
    final int STATE_MESSAGE_RECIEVED = 5;

    String BluetoothText="0";

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bluetoothAdapter=BluetoothAdapter.getDefaultAdapter();

    }


    @Override
    protected void onResume() {
        super.onResume();
    }
    Handler handler = new Handler(Looper.getMainLooper()) {
        @SuppressLint("MissingPermission")
        @Override
        public void handleMessage(@NonNull Message message) {
            switch (message.what) {
                case STATE_CONNECTING:
                    BluetoothText="connecting";
                    break;
                case STATE_CONNECTED:
                    Toast.makeText(BaseClass.this, "Bluetooth Connected", Toast.LENGTH_SHORT).show();
                    break;
                case STATE_CONNECTION_FAILED:
                    BluetoothText="0";
                    Toast.makeText(BaseClass.this, "Bluetooth Connection Failed", Toast.LENGTH_SHORT).show();
                    break;

                case STATE_BLUETOOTH_OFF:
                    //countDownTimer.cancel();
                    break;

                case STATE_MESSAGE_RECIEVED:
                    byte[] readBuffA = (byte[]) message.obj;
                    String tempMsgA = new String(readBuffA, 0, message.arg1);
                    BluetoothText=tempMsgA;

                    break;

            }
        }
    };

    public class ClientDevice extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        @SuppressLint("MissingPermission")
        public ClientDevice(BluetoothDevice device1) {
            device = device1;
            try {
                if (bluetoothAdapter.isEnabled()) {
                    socket = device.createRfcommSocketToServiceRecord(UUID.fromString(uuidID));
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("MissingPermission")
        public void run() {
           bluetoothAdapter.cancelDiscovery();
            try {
                Message connecting = Message.obtain();
                connecting.what = STATE_CONNECTING;
                handler.sendMessage(connecting);

                socket.connect();
                Log.e("Socket Connected","Connected");

                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);

                SendReceieve sendReceieve = new SendReceieve(socket);
                sendReceieve.start();

            } catch (IOException e) {
                Log.e("run catch",e.toString());
                try {
                    socket.close();
                } catch (IOException ex) {
                   Log.e("ex",ex.toString());
                }
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }

    }
    public class SendReceieve extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceieve(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;

            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED, bytes, -1, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                    Message message1 = Message.obtain();
                    message1.what = STATE_CONNECTION_FAILED;
                    handler.sendMessage(message1);

//                    Message message2 = Message.obtain();
//                    message2.what = STATE_CONNECTION_FAILED;
//                    handler.sendMessage(message2);
                    break;
                }
            }
        }

        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
                //cancel();
            }
        }

        public void cancel() {
            try {
                inputStream.close();
                outputStream.close();
                bluetoothSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }

        }
    }


}




// IMP
//public class BaseActivity extends AppCompatActivity {
//
//    static Boolean IsResumecalled = false;
//
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (!IsResumecalled) {
//            IsResumecalled= true;
//            //write your code here
//        }
//    }
//
//}