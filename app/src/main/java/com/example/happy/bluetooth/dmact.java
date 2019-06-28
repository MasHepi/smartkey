package com.example.happy.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class dmact extends AppCompatActivity {
    String address = null, MSG;
    BluetoothAdapter bluetooth2 = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Handler btin;

    InputStream input = null;
    OutputStream output = null;

    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    public dmact(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dmact);

        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS");

        final Button btn = findViewById(R.id.ntr);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new bck(dmact.this).execute();

            }
        });

    }

    public class bck extends AsyncTask<Void,Void,Void>{
        Context ctx;
        public bck (Context ctx){
            this.ctx = ctx;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    bluetooth2 = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = bluetooth2.getRemoteDevice(address);//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                //ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            try {
                isBtConnected = true;
                mmInputStream = btSocket.getInputStream();
                output = btSocket.getOutputStream();
                output.write(("ian" + "+" + "ian" + ";").toString().getBytes());
                output.flush();
                byte[] buff = new byte[1024];
                int leng = 0;
                //leng = input.read(buff);
                String tex = new String(buff,0,leng);
                Log.d("resp",tex);
                //Toast.makeText(this.ctx, tex, Toast.LENGTH_SHORT).show();
                beginListenForData();
                /// /readT wasd = new readT(this.ctx);
                //new Thread(wasd).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class readT implements Runnable{
        Context ctx;
        public readT(Context ctx){
            this.ctx = ctx;
        }

        @Override
        public void run() {
            while (true){
                byte[] buff = new byte[1024];
                int leng = 0;
                try {
                    leng = input.read(buff);
                    String tex = new String(buff,0,leng);
                    Log.d("resp",tex);
                    //Toast.makeText(this.ctx, tex, Toast.LENGTH_SHORT).show();
                    btSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void beginListenForData()
    {
        final Handler handler = new Handler();
        final byte delimiter = 10; //This is the ASCII code for a newline character

        stopWorker = false;
        readBufferPosition = 0;
        readBuffer = new byte[1024];
        workerThread = new Thread(new Runnable()
        {
            public void run()
            {
                while(!Thread.currentThread().isInterrupted() && !stopWorker)
                {
                    try
                    {
                        int bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {
                            byte[] packetBytes = new byte[bytesAvailable];
                            mmInputStream.read(packetBytes);
                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    MSG = data;
                                    readBufferPosition = 0;
                                    //data.replaceAll("[\u0000-\u001f]","");

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            //myLabel.setText(data);
                                            //data.trim();
                                            Log.d("resp",MSG+",lenght: "+MSG.length());
                                            MSG = data.replaceAll("[\u0000-\u001f]","");
                                            Log.d("resp","after: "+MSG+",lenght: "+MSG.length());
                                        }
                                    });
                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    }
                    catch (IOException ex)
                    {
                        stopWorker = true;
                    }
                }
            }
        });

        workerThread.start();
    }
}
