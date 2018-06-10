package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListPopupWindow;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mapbox.api.geocoding.v5.GeocodingCriteria;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.core.exceptions.ServicesException;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxandroiddemo.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeocodingActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private MapboxMap mapboxMap;
  private BottomSheetBehavior sheetBehavior;
  private Button startGeocodeButton;
  private Button chooseCityButton;
  private TextView latTextView;
  private TextView longTextView;
  private TextView geocodeResultTextView;
  private String TAG = "GeocodingActivity";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_geocoding);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    this.mapboxMap = mapboxMap;
    initTextViews();
    initButtons();
  }

  private void initTextViews() {
    latTextView = findViewById(R.id.geocode_latitude_editText);
    longTextView = findViewById(R.id.geocode_longitude_editText);
    geocodeResultTextView = findViewById(R.id.geocode_result_message);
  }

  private void initButtons() {
    startGeocodeButton = findViewById(R.id.start_geocode_button);
    chooseCityButton = findViewById(R.id.choose_city_spinner_button);
    startGeocodeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        makeGeocodeSearch(new LatLng(Double.valueOf(latTextView.getText().toString()),
            Double.valueOf(longTextView.getText().toString())));
      }
    });
    chooseCityButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        showCityListMenu();
      }
    });
  }

  private void showCityListMenu() {
    List<String> modes = new ArrayList<>();
    modes.add("Lima");
    modes.add("Helsinki");
    modes.add("Addis Ababa");
    modes.add("Osaka");
    ArrayAdapter<String> profileAdapter = new ArrayAdapter<>(this,
        android.R.layout.simple_list_item_1, modes);
    ListPopupWindow listPopup = new ListPopupWindow(this);
    listPopup.setAdapter(profileAdapter);
    listPopup.setAnchorView(chooseCityButton);
    listPopup.setOnItemClickListener((parent, itemView, position, id) -> {
      if (position == 0) {
        animateCameraToNewPosition(new LatLng(42.8864, -78.878));
        makeGeocodeSearch(new LatLng(42.8864, -78.878));
      } else if (position == 1) {
        animateCameraToNewPosition(new LatLng(60.1698, 24.938));
        makeGeocodeSearch(new LatLng(60.1698, 24.938));
      } else if (position == 2) {
        animateCameraToNewPosition(new LatLng(8.980, 38.757));
        makeGeocodeSearch(new LatLng(8.980, 38.757));
      } else if (position == 3) {
        animateCameraToNewPosition(new LatLng(34.693, 135.5021));
        makeGeocodeSearch(new LatLng(34.693, 135.5021));
      }
      listPopup.dismiss();
    });
    listPopup.show();
  }

  private void makeGeocodeSearch(LatLng latLng) {
    try {
      MapboxGeocoding client = MapboxGeocoding.builder()
          .accessToken(getString(R.string.access_token))
          .query(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()))
          .geocodingTypes(GeocodingCriteria.TYPE_ADDRESS)
          .build();
      client.enqueueCall(new Callback<GeocodingResponse>() {
        @Override
        public void onResponse(Call<GeocodingResponse> call,
                               Response<GeocodingResponse> response) {
          List<CarmenFeature> results = response.body().features();
          if (results.size() > 0) {

            Log.d(TAG, "onResponse: results.size() > 0");
            CarmenFeature feature = results.get(0);

            Log.d(TAG, "onResponse: feature = " + feature);
            geocodeResultTextView.setText(String.format(getString(R.string.geocode_results),
                feature.toString()));

            animateCameraToNewPosition(latLng);
          } else {
            Toast.makeText(GeocodingActivity.this, R.string.no_results,
                Toast.LENGTH_SHORT).show();
          }
        }

        @Override
        public void onFailure(Call<GeocodingResponse> call, Throwable throwable) {
          Log.e("GeocodingActivity", "Geocoding Failure: " + throwable.getMessage());
        }
      });
    } catch (ServicesException servicesException) {
      Log.e("GeocodingActivity", "Error geocoding: " + servicesException.toString());
      servicesException.printStackTrace();
    }
  }

  private void animateCameraToNewPosition(LatLng latLng) {
    mapboxMap.animateCamera(CameraUpdateFactory
        .newCameraPosition(new CameraPosition.Builder()
            .target(latLng)
            .zoom(12)
            .build()), 1500);
  }

  // Add the mapView lifecycle to the activity's lifecycle methods
  @Override
  public void onResume() {
    super.onResume();
    mapView.onResume();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mapView.onStart();
  }

  @Override
  protected void onStop() {
    super.onStop();
    mapView.onStop();
  }

  @Override
  public void onPause() {
    super.onPause();
    mapView.onPause();
  }

  @Override
  public void onLowMemory() {
    super.onLowMemory();
    mapView.onLowMemory();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    mapView.onDestroy();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    mapView.onSaveInstanceState(outState);
  }
}