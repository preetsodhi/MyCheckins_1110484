package com.example.mycheckins;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;

import static com.example.mycheckins.MainActivity.db;

public class MainFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        // set toolbar
        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar)view.findViewById(R.id.main_toolbar));
        // enable menu
        setHasOptionsMenu(true);

        // retrieve records from db
        Cursor c = db.rawQuery("SELECT * from Receipts", null);
        c.moveToFirst();

        if(c.getCount() > 0) {
            view.findViewById(R.id.no_results_text).setVisibility(View.GONE);

            final ArrayList<CheckinItem> items = new ArrayList<CheckinItem>();

            for (int i = 0; i < c.getCount(); i++, c.moveToNext())
                items.add(new CheckinItem(c.getInt(0), c.getString(1), c.getString(2), c.getString(4)));

            // setup listview
            ((ListView)view.findViewById(R.id.list_view_main)).setAdapter(new ListViewAdapter(getActivity(), items));
            ((ListView)view.findViewById(R.id.list_view_main)).setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    Intent intent = new Intent(getActivity(), DetailsActivity.class);
                    intent.putExtra("mode", "view");
                    intent.putExtra("item_id", items.get(i).id);
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }
            });
        }

        return view;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new:
                Intent i = new Intent(getActivity(), DetailsActivity.class);
                i.putExtra("mode", "new");
                getActivity().startActivity(i);
                getActivity().finish();
                return true;

            case R.id.action_help:
                getActivity().startActivity(new Intent(getActivity(), HelpActivity.class));
                getActivity().finish();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main_toolbar_menu, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }
}
