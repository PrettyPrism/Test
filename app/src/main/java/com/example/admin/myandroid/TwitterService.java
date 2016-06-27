package com.example.admin.myandroid;

import android.app.Activity;
import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.Status;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterService extends IntentService {

    public final static String EXTRA_CONSUMER_KEY = "consumer_key";
    public final static String EXTRA_CONSUMER_SECRET = "consumer_secret";
    public final static String EXTRA_ACCESS_TOKEN = "access_token";
    public final static String EXTRA_ACCESS_TOKEN_SECRET = "access_token_secret";
    public static String EXTRA_isTweet = "extra_isTweet";
    public static String EXTRA_tweet = "extra_tweet";

    static Twitter twitterService = null;

    public TwitterService(String name) {
        super(name);
    }

    public TwitterService() {
        super("TwitterService");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if(twitterService==null) {
            Log.i("message", "create twitter service");
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey(intent.getStringExtra(EXTRA_CONSUMER_KEY));
            cb.setOAuthConsumerSecret(intent.getStringExtra(EXTRA_CONSUMER_SECRET));
            cb.setOAuthAccessToken(intent.getStringExtra(EXTRA_ACCESS_TOKEN));
            cb.setOAuthAccessTokenSecret(intent.getStringExtra(EXTRA_ACCESS_TOKEN_SECRET));
            twitter4j.conf.Configuration c = cb.build();
            twitterService = new TwitterFactory(c).getInstance();
            Log.i("message", "logined");
        }
        boolean isTweet = intent.getBooleanExtra(EXTRA_isTweet, false);
        String tweet = intent.getStringExtra(EXTRA_tweet);
        if(isTweet) {
            Log.i("twitterService", tweet);
            try {
                twitterService.updateStatus(tweet);
            } catch(Exception e) {
                Log.i("tweet error", e.getMessage());
            }
        }
    }

}
