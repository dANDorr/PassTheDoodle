package com.main.passthedoodle;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

public class NewGameActivity extends Activity {

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

	// url to create new game
	private static String url_create_game = "http://passthedoodle.com/test/create_game.php";

	// JSON Node names
	private static final String TAG_SUCCESS = "success";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_newgame);

		// Edit Text
		inputName = (EditText) findViewById(R.id.inputName);
		inputDesc = (EditText) findViewById(R.id.inputDesc);

		// Rounds TextView that updates as you move slider
		roundsLabel = (TextView) findViewById(R.id.asdf);
		roundsLabel.setText("Rounds\t" + MIN_ROUNDS);
		inputRounds = (SeekBar) findViewById(R.id.inputRounds);		
		inputRounds.setOnSeekBarChangeListener(new roundsListener());
		
		// Create button
		Button btnCreateGame = (Button) findViewById(R.id.btnCreateGame);

		// button click event
		btnCreateGame.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View view) {
				// creating new game in background thread
				new CreateNewGame().execute();
			}
		});
	}

	/**
	 * Background Async Task to Create new Game
	 * */
	class CreateNewGame extends AsyncTask<String, String, String> {

		/**
		 * Before starting background thread Show Progress Dialog
		 * */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			pDialog = new ProgressDialog(NewGameActivity.this);
			pDialog.setMessage("Creating Game..");
			pDialog.setIndeterminate(false);
			pDialog.setCancelable(true);
			pDialog.show();
		}

		/**
		 * Creating game
		 * */
		protected String doInBackground(String... args) {
			String name = inputName.getText().toString();
			int roundsInt = inputRounds.getProgress() + MIN_ROUNDS;
			String rounds = "" + roundsInt;
			String description = inputDesc.getText().toString();

			// Building Parameters
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			SharedPreferences pref = getApplicationContext().getSharedPreferences("ptd", 0);
            String session = pref.getString("session", "0");
            params.add(new BasicNameValuePair("PHPSESSID", session));
			params.add(new BasicNameValuePair("name", name));
			params.add(new BasicNameValuePair("rounds", rounds));
			params.add(new BasicNameValuePair("description", description));

			// getting JSON Object
			// Note that create game url accepts POST method
			JSONObject json = jsonParser.makeHttpRequest(url_create_game,
					"POST", params);
			
			// check log cat fro response
			Log.d("Create Response", json.toString());

			// check for success tag
			try {
				int success = json.getInt(TAG_SUCCESS);

				if (success == 1) {
					// successfully created game
					Intent i = new Intent(getApplicationContext(), MainActivity.class);
					i.putExtra("Tab", 2);
					startActivity(i);
					
					// closing this screen
					finish();
				} else {
					// failed to create game
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			return null;
		}

		/**
		 * After completing background task Dismiss the progress dialog
		 * **/
		protected void onPostExecute(String file_url) {
			// dismiss the dialog once done
			pDialog.dismiss();
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
