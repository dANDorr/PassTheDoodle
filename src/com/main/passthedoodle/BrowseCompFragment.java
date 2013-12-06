package com.main.passthedoodle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
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

    public class BrowseCompFragment extends ListFragment {

        // Progress Dialog
        private ProgressDialog pDialog;

        // Creating JSON Parser object
        JSONParser jParser = new JSONParser();

        ArrayList<HashMap<String, String>> gamesList;

        // url to get all games list
        private static String url_list_games = "http://passthedoodle.com/test/list_comp_games.php";

        // JSON Node names
        private static final String TAG_SUCCESS = "success";
        private static final String TAG_GAMES = "games";
        private static final String TAG_ID = "id";
        private static final String TAG_NAME = "name";
        private static final String TAG_ICON = "icon";

        // games JSONArray
        JSONArray games = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_browsecomp, container, false);
            
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
                                refreshList();
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
                public void onItemClick(AdapterView<?> parent, View view, int position, long idd) {
                    // getting values from selected ListItem
                    final String id = ((TextView) view.findViewById(R.id.id)).getText().toString().substring(5);
                    final String name = ((TextView) view.findViewById(R.id.name)).getText().toString();
                    Log.d("VALUES", "id:" + id + " name:" + name);

                    Intent in = new Intent(getActivity(), ViewCompletedActivity.class);
                    in.putExtra(TAG_NAME, name);
                    in.putExtra("option", 2);
                    in.putExtra("game_id", id);
                    startActivity(in);
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
        
        public void refreshList() {
            gamesList.clear();
            new LoadAllGames().execute();
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
                            String id = "Game " + c.getString(TAG_ID);
                            String name = c.getString(TAG_NAME);
                            
                            String icon = Integer.toString(R.drawable.ptd_logo);
                            /*if (!filename.equals("null")) icon = Integer.toString(R.drawable.icon_text);
                            else if (!description.equals("null")) icon = Integer.toString(R.drawable.icon_pencil);
                            else icon = Integer.toString(R.drawable.icon_x);*/

                            // creating new HashMap
                            HashMap<String, String> map = new HashMap<String, String>();

                            // adding each child node to HashMap key => value
                            map.put(TAG_ID, id);
                            map.put(TAG_NAME, name);
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
                        ListAdapter adapter = new SimpleAdapter(
                                getActivity(), gamesList,
                                R.layout.item_layout, new String[] { TAG_ID, TAG_NAME, TAG_ICON },
                                new int[] { R.id.id, R.id.name, R.id.icon });
                        // updating listview
                        setListAdapter(adapter);
                    }
                });
            }
        }
    }