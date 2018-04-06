package com.example.alex.androidlabs;

        import android.content.Context;
        import android.graphics.Bitmap;
        import android.graphics.BitmapFactory;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.os.AsyncTask;
        import android.os.Bundle;
        import android.support.v7.app.AppCompatActivity;
        import android.support.v7.widget.Toolbar;
        import android.util.Log;
        import android.util.Xml;
        import android.view.View;
        import android.widget.ImageView;
        import android.widget.ProgressBar;
        import android.widget.TextView;

        import org.xmlpull.v1.XmlPullParser;
        import org.xmlpull.v1.XmlPullParserException;
        import org.xmlpull.v1.XmlPullParserFactory;

        import java.io.File;
        import java.io.FileInputStream;
        import java.io.FileNotFoundException;
        import java.io.FileOutputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.net.HttpURLConnection;
        import java.net.URL;

public class WeatherForecast extends AppCompatActivity {

    protected static final String ACTIVITY_NAME = "WeatherForecast";
    private ProgressBar progressBar;
    private TextView currentTemp;
    private TextView minTemp;
    private TextView maxTemp;
    private TextView windSpeed;
    private ImageView weatherImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);

        progressBar = (ProgressBar) findViewById(R.id.weatherProgress);
        progressBar.setVisibility(View.VISIBLE);

        currentTemp = (TextView) findViewById(R.id.weatherCurrentTemp);
        minTemp = (TextView) findViewById(R.id.weatherMinTemp);
        maxTemp = (TextView) findViewById(R.id.weatherMaxTemp);
        windSpeed = (TextView) findViewById(R.id.weatherWind);
        weatherImage = (ImageView) findViewById(R.id.weatherImage);

        new ForecastQuery().execute();

        Log.i(ACTIVITY_NAME,"In onCreate()");
    }


    //inner class
    class ForecastQuery extends AsyncTask<String, Integer, String> {

        private String min = null;
        private String max = null;
        private String curr = null;
        private String windNum = null;
        private String windSpd = null;
        private String currWeather = null;
        private Bitmap bitmap;

        @Override
        protected String doInBackground(String... args){
            InputStream stream;

            //checking network connectivity
            ConnectivityManager connMgr = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connMgr != null;
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            if(networkInfo != null && networkInfo.isConnected()){
                Log.i(ACTIVITY_NAME, "Device connected to network");
            }
            else{ Log.e(ACTIVITY_NAME, "No network connection available"); }

            // connecting to url and reading data input stream
            try {
                URL url = new URL("http://api.openweathermap.org/data/2.5/weather?q=ottawa,ca&APPID=d99666875e0e51521f0040a3d97d0f6a&mode=xml&units=metric");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000); //in milliseconds
                conn.setConnectTimeout(15000); //in millisenconds
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                stream = conn.getInputStream();


                XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
                factory.setNamespaceAware(true);
                XmlPullParser parser = factory.newPullParser();
                parser.setInput(stream, null);
                int eventType = parser.getEventType();

                while(eventType != XmlPullParser.END_DOCUMENT){
                    if(eventType != XmlPullParser.START_TAG){
                        eventType = parser.next();
                        continue;
                    }
                    else{
                        if(parser.getName().equals("temperature") ){
                            curr = parser.getAttributeValue(null, "value");
                            publishProgress(25);
                            min = parser.getAttributeValue(null, "min");
                            publishProgress(50);
                            max = parser.getAttributeValue(null, "max");
                            publishProgress(75);
                        }else if(parser.getName().equals("speed")){
                            windNum = parser.getAttributeValue(null, "value");
                            windSpd = parser.getAttributeValue(null, "name");
                            publishProgress(90);
                        }else if(parser.getName().equals("weather")){
                            currWeather = parser.getAttributeValue(null, "icon");
                        }
                        eventType = parser.next();
                    }
                }

                conn.disconnect();


                if(fileExist(currWeather + ".png")){
                    Log.i(ACTIVITY_NAME, "Weather image exists! Read from file");
                    File file = getBaseContext().getFileStreamPath(currWeather + ".png");
                    FileInputStream fis = new FileInputStream(file);
                    bitmap = BitmapFactory.decodeStream(fis);


                }else {
                    Log.i(ACTIVITY_NAME, "Weather image does not exist! Download from URL");

                    URL imageUrl = new URL("http://openweathermap.org/img/w/" + currWeather + ".png");
                    conn = (HttpURLConnection) imageUrl.openConnection();
                    conn.connect();
                    stream = conn.getInputStream();
                    bitmap = BitmapFactory.decodeStream(stream);

                    FileOutputStream outputStream  = openFileOutput(currWeather + ".png", Context.MODE_PRIVATE);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, outputStream );
                    outputStream .flush();
                    outputStream .close();
                    conn.disconnect();
                }

                publishProgress(100);
            } catch (XmlPullParserException | IOException parserException){
                Log.e(ACTIVITY_NAME, parserException.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer ... value){
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(value[0]);
        }

        @Override
        protected void onPostExecute(String args){
            currentTemp.setText("Current: " + curr + "°C");
            minTemp.setText("Min: " + min + "°C");
            maxTemp.setText("Max: " + max + "°C");
            windSpeed.setText("Wind: " + windNum + " km/h \n "
                    + "           " + windSpd);
            weatherImage.setImageBitmap(bitmap);
            progressBar.setVisibility(View.INVISIBLE);
        }

        public boolean fileExist(String name){
            File file = getBaseContext().getFileStreamPath(name);
            return file.exists();
        }

    }


}