package com.example.mobilegcs_lsh;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.LocationTrackingMode;
import com.naver.maps.map.MapView;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.InfoWindow;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.util.FusedLocationSource;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private NaverMap mNaverMap;
    private MapView mapView;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1000;
    private FusedLocationSource locationSource;
    private InfoWindow infoWindow = new InfoWindow();
    private ArrayList<Marker> Marker_list = new ArrayList<Marker>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        locationSource =
                new FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (locationSource.onRequestPermissionsResult(
                requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated()) { // 권한 거부됨
                mNaverMap.setLocationTrackingMode(LocationTrackingMode.None);
            }
            return;
        }
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
    }


    @Override
    public void onMapReady(@NonNull NaverMap naverMap) {
        this.mNaverMap = naverMap;
        infoWindow.setAdapter(new InfoWindow.DefaultTextAdapter(this) {
            @NonNull
            @Override
            public CharSequence getText(@NonNull InfoWindow infoWindow) {
                // 정보 창이 열린 마커의 tag를 텍스트로 노출하도록 반환
                return (CharSequence) infoWindow.getMarker().getTag();
            }
        });
        naverMap.setLocationSource(locationSource);
        naverMap.setLocationTrackingMode(LocationTrackingMode.Follow);
/*
        EditText textView = (EditText)findViewById(R.id.AdressInput);
        String k = textView.getText().toString();
*/
        marking();
        CLEARCtnClick();
        longClick();
    }

    public void longClick() {

        mNaverMap.setOnMapLongClickListener((point, coord) -> {
            reversGeocoder reverse = new reversGeocoder();
            reverse.execute(coord.longitude, coord.latitude);

        });
    }

    public void CLEARCtnClick(){
        Button CLEARBtn = (Button) findViewById(R.id.clear);
        CLEARBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                for(Marker delmarke:Marker_list){
                    delmarke.setMap(null);

                }
                Marker_list.clear();
            }
        });
    }

    public void marking(){
        Button CLEARBtn = (Button) findViewById(R.id.marking);
        EditText text = (EditText)findViewById(R.id.AdressInput);
        CLEARBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                geocoder geocode = new geocoder();
                geocode.execute(text.getText().toString());
            }
        });
    }

    private class reversGeocoder extends AsyncTask<Double, Integer, JSONArray> {
        double latitude,longitude;

        @Override
        protected JSONArray doInBackground(Double... doubles) {
            String str, receiveMsg;
            JSONArray jarray = null;
            try {
                this.longitude = doubles[0];
                this.latitude = doubles[1];
                URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-reversegeocode/v2/gc?request=coordsToaddr&coords=" + longitude + "," + latitude + "&output=json");
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserFactory.newPullParser();
                http.setRequestMethod("GET");
                http.setRequestProperty("Content-Type", "application/json");
                http.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "f24i09guo5");
                http.setRequestProperty("X-NCP-APIGW-API-KEY", "g70kU7oYGHx5ftB7e9ICW7N3Ud3hu12h4BRlGeFL");
                http.connect();
                if (http.getResponseCode() == http.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                    jarray = new JSONObject(receiveMsg).getJSONArray("results");
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jarray;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // 파일 다운로드 퍼센티지 표시 작업
        }

        @Override
        protected void onPostExecute(JSONArray jarray) {
            try {
                JSONObject legalcode = jarray.getJSONObject(0);
                JSONObject dataOBJ = (JSONObject) legalcode.get("region");
                JSONObject area1OBJ = (JSONObject) dataOBJ.get("area1");
                JSONObject area2OBJ = (JSONObject) dataOBJ.get("area2");
                JSONObject area3OBJ = (JSONObject) dataOBJ.get("area3");
                //JSONObject area4OBJ = (JSONObject) dataOBJ.get("area4");

                String area1 = area1OBJ.getString("name");
                String area2 = area2OBJ.getString("name");
                String area3 = area3OBJ.getString("name");
                //String area4 = area4OBJ.getString("name");

                Marker_list.add(new Marker(new LatLng(latitude, longitude)));
                Marker_list.get(Marker_list.size()-1).setPosition(new LatLng(latitude, longitude));
                Marker_list.get(Marker_list.size()-1).setMap(mNaverMap);
                Marker_list.get(Marker_list.size()-1).setTag(area1 + " " + area2 + " " + area3);
                infoWindow.open(Marker_list.get(Marker_list.size()-1));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private class geocoder extends AsyncTask<String, Void, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... strings) {
            String str, receiveMsg;
            JSONArray jarray = null;
            try {
                URL url = new URL("https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query="+strings[0]);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                XmlPullParserFactory parserFactory = XmlPullParserFactory.newInstance();
                XmlPullParser parser = parserFactory.newPullParser();
                http.setRequestMethod("GET");
                http.setRequestProperty("Content-Type", "application/json");
                http.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "f24i09guo5");
                http.setRequestProperty("X-NCP-APIGW-API-KEY", "g70kU7oYGHx5ftB7e9ICW7N3Ud3hu12h4BRlGeFL");
                http.connect();
                if (http.getResponseCode() == http.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(http.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);
                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                    jarray = new JSONObject(receiveMsg).getJSONArray("addresses");
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return jarray;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected void onProgressUpdate(Integer... progress) {
            // 파일 다운로드 퍼센티지 표시 작업
        }

        @Override
        protected void onPostExecute(JSONArray jarray) {
            try {
                JSONObject latlng = jarray.getJSONObject(0);
                double latitude,longitude;
                longitude = latlng.getDouble("x");
                latitude = latlng.getDouble("y");
                Marker_list.add(new Marker(new LatLng(latitude, longitude)));
                Marker_list.get(Marker_list.size()-1).setPosition(new LatLng(latitude, longitude));
                Marker_list.get(Marker_list.size()-1).setMap(mNaverMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}