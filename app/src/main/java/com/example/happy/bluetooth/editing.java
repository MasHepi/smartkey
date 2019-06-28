package com.example.happy.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class editing extends AppCompatActivity {
    Button btnres;
    EditText userlama;
    EditText pinlama;
    EditText userbaru;
    EditText pinbaru;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String address = null;
    String id;
    String pass;
    private ProgressDialog progress;
    BluetoothAdapter bluetooth2 = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    private boolean flagLoginSuccess = false;
    public InputStream tempIn = null;
    gigibiru gigi;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);
        btnres = (Button)findViewById(R.id.tombol6);
        userlama = (EditText)findViewById(R.id.id1);
        pinlama = (EditText)findViewById(R.id.pin1);
        userbaru = (EditText)findViewById(R.id.id2);
        pinbaru = (EditText)findViewById(R.id.pin2);



        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS");
        id = newint.getStringExtra("ID_ADDRESS");
        //id = userlama.getText().toString();
        pass = newint.getStringExtra("PASS_ADDRESS");
        //pass = pinlama.getText().toString();
        gigi = new gigibiru();
        btSocket = gigi.getBT();
        btnres.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent i = new Intent(editing.this, setting.class);
                //startActivity(i);
                new ConnectBT().execute();
            }
        });
    }
    public class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;

        protected void onPreExecute() {
            progress = ProgressDialog.show(editing.this, "Connecting...", "Please wait");  //show a progress dialog
            //super.onPreExecute();
        }
        @Override
        protected Void doInBackground(Void... voids) {
            if (btSocket == null || !isBtConnected)
            {
                btSocket = gigi.getBT();
                /*
                bluetooth2 = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice dispositivo = bluetooth2.getRemoteDevice(address);//connects to the device's address and checks if it's available
                btSocket = dispositivo.createRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();//start connection
                */
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!ConnectSuccess)
            {
                Toast.makeText(getApplicationContext(),"Connection Failed. Try again.",Toast.LENGTH_LONG).show();

                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Connected.",Toast.LENGTH_LONG).show();
                isBtConnected = true;

                if (btSocket!=null)
                {
                    try
                    {
                        tempIn = btSocket.getInputStream();
                        //dari sini beda
                        btSocket.getOutputStream().write(("editadmin;"+userlama.getText().toString() + "+" + pinlama.getText().toString() + "," + userbaru.getText().toString() + "+" + pinbaru.getText().toString() + ".").toString().getBytes());
                        /*
                        byte[] buff = new byte[1024];
                        int leng = 0;
                        leng = tempIn.read(buff);
                        String tex = new String(buff,0,leng);
                        Log.d("resp",tex);
                        */
                        flagLoginSuccess = true;
                        threadConnected a = new threadConnected();
                        Thread loop = new Thread(a);
                        loop.start();
                    }
                    catch (IOException e)
                    {
                        Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_LONG).show();
                    }
                }
            }
            progress.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            btSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class threadConnected implements Runnable {
        @Override
        public void run() {
            while(true) {
                if (flagLoginSuccess) {
                    try {
                        btSocket.getOutputStream().write(("conn").toString().getBytes());
                        Thread.sleep(5000);//ini itu message dari HP bentuk e bytes
                    } catch (IOException e) {
                        //Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
                        break;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}