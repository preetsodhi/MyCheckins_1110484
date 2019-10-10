package com.example.mycheckins;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter {
    private static ArrayList<CheckinItem> items;
    private LayoutInflater mInflater;

    public ListViewAdapter(Context photosFragment, ArrayList<CheckinItem> results){
        items = results;
        mInflater = LayoutInflater.from(photosFragment);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if(convertView == null){
            convertView = mInflater.inflate(R.layout.list_item_layout, null);
            holder = new ViewHolder();
            holder.txt_title = convertView.findViewById(R.id.list_item_title);
            holder.txt_place = convertView.findViewById(R.id.list_item_place);
            holder.txt_date = convertView.findViewById(R.id.list_item_date);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txt_title.setText(items.get(position).title);
        holder.txt_place.setText(items.get(position).place);
        holder.txt_date.setText(items.get(position).date);

        return convertView;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int arg0) {
        return items.get(arg0);
    }

    @Override
    public long getItemId(int arg0) {
        return arg0;
    }

    static class ViewHolder{
        TextView txt_title, txt_place, txt_date;
    }
}
