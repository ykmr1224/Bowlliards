package org.ykmr.twitter;

import java.io.File;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.http.AccessToken;
import twitter4j.http.RequestToken;
import twitter4j.media.ImageUpload;
import twitter4j.media.ImageUploaderFactory;
import twitter4j.media.MediaProvider;

public class TwitterHelper {
	public static TwitterHelper instance = null;
	public static String CONSUMER_KEY = "1kWqaI2CpGjCWbaKj2lUA";
	public static String CONSUMER_SECRET = "P4BebXAF8r90ojpD7IVnGofMXOX9UQte0aE93xreIMs";
	public static final String TWITPIC_API_KEY = "c4a6ebaf27cbbd49ae3f78e778e6799e";

	public static final String TOKEN = "TOKEN";
	public static final String TOKEN_SECRET = "TOKEN_SECRET";
	
	Twitter tw = null;
	RequestToken requestToken = null;
	AccessToken accessToken = null;
	
	public static final int STATUS_INITIAL = 0;
	public static final int STATUS_START = 1;
	public static final int STATUS_AUTHORIZED = 2;
	int status = STATUS_INITIAL;
	
	SharedPreferences pref = null;
	
	public static TwitterHelper getInstance(Context context){
		if(instance == null)
			instance = new TwitterHelper(context);
		return instance;
	}
	
	private TwitterHelper(Context context) {
		pref = context.getSharedPreferences(TwitterHelper.class.toString(), Context.MODE_PRIVATE);
		String token = pref.getString(TOKEN, null);
		String tokenSecret = pref.getString(TOKEN_SECRET, null);
		if(token != null & tokenSecret != null){
			accessToken = new AccessToken(token, tokenSecret);
			tw = new TwitterFactory().getOAuthAuthorizedInstance(CONSUMER_KEY, CONSUMER_SECRET, accessToken);
			status = STATUS_AUTHORIZED;
		}else{
			status = STATUS_INITIAL;
		}
	}
	
	public void clearAuth(){
		accessToken = null;
		status = STATUS_INITIAL;
		tw = null;
		Editor edit = pref.edit();
		edit.remove(TOKEN);
		edit.remove(TOKEN_SECRET);
		edit.commit();
	}
	
	public void storeToken(){
		Editor edit = pref.edit();
		edit.putString(TOKEN, accessToken.getToken());
		edit.putString(TOKEN_SECRET, accessToken.getTokenSecret());
		edit.commit();
	}
	
	public boolean needAuthorize(){
		return status != STATUS_AUTHORIZED;
	}
	
	public boolean startAuthorization(Context context){
		tw = new TwitterFactory().getInstance();
		tw.setOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
		try {
			requestToken = tw.getOAuthRequestToken();
		} catch (TwitterException e) {
			e.printStackTrace();
			return false;
		}
		Uri uri = Uri.parse(requestToken.getAuthenticationURL());
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
		status = STATUS_START;
		return true;
	}
	
	public boolean verifyPIN(Context context, String pin){
		if(status != STATUS_START) return false;
		try {
			if (pin.length() > 0) {
				accessToken = tw.getOAuthAccessToken(requestToken, pin);
			} else {
				accessToken = tw.getOAuthAccessToken();
			}
		} catch (TwitterException te) {
			if (401 == te.getStatusCode()) {
				Toast.makeText(context, "ERROR : Unable to get the access token.", Toast.LENGTH_SHORT).show();
			} else {
				te.printStackTrace();
			}
			return false;
		}
		if(accessToken != null){
			storeToken();
			status = STATUS_AUTHORIZED;
			return true;
		}else{
			return false;
		}
	}
	
	public String getUserName(){
		if(STATUS_AUTHORIZED == status)
			try {
				return tw.getScreenName();
			} catch (IllegalStateException e) {
				e.printStackTrace();
				return null;
			} catch (TwitterException e) {
				e.printStackTrace();
				return null;
			}
		return null;
	}

	public String postImage(File image, String msg) {
		if(status == STATUS_AUTHORIZED){
			if(!image.exists()){
				debug("postImage : Image not extists : " + image.toString());
				return null;
			}
			Configuration conf = new ConfigurationBuilder()
					.setMediaProviderAPIKey(TWITPIC_API_KEY)
					.setOAuthConsumerKey(CONSUMER_KEY)
					.setOAuthConsumerSecret(CONSUMER_SECRET)
					.setOAuthAccessToken(accessToken.getToken())
					.setOAuthAccessTokenSecret(accessToken.getTokenSecret())
					.build();
			ImageUpload upload = new ImageUploaderFactory(conf)
					.getInstance(MediaProvider.TWITPIC);
			String url = null;
			try {
				url = upload.upload(image, msg);
//				System.out.println("Successfully uploaded image to twitpic at " + url);
			} catch (TwitterException e) {
				e.printStackTrace();
				return null;
			}
			return url;
		}else{
			debug("postImage : Status NG");
			return null;
		}
	}

	public boolean tweet(String str){
		if(status == STATUS_AUTHORIZED)
			try {
				tw.updateStatus(str);
				return true;
			} catch (TwitterException e) {
				e.printStackTrace();
			}
		return false;
	}
	
	public int getStatus() {
		return status;
	}
	
	public static void debug(String str){
		Log.d("debug", str);
	}
	
	public String getRequestToken(){
		return requestToken==null?null:requestToken.getToken();
	}
	public String getRequestTokenSecret(){
		return requestToken==null?null:requestToken.getTokenSecret();
	}
}
