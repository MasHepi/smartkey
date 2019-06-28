package com.example.happy.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class adding extends AppCompatActivity {

    EditText tambahanID, tambahanPIN;
    Button btnSubTambah;
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

    String idPass;
    String idPass2;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register);

        tambahanID = (EditText)findViewById(R.id.tambahID);
        tambahanPIN = (EditText)findViewById(R.id.tambahPIN);

        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS");
        id = newint.getStringExtra("ID_ADDRESS");
        pass = newint.getStringExtra("PASS_ADDRESS");
        idPass = id + "+" + pass;

        gigi = new gigibiru();
        btSocket = gigi.getBT();

        btnSubTambah = (Button)findViewById(R.id.tambahkan);
        btnSubTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idPass2 = tambahanID.getText().toString()+"+"+tambahanPIN.getText().toString()+".";
                bck3 bck = new bck3(adding.this,btSocket,idPass,idPass2);
                bck.getmain(new bck.tomain1() {
                    @Override
                    public void tomain2(BluetoothSocket bt) {
                        try {
                            bt.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        finish();
                    }
                });
                bck.execute();
            }
        });
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

    public class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;

        protected void onPreExecute() {
            progress = ProgressDialog.show(adding.this, "Connecting...", "Please wait");  //show a progress dialog
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
                        btSocket.getOutputStream().write(("add;"+idPass+","+idPass2).toString().getBytes());
                        //flagLoginSuccess = true;
                        //adding.threadConnected a = new adding.threadConnected();
                        //Thread loop = new Thread(a);
                        //loop.start();
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
