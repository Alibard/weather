package home.ww;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;


public class MainActivity extends AppCompatActivity {
TextView txt;
    TextView main;
    TextView temp;
    TextView humidity;
    TextView pressure;
    TextView wind;
    ImageView img;
    SupportMapFragment mapFragment;
    GoogleMap map;
    Fragment Mmap;

    public String temss="";
    public static String LOG_TAG = "my_log";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        txt = (TextView)findViewById(R.id.city);
        txt.setVisibility(View.INVISIBLE);
        main = (TextView)findViewById(R.id.main);
        main.setVisibility(View.INVISIBLE);
        temp = (TextView)findViewById(R.id.temp);
        temp.setVisibility(View.INVISIBLE);
        humidity = (TextView)findViewById(R.id.humidity);
        humidity.setVisibility(View.INVISIBLE);
        pressure = (TextView)findViewById(R.id.pressure);
        pressure.setVisibility(View.INVISIBLE);
        wind = (TextView)findViewById(R.id.wind);
        wind.setVisibility(View.INVISIBLE);
        img = (ImageView)findViewById(R.id.icon);

    }

    private void init(String lon,String lat) {
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                                      @Override
                                      public void onMapClick(LatLng arg0) {
                                          // TODO Auto-generated method stub


                                      }
                                  }
        );
        map.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            @Override
            public void onMapLongClick(LatLng latLng) {

            }
        });

        map.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener() {

            @Override
            public void onCameraChange(CameraPosition camera) {

            }
        });

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng ( Double.parseDouble(lat), Double.parseDouble(lon)))
                .zoom(13)
                .tilt(20)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        SearchView sv = new SearchView(this);
        sv.setOnQueryTextListener(new SerchFiltro(this));
MenuItem m1 = menu.add(0, 0, 0, "Item1") ;
        m1.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        m1.setActionView(sv);
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }
    private class SerchFiltro implements OnQueryTextListener{
        Context ctx;
        LayoutInflater lInflater;
        Picasso mPicasso;
        SerchFiltro(Context contex){
            ctx=contex;
            lInflater = (LayoutInflater) ctx
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        }
        @Override
        public boolean onQueryTextSubmit(String query) {

            String[] ss= new String[9];

            ParseTask pars = new ParseTask();
            pars.execute(query);
            try {
                ss=pars.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            txt = (TextView)findViewById(R.id.city);
            txt.setVisibility(View.VISIBLE);
            main = (TextView)findViewById(R.id.main);
            main.setVisibility(View.VISIBLE);
            temp = (TextView)findViewById(R.id.temp);
            temp.setVisibility(View.VISIBLE);
            humidity = (TextView)findViewById(R.id.humidity);
            humidity.setVisibility(View.VISIBLE);
            pressure = (TextView)findViewById(R.id.pressure);
            pressure.setVisibility(View.VISIBLE);
            wind = (TextView)findViewById(R.id.wind);
            wind.setVisibility(View.VISIBLE);

            txt.setText("Weather in " + query);
            main.setText("Weather is : " + ss[1]);
            temp.setText("Temperature " + ss[3] + "K");
            humidity.setText("Humidity : " + ss[4]);
            pressure.setText("Pressure :" + ss[5]);
            wind.setText("Wind : "+ss[6]+" m/s");

            img = (ImageView)findViewById(R.id.icon);

            mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            map = mapFragment.getMap();

            Log.d(LOG_TAG, ss[7]+" "+ss[8]);
            if(ss[7]!=null&ss[8]!=null) {
                init(ss[7], ss[8]);
            }
         /*   mPicasso.with(ctx)
                    //.load("openweathermap.org/img/w/" + ss[2] + ".png")
                    .load("http://openweathermap.org/img/w/01d.png")
                    .error(R.drawable.search)
                    .into(img);*/
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
                 return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }





    private class ParseTask extends AsyncTask<String, Void, String[]> {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String resultJson = "";

        @Override
        protected String[] doInBackground(String... params) {
            // получаем данные с внешнего ресурса
            String city =params[0];
            String[] wather = new String[9];
            try {
                URL url = new URL("http://openweathermap.org/data/2.5/weather/?q="+city);

                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }

                resultJson = buffer.toString();

            } catch (Exception e) {
                e.printStackTrace();
            }

            JSONObject dataJsonObj = null;



            try {
                dataJsonObj = new JSONObject( resultJson);
                JSONArray wath = dataJsonObj.getJSONArray("weather");
              //JSONObject mainw = wath.getJSONObject(1);

                for (int i = 0; i < wath.length(); i++) {
                   JSONObject   mainw = wath.getJSONObject(i);
                    wather[1]=mainw.getString("description");
                    wather[2]=mainw.getString("icon");

                }


                JSONObject main = dataJsonObj.getJSONObject("main");
                    wather[3]= main.getString("temp");
                    wather[4]= main.getString("humidity");
                    wather[5]= main.getString("pressure");
                JSONObject wind = dataJsonObj.getJSONObject("wind");
                wather[6]= wind.getString("speed");
                JSONObject coord = dataJsonObj.getJSONObject("coord");
                    wather[7]= coord.getString("lon");
                    wather[8]= coord.getString("lat");


          //      Log.d(LOG_TAG, "Второе имя: " + secondName);
                Log.d(LOG_TAG, "Второе имя2: " + main.getString("temp"));


            } catch (JSONException e) {
                e.printStackTrace();
            }
            return wather;
        }

        @Override
        protected void onPostExecute(String[] strJson) {

            super.onPostExecute(strJson);

        }
    }
}
