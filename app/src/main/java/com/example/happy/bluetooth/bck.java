package com.example.happy.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class bck extends AsyncTask<String,Void,Void> {
    String address = null;
    BluetoothAdapter bluetooth2 = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    Context ctx;
    OutputStream output = null;
    InputStream mmInputStream = null;
    public bck (Context ctx){
        this.ctx = ctx;
    }

    interface tomain1{
        public void tomain2(BluetoothSocket bt);
    }

    tomain1 tomain3;

    void getmain(tomain1 tomain1){
        this.tomain3 = tomain1;
    }

    @Override
    protected Void doInBackground(String... voids) {
        try
        {
            if (btSocket == null || !isBtConnected)
            {
                bluetooth2 = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice dispositivo = bluetooth2.getRemoteDevice(voids[0]);//connects to the device's address and checks if it's available
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
        isBtConnected = true;
        //mmInputStream = btSocket.getInputStream();
        //output = btSocket.getOutputStream();
        tomain3.tomain2(btSocket);
                /*
                output.write(("login;" + idInput.getText().toString() + "+" + passwordInput.getText().toString() + ".").toString().getBytes());
                output.flush();
                //leng = input.read(buff);
                //Toast.makeText(this.ctx, tex, Toast.LENGTH_SHORT).show();
                beginListenForData();
                /// /readT wasd = new readT(this.ctx);
                //new Thread(wasd).start();
                */
    }
}
