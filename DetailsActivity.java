package com.example.mycheckins;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class DetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();

        if (count == 0) {
            Intent myIntent = new Intent(this, MainActivity.class);
            this.startActivity(myIntent);
            this.finish();
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }

    }
}
