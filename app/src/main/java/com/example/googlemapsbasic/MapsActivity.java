package com.example.googlemapsbasic;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback{ // LocationListener

    private GoogleMap mMap;
    //private ActivityMapsBinding binding;
private String textJSON = "";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);




    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        checkLocationPermission();  //Cridem a la funció que demana permís ubicació


        //Permetre fer zoom
        UiSettings settings = mMap.getUiSettings();
        settings.setZoomControlsEnabled(true);


        // Add a marker in Sydney and move the camera
       /* LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
*/
        //Aconseguir la meva ubicació amb Location

        LocationManager gestorLoc = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        //gestorLoc.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
        Location loc = gestorLoc.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        //loc és la meva ubicació, la puc utilitzar per calcular distàncies a altres punts.



        //Descarregar JSON amb Volley. Per afegir els markers ho farem al onMapReady
//https://run.mocky.io/v3/74f70c0c-f1dc-4424-8c86-d4879396d79d
        String url = "https://run.mocky.io/v3/74f70c0c-f1dc-4424-8c86-d4879396d79d";


// Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        textJSON=response.toString();  //Json en format String

                        try {
                            JSONObject jsonRest= new JSONObject(textJSON);

                            JSONArray array = jsonRest.getJSONArray("restaurantes");
                            // array.length em servirà per recorrer tots els rest.

                            for(int i = 0;i<array.length() ; i++) {
                                JSONObject obj = (JSONObject) array.get(i);

                                Double latitud = obj.getDouble("latitud");
                                Double longitud = obj.getDouble("longitud");

                                String nom = obj.getString("nombre");

                                String opinion = obj.getString("opinion");

                                // add marker a cada restaurant que trobi

                                LatLng rest = new LatLng(latitud, longitud);

                                MarkerOptions options = new MarkerOptions();
                                options.position(rest);
                                options.title(nom);
                                options.snippet(opinion);
                                options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                mMap.addMarker(options);
                                //mMap.addMarker(new MarkerOptions().position(rest).title(nom).snippet(opinion).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(rest, 15));

                                Location rest1 =  new Location("rest1");
                                rest1.setLatitude(latitud);
                                rest1.setLongitude(longitud);

                                rest1.distanceTo(loc);

                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }


                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "That didn't work!", Toast.LENGTH_LONG).show();



                    }
                });

        queue.add(jsonObjectRequest);






    }

    public boolean checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);


        }
        return false;  //Després s'executarà el mètode onRequestPermissionsResult
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Ja pots utilitzar la ubicació
                        mMap.setMyLocationEnabled(true);

                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.

                }
                return;
            }

        }
    }


    /*
    @Override
    public void onLocationChanged(Location location) {

        String text = "Posició actual:\n" +
                "Latitud = " + location.getLatitude() + "\n" +
                "Longitud = " + location.getLongitude();

        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG).show();

    }



    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(getApplicationContext(),
                "GPS desactivat per l’usuari",
                Toast.LENGTH_LONG ).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getApplicationContext(),
                "GPS habilitat per l’usuari",
                Toast.LENGTH_LONG).show();
    }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        String missatge = "";
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                missatge = "GPS status: Out of service";
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                missatge = "GPS status: Temporarily  unavailable";
                break;
            case LocationProvider.AVAILABLE:
                missatge = "GPS status: Available";
                break;
        }

        Toast.makeText(getApplicationContext(),
                missatge,
                Toast.LENGTH_LONG).show();
    }
*/
}

