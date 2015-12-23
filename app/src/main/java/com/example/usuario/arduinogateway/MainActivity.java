package com.example.usuario.arduinogateway;

import android.os.Environment;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import br.ufc.great.loccamlib.LoccamListener;
import br.ufc.great.loccamlib.LoccamManager;
import br.ufc.great.syssu.base3.Provider;
import br.ufc.great.syssu.base.Tuple;
import br.ufc.great.syssu.base.interfaces.IClientReaction;
import br.ufc.great.syssu.base.interfaces.ISysSUService;

public class MainActivity extends AppCompatActivity implements LoccamListener {

    private LoccamManager loccam;

    private final static String CONTEXT_KEY1 = "context.ambient.temperature";
    private final static String CONTEXT_KEY2 = "context.ambient.luminosity";

    private TextView tvArduinoValue;

    private double value;

    private SyssuManager mSyssu;

    private int messageCount = 0;

    private final static String MYID = "arduinoGateway";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            createFileOnDevice(false);
        } catch (IOException e) {
            e.printStackTrace();
        }

        mSyssu = SyssuManager.getInstance(this);
        mSyssu.start();

        tvArduinoValue = (TextView) findViewById(R.id.tv_arduino_value);

        loccam = new LoccamManager(this, "ArduinoGateway");
        loccam.connect(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        loccam.disconnect();
    }

    @Override
    public void onServiceConnected(ISysSUService iSysSUService) {

        loccam.init(CONTEXT_KEY1);
        loccam.init(CONTEXT_KEY2);

        IClientReaction reaction1 = new IClientReaction.Stub(){

            @Override
            public void react(Tuple tuple) throws RemoteException {

                value = -1;

                try {
                    String dataS = tuple.getField(2).getValue().toString();
                    JSONObject data = new JSONObject(dataS.substring(1, dataS.length() - 1));
                    value = Double.valueOf(data.getString("Data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                br.ufc.great.syssu.base3.Tuple t = new br.ufc.great.syssu.base3.Tuple();
                t.addField("id", MYID);
                t.addField("temperature", "" + value);
                t.addField("messageCount", "" + messageCount);

                log("id=" + MYID + ",temperature=" + value + ",timestamp=" + System.currentTimeMillis() + ",messagecount=" + messageCount);

                mSyssu.put(t, Provider.ADHOC);
                messageCount++;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvArduinoValue.setText("Arduino Value: " + value);
                    }
                });
            }
        };

        loccam.getASync(reaction1, CONTEXT_KEY1);

        IClientReaction reaction2 = new IClientReaction.Stub(){

            @Override
            public void react(Tuple tuple) throws RemoteException {

                value = -1;

                try {
                    String dataS = tuple.getField(2).getValue().toString();
                    JSONObject data = new JSONObject(dataS.substring(1, dataS.length() - 1));
                    value = Double.valueOf(data.getString("Data"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                br.ufc.great.syssu.base3.Tuple t = new br.ufc.great.syssu.base3.Tuple();
                t.addField("id", MYID);
                t.addField("luminosity", "" + value);
                t.addField("messageCount", "" + messageCount);

                log("id=" + MYID + ",luminosity="+value + ",timestamp=" + System.currentTimeMillis() + ",messagecount=" + messageCount);
                mSyssu.put(t, Provider.ADHOC);
                messageCount++;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvArduinoValue.setText("Arduino Value: " + value);
                    }
                });
            }
        };
        loccam.getASync(reaction2, CONTEXT_KEY2);
    }

    private void log(final String s) {

                writeToFile(s);

    }

    @Override
    public void onServiceDisconnected() {

    }

    @Override
    public void onLoccamException(Exception e) {

    }

    public static BufferedWriter out;

    private void createFileOnDevice(Boolean append) throws IOException {
                /*
                 * Function to initially create the log file and it also writes the time of creation to file.
                 */
        File Root = Environment.getExternalStorageDirectory();
        if(Root.canWrite()){
            File  LogFile = new File(Root, "Log.txt");
            FileWriter LogWriter = new FileWriter(LogFile, append);
            out = new BufferedWriter(LogWriter);
            Date date = new Date();
            out.write("Logged at" + String.valueOf(date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds() + "\n"));
            out.flush();

        }
    }

    public void writeToFile(String message) {
        try {
            out.write(message + "\n");
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
