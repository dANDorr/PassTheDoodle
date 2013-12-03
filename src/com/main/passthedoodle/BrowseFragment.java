package com.main.passthedoodle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

    public class BrowseFragment extends ListFragment {

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
        private static final String TAG_NAME = "name";
        private static final String TAG_FILENAME = "filename";
        private static final String TAG_DESC = "description";
        private static final String TAG_ICON = "icon";

        // games JSONArray
        JSONArray games = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    		View rootView = inflater.inflate(R.layout.fragment_browse, container, false);
    		
    		rootView.findViewById(R.id.btnAdd)
			            .setOnClickListener(new View.OnClickListener() {
			                @Override
			                public void onClick(View view) {
			                	ViewPager vp = (ViewPager) getActivity().findViewById(R.id.main_pager);
								vp.setCurrentItem(getResources().getInteger(R.integer.create_tab));
			                }
			            });
    		
    		rootView.findViewById(R.id.btnClear)
			            .setOnClickListener(new View.OnClickListener() {
			                @Override
			                public void onClick(View view) {
			                	//TODO: implement refresh games
			                }
			            });
    		
    		return rootView;
    	}
        
        @Override
        public void onActivityCreated(Bundle savedInstanceState)   {
        	super.onActivityCreated(savedInstanceState);
            
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
                    String id = ((TextView) view.findViewById(R.id.id)).getText().toString();
                    String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                    String filename = ((TextView) view.findViewById(R.id.filename)).getText().toString();
                    String description = ((TextView) view.findViewById(R.id.description)).getText().toString();

                    // Starting new intent
                    Intent in;
/*                    try {
                        /*if (Integer.parseInt(games.getJSONObject(Integer.parseInt(id)).getString(TAG_CURSEQ)) % 2 == 0) {
                            in= new Intent(getActivity(), DrawingActivity.class); ///EditGameActivity.class
                        } else {
                            in= new Intent(getActivity(), TextActivity.class); ///EditGameActivity.class
                        }//*
                        // sending id to next activity
                        in.putExtra(TAG_ID, id);
                        in.putExtra(TAG_NAME, name);
                        in.putExtra(TAG_FILENAME, filename);
                        in.putExtra(TAG_DESC, description);
                        
                        startActivity(in);
                    } catch (NumberFormatException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
*/                    
                    // starting new activity and expecting some response back
                    //startActivityForResult(in, 100);
                }
            });
        }

        // Response from Edit Game Activity
        @Override
		public void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            // if result code 100
            if (resultCode == 100) { 
                // if result code 100 is received 
                // means user edited/deleted game
                // reload this screen again
                Intent intent = getActivity().getIntent();
                getActivity().finish();
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
                pDialog = new ProgressDialog(getActivity());
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
                SharedPreferences pref = getActivity().getSharedPreferences("ptd", 0);
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
                            String id = c.getString(TAG_ID);
                            String name = c.getString(TAG_NAME);
                            String filename = c.getString(TAG_FILENAME);
                            String description = c.getString(TAG_DESC);
                            String icon;
                            if (filename != null) icon = "R.drawable.brush";
                            else if (description != null) icon = "R.drawable.new_pic";
                            else icon = "R.drawable.ptd_icon";

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(TAG_ID, id);
                            map.put(TAG_NAME, name);
                            map.put(TAG_FILENAME, filename);
                            map.put(TAG_DESC, description);
                            map.put(TAG_ICON, icon);

                            // adding HashList to ArrayList
                            gamesList.add(map);
                        }
                    } else if (success == 0) {
                        return "0";
                    } else if (success == 2) {
                        return "2";
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    System.out.print("JSON Exception occurred");
                    Toast.makeText(getActivity(), "JSON Error occurred. Try logging in again.", Toast.LENGTH_LONG).show();
                } catch (NullPointerException n) {
                    n.printStackTrace();
                    System.out.print("Null Pointer Exception occurred");
                    Toast.makeText(getActivity(), "Null Error occurred. Try logging in again.", Toast.LENGTH_LONG).show();
                } catch (RuntimeException r) {
                    r.printStackTrace();
                    System.out.print("Runtime Exception occurred");
                    Toast.makeText(getActivity(), "Runtime Error occurred. Try logging in again.", Toast.LENGTH_LONG).show();
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
                    if (file_url.equals("0")) {
                        Toast.makeText(getActivity(), "No games found. Try creating a new game", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getActivity(), "Log in or register please.", Toast.LENGTH_LONG).show();
                        Intent i = new Intent(getActivity(), LoginActivity.class);
                        // Closing all previous activities
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(i);
                    }
                }
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        /**
                         * Updating parsed JSON data into ListView
                         * */
                        String icon = "R.drawable.ptd_logo";
                        ListAdapter adapter = new SimpleAdapter(
                        		getActivity(), gamesList,
                                R.layout.item_layout, new String[] { TAG_ID, TAG_NAME, icon, TAG_FILENAME, TAG_DESC },
                                new int[] { R.id.id, R.id.name, R.id.icon, R.id.filename, R.id.description });
                        // updating listview
                        setListAdapter(adapter);
                    }
                });

            }
        }
    }