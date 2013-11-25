package com.main.passthedoodle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

    public class GameActivity extends ListActivity {

        // Progress Dialog
        private ProgressDialog pDialog;

        // Creating JSON Parser object
        JSONParser jParser = new JSONParser();

        ArrayList<HashMap<String, String>> gamesList;

        // url to get all games list
        private static String url_list_games = "http://passthedoodle.com/test/list_games.php";

        // JSON Node names
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_GAMES = "games";
        private static final String TAG_ID = "id";
        private static final String TAG_CREATOR = "creator_id";
        private static final String TAG_CURSEQ = "cur_sequence";

        // games JSONArray
        JSONArray games = null;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_game);
            
            // Hashmap for ListView
            gamesList = new ArrayList<HashMap<String, String>>();

            // Loading games in Background Thread
            new LoadAllGames().execute();

            // Get listview
            ListView lv = getListView();

            // on seleting single game
            // launching Edit Game Screen
            lv.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                        int position, long idd) {
                    // getting values from selected ListItem
                    String id = ((TextView) view.findViewById(R.id.id)).getText()
                            .toString();

                    // Starting new intent
                    Intent in = new Intent(getApplicationContext(),
                            GameActivity.class); ///EditGameActivity.class
                    // sending id to next activity
                    in.putExtra(TAG_ID, id);
                    
                    // starting new activity and expecting some response back
                    startActivityForResult(in, 100);
                }
            });
        }
        
        public void onClick_Add(View v) {
            Intent intent = new Intent(getApplicationContext(), NewGameActivity.class);
            startActivity(intent);
        }

        public void onClick_Refresh(View v) {
            //TODO: implement refresh games
        }

        // Response from Edit Game Activity
        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            // if result code 100
            if (resultCode == 100) { 
                // if result code 100 is received 
                // means user edited/deleted game
                // reload this screen again
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        }

        /**
         * Background Async Task to Load all game by making HTTP Request
         * */
        class LoadAllGames extends AsyncTask<String, String, String> {

            /**
             * Before starting background thread Show Progress Dialog
             * */
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                pDialog = new ProgressDialog(GameActivity.this);
                pDialog.setMessage("Loading games. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setCancelable(false);
                pDialog.show();
            }

            /**
             * getting All games from url
             * */
            protected String doInBackground(String... args) {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                SharedPreferences pref = getApplicationContext().getSharedPreferences("ptd", 0);
                String session = pref.getString("session", "0");
                params.add(new BasicNameValuePair("PHPSESSID", session));
                // getting JSON string from URL
                JSONObject json = jParser.makeHttpRequest(url_list_games, "POST", params);
                // Check your log cat for JSON response
                Log.d("All Games: ", json.toString());
                
                try {
                    // Checking for SUCCESS TAG
                    int success = json.getInt(TAG_SUCCESS);

                    if (success == 1) {
                        // games found
                        // Getting Array of Games
                        games = json.getJSONArray(TAG_GAMES);

                        // looping through All Games
                        for (int i = 0; i < games.length(); i++) {
                            JSONObject c = games.getJSONObject(i);

                            // Storing each json item in variable
                            String id = "Game " + c.getString(TAG_ID);
                            String creator = c.getString(TAG_CREATOR);
                            String curSequence;
                            if (Integer.parseInt(c.getString(TAG_CURSEQ)) % 2 == 0) {
                                curSequence = "Draw it!!";
                            } else {
                                curSequence = "Describe it!!";
                            }

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(TAG_ID, id);
                            map.put(TAG_CREATOR, creator);
                            map.put(TAG_CURSEQ, curSequence);

                            // adding HashList to ArrayList
                            gamesList.add(map);
                        }
                    } else if (success == 0) {
                        return "1";
                    } else if (success == 2) {
                        return "2";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.print("JSON Exception occurred");
                    Toast.makeText(getApplicationContext(), "JSON Error occurred. Try logging in again.", Toast.LENGTH_LONG).show();
                } catch (NullPointerException n) {
                    n.printStackTrace();
                    System.out.print("Null Pointer Exception occurred");
                    Toast.makeText(getApplicationContext(), "Null Error occurred. Try logging in again.", Toast.LENGTH_LONG).show();
                } catch (RuntimeException r) {
                    r.printStackTrace();
                    System.out.print("Runtime Exception occurred");
                    Toast.makeText(getApplicationContext(), "Runtime Error occurred. Try logging in again.", Toast.LENGTH_LONG).show();
                }
                return null;
            }

            /**
             * After completing background task Dismiss the progress dialog
             * **/
            protected void onPostExecute(String file_url) {
                // dismiss the dialog after getting all games
                pDialog.dismiss();
                // updating UI from Background Thread
                if (file_url != null) {
                    if (file_url.equals("1")) {
                        Toast.makeText(getApplicationContext(), "No games found. Try creating a new game", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Error. Try logging in again.", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        // Closing all previous activities
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                }
                runOnUiThread(new Runnable() {
                    public void run() {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        ListAdapter adapter = new SimpleAdapter(
                                GameActivity.this, gamesList,
                                R.layout.item_layout, new String[] { TAG_ID,
                                        TAG_CURSEQ},
                                new int[] { R.id.id, R.id.play });
                        // updating listview
                        setListAdapter(adapter);
                    }
                });

            }
        }
    }