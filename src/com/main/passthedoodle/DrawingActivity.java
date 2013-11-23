package com.main.passthedoodle;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class DrawingActivity extends Activity implements OnClickListener {

	//custom drawing view
	private DrawingView drawView;
	//buttons
	private ImageButton currPaint, drawBtn, eraseBtn, newBtn, saveBtn;
	//sizes
	private float extraSmallBrush, smallBrush, mediumBrush, largeBrush, extraLargeBrush;

	byte[] byteArray;
	private SubmitDrawingTask mDrawingTask = null;
	private boolean isLocal = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawing);

		//get drawing view
		drawView = (DrawingView)findViewById(R.id.drawing);

		//get the palette and first color button
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

		//sizes from dimensions
		extraSmallBrush = getResources().getInteger(R.integer.extra_small_size);
		smallBrush = getResources().getInteger(R.integer.small_size);
		mediumBrush = getResources().getInteger(R.integer.medium_size);
		largeBrush = getResources().getInteger(R.integer.large_size);
		extraLargeBrush = getResources().getInteger(R.integer.extra_large_size);

		//draw button
		drawBtn = (ImageButton)findViewById(R.id.draw_btn);
		drawBtn.setOnClickListener(this);

		//set initial size
		drawView.setBrushSize(mediumBrush);

		//erase button
		eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
		eraseBtn.setOnClickListener(this);

		//new button
		newBtn = (ImageButton)findViewById(R.id.new_btn);
		newBtn.setOnClickListener(this);

		//save button
		saveBtn = (ImageButton)findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);
		
		//prompt button
        saveBtn = (ImageButton)findViewById(R.id.prompt_btn);
        saveBtn.setOnClickListener(this);
        
      //save button
        saveBtn = (ImageButton)findViewById(R.id.submit_btn);
        saveBtn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//user clicked paint
	public void paintClicked(View view){
		//use chosen color

		//set erase false
		drawView.setErase(false);
		drawView.setBrushSize(drawView.getLastBrushSize());

		if(view!=currPaint){
			ImageButton imgView = (ImageButton)view;
			String color = view.getTag().toString();
			drawView.setColor(color);
			//update ui
			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			currPaint=(ImageButton)view;
		}
	}

	@Override
	public void onClick(View view){

		if(view.getId()==R.id.draw_btn){
			//draw button clicked
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Brush size:");
			brushDialog.setContentView(R.layout.brush_chooser);
			//listen for clicks on size buttons
			ImageButton extraSmallBtn = (ImageButton)brushDialog.findViewById(R.id.extra_small_brush);
			extraSmallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(false);
					drawView.setBrushSize(extraSmallBrush);
					drawView.setLastBrushSize(extraSmallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(false);
					drawView.setBrushSize(smallBrush);
					drawView.setLastBrushSize(smallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(false);
					drawView.setBrushSize(mediumBrush);
					drawView.setLastBrushSize(mediumBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(false);
					drawView.setBrushSize(largeBrush);
					drawView.setLastBrushSize(largeBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton extraLargeBtn = (ImageButton)brushDialog.findViewById(R.id.extra_large_brush);
			extraLargeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(false);
					drawView.setBrushSize(extraLargeBrush);
					drawView.setLastBrushSize(extraLargeBrush);
					brushDialog.dismiss();
				}
			});
			//show and wait for user interaction
			brushDialog.show();
		}
		else if(view.getId()==R.id.erase_btn){
			//switch to erase - choose size
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Eraser size:");
			brushDialog.setContentView(R.layout.brush_chooser);
			//size buttons
			ImageButton extraSmallBtn = (ImageButton)brushDialog.findViewById(R.id.extra_small_brush);
			extraSmallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(extraSmallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(smallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(mediumBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(largeBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton extraLargeBtn = (ImageButton)brushDialog.findViewById(R.id.extra_large_brush);
			extraLargeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					drawView.setErase(true);
					drawView.setBrushSize(extraLargeBrush);
					brushDialog.dismiss();
				}
			});
			brushDialog.show();
		}
		else if(view.getId()==R.id.new_btn){
			//new button
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			newDialog.setTitle("New drawing");
			newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
			newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					drawView.startNew();
					dialog.dismiss();
				}
			});
			newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			newDialog.show();
		}
		else if(view.getId()==R.id.save_btn){
			//save drawing
			AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
			saveDialog.setTitle("Save drawing");
			saveDialog.setMessage("Save drawing to device Gallery?");
			saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					//save drawing
					drawView.setDrawingCacheEnabled(true);
					//attempt to save
					String imgSaved = MediaStore.Images.Media.insertImage(
							getContentResolver(), drawView.getDrawingCache(),
							UUID.randomUUID().toString()+".png", "drawing");
					//feedback
					if(imgSaved!=null){
						Toast savedToast = Toast.makeText(getApplicationContext(), 
								"Drawing saved to Gallery!", Toast.LENGTH_SHORT);
						savedToast.show();
					}
					else{
						Toast unsavedToast = Toast.makeText(getApplicationContext(), 
								"Oops! Image could not be saved.", Toast.LENGTH_SHORT);
						unsavedToast.show();
					}
					drawView.destroyDrawingCache();
				}
			});
			saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			saveDialog.show();
		}
		else if(view.getId()==R.id.prompt_btn){
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("Prompt");

            // Local play
            Intent intent = getIntent();
            String promptString = intent.getStringExtra("Text");
            if (promptString != null) {
            	// prompt received text from TextActivity
            	newDialog.setMessage(promptString);
            }
            else {
            	// Starting new local game, generate word for prompt.
            	// Pass in .txt filename for the appropriate difficulty level.
            	promptString = new WordGenerator(this, "hard.txt").getWord();
            	newDialog.setMessage(promptString);            	
            }
            
            newDialog.setNeutralButton("OK", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();
                }
            });
            newDialog.show();
        }
		else if(view.getId()==R.id.submit_btn){
            //new button
            AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
            newDialog.setTitle("Submit");
            newDialog.setMessage("Submit drawing?");
            newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.dismiss();

                    // Local play - pass drawing image to TextActivity
                    Intent intent = new Intent(DrawingActivity.this, TextActivity.class);

                    drawView.setDrawingCacheEnabled(true);
                    Bitmap passBitmap = drawView.getDrawingCache().copy(Bitmap.Config.ARGB_8888, false);
                    drawView.destroyDrawingCache();

                    ByteArrayOutputStream passStream = new ByteArrayOutputStream();
                    passBitmap.compress(Bitmap.CompressFormat.PNG, 100, passStream);
                    byteArray = passStream.toByteArray();

                    // Submit picture to server
                    mDrawingTask = new SubmitDrawingTask();
                    mDrawingTask.execute();

                    // tells TextActivity which image loading method to use
                    /*intent.putExtra("isLocal", isLocal);
                    intent.putExtra("Image", byteArray);
                    startActivity(intent);
                    finish();*/
                }
            });
            newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
                public void onClick(DialogInterface dialog, int which){
                    dialog.cancel();
                }
            });
            newDialog.show();
        }
	}

	   public class SubmitDrawingTask extends AsyncTask<String, Void, String> {

	        protected String doInBackground(String... path) {

	            SharedPreferences pref = getApplicationContext().getSharedPreferences("ptd", 0);
	            String session = pref.getString("session", "0"); //hopefully 0 never happens, but it won't pass as a session anyway
	            Log.d("session_post", session);

	            String output = null;

	            String byteArrayEnc = Base64.encodeToString(byteArray, 0);

	            //System.out.println
	            Log.d("log_tag", "uploading image now -- " + byteArrayEnc);

	            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
	            nameValuePairs.add(new BasicNameValuePair("PHPSESSID", session));
	            nameValuePairs.add(new BasicNameValuePair("Image", byteArrayEnc));

	            try {
	                HttpClient httpclient = new DefaultHttpClient();
	                HttpPost httppost = new HttpPost("http://passthedoodle.com/test/mdrawing.php");
	                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

	                HttpResponse response = httpclient.execute(httppost);
	                HttpEntity entity = response.getEntity();

	                // print response
	                output = EntityUtils.toString(entity) + " (StatusCode: " + response.getStatusLine().getStatusCode() + ")";
	                Log.d("GET RESPONSE", output);

	                Log.d("log_tag", "Connection is good!");

	            } catch (Exception e) {
	                Log.e("log_tag", "Error in http connection -- " + e.toString());
	        }
	        return output;
	    }
	}
}
