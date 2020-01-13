package com.example.weather1;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.weather1.app.MyApplication;
import com.example.weather1.bean.City;
import com.example.weather1.db.CityDB;
import com.example.weather1.db.FilterListener;
import com.example.weather1.db.MyAdapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class SelectCity extends AppCompatActivity{
    private static final String TAG = "SelectCity";
    private ImageView mBackBtn;
    private ListView citylistview;
    private CityDB mCityDB;
    private List<City> mCityList;
    private TextView title_name;

    private EditText searchEdit;
    private MyAdapter adapter = null;
    private List<String> dataList =new ArrayList<>();

    private String getcity;

    private String selected_city;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seclect_city);
        Log.d(TAG,"onCreate()");

        title_name=(TextView)findViewById(R.id.title_name);
        SharedPreferences pref=getSharedPreferences("CityActivity",MODE_PRIVATE);
        title_name.setText(getString(R.string.title_name,pref.getString("city","XX")));
        mBackBtn =(ImageView)findViewById(R.id.title_back);
        mBackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              finish();
            }
        });

        mCityDB=openCityDB();
        initCitylistview();
    }


    private CityDB openCityDB() {
        String path = "/data"
                + Environment.getDataDirectory().getAbsolutePath
                ()
                + File.separator + getPackageName()
                + File.separator + "databases1"
                + File.separator
                + CityDB.CITY_DB_NAME;
        File db = new File(path);
        Log.d(TAG,path);
        if (!db.exists()) {
            String pathfolder = "/data"
                    + Environment.getDataDirectory().getAbsolutePath()
                    + File.separator + getPackageName()
                    + File.separator + "databases1"
                    + File.separator;
            File dirFirstFolder = new File(pathfolder);
            if(!dirFirstFolder.exists()){
                dirFirstFolder.mkdirs();
                Log.i("MyApp","mkdirs");
            }
            Log.i("MyApp","db is not exists");
            try {
                InputStream is = getAssets().open("city.db");
                FileOutputStream fos = new FileOutputStream(db);
                int len = -1;
                byte[] buffer = new byte[1024];
                while ((len = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                    fos.flush();
                }
                fos.close();
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(0);
            }
        }
        return new CityDB(this, path);
    }
    private boolean prepareCityList() {
        mCityList = mCityDB.getAllCity();
        int i=0;
        for (City city : mCityList) {
            i++;
            String cityName = city.getCity();
            String cityCode = city.getNumber();
            Log.d(TAG,cityCode+":"+cityName);
            dataList.add(city.getCity());
        }
        Log.d(TAG,"i="+i);
        return true;
    }

    public void initCitylistview() {
        citylistview = (ListView) findViewById(R.id.cityListview);
        prepareCityList();
        Log.d(TAG, "initCityList: "+mCityList.toString());
//
        searchEdit = (EditText) findViewById(R.id.search);
        setData();// 给listView设置adapter
        setListeners();// 设置监听

//        CityAdapter cityAdapter=new CityAdapter(
//                SelectCity.this,R.layout.city_item,mCityList);
//        citylistview.setAdapter(cityAdapter);
//
//        citylistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                City city = mCityList.get(position);
//                getcity=city.getCity();
//                Log.d(TAG, "onItemClick: selectcity"+getcity);
//                SharedPreferences pref=getSharedPreferences("CityActivity",MODE_PRIVATE);
//                title_name.setText(getString(R.string.title_name,pref.getString("city","XX")));
//                SharedPreferences.Editor editor=getSharedPreferences("CityActivity",MODE_PRIVATE).edit();
//                editor.putString("city",getcity);
//                editor.putString("number",city.getNumber());
//                editor.apply();
//                finish();
////                Intent i = new Intent();
////                i.putExtra("cityCode", getcity);
////                setResult(RESULT_OK, i);
//  //              finish();
//            }
//        });
    }
    private void setData() {
        // 这里创建adapter的时候，构造方法参数传了一个接口对象，这很关键，回调接口中的方法来实现对过滤后的数据的获取
        adapter = new MyAdapter(dataList, this, new FilterListener() {
            // 回调方法获取过滤后的数据
            public void getFilterData(List<String> list) {
                // 这里可以拿到过滤后数据，所以在这里可以对搜索后的数据进行操作
                Log.e("TAG", "接口回调成功");
                Log.e("TAG", list.toString());
                setItemClick(list);
            }
        });
        citylistview.setAdapter(adapter);
    }

    /**
     * 给listView添加item的单击事件
     * @param filter_lists  过滤后的数据集
     */
    protected void setItemClick(final List<String> filter_lists) {
        citylistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // 点击对应的item时，弹出toast提示所点击的内容
                getcity=filter_lists.get(position);

                Log.d(TAG, "onItemClick: selectcity"+getcity);
                SharedPreferences pref=getSharedPreferences("CityActivity",MODE_PRIVATE);
                title_name.setText(getString(R.string.title_name,pref.getString("city","XX")));
                SharedPreferences.Editor editor=getSharedPreferences("CityActivity",MODE_PRIVATE).edit();
                editor.putString("city",getcity);
               // editor.putString("number",city.getNumber());
                editor.apply();
                finish();
//                Intent i = new Intent();
//                i.putExtra("cityCode", selected_city);
//                setResult(RESULT_OK, i);
//                finish();
            }
        });
    }


    private void setListeners() {
        // 没有进行搜索的时候，也要添加对listView的item单击监听
        setItemClick(dataList);
        /**
         * 对编辑框添加文本改变监听，搜索的具体功能在这里实现
         * 很简单，文本该变的时候进行搜索。关键方法是重写的onTextChanged（）方法。
         */
        searchEdit.addTextChangedListener(new TextWatcher() {
            /**
             *
             * 编辑框内容改变的时候会执行该方法
             */
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                // 如果adapter不为空的话就根据编辑框中的内容来过滤数据
                if(adapter != null){
                    adapter.getFilter().filter(s);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }
        });
    }
}
