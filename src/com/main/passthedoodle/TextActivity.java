package com.main.passthedoodle;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.nostra13.universalimageloader.utils.StorageUtils;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class TextActivity extends Activity {
	TextView promptTextView;
    ImageView drawingImageView;
    ProgressBar spinningCircle;
    EditText submitText;
    
    private SubmitTextTask mTextTask = null;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text);		
		submitText = (EditText) findViewById(R.id.text);
		promptTextView = (TextView) findViewById(R.id.text_activity_prompt_view);
		drawingImageView = (ImageView) findViewById(R.id.drawing_image_view);
		spinningCircle = (ProgressBar) findViewById(R.id.loading_image_circle);
		
		promptTextView.setText(getPromptString());
		
		LocalPlayHandler lph = LocalPlayHandler.getInstance();
		
		// Configure Android-Universal-Image-Loader which will handle image caching
		// might move configuration into MainActivity
        File cacheDir = StorageUtils.getCacheDirectory(this);
        DisplayImageOptions dio = new DisplayImageOptions.Builder()
									        .cacheInMemory(true)
									        .cacheOnDisc(true)
									        .build();
        ImageLoaderConfiguration ilConfig = new ImageLoaderConfiguration.Builder(this)
        										.defaultDisplayImageOptions(dio)
        										.discCache(new UnlimitedDiscCache(cacheDir))
        										.writeDebugLogs()
        										.build();
        ImageLoader imageLoader = ImageLoader.getInstance();
        imageLoader.init(ilConfig);

		// passed into displayImage method of ImageLoader so progress circle is displayed
		SimpleImageLoadingListener sill = new SimpleImageLoadingListener() {
			@Override
			public void onLoadingStarted(String imageUri, View view) {
				spinningCircle.setVisibility(View.VISIBLE);
			}

			@Override
			public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
				String message = null;
				switch (failReason.getType()) {
					case IO_ERROR:
						message = "Input/Output error";
						break;
					case DECODING_ERROR:
						message = "Image can't be decoded";
						break;
					case NETWORK_DENIED:
						message = "Downloads are denied";
						break;
					case OUT_OF_MEMORY:
						message = "Out Of Memory error";
						break;
					case UNKNOWN:
						message = "Unknown error";
						break;
				}
				Toast.makeText(TextActivity.this, message, Toast.LENGTH_SHORT).show();

				spinningCircle.setVisibility(View.GONE);
			}

			@Override
			public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
				spinningCircle.setVisibility(View.GONE);
			}
		};
		
		// which image source to use
		if (getIntent().getBooleanExtra("isLocal", false))
			imageLoader.displayImage(lph.currentImage, drawingImageView, sill);
		else {
		    String imgURL = "http://passthedoodle.com/i/" + this.getIntent().getStringExtra("filename");
		    try {
		        imageLoader.displayImage(imgURL, drawingImageView, sill);
		    } catch (Exception e) {
		        //useless?
		        imageLoader.displayImage(getImageURL(), drawingImageView, sill);
		    }
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private String getPromptString() {
		return "You are guessing the doodle";
	}
	
	private String getImageURL() {
	    try {
            return "http://passthedoodle.com/i/" + new GetImage().execute().get();
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ExecutionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
            
		return "http://i.imgur.com/t2APljR.gif";
	}
	
	public void sendText(View view) {
		// false is default (launched from main menu)
		if (getIntent().getBooleanExtra("isLocal", false)) {
		    EditText passText = (EditText) findViewById(R.id.text);
		    
		    LocalPlayHandler lph = LocalPlayHandler.getInstance();
		    lph.endText(passText.getText().toString());
		    
		    Intent intent;
		    if (lph.gameHasEnded())
		    	intent = new Intent(this, ViewCompletedActivity.class);
		    else
		    	intent = new Intent(this, DrawingActivity.class);
		    intent.putExtra("isLocal", true);
		    startActivity(intent);
		    finish();
		}
		else {			
		    mTextTask = new SubmitTextTask();
		    mTextTask.execute(submitText.getText().toString());
		}
	}
	
	public class GetImage extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... arg0) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String game_id = getIntent().getStringExtra("game_id");
            int responseCode = 0;
            params.add(new BasicNameValuePair("game_id", game_id));
            String filename = "";
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://passthedoodle.com/test/get_i.php");
                httppost.setEntity(new UrlEncodedFormEntity(params));
                HttpResponse response = httpclient.execute(httppost);
                
                Scanner sc = new Scanner(response.getEntity().getContent());
                filename = sc.next();
                
                responseCode = response.getStatusLine().getStatusCode();
                Log.d("GET RESPONSE", filename + " -- statuscode: " + responseCode);

            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection -- " + e.toString());
            }
            return filename;
        }
	}
	
    public class SubmitTextTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... arg0) {
        	SharedPreferences pref = getApplicationContext().getSharedPreferences("ptd", 0);
        	String session = pref.getString("session", "0"); //hopefully 0 never happens, but it won't pass as a session anyway
        	Log.d("session_post", session);
        	
        	HttpClient httpClient = new DefaultHttpClient();
        	HttpPost httpPost = new HttpPost("http://passthedoodle.com/test/mtext.php");
			String gameid = getIntent().getStringExtra("id");
			
			List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
			nameValuePairList.add(new BasicNameValuePair("description", arg0[0]));
			nameValuePairList.add(new BasicNameValuePair("PHPSESSID", session));
			nameValuePairList.add(new BasicNameValuePair("game_id", gameid));
			int responseCode = 0;
			
            try {
            	// TODO: secure this or you're fired (https and cert)
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);
                httpPost.setEntity(urlEncodedFormEntity);
                
                // send POST data
                HttpResponse response = httpClient.execute(httpPost);
                
                // get stuff back
                responseCode = response.getStatusLine().getStatusCode();
                
            } catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            Log.d("Status", "statusCode: " + responseCode);
            return responseCode;
        }

        @Override
        protected void onPostExecute(Integer headerCode) {
            //mAuthTask = null;
            //showProgress(false);
            
            // We could probably be more specific here about what we tell the user
            //but this should be okay for now
            if (headerCode == 202) { //Authorized
            	// TODO: Alter this to send user to the game menu
                Toast.makeText(getApplicationContext(), "Description submitted!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("Tab", 2);
                startActivity(intent);
                finish();
            } else if (headerCode == 401) { //Unauthorized
                Toast.makeText(getApplicationContext(), "Incorrect username/password!", Toast.LENGTH_LONG).show();
            } else if (headerCode >= 500 && headerCode <= 600) { //Server problem (better fix it!)
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_LONG).show();            
            } else { //Some other code (likely broken)
            	Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_LONG).show();
            }
        }

        @Override
        protected void onCancelled() {
            //mAuthTask = null;
            //showProgress(false);
        }
    }
}
