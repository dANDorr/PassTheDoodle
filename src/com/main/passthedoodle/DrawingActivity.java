package com.main.passthedoodle;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import yuku.ambilwarna.AmbilWarnaDialogFragment;
import yuku.ambilwarna.OnAmbilWarnaListener;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DrawingActivity extends FragmentActivity implements OnClickListener {

	//custom drawing view
	private DrawingView drawView;
	//buttons
	private ImageButton currPaint, drawBtn, colorBtn, newBtn, saveBtn;
	//sizes
	private float extraSmallBrush, smallBrush, mediumBrush, largeBrush, extraLargeBrush;

	byte[] byteArray;
	private SubmitDrawingTask mDrawingTask = null;
	
	private boolean isLocal;
	
	String promptString = "";

	//for image import
	private static final int SELECT_IMAGE = 187;
	private static final int CAMERA_REQUEST = 666;
	private Uri cameraUri;
	private boolean usedImport = false;
	
	private int pickerColor = Color.BLACK;
	private TextView promptTextView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawing);

		//get drawing view
		drawView = (DrawingView)findViewById(R.id.drawing);

		/*
		//get the palette and first color button
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		currPaint = (ImageButton)paintLayout.getChildAt(0);
		currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
		*/

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

		//color picker button
		colorBtn = (ImageButton)findViewById(R.id.color_btn);
		colorBtn.setColorFilter(pickerColor, android.graphics.PorterDuff.Mode.MULTIPLY);
		colorBtn.setOnClickListener(this);

		//new button
		newBtn = (ImageButton)findViewById(R.id.new_btn);
		newBtn.setOnClickListener(this);

		//save button
		saveBtn = (ImageButton)findViewById(R.id.save_btn);
		saveBtn.setOnClickListener(this);
		
		//import image button
        saveBtn = (ImageButton)findViewById(R.id.import_btn);
        saveBtn.setOnClickListener(this);
        
        //submit button
        saveBtn = (ImageButton)findViewById(R.id.submit_btn);
        saveBtn.setOnClickListener(this);
        
        isLocal = getIntent().getBooleanExtra("isLocal", false);
        
        setPrompt();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_menu, menu);
		return true;
	}
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId()==R.id.action_report)
		{
			ReportTask rTask = new ReportTask();
			rTask.execute(getIntent().getStringExtra("id"), getIntent().getStringExtra("cur_sequence"));
		}
		return true;
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) { 
	    super.onActivityResult(requestCode, resultCode, imageReturnedIntent); 

	    switch(requestCode) { 
	    case SELECT_IMAGE:
	        if(resultCode == RESULT_OK){
	            Uri selectedImageUri = imageReturnedIntent.getData();
	            InputStream imageStream = null;
				try {
					imageStream = getContentResolver().openInputStream(selectedImageUri);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	            Bitmap selectedImageBitmap = BitmapFactory.decodeStream(imageStream);
	            drawView.setCanvas(selectedImageBitmap);
	        }
		case CAMERA_REQUEST:
	    	if (resultCode == RESULT_OK) {
	    		Uri imageUri = null;
	    		Bitmap photoBitmap;
	    		if (imageReturnedIntent != null) {
	    			if (imageReturnedIntent.hasExtra("data")) {
	    				photoBitmap = imageReturnedIntent.getParcelableExtra("data");
	    				drawView.setCanvas(photoBitmap);
	    			}
	    		}
	    		else {
	    			//TODO do something with the image saved in specficied folder
	    			try {
						photoBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), cameraUri);
						drawView.setCanvas(photoBitmap);
						//getContentResolver().delete(cameraUri, null, null);
					} catch (FileNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
	    	}
	    	else {
	    			//(resultCode == RESULT_CANCELED) //
	            Toast.makeText(getApplicationContext(), "Cancelled",
	                    Toast.LENGTH_SHORT).show();
	        }
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
		else if(view.getId()==R.id.color_btn){
			//use chosen color

			//set erase false
			drawView.setErase(false);
			drawView.setBrushSize(drawView.getLastBrushSize());
			
			// create OnAmbilWarnaListener instance
		    // new color can be retrieved in onOk() event
		    OnAmbilWarnaListener onAmbilWarnaListener = new OnAmbilWarnaListener() {
		        @Override
		        public void onCancel(AmbilWarnaDialogFragment dialogFragment) {
		            Log.d("TAG", "onCancel()");
		        }
		        @Override
		        public void onOk(AmbilWarnaDialogFragment dialogFragment, int color) {
		            Log.d("COLORZZZZZZZZ", "onOk(). Color: " + color);

		            //MainActivity.this.mColor = color;
		            pickerColor = color;
		            drawView.setColor(color);
		            colorBtn.setColorFilter(pickerColor, android.graphics.PorterDuff.Mode.MULTIPLY);
		            //drawView.setColor("" + Color.HSVToColor(color));
		        }
		    };		    
		    // create new instance of AmbilWarnaDialogFragment and set OnAmbilWarnaListener listener to it
		    // show dialog fragment with some tag value
		    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		    AmbilWarnaDialogFragment fragment = AmbilWarnaDialogFragment.newInstance(pickerColor);
		    fragment.setOnAmbilWarnaListener(onAmbilWarnaListener);
		    fragment.show(ft, "color_picker_dialog");
		}
		else if(view.getId()==R.id.new_btn){
			//new button
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			newDialog.setTitle("New drawing");
			newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
			newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					usedImport = false;
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
		else if(view.getId()==R.id.import_btn){
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			newDialog.setTitle("Import image");
			newDialog.setMessage("Select your source (you will lose the current drawing)");
			newDialog.setPositiveButton("Gallery", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					usedImport = true;
					Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
					galleryIntent.setType("image/*");
					startActivityForResult(galleryIntent, SELECT_IMAGE);
				}
			});
			newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			newDialog.setNeutralButton("Camera", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					usedImport = true;
					Intent cameraIntent =
								new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
					File out = new File(Environment.getExternalStorageDirectory(), "ptd_cam.jpg");
					cameraUri = Uri.fromFile(out);
					cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);               
                    startActivityForResult(cameraIntent, CAMERA_REQUEST);
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

                    drawView.setDrawingCacheEnabled(true);
                    Bitmap passBitmap = drawView.getDrawingCache().copy(Bitmap.Config.ARGB_8888, false);
                    drawView.destroyDrawingCache();

                    if (isLocal) {
                        // write image to cache, pass its file path to LocalPlayHandler.
                    	// image probably gets cached twice when UIL loads the file but oh well
                        CacheWriter cw = new CacheWriter();
                        String hash = "" + passBitmap.hashCode();
                        String imagePath = cw.putBitmapInDiskCache(DrawingActivity.this, passBitmap, hash);
                        LocalPlayHandler lph = LocalPlayHandler.getInstance();
                        lph.endDrawing(imagePath);
                        
            		    Intent intent;
            		    if (lph.gameHasEnded())
            		    	intent = new Intent(DrawingActivity.this, ViewCompletedActivity.class);
            		    else
            		    	intent = new Intent(DrawingActivity.this, TextActivity.class);
            		    // tells TextActivity which image loading method to use
            		    intent.putExtra("isLocal", true); 

                        startActivity(intent);
                        finish();
                    } else {
                    	ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    	if (usedImport)
                    		passBitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                    	else
                    		passBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);                    	

                        byteArray = stream.toByteArray();
                    	// Submit picture to server
                        mDrawingTask = new SubmitDrawingTask();
                        mDrawingTask.execute();
                    }
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
	public class CacheWriter {
		public String putBitmapInDiskCache(Context cont, Bitmap drawing, String hash) {     
		    // Create a path pointing to the system-recommended cache dir for the app, with sub-dir named
		    // temp
			File cacheDir = new File(cont.getCacheDir(), "");
			cacheDir.mkdirs();
		    // Create a path in that dir for a file, named by the default hash
		    File cacheFile = new File(cacheDir, hash+".png");
		    try {
		        // Create a file at the file path, and open it for writing the output stream
		        cacheFile.createNewFile();
		        FileOutputStream fos = new FileOutputStream(cacheFile);
		        // Write the bitmap to the output stream (and thus the file) in PNG format (lossless compression)     
		        drawing.compress(CompressFormat.PNG, 100, fos);
		        // Flush and close the output stream
		        fos.flush();
		        fos.close();
		        Log.d("putBitmapInDiskCache", "wrote to " + cacheFile.getAbsolutePath());
		    } catch (Exception e) {
		        // Log anything that might go wrong with IO to file      
		        Log.e("SubmitDrawingCache", "Error when saving image to cache. ", e);
		    }
		    return "file://"+cacheFile.getAbsolutePath();
		}
	}
	
	
	private void setPrompt() {
		promptTextView = (TextView) findViewById(R.id.drawing_activity_prompt_view);

        if (isLocal) {
        	LocalPlayHandler lph = LocalPlayHandler.getInstance();
        	promptString = lph.currentText;
        }
        else { // Not local so load prompt from database
            promptString = this.getIntent().getStringExtra("description");
            try {
                if (promptString.equals(""))
                    //useless?
                    promptString = new GetPrompt().execute().get();
            } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            } catch (ExecutionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
            }
        }
        
        promptTextView.setText("You are doodling: " + promptString + "!");
	}
	
	public class GetPrompt extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... arg0) {
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            String game_id = getIntent().getStringExtra("game_id");
            int responseCode = 0;
            params.add(new BasicNameValuePair("game_id", game_id));
            String prompt = "";
            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://passthedoodle.com/test/get_desc.php");
                httppost.setEntity(new UrlEncodedFormEntity(params));
                HttpResponse response = httpclient.execute(httppost);
                
                Scanner sc = new Scanner(response.getEntity().getContent());
                prompt = sc.next();
                
                responseCode = response.getStatusLine().getStatusCode();
                Log.d("GET RESPONSE", prompt + " -- statuscode: " + responseCode);

            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection -- " + e.toString());
            }
            return prompt;
        }
    }

	public class SubmitDrawingTask extends AsyncTask<String, Void, Integer> {
	    @Override
        protected Integer doInBackground(String... path) {

            SharedPreferences pref = getApplicationContext().getSharedPreferences("ptd", 0);
            String session = pref.getString("session", "0"); //hopefully 0 never happens, but it won't pass as a session anyway
            Log.d("session_post", session);

            String output = null;
            int responseCode = 0;

            String byteArrayEnc = Base64.encodeToString(byteArray, 0);

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
            nameValuePairs.add(new BasicNameValuePair("PHPSESSID", session));
            String gameid = getIntent().getStringExtra("id");
            nameValuePairs.add(new BasicNameValuePair("game_id", gameid));
            nameValuePairs.add(new BasicNameValuePair("Image", byteArrayEnc));

            try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://passthedoodle.com/test/mdrawing.php");
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                HttpResponse response = httpclient.execute(httppost);

                // print response
                output = EntityUtils.toString(response.getEntity());
                responseCode = response.getStatusLine().getStatusCode();
                Log.d("GET RESPONSE", output + " -- statuscode: " + responseCode);

            } catch (Exception e) {
                Log.e("log_tag", "Error in http connection -- " + e.toString());
            }
            return responseCode;
	    }
	    
	    @Override
        protected void onPostExecute(Integer headerCode) {
            
            if (headerCode == 202) { //Authorized
                // TODO: Alter this to send user to the game menu
                Toast.makeText(getApplicationContext(), "Drawing submitted!", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
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
	}
	public class ReportTask extends AsyncTask<String, String, Integer>{

    	@Override
    	protected Integer doInBackground(String... arg0) {
    		HttpClient httpClient = new DefaultHttpClient();
    		HttpPost httpPost = new HttpPost("http://passthedoodle.com/test/report.php");
    		
    		SharedPreferences pref = getApplicationContext().getSharedPreferences("ptd", 0);
        	String session = pref.getString("session", "0");
    		
    		BasicNameValuePair gameBasicNameValuePair = new BasicNameValuePair("game", arg0[0]);
    		BasicNameValuePair sequenceBasicNameValuePair = new BasicNameValuePair("sequence", arg0[1]);
    		
    		List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
    		nameValuePairList.add(new BasicNameValuePair("PHPSESSID", session));
    		nameValuePairList.add(gameBasicNameValuePair);
    		nameValuePairList.add(sequenceBasicNameValuePair);
    		
    		int responseCode = 0;
    		
    		try {
            	// TODO: secure this or you're fired (https and cert)
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);
                httpPost.setEntity(urlEncodedFormEntity);
                
                // send POST data
                HttpResponse response = httpClient.execute(httpPost);
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
            
            Log.d("Report", "statusCode: " + responseCode);
    		return responseCode;
    	}
    	
    	@Override
    	protected void onPostExecute(Integer headerCode) {
    		if(headerCode == 401)
    		{
    			Toast.makeText(getApplicationContext(), "You must log in to report", Toast.LENGTH_LONG).show();
    		}else if(headerCode == 200)
    		{
    			Toast.makeText(getApplicationContext(), "Reported", Toast.LENGTH_LONG).show();
    		}
    		else if(headerCode == 403)
    		{
    			Toast.makeText(getApplicationContext(), "Your account is banned and cannot report", Toast.LENGTH_LONG).show();
    		} else if (headerCode >= 500 && headerCode <= 600) { //Server problem (better fix it!)
                Toast.makeText(getApplicationContext(), "Server error" + String.valueOf(headerCode), Toast.LENGTH_LONG).show();            
            } else { //Some other code (likely broken)
            	Toast.makeText(getApplicationContext(), "Unknown error " + String.valueOf(headerCode), Toast.LENGTH_LONG).show();
            }
    	}
	}
}
