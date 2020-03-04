package com.example.bledtsender;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ikovac.timepickerwithseconds.MyTimePickerDialog;
import com.ikovac.timepickerwithseconds.TimePicker;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    Button On,Off,List,Listen,btnSend;
    BluetoothAdapter bluetoothAdapter;
    ListView Pdevices;
    TextView Status,MessageRe,DtTm;
    EditText MsgSend,TimeSend,AM_PM;
    int mYear, mDay, mMonth;
    ImageView GetCalendar,GetTime;
    String mm_precede = "", hr_precede="", sec_precede="";
    BluetoothDevice[] btArray;
    String time;

    Handler someHandler;
    SendReceive sendReceive;

    static final int STATE_LISTENING=1;
    static final int STATE_CONNECTING=2;
    static final int STATE_CONNECTED=3;
    static final int STATE_CONNECTION_FAILED=4;
    static final int STATE_MESSAGE_RECEIVED=5;
    private static final String APP_NAME="BTDTS";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    //51bc2b24-5890-11ea-8e2d-0242ac130003
    // 00001101-0000-1000-8000-00805F9B34FB
    String AMPM;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        On = findViewById(R.id.ON);
        Off = findViewById(R.id.OFF);
        Listen = findViewById(R.id.Listen);
        btnSend = findViewById(R.id.Send);
        List = findViewById(R.id.List);
        Pdevices = findViewById(R.id.PDList);
        Status = findViewById(R.id.Status);
        MessageRe = findViewById(R.id.MessageRe);
        MsgSend = findViewById(R.id.MsgSend);
        GetCalendar = findViewById(R.id.GetCalendar);
        GetTime = findViewById(R.id.GetTime);
        TimeSend = findViewById(R.id.Time);
        AM_PM = findViewById(R.id.AM_PM);
        DtTm = findViewById(R.id.DtTm);

        MsgSend.setEnabled(false);
        TimeSend.setEnabled(false);
        AM_PM.setEnabled(false);
        final String curr_date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(new Date());
        MsgSend.setText(curr_date);

        someHandler = new Handler(getMainLooper());
        someHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Calendar calendar = Calendar.getInstance();
                int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                int mMinute = calendar.get(Calendar.MINUTE);
                int mSeconds = calendar.get(Calendar.SECOND);
                AMPM = " AM";
                if (mHour >= 12) {
                    AMPM = " PM";
                    if (mHour >= 13 && mHour < 24) {
                        mHour -= 12;
                    }
                    else {
                        mHour = 12;
                    }
                } else if (mHour == 0) {
                    mHour = 12;
                }
                if (mMinute < 10) {
                    mm_precede = "0";
                }
                if (mHour < 10){
                    hr_precede = "0";
                }
                if (mSeconds < 10){
                    sec_precede = "0";
                } else {
                    sec_precede = "";
                }
                String Time = hr_precede + mHour + ":" + mm_precede + mMinute + ":" + sec_precede + mSeconds;
