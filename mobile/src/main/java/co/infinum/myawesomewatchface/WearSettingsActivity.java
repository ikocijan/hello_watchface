package co.infinum.myawesomewatchface;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import co.infinum.shared_code.Constants;

public class WearSettingsActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    Spinner spinner;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_settings);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Wearable.API)
                .build();

        init();

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    private void init() {

        spinner = (Spinner) findViewById(R.id.spinner);

        List<String> colorList = new ArrayList<>();
        colorList.add("BLACK");
        colorList.add("WHITE");

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, colorList);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onColorSelected(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void onColorSelected(int position) {

        int color;

        switch (position) {
            case 0:
                color = getResources().getColor(android.R.color.white);
                break;

            case 1:
                color = getResources().getColor(android.R.color.black);
                break;

            default:
                color = getResources().getColor(android.R.color.white);
                break;
        }

        PutDataMapRequest dataMapRequest = PutDataMapRequest.create(Constants.MAP_REQUEST_PATH);

        dataMapRequest.getDataMap().putInt(Constants.KEY_BACKGROUND_COLOR, color);

        PutDataRequest putDataReq = dataMapRequest.asPutDataRequest();
        Wearable.DataApi.putDataItem(googleApiClient, putDataReq);


    }


}
