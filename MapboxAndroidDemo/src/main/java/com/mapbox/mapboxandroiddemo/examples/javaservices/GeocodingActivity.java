package com.mapbox.mapboxandroiddemo.examples.javaservices;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeocodingActivity extends AppCompatActivity implements OnMapReadyCallback {

  private MapView mapView;
  private BottomSheetBehavior sheetBehavior;
  private Button startGeocodeButton;
  private TextView latTextView;
  private TextView longTextView;
  private TextView geocodeResultOne;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Mapbox access token is configured here. This needs to be called either in your application
    // object or in the same activity which contains the mapview.
    Mapbox.getInstance(this, getString(R.string.access_token));

    // This contains the MapView in XML and needs to be called after the access token is configured.
    setContentView(R.layout.activity_javaservices_geocoding);

    LinearLayout bottomSheet = findViewById(R.id.bottom_sheet);
    sheetBehavior = BottomSheetBehavior.from(bottomSheet);

    mapView = findViewById(R.id.mapView);
    mapView.onCreate(savedInstanceState);
    mapView.getMapAsync(this);
  }

  @Override
  public void onMapReady(MapboxMap mapboxMap) {
    initTextViews();
    initButton();
    initListeners();
    sheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
      @Override
      public void onStateChanged(@NonNull View bottomSheet, int newState) {
        switch (newState) {
          case BottomSheetBehavior.STATE_HIDDEN:
            break;
          case BottomSheetBehavior.STATE_EXPANDED: {
            Toast.makeText(GeocodingActivity.this, "Expanded", Toast.LENGTH_SHORT).show();
          }
          break;
          case BottomSheetBehavior.STATE_COLLAPSED: {
            Toast.makeText(GeocodingActivity.this, "Collapsed", Toast.LENGTH_SHORT).show();
          }
          break;
          case BottomSheetBehavior.STATE_DRAGGING:
            break;
          case BottomSheetBehavior.STATE_SETTLING:
            break;
        }
      }

      @Override
      public void onSlide(@NonNull View bottomSheet, float slideOffset) {

      }
    });
  }

  private void initTextViews() {
    latTextView = findViewById(R.id.geocode_latitude_editText);
    longTextView = findViewById(R.id.geocode_longitude_editText);
    longTextView = findViewById(R.id.geocode_result_one);
  }

  private void initButton() {
    startGeocodeButton = findViewById(R.id.start_geocode_button);
    startGeocodeButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        makeGeocodeSearch(new LatLng(Double.valueOf(latTextView.getText().toString()),
            Double.valueOf(longTextView.getText().toString())));
      }
    });
  }

  private void initListeners() {

  }

  private void makeGeocodeSearch(LatLng latLng) {
    try {
      MapboxGeocoding client = MapboxGeocoding.builder()
          .accessToken(getString(R.string.access_token))
          .query(Point.fromLngLat(latLng.getLongitude(), latLng.getLatitude()))
          .geocodingTypes(GeocodingCriteria.TYPE_COUNTRY)
          .build();

      client.enqueueCall(new Callback<GeocodingResponse>() {
        @Override
        public void onResponse(Call<GeocodingResponse> call, Response<GeocodingResponse> response) {

          List<CarmenFeature> results = response.body().features();
          if (results.size() > 0) {
            CarmenFeature feature = results.get(0);
            longTextView.setText(feature.placeName());
          } else {
            Toast.makeText(GeocodingActivity.this, R.string.no_results, Toast.LENGTH_SHORT).show();
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