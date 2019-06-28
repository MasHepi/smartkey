package com.example.happy.bluetooth;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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

public class deleting extends AppCompatActivity {
    ListView l;
    gigibiru gigi;
    BluetoothSocket bt;
    EditText id,pass;
    Button btngo;
    String idpass,MSG;
    BluetoothSocket btsock;

    InputStream mmInputStream;
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;

    private boolean isBtConnected = false;
    private boolean flagLoginSuccess = false;

    OutputStream output = null;
    private ProgressDialog progress;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete);

        l = findViewById(R.id.listuser);
        id = findViewById(R.id.id);
        pass = findViewById(R.id.pass);
        btngo = findViewById(R.id.tombol1);
        gigi = new gigibiru();

        //verifikasi
        btngo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                idpass = id.getText().toString()+"+"+pass.getText().toString();
                bck2 bck = new bck2(deleting.this,gigi.getBT(),idpass);
                bck.getmain(new bck.tomain1() {
                    @Override
                    public void tomain2(BluetoothSocket bt) {
                        try {
                            btsock = bt;
                            mmInputStream = bt.getInputStream();
                            beginListenForData(bt);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
                bck.execute();

            }
        });
    }

    void setup(String[] procDataFromBT2){
        ArrayAdapter adapter = new ArrayAdapter(this,android.R.layout.simple_list_item_1, procDataFromBT2);
        l.setAdapter(adapter);
        l.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String info = ((TextView) view).getText().toString();

                DialogInterface.OnClickListener wasd = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Toast.makeText(deleting.this,"yes "+info.substring(1,info.length()),Toast.LENGTH_SHORT).show();
                                try {
                                    btsock.getOutputStream().write(("delete;"+idpass+","+info+".\n").getBytes());
                                    btsock.close();
                                    finish();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case DialogInterface.BUTTON_NEGATIVE:
                                //Toast.makeText(deleting.this,"no "+info,Toast.LENGTH_SHORT).show();
                                break;
                        }

                    }
                };
                AlertDialog.Builder b = new AlertDialog.Builder(view.getContext());
                b.setMessage("Are you sure").setPositiveButton("yes",wasd).setNegativeButton("no",wasd).show();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            btsock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void beginListenForData(BluetoothSocket bt)
    {
        try {
            mmInputStream = bt.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
                                    //final String data = new String(encodedBytes, "US-ASCII");
                                    MSG = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    MSG = MSG.substring(0,MSG.length()-2);
                                    final String[] wasd = MSG.split(",");
                                    Log.d("resp123",MSG);
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            setup(wasd);
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
