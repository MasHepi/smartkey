package com.example.happy.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

public class bck2 extends AsyncTask<Void, Void, Void>
{
    private boolean ConnectSuccess = true;
    private ProgressDialog progress;
    Context ctx;
    BluetoothSocket bt;
    private boolean isBtConnected = false;
    private boolean flagLoginSuccess = false;
    String idpass;
    OutputStream output = null;

    public bck2 (Context ctx, BluetoothSocket bt, String idpass){
        this.ctx = ctx;
        this.bt = bt;
        this.idpass = idpass;
    }

    protected void onPreExecute() {
        progress = ProgressDialog.show(this.ctx, "Connecting...", "Please wait");  //show a progress dialog
        //super.onPreExecute();
    }

    interface tomain1{
        public void tomain2(BluetoothSocket bt);
    }

    bck.tomain1 tomain3;

    void getmain(bck.tomain1 tomain1){
        this.tomain3 = tomain1;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return null;
    }
    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (!ConnectSuccess)
        {
            Toast.makeText(ctx,"Connection Failed. Try again.",Toast.LENGTH_LONG).show();
        }
        else
        {
            Toast.makeText(ctx,"Connected.",Toast.LENGTH_LONG).show();
            isBtConnected = true;
            try {
                output = bt.getOutputStream();
                output.write(("getallusers;" + idpass + ".\n").toString().getBytes());
                output.flush();
                tomain3.tomain2(bt);
            } catch (IOException e) {
                e.printStackTrace();
            }


        }
        progress.dismiss();
    }

}