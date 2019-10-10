package com.example.mycheckins;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.app.Activity.RESULT_OK;

public class DetailsFragment extends Fragment {

    View view;
    double latitude;
    double longitude;
    Bitmap receipt_image;
    SQLiteDatabase db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_details, container, false);
        ((AppCompatActivity)getActivity()).setSupportActionBar((Toolbar)view.findViewById(R.id.main_toolbar));

        db = MainActivity.db;
        receipt_image = null;
        longitude = 0;
        latitude = 0;

        // check which mode this activity is opened in - add new OR view details
        String mode = getActivity().getIntent().getStringExtra("mode");
        final int item_id = getActivity().getIntent().getIntExtra("item_id", -1);

        // mode specific code
        if(mode.equals("new")) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Add New");

            // set current date as date button text
            ((Button)view.findViewById(R.id.input_date_btn)).setText("DATE: " + new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date()));

            // open datepicker on button click
            view.findViewById(R.id.input_date_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(final View view) {
                    final Calendar c = Calendar.getInstance();
                    int year = c.get(Calendar.YEAR);
                    int month = c.get(Calendar.MONTH);
                    int day = c.get(Calendar.DAY_OF_MONTH);

                    // set picked date as date button text
                    new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                            ((Button)view.findViewById(R.id.input_date_btn)).setText("DATE: " + i + "-" + (i1 + 1) + "-" + i2);
                        }
                    }, year, month, day).show();
                }
            });

            // get current location
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            else
                doLocationStuff();

            // open camera
            view.findViewById(R.id.input_image_btn).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                        startActivityForResult(takePictureIntent, 100);
                    }
                }
            });

            // save record to db
            ((Button)view.findViewById(R.id.btn_save_delete)).setText("SAVE");
            ((Button)view.findViewById(R.id.btn_save_delete)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // convert Bitmap image to blob
                    byte[] receipt_image_blob;
                    if(receipt_image != null) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        receipt_image.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                        receipt_image_blob = outputStream.toByteArray();
                    } else {
                        receipt_image_blob = null;
                    }

                    ContentValues cv = new  ContentValues();
                    cv.put("title", ((EditText)view.findViewById(R.id.input_title)).getText().toString());
                    cv.put("place", ((EditText)view.findViewById(R.id.input_place)).getText().toString());
                    cv.put("details", ((EditText)view.findViewById(R.id.input_details)).getText().toString());
                    cv.put("date", ((Button)view.findViewById(R.id.input_date_btn)).getText().toString().replace("DATE: ", "").trim());
                    cv.put("longitude", longitude);
                    cv.put("latitude", latitude);
                    cv.put("image", receipt_image_blob);
                    db.insert("Receipts", null, cv );

                    Toast.makeText(getActivity(), "Record Saved", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }
            });
        } else if (mode.equals("view")) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("DETAILS");

            Cursor c = db.rawQuery("SELECT * from Receipts WHERE id = " + item_id, null);
            c.moveToFirst();

            // record does not exists for request id. go back to main activity
            if(c.getCount() == 0) { getActivity().onBackPressed(); }

            // pull date for request id and show it
            for (int i = 0; i < c.getCount(); i++, c.moveToNext()) {
                ((EditText)view.findViewById(R.id.input_title)).setText(c.getString(1));
                ((EditText)view.findViewById(R.id.input_title)).setFocusable(false);

                ((EditText)view.findViewById(R.id.input_place)).setText(c.getString(2));
                ((EditText)view.findViewById(R.id.input_place)).setFocusable(false);

                ((EditText)view.findViewById(R.id.input_details)).setText(c.getString(3));
                ((EditText)view.findViewById(R.id.input_details)).setFocusable(false);

                ((Button)view.findViewById(R.id.input_date_btn)).setText("DATE: " + c.getString(4));

                longitude = c.getDouble(5);
                latitude = c.getDouble(6);
                ((TextView)view.findViewById(R.id.input_location)).setText("Longitude: " + longitude + "\n" + "Latitude: " + latitude);

                if(c.getBlob(7) != null) {
                    byte[] img_blob = c.getBlob(7);
                    receipt_image = BitmapFactory.decodeByteArray(img_blob, 0, img_blob.length);
                    ((ImageView)view.findViewById(R.id.input_image)).setImageBitmap(receipt_image);
                }

                ((Button)view.findViewById(R.id.input_image_btn)).setEnabled(false);
            }


            ((Button)view.findViewById(R.id.btn_save_delete)).setText("DELETE");
            ((Button)view.findViewById(R.id.btn_save_delete)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    db.execSQL("DELETE FROM Receipts WHERE id = " + item_id);
                    Toast.makeText(getActivity(), "Record Deleted", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }
            });
        }

        // common code for both modes

        // open google maps with provided longitude and latitude
        view.findViewById(R.id.show_map_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(latitude == 0 || longitude == 0) {
                    Toast.makeText(getActivity(), "Location not available", Toast.LENGTH_SHORT).show();
                } else {
                    String uri = String.format(Locale.ENGLISH, "geo:%f,%f", latitude, longitude);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    getActivity().startActivity(intent);
                }
            }
        });

        // open sharesheet with user provided data
        view.findViewById(R.id.btn_share).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String share_text = "Title: " + ((EditText)view.findViewById(R.id.input_title)).getText() + "\n";
                share_text += "Place: " + ((EditText)view.findViewById(R.id.input_place)).getText() + "\n";
                share_text += "Details: " + ((EditText)view.findViewById(R.id.input_details)).getText() + "\n";
                share_text += "Date: " + ((Button)view.findViewById(R.id.input_date_btn)).getText().toString().replace("DATE: ", "") + "\n";
                share_text += "Location: (lat: " + latitude + ", long: " + longitude + ")" + "\n";

                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, share_text);
                sendIntent.setType("text/plain");
                startActivity(Intent.createChooser(sendIntent, null));
            }
        });

        return view;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 200:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    doLocationStuff();
                else {
                    Toast.makeText(getActivity(), "Permission Required", Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                }
        }
    }

    @SuppressLint("MissingPermission")
    public void doLocationStuff() {
        final LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.d("##### Location Changes", location.toString());
                longitude = location.getLongitude();
                latitude = location.getLatitude();
                ((TextView)view.findViewById(R.id.input_location)).setText("Longitude: " + longitude + "\n" + "Latitude: " + latitude);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("##### Status Changed", String.valueOf(status));
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("##### Provider Enabled", provider);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("##### Provider Disabled", provider);
            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        criteria.setAltitudeRequired(false);
        criteria.setBearingRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);

        final LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestSingleUpdate(criteria, locationListener, null);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 100 && resultCode == RESULT_OK) {
            receipt_image = (Bitmap) data.getExtras().get("data");
            ((ImageView)view.findViewById(R.id.input_image)).setImageBitmap(receipt_image);
        }
    }

}
