package com.example.happy.bluetooth;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

public class setting extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    EditText edx;
    RadioGroup rg;

    ///
    Button btnDis, btnGanti, btnTambah,btnDel;
    String address = null;
    String id;
    String pass;
    private ProgressDialog progress;
    BluetoothAdapter bluetooth2 = null;
    static BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    TextView text1;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private boolean flagLoginSuccess = false;
    public String bluetoothMessage = "";
    int motiondouble =0;
    int motiondouble1 = 0;
    public InputStream tempIn = null;
    ///
    gigibiru gigi;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //
        Intent newint = getIntent();
        address = newint.getStringExtra("EXTRA_ADDRESS");
        id = newint.getStringExtra("ID_ADDRESS");
        pass = newint.getStringExtra("PASS_ADDRESS");
        //
        gigi = new gigibiru(address);


        new ConnectBT().execute();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        rg = findViewById(R.id.rGgroup);
        for (int i =0; i<rg.getChildCount();i++){
            ((RadioButton) rg.getChildAt(i)).setEnabled(false);
        }

        final RadioButton s30 = findViewById(R.id.s30);
        RadioButton m1 = findViewById(R.id.m1);
        RadioButton m2 = findViewById(R.id.m2);
        RadioButton m5 = findViewById(R.id.m5);

        rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.s30:
                        Toast.makeText(setting.this, "security 30 sec",Toast.LENGTH_SHORT).show();
                        try {
                            btSocket.getOutputStream().write("command;sec,30.".toString().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.m1:
                        Toast.makeText(setting.this, "security 1 min",Toast.LENGTH_SHORT).show();
                        try {
                            btSocket.getOutputStream().write("command;sec,60.".toString().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.m2:
                        Toast.makeText(setting.this, "security 2 min",Toast.LENGTH_SHORT).show();
                        try {
                            btSocket.getOutputStream().write("command;sec,120.".toString().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case R.id.m5:
                        Toast.makeText(setting.this, "security 5 min",Toast.LENGTH_SHORT).show();
                        try {
                            btSocket.getOutputStream().write("command;sec,300.".toString().getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                }
            }
        });




        Switch swt = findViewById(R.id.swit_sec);
        swt.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked == true){
                    Toast.makeText(setting.this, "security on",Toast.LENGTH_SHORT).show();
                    rg.setEnabled(true);
                    for (int i =0; i<rg.getChildCount();i++){
                        ((RadioButton) rg.getChildAt(i)).setEnabled(true);
                    }
                }else {
                    Toast.makeText(setting.this, "security off",Toast.LENGTH_SHORT).show();
                    rg.clearCheck();
                    for (int i =0; i<rg.getChildCount();i++){
                        ((RadioButton) rg.getChildAt(i)).setEnabled(false);
                    }
                }
            }
        });


        ///
        final ToggleButton btn_starter =  (ToggleButton)findViewById(R.id.starter);
        btnDis = (Button)findViewById(R.id.disconnect);
        btn_starter.setTextOff("");
        btn_starter.setTextOn("");


        btnDis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Disconnect();
                //finish();
            }
        });

        btnTambah = (Button)findViewById(R.id.tambah);
        btnTambah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(setting.this, adding.class);
                i.putExtra("EXTRA_ADDRESS", address); //this will be received at ledControl (class) Activity
                i.putExtra("ID_ADDRESS", id);
                i.putExtra("PASS_ADDRESS", pass);
                //Disconnect();
                startActivity(i);
            }
        });

        btnGanti = (Button)findViewById(R.id.ganti);
        btnGanti.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(setting.this, editing.class);
                i.putExtra("EXTRA_ADDRESS", address); //this will be received at ledControl (class) Activity
                i.putExtra("ID_ADDRESS", id);
                i.putExtra("PASS_ADDRESS", pass);
                //Disconnect();
                startActivity(i);
            }
        });

        btnDel = (Button)findViewById(R.id.deletebtn);
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(setting.this, deleting.class);
                startActivity(i);
            }
        });


        btn_starter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                motiondouble++;
                Handler haha = new Handler();
                haha.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (motiondouble==1){
                            if(motiondouble1 == 1){
                                Toast.makeText(setting.this,"kunci buka, mesin idup",Toast.LENGTH_SHORT).show();
                                kunciON_mesinON();
                                btn_starter.setBackgroundResource(R.drawable.powergr);
                                motiondouble1 = 0;
                            }else{
                                Toast.makeText(setting.this,"kunci buka, mesin mati",Toast.LENGTH_SHORT).show();
                                kunciON_mesinOFF();
                                btn_starter.setBackgroundResource(R.drawable.powerred);
                                Log.d("pencet","suksex");
                                motiondouble1=1;
                            }
                        }else if (motiondouble == 2){
                            Toast.makeText(setting.this,"kunci buka, mesin idup",Toast.LENGTH_SHORT).show();
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
                Toast.makeText(setting.this,"kunci tutup, mesin mati",Toast.LENGTH_SHORT).show();
                kunciOFF_mesinOFF();
                btn_starter.setBackgroundResource(R.drawable.power);
                return true;
            }
        });
        ///
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    ////

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
        Toast.makeText(setting.this,"wasd",Toast.LENGTH_SHORT).show();
        if (btSocket!=null)
        {
            try
            {
                Log.d("pencet","sukses");
                btSocket.getOutputStream().write("command;on1.".toString().getBytes());

                //ini itu message dari HP bentuk e bytes

            }
            catch (IOException e)
            {
                e.printStackTrace();
                //Log.d("wasd", "nope");
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
                btSocket.getOutputStream().write("command;on2.".toString().getBytes());
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
                btSocket.getOutputStream().write("command;off.".toString().getBytes());
                //ini itu message dari HP bentuk e bytes

            }
            catch (IOException e)
            {
                Toast.makeText(getApplicationContext(),"ERROR",Toast.LENGTH_LONG).show();
            }
        }
    }
/*
    @Override
    protected void onResume() {
        super.onResume();
        recreate();
    }*/

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
        startActivity(getIntent());
    }

    public class ConnectBT extends AsyncTask<Void, Void, Void>
    {
        private boolean ConnectSuccess = true;
        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(setting.this, "Connecting...", "Please wait");  //show a progress dialog
            //super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (btSocket == null || !isBtConnected)
            {
                try {
                    gigi.connectBT();
                    btSocket = gigi.getBT();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
                        btSocket.getOutputStream().write(("login;" + id + "+" + pass + ".\n").toString().getBytes());

                        threadConnected a = new threadConnected();
                        Thread loop = new Thread(a);
                        loop.start();
                        flagLoginSuccess = true;

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
                        btSocket.getOutputStream().write(("conn\n").toString().getBytes());
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
