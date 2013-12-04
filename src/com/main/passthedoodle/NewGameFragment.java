package com.main.passthedoodle;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class NewGameFragment extends Fragment {

	// Progress Dialog
	private ProgressDialog pDialog;

	JSONParser jsonParser = new JSONParser();
	EditText inputName;
	SeekBar inputRounds;
	EditText inputDesc;
	TextView roundsLabel;
	
	// Since SeekBar has no min. value attribute (it always starts at 0)
	// we add MIN_ROUNDS to every call to SeekBar's value.
	// Max is 17 + 3 = 20
	final int MIN_ROUNDS = 3;
	
	boolean selectedLocal;

	// url to create new game
	private static String url_create_game = "http://passthedoodle.com/test/create_game.php";

	// JSON Node names
	//private static final String TAG_SUCCESS = "success";

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_newgame, container, false);
		return rootView;
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		// Edit Text
		inputName = (EditText) getView().findViewById(R.id.inputName);
		inputDesc = (EditText) getView().findViewById(R.id.inputDesc);

		// Rounds TextView that updates as you move slider
		roundsLabel = (TextView) getView().findViewById(R.id.asdf);
		roundsLabel.setText("Rounds\t" + MIN_ROUNDS);
		inputRounds = (SeekBar) getView().findViewById(R.id.inputRounds);		
		inputRounds.setOnSeekBarChangeListener(new roundsListener());
		
		// Create button
		Button btnCreateGame = (Button) getView().findViewById(R.id.btnCreateGame);
		
		// Local play toggle
		ToggleButton tog = (ToggleButton) getView().findViewById(R.id.button_local_toggle);

		tog.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				boolean on = ((ToggleButton) view).isChecked();
			    if (on) {
			        selectedLocal = true;
			    } else {
			        selectedLocal = false;
			    }
			}
		});
		
		// button click event
		btnCreateGame.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (selectedLocal) {
					// create local game
					int roundsInt = inputRounds.getProgress() + MIN_ROUNDS;
					Intent intent = new Intent(getActivity(), DrawingActivity.class);
	                intent.putExtra("isLocal", true);
	                LocalPlayHandler lph = LocalPlayHandler.getInstance();
	                lph.startGame(getActivity(), roundsInt);
	                startActivity(intent);
				} else {
					// creating new game in background thread
					new CreateNewGame().execute();
				}
			}
		});
	}

	/**
	 * Background Async Task to Create new Game
	 * */
	class CreateNewGame extends AsyncTask<String, String, Integer> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Creating Game..");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/**
		 * Creating game
		 * */
		protected Integer doInBackground(String... args) {
			String name = inputName.getText().toString();
			int roundsInt = inputRounds.getProgress() + MIN_ROUNDS;
			String rounds = "" + roundsInt;
			String description = inputDesc.getText().toString();
			String output = null;
            int responseCode = 0;
            
            Log.d("post", "name:"+name+" rounds:"+rounds+" description:"+description);

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			SharedPreferences pref = getActivity().getApplicationContext().getSharedPreferences("ptd", 0);
            String session = pref.getString("session", "0");
            params.add(new BasicNameValuePair("PHPSESSID", session));
			params.add(new BasicNameValuePair("name", name));
			params.add(new BasicNameValuePair("rounds", rounds));
			params.add(new BasicNameValuePair("description", description));
			
			try {
                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost(url_create_game);
                httppost.setEntity(new UrlEncodedFormEntity(params));
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

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(Integer headerCode) {
			// dismiss the dialog once done
			pDialog.dismiss();
			if (headerCode == 202) 
			    Toast.makeText(getActivity().getApplicationContext(), "Game successfully created.", Toast.LENGTH_LONG).show();
			else if (headerCode == 401)
			    Toast.makeText(getActivity().getApplicationContext(), "Error. Game not created.", Toast.LENGTH_LONG).show();
			else Toast.makeText(getActivity().getApplicationContext(), "Unknown error. Game not created.", Toast.LENGTH_LONG).show();
		}
	}

	private class roundsListener implements SeekBar.OnSeekBarChangeListener {

        public void onProgressChanged(SeekBar seekBar, int progress,
                boolean fromUser) {
        	int rounds = progress + MIN_ROUNDS;
                            // Log the progress
            Log.d("DEBUG", "Progress is: " + rounds);
                            //set textView's text
            roundsLabel.setText("Rounds\t" + rounds);
        }

        public void onStartTrackingTouch(SeekBar seekBar) {}

        public void onStopTrackingTouch(SeekBar seekBar) {}
    }
}
