package com.zz.scandemo;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextClock;
import android.widget.TextView;

import com.ruijie.uhflib.uhf.constant.UhfConstant;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import ruijie.com.uhflib.uhf.InventoryData;
import ruijie.com.uhflib.uhf.Linker;
import ruijie.com.uhflib.uhf.PacketParser;

public class ScanActivity extends Activity implements PacketParser {
    private Button btStartScan,btStopScan;
    private TextView yibaoNumTV,xueliNumTV,shanquanNumTV,totalNumTV;
    private EditText scanTimeTE,stopTimeTE;
    private Integer scanTime=0;
    private Integer stopTime=0;
    private ScanActivity self;
    private HashMap<String,Integer> tagMap = new HashMap<String,Integer>();
//    private HashMap<String,String> libaoMap = new HashMap<String,String>();
//    private HashMap<String,String> shanquanMap = new HashMap<String,String>();
//    private HashMap<String,String> xueliMap = new HashMap<String,String>();
    private Thread autoThread;
    private Linker inventory = new Linker();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);
        self = this;
        String ttyStr  = "/dev/ttymxc1";
        int a = inventory.initInventory(ScanActivity.this,ttyStr, 115200);
        Log.e("rfid","initInventory "+a);

        int setPower = 260;
        boolean a0 = inventory.enableAnt(0,setPower,260);
        boolean a1 = inventory.enableAnt(1,setPower,260);
        boolean a2 = inventory.enableAnt(2,setPower,260);
        boolean a3 = inventory.enableAnt(3,setPower,260);
        inventory.disableAnt(4);
        inventory.disableAnt(5);
        inventory.disableAnt(6);
        inventory.disableAnt(7);
        yibaoNumTV = findViewById(R.id.yibaoNumTV);
        xueliNumTV = findViewById(R.id.xueliNumTV);
        shanquanNumTV = findViewById(R.id.shanquanNumTV);
        totalNumTV = findViewById(R.id.totalNumTV);
        btStartScan = findViewById(R.id.bt_start_scan);
        btStopScan = findViewById(R.id.bt_stop_scan);
        scanTimeTE = findViewById(R.id.scanTimeTE);
        stopTimeTE = findViewById(R.id.stopTimeTE);


        InputStream inputStreamlibao = getResources().openRawResource(R.raw.libao);
        txtStringToMap(inputStreamlibao,tagMap,1);
        InputStream inputStreamxueli = getResources().openRawResource(R.raw.xueli);
        txtStringToMap(inputStreamxueli,tagMap,2);
        InputStream inputStreamshanquan = getResources().openRawResource(R.raw.shanquan);
        txtStringToMap(inputStreamshanquan,tagMap,3);
        Log.e("rfid",tagMap.get("E28011606000020A936B3D10")+"|"+tagMap.get("E28011606000020A936B3D10")*10);
        Log.e("rfid",tagMap.get("E28011606aaaa")+"");
        for (String in : tagMap.keySet()) {
            //map.keySet()返回的是所有key的值
            Integer val = tagMap.get(in);
            //tagMap.put(in,100);

            Log.e("rfid",in+" "+tagMap.get(in)+" "+(tagMap.get(in).intValue() == 2));
        }


        btStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanTime = Integer.parseInt(scanTimeTE.getText().toString());
                stopTime = Integer.parseInt(stopTimeTE.getText().toString());
                if(scanTime==0 && stopTime==0){
                    Log.e("rfid","click startOne");
                    startOne();
                }else{
                    autoThread = new Thread(autoCheckThread);
                    autoThread.start();
                }


            }
        });
        btStopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanTime = Integer.parseInt(scanTimeTE.getText().toString());
                stopTime = Integer.parseInt(stopTimeTE.getText().toString());
                if(scanTime==0 && stopTime==0){
                    Log.e("rfid","click stopOne");
                    stopOne();
                }else{
                    autoThread.interrupt();
                    stopOne();

                }


            }

        });
    }

    private void startOne(){
        yibaoNumTV.setText("0");
        xueliNumTV.setText("0");
        shanquanNumTV.setText("0");
        totalNumTV.setText("0");
        yibaoNum = 0;
        xueliNum = 0;
        shanquanNum = 0;
        totalNum = 0;
//        tagMap.clear();
//        InputStream inputStreamlibao = getResources().openRawResource(R.raw.libao);
//        txtStringToMap(inputStreamlibao,tagMap,1);
//        InputStream inputStreamxueli = getResources().openRawResource(R.raw.xueli);
//        txtStringToMap(inputStreamxueli,tagMap,2);
//        InputStream inputStreamshanquan = getResources().openRawResource(R.raw.shanquan);
//        txtStringToMap(inputStreamshanquan,tagMap,3);

        for (String epc : tagMap.keySet()) {

            Integer val = tagMap.get(epc);//得到每个key多对用value的值

            if(val.intValue() == 10){
                tagMap.put(epc,1);
            }else if(val.intValue() == 20){
                tagMap.put(epc,2);
            }else if(val.intValue() == 30){
                tagMap.put(epc,3);
            }else{

            }
        }
        Thread t=new Thread(){
            @Override
            public void run(){
                inventory.startInventory();

            }
        };
        t.start();
    }
    private void stopOne(){
        inventory.stopInventory();
        totalNumTV.setText(totalNum+"（盘点已停止）");

    }
    private Handler autoCheckhandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    Log.e("rfid","auto startOne");
                    startOne();
                    break;
                case 2:
                    Log.e("rfid","auto stopOne");
                    stopOne();
                    break;
                case 3:
                    totalNumTV.setText(totalNum+"");
                    yibaoNumTV.setText(yibaoNum+"");
                    xueliNumTV.setText(xueliNum+"");
                    shanquanNumTV.setText(shanquanNum+"");
                    break;
                default:
                    break;


            }

        }
    };
    Thread autoCheckThread = new Thread(){
        @Override
        public void run(){
            try {

                while (true){
                    Message message1 = autoCheckhandler.obtainMessage();
                    message1.what = 1;
                    autoCheckhandler.sendMessage(message1);
                    Thread.sleep(scanTime*1000);


                    Message message2 = autoCheckhandler.obtainMessage();
                    message2.what = 2;
                    autoCheckhandler.sendMessage(message2);
                    Thread.sleep(stopTime*1000);

                }
            }catch (Exception e){
                Log.e("rfid","autoCheckThread e "+e);

            }
        }
    };

    private int yibaoNum = 0;
    private int xueliNum = 0;
    private int shanquanNum = 0;
    private int totalNum = 0;
    @Override
    public void on18k6cAntennaCyCleEnd(InventoryData[] inventoryData) {

            Log.e(UhfConstant.TAG, "扫描到标签" + inventoryData.length + "个"+" ..."+inventoryData[0].getEpc());

            for(InventoryData data : inventoryData){
                if(tagMap.get(data.getEpc())==null){
                    //Integer newVal = tagMap.get(data.getEpc()).intValue()*10;
                    //tagMap.put(data.getEpc(),newVal);
                    tagMap.put(data.getEpc(),0);
                    totalNum++;

                }else{
                    //tagMap.put(data.getEpc(),30);
                    Integer val = tagMap.get(data.getEpc()).intValue();
                    if(val.intValue() == 1){
                        tagMap.put(data.getEpc(),10);
                        yibaoNum++;
                        totalNum++;
                    }else if(val.intValue() == 2){
                        tagMap.put(data.getEpc(),20);
                        xueliNum++;
                        totalNum++;
                    }else if(val.intValue() == 3){
                        tagMap.put(data.getEpc(),30);
                        shanquanNum++;
                        totalNum++;
                    }

                }
//                if("E28011606000020A936B3D10".equals(data.getEpc())){
//                    tagMap.put("E28011606000020A936B3D10",30);
//                }

            }

        Message message3 = autoCheckhandler.obtainMessage();
        message3.what = 3;
        autoCheckhandler.sendMessage(message3);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                    totalNumTV.setText(totalNum);
//                    yibaoNumTV.setText(yibaoNum);
//                    xueliNumTV.setText(xueliNum);
//                    shanquanNumTV.setText(shanquanNum);
//
//                }
//            });


    }

    @Override
    public void on18k6cAntennaBegin(int antNumber) {
        Log.d("on18kAntennaCyCleBegin","antNumber"+antNumber);
    }

    @Override
    public void on18K16cInverntoyAbort(int status) {
        Log.e("on18K16cInverntoyAbort","status="+status);
    }

    @Override
    public void on18K16cInverntoyEndAbort(int status,String[] a,int[] b,String[] c) {
        Log.e("EndAbort","status="+status);
    }

    public static void txtStringToMap(InputStream inputStream,HashMap<String,Integer> map,Integer tag) {
        InputStreamReader inputStreamReader = null;
        try {
            inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(inputStreamReader);
        //StringBuffer sb = new StringBuffer("");
        String line;
        try {
            while ((line = reader.readLine()) != null) {
//                sb.append(line);
//                sb.append("\n");
                map.put(line.trim(),tag);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //return sb.toString();
    }

}
