package com.example.weather1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.weather1.gson.forecastBean;
import com.example.weather1.gson.lifestyleBean;
import com.example.weather1.util.NetUtil;
import com.google.gson.Gson;
import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.weather1.gson.utility.transresponseForecast;
import static com.example.weather1.gson.utility.transresponseLifestyle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    public SwipeRefreshLayout swipeRefresh;
    private String mWeatherId;
    private static final String TAG = "MainActivity";
    TextView tv_time;
    TextView tv_temp;
    TextView tv_tmp_interval;
    TextView tv_cond_txt;
    TextView tv_wind;
    TextView tv_hum;
    Button mCitySelect;
    ImageView iv1;
    TextView city_name;
    TextView comfort;
    TextView uv;
    TextView sport;
    LinearLayout forecastLayout;


    String cur_city="北京";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.colorPrimary);

        SharedPreferences pref=getSharedPreferences("CityActivity",MODE_PRIVATE);
        cur_city=pref.getString("city","北京");///默认北京

        comfort = findViewById(R.id.comfort_text);
        uv = findViewById(R.id.uv_text);
        sport = findViewById(R.id.sport_text);

//        tv_time = findViewById(R.id.tv_time);
        tv_temp = findViewById(R.id.tv_temp);
//        tv_tmp_interval = findViewById(R.id.tv_tmp_interval);
        tv_cond_txt = findViewById(R.id.tv_cond_txt);
         tv_wind = findViewById(R.id.tv_wind);
        tv_hum = findViewById(R.id.tv_hum);
        iv1 = (ImageView)findViewById(R.id.iv1);
        city_name=(TextView)findViewById(R.id.city_name);
        city_name.setText(cur_city);

        mCitySelect = (Button) findViewById(R.id.city_select);
        mCitySelect.setOnClickListener(this);
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeatherData();
                Toast.makeText(MainActivity.this, "刷新成功！", Toast.LENGTH_LONG).show();
            }
        });

        //requestWeatherData();
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

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences pref=getSharedPreferences("CityActivity",MODE_PRIVATE);
        cur_city = getString(R.string.title_name,pref.getString("city","XX")).replace("当前城市：","");
        requestWeatherData();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.city_select){
            Intent i = new Intent(this, SelectCity.class);
            startActivity(i);
//            Intent i= new Intent(this,SelectCity.class);
//            i.putExtra("curcity",cur_city);
//            startActivityForResult(i,1);////requestcode为1
        }
    }



    private Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    updateforecastUi((forecastBean)msg.obj);
                    break;
                case 2:
                    updatelifesyleUi((lifestyleBean) msg.obj);
                    break;
                default:
                    break;
            }
        }
    };
    private void requestWeatherData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    String request_url1 = "https://free-api.heweather.net/s6/weather/now?location=";
                    String key = "&key=ba2990322c274cb3b0834faf56d60df4";
                    //SharedPreferences pref=getSharedPreferences("CityActivity",MODE_PRIVATE);
                    //cur_city = getString(R.string.title_name,pref.getString("city","XX")).replace("当前城市：","");
                    //String cityName="北京";
                    String request_url = request_url1+cur_city+key;
