package com.example.happy.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class gigibiru {
    BluetoothAdapter bluetooth2 = null;
    static BluetoothSocket btSocket = null;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    String wasd,MSG;

    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    public gigibiru(){

    }
    public gigibiru(String wasd){
        this.wasd = wasd;
    }
    public void connectBT() throws IOException {
        bluetooth2 = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
        BluetoothDevice dispositivo = bluetooth2.getRemoteDevice(wasd);//connects to the device's address and checks if it's available
        btSocket = dispositivo.createRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
        btSocket.connect();//start connection

    }
    public BluetoothSocket getBT(){
        return btSocket;
    }
    public void writeGigi(String msg){
        try {
            btSocket.getOutputStream().write(msg.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void readGigi(){

    }

    public String getData(){
        return MSG;
    }



}
