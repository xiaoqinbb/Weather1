package com.example.weather1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.weather1.bean.City;

import java.util.List;

public class CityAdapter extends ArrayAdapter<City> {

    private int resID;

    public CityAdapter(Context context, int textViewID, List<City> obj){
        super(context,textViewID,obj);
        resID=textViewID;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        City cityBean=getItem(position);
        View view;
        if(convertView==null){
            view= LayoutInflater.from(getContext()).inflate(resID,parent,false);
        }else {
            view = convertView;
        }
        TextView cityName=(TextView)view.findViewById(R.id.city_item);
//        ImageView xqbb=(ImageView)view.findViewById(R.id.title_back);
        cityName.setText(cityBean.getCity());
//        xqbb.setImageResource(R.drawable.ic_home);
        return view;
    }
}
