package com.example.weather1;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.ImageView;

import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends AppCompatActivity {
    TextView tv_time;
    TextView tv_temp;
    TextView tv_tmp_interval;
    TextView tv_cond_txt;
    TextView tv_wind;
    TextView tv_hum;

    ImageView iv1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_time = findViewById(R.id.tv_time);
        tv_temp = findViewById(R.id.tv_temp);
        tv_tmp_interval = findViewById(R.id.tv_tmp_interval);
        tv_cond_txt = findViewById(R.id.tv_cond_txt);
        tv_wind = findViewById(R.id.tv_wind);
        tv_hum = findViewById(R.id.tv_hum);
        iv1 = (ImageView)findViewById(R.id.iv1);
        new TimeThread().start();//启动时间线程

        Timer timer = new Timer();
        timer.schedule(timertask, 0, 30*60*1000);//30分钟执行一次timertask.run()
    }
    TimerTask timertask = new TimerTask() {
        @Override
        public void run() {
            requestWeatherData();
        }
    };

    private void requestWeatherData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String request_url = "https://free-api.heweather.net/s6/weather/now?location=daxing&key=ba2990322c274cb3b0834faf56d60df4";
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(request_url).build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseJsonData(responseData);

                    String request_url_forcast = " https://free-api.heweather.net/s6/weather/forecast?location=daxing&key=ba2990322c274cb3b0834faf56d60df4";
                    OkHttpClient client_forcast = new OkHttpClient();
                    Request request_forcast = new Request.Builder().url(request_url_forcast).build();
                    Response response_forcast = client_forcast.newCall(request_forcast).execute();
                    String responseData_forcast = response_forcast.body().string();
                    parseJsonData_forcast(responseData_forcast);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }
    private void parseJsonData(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            Log.d("MainActivity", jsonObject.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            JSONObject resultJsonObject = jsonArray.getJSONObject(0);
            Log.d("MainActivity", resultJsonObject.toString());
            JSONObject nowObject = resultJsonObject.getJSONObject("now");
            //获取con_txt：阴
            String cond_txt = nowObject.getString("cond_txt");
            Log.d("MainActivity", cond_txt);
            //获取wind：东北风一级
            String wind_dir = nowObject.getString("wind_dir");
            String wind_sc = nowObject.getString("wind_sc");
            String wind = wind_dir + wind_sc + "级";
            Log.d("MainActivity", wind);
            //获取temp：23度
            String tmp = nowObject.getString("tmp");
            Log.d("MainActivity", tmp + "℃");
            //获取hum：56
            String hum = "湿度" + nowObject.getString("hum");
            Log.d("MainActivity", hum);
            updateUi(tmp, cond_txt, wind, hum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //解析预测的数据
    private void parseJsonData_forcast(String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            Log.d("MainActivity", jsonObject.toString());
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            JSONObject resultJsonObject = jsonArray.getJSONObject(0);
            Log.d("MainActivity", resultJsonObject.toString());
            JSONArray dailyObject = resultJsonObject.getJSONArray("daily_forecast");
            Log.d("MainActivity", dailyObject.toString());
            JSONObject resultforcastJsonObject = dailyObject.getJSONObject(0);
            Log.d("MainActivity", resultforcastJsonObject.toString());
            String tmp_max = resultforcastJsonObject.getString("tmp_max");
            Log.d("MainActivity", tmp_max);
            String tmp_min = resultforcastJsonObject.getString("tmp_min");
            Log.d("MainActivity", tmp_min);
            String tmp_interval = tmp_min + " ~ " + tmp_max + "℃";
            tv_tmp_interval.setText(tmp_interval);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateUi(final String tmp, final String cond_txt, final String wind, final String hum ) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                /*
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
                Date date = new Date(System.currentTimeMillis());
                tv_time.setText(simpleDateFormat.format(date));
                */
                tv_wind.setText(wind);
                tv_cond_txt.setText(cond_txt);
                tv_temp.setText(tmp);
                tv_hum.setText(hum);

                if(cond_txt.contains("晴")) {
                    iv1.setImageResource(R.drawable.sun);
                }
                else if(cond_txt.contains("雨")) {
                    iv1.setImageResource(R.drawable.rain);
                }
                else if(cond_txt.contains("云")) {
                    iv1.setImageResource(R.drawable.cloud);
                }
                else if(cond_txt.contains("雪")) {
                    iv1.setImageResource(R.drawable.snow);
                }
                else{
                    iv1.setImageResource(R.drawable.others);
                }

            }
        });
    }
    public class TimeThread extends Thread{
        @Override
        public void run() {
            super.run();
            do{
                try {
                    Thread.sleep(1000);
                    Message msg = new Message();
                    msg.what = 1;
                    handler.sendMessage(msg);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }while (true);

        }
    }
    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    tv_time.setText(new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date(System.currentTimeMillis())));
                    break;
            }
            return false;
        }
    });

}

