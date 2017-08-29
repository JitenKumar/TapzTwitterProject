package com.jiten.tapztwitter;

import java.io.InputStream;
import java.util.List;

import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.URLEntity;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jiten.tapztwitter.DatabaseClass;
import com.jiten.tapztwitter.WebViewActivity;

public class MainActivity extends Activity implements OnClickListener {

    /* Shared preference keys */
    private static final String PREF_NAME = "sample_twitter_pref";
    private static final String PREF_KEY_OAUTH_TOKEN = "oauth_token";
    private static final String PREF_KEY_OAUTH_SECRET = "oauth_token_secret";
    private static final String PREF_KEY_TWITTER_LOGIN = "is_twitter_loggedin";
    private static final String PREF_USER_NAME = "twitter_user_name";

    /* Any number for uniquely distinguish your request */
    public static final int WEBVIEW_REQUEST_CODE = 200;


    private static Twitter twitter;
    private static RequestToken requestToken;

    private static SharedPreferences mSharedPreferences;


    private TextView userName;
    private View loginLayout;
    private View shareLayout;
    private Button hastags;

    private String consumerKey = null;
    private String consumerSecret = null;
    private String callbackUrl = null;
    private String oAuthVerifier = null;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Strict mode for the Thread

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

		/* initializing twitter parameters from string.xml */
        initTwitterConfigs();

		/* Setting activity layout file */
        setContentView(R.layout.activity_main);


        loginLayout = (RelativeLayout) findViewById(R.id.login_layout);
        shareLayout = (LinearLayout) findViewById(R.id.share_layout);

        userName = (TextView) findViewById(R.id.user_name);

		/* register button click listeners */
        findViewById(R.id.btn_login).setOnClickListener(this);
        // findViewById(R.id.btn_share).setOnClickListener(this);
        findViewById(R.id.btn_hashtags).setOnClickListener(this);
        findViewById(R.id.btn_time).setOnClickListener(this);
        findViewById(R.id.btn_web).setOnClickListener(this);

		/* Check if required twitter keys are set */
        if (TextUtils.isEmpty(consumerKey) || TextUtils.isEmpty(consumerSecret)) {
            Toast.makeText(this, "Twitter key and secret not configured",
                    Toast.LENGTH_SHORT).show();
            return;
        }

		/* Initialize application preferences */
        mSharedPreferences = getSharedPreferences(PREF_NAME, 0);

        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

		/*  if already logged in, then hide login layout and show share layout */
        if (isLoggedIn) {
            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);

