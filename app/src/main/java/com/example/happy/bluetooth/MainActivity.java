package com.example.happy.bluetooth;

import android.bluetooth.BluetoothA2dp;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button btn1;
    EditText idInput;
    EditText passwordInput;
    ListView devicelist;
    private BluetoothAdapter bluetooth1 = null;
    private Set<BluetoothDevice> pairedDevices;

    String address = null;
    BluetoothAdapter bluetooth2 = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    OutputStream output = null;

    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    boolean counter = false;
    volatile boolean stopWorker;

    String verif="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn1 = (Button) findViewById(R.id.tombol1);
        devicelist = (ListView)findViewById(R.id.list1);
        idInput = (EditText)findViewById(R.id.id);
        passwordInput = (EditText)findViewById(R.id.pass);
        bluetooth1 = BluetoothAdapter.getDefaultAdapter();

        if (bluetooth1 == null) {
            //Show a mensag. that thedevice has no bluetooth adapter
            Toast.makeText(getApplicationContext(), "Bluetooth Device Not Available", Toast.LENGTH_LONG).show();
            //finish apk
            finish();
        } else {
            if (bluetooth1.isEnabled()) {
            } else {
                //Ask to the user turn the bluetooth on
                Intent turnBTon = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(turnBTon, 1);
            }
        }

        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pairedDeviceList();
            }
        });
    }

    private void pairedDeviceList(){

        pairedDevices = bluetooth1.getBondedDevices();
        ArrayList list = new ArrayList();

        if (pairedDevices.size()>0)
        {
            for(BluetoothDevice bt : pairedDevices)
            {
                list.add(bt.getName() + "\n" + bt.getAddress()); //Get the device's name and the address
            }
        }
        else
        {
            Toast.makeText(getApplicationContext(), "No Paired Bluetooth Devices Found.", Toast.LENGTH_LONG).show();
        }



        final ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, list);
        devicelist.setAdapter(adapter);


        devicelist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the device MAC address, the last 17 chars in the View
                String info = ((TextView) view).getText().toString();
                address = info.substring(info.length() - 17);
                bck bck = new bck(MainActivity.this);
                bck.getmain(new bck.tomain1() {
                    @Override
                    public void tomain2(BluetoothSocket bt) {
                        try {
                            mmInputStream = bt.getInputStream();
                            output = bt.getOutputStream();
                            output.write(("login;" + idInput.getText().toString() + "+" + passwordInput.getText().toString() + ".\n").toString().getBytes());
                            output.flush();
                            beginListenForData(bt);
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                bt.close();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                });
                bck.execute(address);
            }
        }); //Method called when the device from the list is clicked
    }

    void beginListenForData(final BluetoothSocket bt)
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
                                    //final String data = new String(encodedBytes);
                                    verif = new String(encodedBytes);
                                    Log.d("resp321", ""+verif.length());
                                    readBufferPosition = 0;
                                    String data = "";
                                    /*
                                    for(int j=0; j<verif.length(); j++){
                                        data+= verif.charAt(j);
                                        Log.d("data","char at "+j +" "+verif.charAt(j));
                                    }*/
                                    verif = verif.substring(0,verif.length()-1);

                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {

                                            Log.d("resp123",verif);
                                            //verif = data;
                                            Log.d("resp321",verif);
                                            if(verif.equals("success")){
                                                try {
                                                    bt.close();
                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                }
                                                // Make an intent to start next activity.

                                                Intent i = new Intent(MainActivity.this, setting.class);
                                                //Intent i = new Intent(MainActivity.this,dmact.class);
                                                //Change the activity.
                                                i.putExtra("EXTRA_ADDRESS", address); //this will be received at ledControl (class) Activity
                                                i.putExtra("ID_ADDRESS", idInput.getText().toString().trim());
                                                i.putExtra("PASS_ADDRESS", passwordInput.getText().toString().trim());
                                                startActivity(i);

                                            }else{
                                                Log.d("wasd", verif);
                                            }
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
