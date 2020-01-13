package com.example.weather1.gson;

import org.json.JSONArray;
import org.json.JSONObject;
import com.google.gson.Gson;

/**
 * Created by LY on 2019/9/26.
 */

public class utility {
    public static forecastBean transresponseForecast(String response)
    {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent =jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,forecastBean.class);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
    public static lifestyleBean transresponseLifestyle(String response)
    {
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONArray jsonArray = jsonObject.getJSONArray("HeWeather6");
            String weatherContent =jsonArray.getJSONObject(0).toString();
            return new Gson().fromJson(weatherContent,lifestyleBean.class);
        }catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

}