//                    String request_url = "https://free-api.heweather.net/s6/weather/now?location=daxing&key=ba2990322c274cb3b0834faf56d60df4";
                    Log.d(TAG,request_url);
                    OkHttpClient client = new OkHttpClient();
                    Request request = new Request.Builder().url(request_url).build();
                    Response response = client.newCall(request).execute();
                    String responseData = response.body().string();
                    parseJsonData(responseData);


                    String request_url1_forcast = " https://free-api.heweather.net/s6/weather/forecast?location=";
                    String request_url_forcast = request_url1_forcast+cur_city+key;

                    //String request_url_forcast = " https://free-api.heweather.net/s6/weather/forecast?location=daxing&key=ba2990322c274cb3b0834faf56d60df4";
                    OkHttpClient client_forcast = new OkHttpClient();
                    Request request_forcast = new Request.Builder().url(request_url_forcast).build();
                    Response response_forcast = client_forcast.newCall(request_forcast).execute();
                    String responseData_forcast = response_forcast.body().string();

                    forecastBean weatherforecast=transresponseForecast(responseData_forcast);
                    if(weatherforecast!=null)//////用消息机制在主线程中更新UI
                    {
                        Message msg =new Message();
                        msg.what = 1;
                        msg.obj=weatherforecast;
                        mHandler.sendMessage(msg);
                    }

                    String request_url1_lifestyle = " https://free-api.heweather.net/s6/weather/lifestyle?location=";
                    String request_url_lifestyle = request_url1_lifestyle+cur_city+key;
                    //String request_url_forcast = " https://free-api.heweather.net/s6/weather/forecast?location=daxing&key=ba2990322c274cb3b0834faf56d60df4";
                    OkHttpClient client_lifestyle = new OkHttpClient();
                    Request request_lifestyle = new Request.Builder().url(request_url_lifestyle).build();
                    Response response_lifestyle = client_lifestyle.newCall(request_lifestyle).execute();
                    String responseData_lifestyle = response_lifestyle.body().string();
                    lifestyleBean weatherlifestyle=transresponseLifestyle(responseData_lifestyle);
                    if(weatherlifestyle!=null)//////用消息机制在主线程中更新UI
                    {
                        Message msg =new Message();
                        msg.what = 2;
                        msg.obj=weatherlifestyle;
                        mHandler.sendMessage(msg);
                    }
                    swipeRefresh.setRefreshing(false);
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
    private void updatelifesyleUi(lifestyleBean weatherlifestyle ){
        List<lifestyleBean.LifestyleBean> lifestyle = weatherlifestyle.getLifestyle();
        String comforttxt = lifestyle.get(0).getTxt();
        String sporttxt = lifestyle.get(3).getTxt();
        String uvtxt = lifestyle.get(5).getTxt();
        comfort.setText(comforttxt);
        sport.setText(sporttxt);
        uv.setText(uvtxt);
    }
    //预测的数据
    private void updateforecastUi(forecastBean weatherforecast ){
//        forecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
//        List<forecastBean.DailyForecastBean> mylist=weatherforecast.getDaily_forecast();
//        forecastLayout.removeAllViews();
//        for (forecastBean.DailyForecastBean forecast : mylist) {
//            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, forecastLayout, false);
//            TextView dateText = (TextView) view.findViewById(R.id.date_text);
//            TextView infoText = (TextView) view.findViewById(R.id.info_text);
//            TextView maxText = (TextView) view.findViewById(R.id.max_text);
//            TextView minText = (TextView) view.findViewById(R.id.min_text);
//            dateText.setText(forecast.getDate());
//            infoText.setText(forecast.getCond_txt_d());
//            maxText.setText(forecast.getTmp_max());
//            minText.setText(forecast.getTmp_min());
//            forecastLayout.addView(view);
//        }
//        Log.d("预测数据","已更新");

        List<forecastBean.DailyForecastBean> mylist=weatherforecast.getDaily_forecast();
        TextView dateText = (TextView)findViewById(R.id.date_text);
        TextView infoText = (TextView) findViewById(R.id.info_text);
        TextView maxText = (TextView) findViewById(R.id.max_text);
        TextView minText = (TextView) findViewById(R.id.min_text);
        dateText.setText(mylist.get(0).getDate());
        infoText.setText(mylist.get(0).getCond_txt_d());
        maxText.setText(mylist.get(0).getTmp_max());
        minText.setText(mylist.get(0).getTmp_min());
        TextView dateText2 = (TextView)findViewById(R.id.date_text2);
        TextView infoText2 = (TextView) findViewById(R.id.info_text2);
        TextView maxText2 = (TextView) findViewById(R.id.max_text2);
        TextView minText2 = (TextView) findViewById(R.id.min_text2);
        dateText2.setText(mylist.get(1).getDate());
        infoText2.setText(mylist.get(1).getCond_txt_d());
        maxText2.setText(mylist.get(1).getTmp_max());
        minText2.setText(mylist.get(1).getTmp_min());
        TextView dateText3 = (TextView)findViewById(R.id.date_text3);
        TextView infoText3 = (TextView) findViewById(R.id.info_text3);
        TextView maxText3 = (TextView) findViewById(R.id.max_text3);
        TextView minText3 = (TextView) findViewById(R.id.min_text3);
        dateText3.setText(mylist.get(2).getDate());
        infoText3.setText(mylist.get(2).getCond_txt_d());
        maxText3.setText(mylist.get(2).getTmp_max());
        minText3.setText(mylist.get(2).getTmp_min());
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
                tv_temp.setText(tmp+"℃");
                tv_hum.setText(hum+"%");

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

                city_name.setText(cur_city);
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
                    //tv_time.setText(new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date(System.currentTimeMillis())));
                    break;
            }
            return false;
        }
    });

}

