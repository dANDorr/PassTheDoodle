package com.main.passthedoodle;

import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;


public class TextActivity extends Activity {
	TextView promptTextView;
    ImageView drawingImageView;
    ProgressBar spinningCircle;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_text);		
		promptTextView = (TextView) findViewById(R.id.prompt_text_view);
		drawingImageView = (ImageView) findViewById(R.id.drawing_image_view);
		spinningCircle = (ProgressBar) findViewById(R.id.loading_image_circle);
		
		promptTextView.setText(getPromptString());
		new DownloadDrawingTask().execute(getImageURL());
	}
	
	private String getPromptString() {
		return "You are guessing ____'s doodle";
	}
	
	private String getImageURL() {
		return "http://i.imgur.com/t2APljR.gif";
	}

	private class DownloadDrawingTask extends AsyncTask<String, Void, Bitmap> {

		protected void onPreExecute() {
			// display spinning progress circle before image loads
			spinningCircle.setVisibility(View.VISIBLE);
            super.onPreExecute();
            
		}
        protected Bitmap doInBackground(String... urls) {
        	// params comes from the execute() call: params[0] is the url.
        	return downloadBitmap(urls[0]);
        }
        
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(Bitmap result) {
        	// hide spinning progress circle once image is finished loading
        	spinningCircle.setVisibility(View.GONE);
            drawingImageView.setImageBitmap(result);
       }
    }
	
	private static Bitmap downloadBitmap(String url) {
        final AndroidHttpClient client = AndroidHttpClient.newInstance("Android");
        final HttpGet getRequest = new HttpGet(url);
        
        try {
            HttpResponse response = client.execute(getRequest);
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                Log.w("DownloadDrawing", "Error " + statusCode
                        + " while retrieving bitmap from " + url);
                return null;
            }
 
            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    final Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    return bitmap;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (Exception e) {
            // Could provide a more explicit error message for IOException or
            // IllegalStateException
            getRequest.abort();
            Log.w("DownloadDrawing", "Error while retrieving bitmap from " + url);
        } finally {
            if (client != null) {
                client.close();
            }
        }
        return null;
    }
	
	public void sendText(View view) {
	    // Do something in response to button
	}
}
