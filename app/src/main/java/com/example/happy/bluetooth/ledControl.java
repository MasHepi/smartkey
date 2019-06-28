package com.example.happy.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class ledControl extends AppCompatActivity{
    Button btnDis;
    String address = null;
    String id;
    String pass;
    private ProgressDialog progress;
    BluetoothAdapter bluetooth2 = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    TextView text1;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean flagLoginSuccess = false;
    public String bluetoothMessage = "";
    int motiondouble =0;
    int motiondouble1 = 0;


    public InputStream tempIn = null;

    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS");
        id = newint.getStringExtra("ID_ADDRESS");
        pass = newint.getStringExtra("PASS_ADDRESS");

        setContentView(R.layout.home);

        final ToggleButton btn_starter =  (ToggleButton)findViewById(R.id.starter);
        btnDis = (Button)findViewById(R.id.disconnect);
        btn_starter.setTextOff("");
        btn_starter.setTextOn("");
        new ConnectBT().execute();

        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
                //finish();
            }
        });

        btn_starter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motiondouble++;
                //Toast.makeText(ledControl.this,"click",Toast.LENGTH_SHORT).show();
                Handler haha = new Handler();
                haha.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (motiondouble==1){
                            if(motiondouble1 == 1){
                                Toast.makeText(ledControl.this,"kunci buka, mesin idup",Toast.LENGTH_SHORT).show();
                                kunciON_mesinON();
                                btn_starter.setBackgroundResource(R.drawable.powergr);
                                motiondouble1 = 0;
                            }else{
                                Toast.makeText(ledControl.this,"kunci buka, mesin mati",Toast.LENGTH_SHORT).show();
                                kunciON_mesinOFF();
                                btn_starter.setBackgroundResource(R.drawable.powerred);
                                motiondouble1=1;
                            }
                        }else if (motiondouble == 2){
                            Toast.makeText(ledControl.this,"kunci buka, mesin idup",Toast.LENGTH_SHORT).show();
                            kunciON_mesinON();
                            btn_starter.setBackgroundResource(R.drawable.powergr);
                        }
                        motiondouble=0;
                    }
                },500);
            }
        });
        btn_starter.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                motiondouble1 = 0;
                Toast.makeText(ledControl.this,"kunci tutup, mesin mati",Toast.LENGTH_SHORT).show();
                kunciOFF_mesinOFF();
                btn_starter.setBackgroundResource(R.drawable.power);
                return true;
            }
        });
    }

    private void Disconnect()
    {
        if (btSocket!=null){
            try {
                btSocket.close(); //close connection
            }
            catch (IOException e) {
                Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_LONG).show();
            }
        }
        finish();
    }

    private void kunciON_mesinOFF()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("on1".toString().getBytes());
                //ini itu message dari HP bentuk e bytes

            }
            catch (IOException e)
            {
                Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void kunciON_mesinON()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("on2".toString().getBytes());
                //ini itu message dari HP bentuk e bytes

            }
            catch (IOException e)
            {
                Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_LONG).show();
            }
        }
    }

    private void kunciOFF_mesinOFF()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("off".toString().getBytes());
                //ini itu message dari HP bentuk e bytes

            }
            catch (IOException e)
            {
                Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_LONG).show();
            }
        }
    }


    public class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;
        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
            //super.onPreExecute();
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
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!ConnectSuccess)
            {
                Toast.makeText(getApplicationContext(),"Connection Failed. Is it a SPP Bluetooth? Try again.",Toast.LENGTH_LONG).show();
                finish();
            }
            else
            {
                Toast.makeText(getApplicationContext(),"Connected.",Toast.LENGTH_LONG).show();
                isBtConnected = true;
                try {
                    tempIn = btSocket.getInputStream();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (btSocket!=null)
                {
                    try
                    {
                        btSocket.getOutputStream().write((id + "+" + pass + ";").toString().getBytes());

                        threadConnected a = new threadConnected();
                        Thread loop = new Thread(a);
                        loop.start();

                        threadGettingMessage b = new threadGettingMessage();
                        Thread loop2 = new Thread(b);
                        loop2.start();
                        //ini itu message dari HP bentuk e bytes
//                        byte [] b = new byte[256];
//                        int a = tempIn.read(b);
//                        bluetoothMessage = new String (b, 0, a);
//                        System.out.println(bluetoothMessage);
//
//                        a = tempIn.read(b);
//                        bluetoothMessage = new String (b, 0, a);
//                        System.out.println(bluetoothMessage);
//
//                        a = tempIn.read(b);
//                        bluetoothMessage = new String (b, 0, a);
//                        System.out.println(bluetoothMessage);
//
//                        a = tempIn.read(b);
//                        bluetoothMessage = new String (b, 0, a);
//                        System.out.println(bluetoothMessage);
//                        btSocket.getInputStream().read();
//
//                        bluetoothMessage = "";
//
//                        System.out.println(b.length + " Hellow");
//                        for(int i=0; i<b.length; i++){
//                            System.out.println(b[i]);
//                        }
//
//                        for(int i=0;i<b.length;i++){
//                            bluetoothMessage += (char)b[i];
//                        }
//                        Toast.makeText(getApplicationContext(),bluetoothMessage,Toast.LENGTH_LONG).show();
//                        if(bluetoothMessage.equals("success")){
//                            flagLoginSuccess = true;
//                            threadConnected a = new threadConnected();
//                            Thread loop = new Thread(a);
//                            loop.run();
//                        }
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

    public class threadGettingMessage implements Runnable{

        @Override
        public void run() {
            while(true) {
                try {
//                while(btSocket.getInputStream().available()<0){
//////                    Thread.sleep(10);
////                }
                    byte btMessage[] = new byte[1000];
                    int btMessNum = 1;
                    while(btSocket.getInputStream().available()<0);
                    btMessNum = btSocket.getInputStream().read(btMessage);
                    String a = new String(btMessage, 0, btMessNum);
                    Log.d("resp", a);
//                Toast.makeText(getApplicationContext(),a,Toast.LENGTH_LONG).show();
                    flagLoginSuccess = true;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
