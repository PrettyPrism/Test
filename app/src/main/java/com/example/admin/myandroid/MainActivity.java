package com.example.admin.myandroid;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

import twitter4j.AsyncTwitter;
import twitter4j.AsyncTwitterFactory;
import twitter4j.Status;
import twitter4j.Twitter;
//import twitter4j.TwitterAdapter;
import twitter4j.TwitterAdapter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterListener;
import twitter4j.auth.AccessToken;
import twitter4j.conf.ConfigurationBuilder;

public class MainActivity extends AppCompatActivity implements OnClickListener, SensorEventListener, LocationListener {
    AsyncTwitterFactory factory = new AsyncTwitterFactory();
    AsyncTwitter twitter = factory.getInstance();
    Twitter myTwitter;
    AccessToken accessToken;
    final int REQUEST_ACCESS_TOKEN = 0;
    final String consumer_key = "mN3nLNC0DKY1hvrut1rJZVdqG";
    final String consumer_secret = "zeoqUjyvaNTZDiDTbE2ERyw2j7JDTJGqUE6pZHCJLBlbAcIYbJ";
    String token = "";
    String token_secret = "";
    SharedPreferences pref;
    SharedPreferences.Editor editor;

    private SensorManager sm;
    private LocationManager lm;
    float[] data = new float[3];
    long start,end;
    TextView time, locate, prov;
    boolean vibra = false;
    int location_min_time = 0, location_min_distance = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        lm = (LocationManager)getSystemService(Service.LOCATION_SERVICE);
        start = System.currentTimeMillis();
        data[0] = 0;
        data[1] = 0;
        data[2] = 0;
        time = (TextView)findViewById(R.id.acceleText);
        locate = (TextView)findViewById(R.id.locationText);
        prov = (TextView)findViewById(R.id.providerText);
/*
        final Button tb = (Button)findViewById(R.id.tweet_button);
        tb.setOnClickListener(this);
*/
        pref = getSharedPreferences("t4jdata", Activity.MODE_PRIVATE);
        token=pref.getString("token", "");
        token_secret=pref.getString("token_secret", "");

        twitter.setOAuthConsumer(consumer_key, consumer_secret);
        twitter.getOAuthRequestTokenAsync();

        if(token.length()==0){
            Intent intent = new Intent(getApplicationContext(), OAuthActivity.class);
            intent.putExtra(OAuthActivity.EXTRA_CONSUMER_KEY, consumer_key);
            intent.putExtra(OAuthActivity.EXTRA_CONSUMER_SECRET, consumer_secret);
            startActivityForResult(intent, REQUEST_ACCESS_TOKEN);
        } else {
            accessToken = new AccessToken(token, token_secret);
            Intent intent = new Intent(MainActivity.this, TwitterService.class);
            intent.putExtra(TwitterService.EXTRA_CONSUMER_KEY, consumer_key);
            intent.putExtra(TwitterService.EXTRA_CONSUMER_SECRET, consumer_secret);
            intent.putExtra(TwitterService.EXTRA_ACCESS_TOKEN, token);
            intent.putExtra(TwitterService.EXTRA_ACCESS_TOKEN_SECRET, token_secret);
            startService(intent);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_ACCESS_TOKEN && resultCode == Activity.RESULT_OK) {
            token = data.getStringExtra(OAuthActivity.EXTRA_ACCESS_TOKEN);
            token_secret = data.getStringExtra(OAuthActivity.EXTRA_ACCESS_TOKEN_SECRET);
            accessToken = new AccessToken(token, token_secret);

            //  accesstokenを記録して2回目以降自動にログインする
            editor = pref.edit();
            editor.putString("token", token);
            editor.putString("token_secret", token_secret);
            editor.commit();

            Intent intent = new Intent(MainActivity.this, TwitterService.class);
            intent.putExtra(TwitterService.EXTRA_CONSUMER_KEY, consumer_key);
            intent.putExtra(TwitterService.EXTRA_CONSUMER_SECRET, consumer_secret);
            intent.putExtra(TwitterService.EXTRA_ACCESS_TOKEN, token);
            intent.putExtra(TwitterService.EXTRA_ACCESS_TOKEN_SECRET, token_secret);
            startService(intent);
        }
    }
    @Override
    public void onClick(View view) {
        /*
        switch(view.getId()) {
            case R.id.tweet_button:
            {
                final TextView tweet = (TextView)findViewById(R.id.tweetText);
                Intent intent = new Intent(MainActivity.this, TwitterService.class);
                intent.putExtra(TwitterService.EXTRA_isTweet, true);
                intent.putExtra(TwitterService.EXTRA_tweet, tweet.getText().toString());
                startService(intent);
                tweet.setText("");
                break;
            }
        }
        */
    }
    @Override
    protected void onResume() {
        //センサマネージャ等の設定
        super.onResume();
        List<Sensor> sensors = sm.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensors.size() > 0) {
            Sensor s = sensors.get(0);
            sm.registerListener(this, s, SensorManager.SENSOR_DELAY_NORMAL);
        }
        boolean isNetworkEnabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (isNetworkEnabled) {
            //noinspection ResourceType
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,location_min_time,location_min_distance,this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        String m = "";
        //位置情報の表示
        m += "経度 : "+location.getLongitude()+"\n";//緯度の取得
        m += "緯度 : "+location.getLatitude()+"\n";//経度の取得
        locate.setText(m);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle bundle) {
        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                prov.setText(provider+"が圏外になっていて利用できません");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                prov.setText("一時的に"+provider+"が利用できません");
                break;
            case LocationProvider.AVAILABLE:
                prov.setText(provider+"が利用できます");
                break;
        }
    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        String m = "";
        float x=0, y=0, z=0;
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                x = sensorEvent.values[0];
                y = sensorEvent.values[1];
                z = sensorEvent.values[2];
                if (Math.abs(data[0]-x)>1 || Math.abs(data[1]-y)>1 || Math.abs(data[2]-z)>1) {
                    //加速度が一定以上の変化があった場合
                    data[0] = x;
                    data[1] = y;
                    data[2] = z;
                    start = System.currentTimeMillis();
                    vibra = false;
                }
                end = System.currentTimeMillis();
        }
        //情報表示用の処理
        m += x+"\n";
        m += y+"\n";
        m += z+"\n";
        m += ((end-start)/1000)+"秒";
        time.setText(m);
        if ((end-start)/1000 >= 10 && !vibra) {
            //もし一定以上加速度が変わらなかった場合、バイブレーションを起動する
            ((Vibrator) getSystemService(Context.VIBRATOR_SERVICE)).vibrate(new long[]{500,200,500,200},-1);
            vibra = true;
            Intent intent = new Intent(MainActivity.this, TwitterService.class);
            intent.putExtra(TwitterService.EXTRA_isTweet, true);
//            intent.putExtra(TwitterService.EXTRA_tweet, "10秒");
//            intent.putExtra(TwitterService.EXTRA_tweet, "10秒も放置された");
            intent.putExtra(TwitterService.EXTRA_tweet, "10秒間も音沙汰無し");
            startService(intent);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