//                TimeSend.setText(Time);
//                AM_PM.setText(AMPM);
//                DtTm.setText(curr_date + " " + Time + AMPM);
                someHandler.postDelayed(this,1000);
            }
        },10);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()){
            Intent TurnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(TurnOn,0);
            Toast.makeText(MainActivity.this, "Turned On ", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(MainActivity.this, "Already ON", Toast.LENGTH_SHORT).show();
        }
        On.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!bluetoothAdapter.isEnabled()){
                    Intent TurnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(TurnOn,0);
                    Toast.makeText(MainActivity.this, "Turned On ", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(MainActivity.this, "Already ON", Toast.LENGTH_SHORT).show();
                }
            }
        });

        Off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothAdapter.disable();
                Toast.makeText(MainActivity.this, "Turned Off", Toast.LENGTH_SHORT).show();
            }
        });

        List.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index = 0;

                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {
                        btArray[index] = device;
                        strings[index] = device.getName();
                        index++;
                    }
                    ArrayAdapter arrayAdapter = new ArrayAdapter(MainActivity.this, android.R.layout.simple_list_item_1, strings);
                    Pdevices.setAdapter(arrayAdapter);
                }
            }
        });

        Pdevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClientClass clientClass = new ClientClass(btArray[position]);
                clientClass.start();
                Status.setText("Connecting");
            }
        });

        Listen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ServerClass serverClass = new ServerClass();
                serverClass.start();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Handler handler = new Handler(getMainLooper());
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String string = String.valueOf(DtTm.getText());
                        sendReceive.write(string.getBytes());
                        handler.postDelayed(this,500);
                    }
                },10);
            }
        });

        GetCalendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog;
                datePickerDialog = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String mMonth,mYear,mDay;
                        if(String.valueOf(month).length() == 1){
                            mMonth = "0" + (month + 1);
                        }else {
                            mMonth=String.valueOf(month + 1);
                        }
                        if(String.valueOf(dayOfMonth).length() ==1){
                            mDay="0"+dayOfMonth;
                        }else {
                            mDay= String.valueOf(dayOfMonth);
                        }
                        if(String.valueOf(year).length()==1){
                            mYear="0"+year;
                        }else {
                            mYear=String.valueOf(year);
                        }
                        MsgSend.setText(mMonth + "/" + mDay + "/" + mYear);
                        DtTm.setText(mMonth + "/" + mDay + "/" + mYear + " " + TimeSend.getText().toString() + AM_PM.getText().toString());
                    }
                },mYear,mMonth,mDay);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                datePickerDialog.show();
            }
        });

        GetTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int mHour = calendar.get(Calendar.HOUR_OF_DAY);
                int mMinute = calendar.get(Calendar.MINUTE);
                int mSeconds = calendar.get(Calendar.SECOND);

                MyTimePickerDialog myTimePickerDialog = new MyTimePickerDialog(MainActivity.this, new MyTimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute, int seconds) {
                        AMPM = " AM";
                        if (hourOfDay >= 12){
                            AMPM = " PM";
                            if (hourOfDay >= 13 && hourOfDay < 24){
                                hourOfDay -= 12;
                            }
                            else {
                                hourOfDay = 12;
                            }
                        }
                        else if (hourOfDay == 0){
                            hourOfDay = 12;
                        }
                        time = String.format("%02d", hourOfDay) +
                                ":" + String.format("%02d", minute) +
                                ":" + String.format("%02d", seconds);
                        TimeSend.setText(time);
                        AM_PM.setText(AMPM);
                        DtTm.setText(MsgSend.getText().toString() + " " + time + AMPM);
                    }
                },mHour,mMinute,mSeconds,false);
                myTimePickerDialog.show();
            }
        });
    }

    Handler handler=new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {

            switch (msg.what){
                case STATE_LISTENING:
                    Status.setText("Listening");
                    break;
                case STATE_CONNECTING:
                    Status.setText("Connecting");
                    break;
                case STATE_CONNECTED:
                    Status.setText("Connected");
                    break;
                case STATE_CONNECTION_FAILED:
                    Status.setText("Connection Failed");
                    break;
                case STATE_MESSAGE_RECEIVED:
                    byte[] readBuffer =(byte[]) msg.obj;
                    String tempMsg=new String(readBuffer,0,msg.arg1);
                    MessageRe.setText(tempMsg);

                    break;
            }
            return true;
        }
    });

    private class ServerClass extends Thread{
        private BluetoothServerSocket serverSocket;

        public ServerClass (){
            try {
                serverSocket=bluetoothAdapter.listenUsingRfcommWithServiceRecord(APP_NAME,MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            BluetoothSocket socket=null;

            while (socket==null){
                try {
                    Message message = android.os.Message.obtain();
                    message.what=STATE_CONNECTING;
                    handler.sendMessage(message);
                    socket=serverSocket.accept();
                } catch (IOException e) {
                    Message message = android.os.Message.obtain();
                    message.what=STATE_CONNECTION_FAILED;
                    handler.sendMessage(message);
                    e.printStackTrace();
                }
                if (socket!=null){
                    Message message = android.os.Message.obtain();
                    message.what=STATE_CONNECTED;
                    handler.sendMessage(message);

                    sendReceive=new SendReceive(socket);
                    sendReceive.start();
                    break;
                }
            }
        }
    }

    private class ClientClass extends Thread{
        private BluetoothDevice device;
        private BluetoothSocket socket;

        public ClientClass(BluetoothDevice device1){
            device=device1;

            try {
                socket=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run(){
            try {
                socket.connect();
                Message message= android.os.Message.obtain();
                message.what=STATE_CONNECTED;
                handler.sendMessage(message);

                sendReceive=new SendReceive(socket);
                sendReceive.start();
            } catch (IOException e) {
                Message message= android.os.Message.obtain();
                message.what=STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
                e.printStackTrace();
            }
        }
    }

    private class SendReceive extends Thread{
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public SendReceive(BluetoothSocket socket){
            bluetoothSocket=socket;
            InputStream tempIn=null;
            OutputStream tempOut=null;

            try {
                tempIn=bluetoothSocket.getInputStream();
                tempOut=bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream=tempIn;
            outputStream=tempOut;
        }

        public void run(){
            byte[] buffer =new byte[1024];
            int bytes;

            while (true){
                try {
                    bytes=inputStream.read(buffer);
                    handler.obtainMessage(STATE_MESSAGE_RECEIVED,bytes,-1,buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}