            String username = mSharedPreferences.getString(PREF_USER_NAME, "");
            userName.setText(getResources().getString(R.string.hello)
                    + username);

        } else {
            loginLayout.setVisibility(View.VISIBLE);
            shareLayout.setVisibility(View.GONE);

            Uri uri = getIntent().getData();

            if (uri != null && uri.toString().startsWith(callbackUrl)) {

                String verifier = uri.getQueryParameter(oAuthVerifier);

                try {

					/* Getting oAuth authentication token */
                    AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

					/* Getting user id form access token */
                    long userID = accessToken.getUserId();
                    final User user = twitter.showUser(userID);
                    final String username = user.getName();

					/* save updated token */
                    saveTwitterInfo(accessToken);

                    //loginLayout.setVisibility(View.GONE);
                    //shareLayout.setVisibility(View.VISIBLE);
                    //userName.setText(getString(R.string.hello) + username);

                } catch (Exception e) {
                    Log.e("Failed to login Twitter", e.getMessage());
                }
            }

        }
    }


    /**
     * Saving user information, after user is authenticated for the first time.
     * You don't need to show user to login, until user has a valid access toen
     */
    private void saveTwitterInfo(AccessToken accessToken) {

        long userID = accessToken.getUserId();

        User user;
        try {
            user = twitter.showUser(userID);

            String username = user.getName();

			/* Storing oAuth tokens to shared preferences */
            Editor e = mSharedPreferences.edit();
            e.putString(PREF_KEY_OAUTH_TOKEN, accessToken.getToken());
            e.putString(PREF_KEY_OAUTH_SECRET, accessToken.getTokenSecret());
            e.putBoolean(PREF_KEY_TWITTER_LOGIN, true);
            e.putString(PREF_USER_NAME, username);
            e.commit();

        } catch (TwitterException e1) {
            e1.printStackTrace();
        }
    }

    /* Reading twitter essential configuration parameters from strings.xml */
    private void initTwitterConfigs() {
        consumerKey = getString(R.string.twitter_consumer_key);
        consumerSecret = getString(R.string.twitter_consumer_secret);
        callbackUrl = getString(R.string.twitter_callback);
        oAuthVerifier = getString(R.string.twitter_oauth_verifier);
    }


    private void loginToTwitter() {
        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

        if (!isLoggedIn) {
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);

            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);

                /**
                 *  Loading twitter login page on webview for authorization
                 *  Once authorized, results are received at onActivityResult
                 *  */
                final Intent intent = new Intent(this, WebViewActivity.class);
                intent.putExtra(WebViewActivity.EXTRA_URL, requestToken.getAuthenticationURL());
                startActivityForResult(intent, WEBVIEW_REQUEST_CODE);

            } catch (TwitterException e) {
                e.printStackTrace();
            }
        } else {

            loginLayout.setVisibility(View.GONE);
            shareLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            String verifier = data.getExtras().getString(oAuthVerifier);
            try {
                AccessToken accessToken = twitter.getOAuthAccessToken(requestToken, verifier);

                long userID = accessToken.getUserId();
                final User user = twitter.showUser(userID);
                String username = user.getName();

                saveTwitterInfo(accessToken);

                loginLayout.setVisibility(View.GONE);
                shareLayout.setVisibility(View.VISIBLE);

                TwitterFactory factory = new TwitterFactory();
                Twitter twitter = factory.getInstance();
                twitter.setOAuthConsumer(consumerKey, consumerSecret);
                twitter.setOAuthAccessToken(accessToken);
                List<Status> statuses = twitter.getHomeTimeline(new Paging(1,400));
                //Toast.makeText(getApplicationContext(),statuses.size(),Toast.LENGTH_LONG).show();
                tstamp=uname=implink=new String[statuses.size()];
                String jiten=new String();
                String jiten1=new String();
                int i=-1;

                DatabaseClass op=new DatabaseClass(getApplicationContext(),"tapzo",null,3);

                SQLiteDatabase db = op.getWritableDatabase();

                for(Status s:statuses) {

                    URLEntity[] urls = s.getURLEntities();
                    for (URLEntity url : urls) {
                        jiten = url.getURL();
                    }
                    if (jiten.length() < 1)
                        continue;
                    implink[++i] = jiten;
                    uname[i] = s.getUser().getName() + "----" + s.getId();
                    tstamp[i] = s.getCreatedAt().toString();
                    jiten = (" " + i + "-" + implink[i] + uname[i] + tstamp[i]) + jiten1;
                    //insert

                    ContentValues cr = new ContentValues();
                    cr.put("userid",user.getId());
                    cr.put("tweetid", s.getId());
                    cr.put("url",jiten);
                    cr.put("timestamp",s.getCreatedAt().toString() );
                    try {

                        db.insert("tstat", null, cr);
                    }
                    catch(Exception c){

                    }
                }
                // After this get the data and use the cursor to set the data to adapter and then display in a list view

                Cursor cr = db.rawQuery("Select * from JitenData ORDER BY timestamp DESC",null);

                cr.moveToFirst();

                while (cr.isAfterLast() == false) {
                    // get the data column wise and set to Adapter
                    String userid = cr.getString(cr.getColumnIndex("userid"));
                    String tweetid = cr.getString(cr.getColumnIndex("tweetid"));
                    String url = cr.getString(cr.getColumnIndex("url"));
                    String timestamp = cr.getString(cr.getColumnIndex("timestamp"));
                    cr.moveToNext();
                }


                userName.setText(MainActivity.this.getResources().getString(
                        R.string.hello) + username);

            } catch (Exception e) {
                Log.e("Twitter Login Failed", e.getMessage());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_login:
                loginToTwitter();
                break;
            case R.id.btn_web:
                try {
                    openwebView();
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                break;


        }
    }

    private void openwebView() throws TwitterException {
        // The factory instance is re-useable and thread safe.
//
        boolean isLoggedIn = mSharedPreferences.getBoolean(PREF_KEY_TWITTER_LOGIN, false);

        if (!isLoggedIn) {
            final ConfigurationBuilder builder = new ConfigurationBuilder();
            builder.setOAuthConsumerKey(consumerKey);
            builder.setOAuthConsumerSecret(consumerSecret);

            final Configuration configuration = builder.build();
            final TwitterFactory factory = new TwitterFactory(configuration);
            twitter = factory.getInstance();

            try {
                requestToken = twitter.getOAuthRequestToken(callbackUrl);

                /**
                 *  Loading twitter login page on webview for authorization
                 *  Once authorized, results are received at onActivityResult
                 *  */
                // The factory instance is re-useable and thread safe.
                Twitter twitter = TwitterFactory.getSingleton();
                List<Status> statuses = twitter.getHomeTimeline();
                System.out.println("Showing home timeline.");
                for (Status status : statuses) {
                    System.out.println(status.getUser().getName() + ":" +
                            status.getText());
                }
            } catch (Exception e1) {
                Log.e("Twitter Login Failed", e1.getMessage());


            }
        }
    }
}

