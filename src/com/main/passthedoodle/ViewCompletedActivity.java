package com.main.passthedoodle;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.utils.StorageUtils;

public class ViewCompletedActivity extends FragmentActivity {
	static ArrayList<RoundInfo> stringsList;
	ViewPager mViewPager;
	ViewCompletedPagerAdapter mViewCompletedPagerAdapter;
	ImageLoader imageLoader;
	DisplayImageOptions options;
	
	// Progress Dialog
    private ProgressDialog pDialog;
    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    //ArrayList<HashMap<String, String>> gamesList;
    // url to get all games list
    private static String url_list_games = "http://passthedoodle.com/test/get_comp_game.php";
    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_GAME = "game";
    //private static final String TAG_USERNAME = "username";
    //private static final String TAG_SEQ = "sequence";
    private static final String TAG_DESC = "description";
    private static final String TAG_FILENAME = "filename";
    // games JSONArray
    JSONArray game = null;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Configure Android-Universal-Image-Loader which will handle image caching
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
        imageLoader = ImageLoader.getInstance();
        imageLoader.init(ilConfig);

        
        // Stores strings that will be passed to each round fragment
        // Size of the list = # of drawings in the game = # fragments
        stringsList = new ArrayList<RoundInfo>();
        
        if (getIntent().getBooleanExtra("isLocal", false)) { // local
        	LocalPlayHandler lph = LocalPlayHandler.getInstance();
        	stringsList = lph.gameRecord;
        }
        
        else if (getIntent().getIntExtra("option", 0) == 0) //sample
            	buildTest();
        	
        else if (getIntent().getIntExtra("option", 0) == 2) { // servers
            // TODO: do non-local stuff
            // - show completed game list from online db (similar to BrowseFragment).
            // - upon selection retrieve images and description of game from online db.
            // - implement it in mViewPager
            
            // game_id is set to 100 for now from MainActivity
            String game_id = getIntent().getStringExtra("game_id");
            new LoadCompGame().execute(game_id);
            // looping through All Games
            int gameLength = game.length();
            for (int i = 0; i < gameLength-1; i+=2) {
                String url;
                String prompt;
                String desc;
                try {
                    url = "http://passthedoodle.com/i/" + game.getJSONObject(i+1).getString(TAG_DESC);
                    prompt = game.getJSONObject(i).getString(TAG_FILENAME);
                    desc = game.getJSONObject(i+2).getString(TAG_DESC);
                    if (i <= gameLength-1)
                        stringsList.add(new RoundInfo(url, prompt, desc));
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
            /* Implement method to retrieve from db
             * for every drawing of gameID
                    RoundInfo.imageUrl <- image urls
                    RoundInfo.prompt <- description for (drawing sequence) - 1
                    RoundInfo.guess <- description for (drawing sequence) + 1 or
                        ^set as empty string if odd # total rounds and game ends on a drawing
                    add RoundInfo to stringsList
             */
        }
        
        if (!stringsList.isEmpty()) {
            setContentView(R.layout.activity_viewcompleted);
            mViewCompletedPagerAdapter = new ViewCompletedPagerAdapter(getSupportFragmentManager());
            mViewPager = (ViewPager) findViewById(R.id.completed_pager);
            mViewPager.setPageTransformer(false, new DepthPageTransformer()); // fancy animation
            mViewPager.setAdapter(mViewCompletedPagerAdapter);
            mViewPager.setOffscreenPageLimit(2);
        } else {
            Toast.makeText(this, "Game is empty.", Toast.LENGTH_LONG).show();
        }
    }

    private void buildTest() {
        stringsList.add(new RoundInfo("http://passthedoodle.com/i/26_1385351398.png", "Turtle", "Frog"));
        stringsList.add(new RoundInfo("http://passthedoodle.com/i/3z1qsrmi.png", "Frog", "Battletoad"));
        stringsList.add(new RoundInfo("http://passthedoodle.com/i/l2zcjzwvcd.png", "Battletoad", "Wilson"));
        stringsList.add(new RoundInfo("http://passthedoodle.com/i/l2z7ym63g7.png", "Wilson", "Heart"));
        stringsList.add(new RoundInfo("http://passthedoodle.com/i/4qm1aswl.png", "Heart", "Kenny"));
        stringsList.add(new RoundInfo("http://passthedoodle.com/i/l2zh5dnqxu.png", "Kenny", "The End"));
    }
    
	public static class ViewCompletedPagerAdapter extends FragmentStatePagerAdapter {

        public ViewCompletedPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
        	// fragment's tab index corresponds to its index in stringsList
        	// mod by stringsList size to achieve loop effect
        	int i = position % stringsList.size();
            Fragment frag = new ViewCompletedFragment();
            Bundle args = new Bundle();
            //args.putInt("DrawingNumber", i);
            args.putString("URL", stringsList.get(i).imageUrl);
            args.putString("Prompt", stringsList.get(i).prompt);
            args.putString("Description", stringsList.get(i).desc);
            frag.setArguments(args);

            return frag;
        }

        @Override
        public int getCount() {
        	return 999999; // arbitrarily big number
            //return stringsList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	int i = position % stringsList.size();
        	
        	String str = " \u2192 " + (i*2 + 2);
        	if (stringsList.get(i).desc.equals("")) {
        		// ended on a drawing so only one # should be shown in tab title
        		str = ""; 
        	}        	
            return (i*2 + 1) + str;
        }
    }
	
	/**
	 * Background Async Task to Load Completed Game description and images by making HTTP Request
	 * */
	class LoadCompGame extends AsyncTask<String, String, String> {

	    /**
	     * Before starting background thread Show Progress Dialog
	     * */
	    @Override
	    protected void onPreExecute() {
	        super.onPreExecute();
	        ProgressDialog pDialog = new ProgressDialog(getApplicationContext());
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
	        SharedPreferences pref = getSharedPreferences("ptd", 0);
	        String session = pref.getString("session", "0");
	        params.add(new BasicNameValuePair("PHPSESSID", session));
	        params.add(new BasicNameValuePair("game_id", getIntent().getStringExtra("game_id")));
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
	                game = json.getJSONArray(TAG_GAME);
	            } else if (success == 0) {
	                return "0";
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
	            if (file_url.equals("0")) {
	                Toast.makeText(getApplicationContext(), "Game not found.", Toast.LENGTH_LONG).show();
	            } else {
	                Toast.makeText(getApplicationContext(), "Log in or register please.", Toast.LENGTH_LONG).show();
	                Intent i = new Intent(getApplicationContext(), LoginActivity.class);
	                // Closing all previous activities
	                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	                startActivity(i);
	            }
	        }
	        //((Activity) getApplicationContext()).runOnUiThread(new Runnable() {
	        //    public void run() {
	                /**
	                 * Updating parsed JSON data into ListView
	                 * */
/*	                ListAdapter adapter = new SimpleAdapter(
	                        getApplicationContext(), gamesList,
	                        R.layout.item_layout, new String[] { TAG_ID, TAG_NAME, TAG_FILENAME, TAG_DESC, TAG_ICON },
	                        new int[] { R.id.id, R.id.name, R.id.filename, R.id.description, R.id.icon });
	                // updating listview
	                setListAdapter(adapter);
	            }
	        });*/
	    }
	}

}

class DepthPageTransformer implements ViewPager.PageTransformer {
	private static float MIN_SCALE = 0.75f;

	public void transformPage(View view, float position) {
		int pageWidth = view.getWidth();

        if (position < -1) { // [-Infinity,-1)
            // This page is way off-screen to the left.
            view.setAlpha(0);

        } else if (position <= 0) { // [-1,0]
            // Use the default slide transition when moving to the left page
            view.setAlpha(1);
            view.setTranslationX(0);
            view.setScaleX(1);
            view.setScaleY(1);

        } else if (position <= 1) { // (0,1]
            // Fade the page out.
            view.setAlpha(1 - position);

            // Counteract the default slide transition
            view.setTranslationX(-1 * view.getWidth() * position);
            //view.setTranslationX(pageWidth * -position);

            // Scale the page down (between MIN_SCALE and 1)
            float scaleFactor = MIN_SCALE
                    + (1 - MIN_SCALE) * (1 - Math.abs(position));
            view.setScaleX(scaleFactor);
            view.setScaleY(scaleFactor);

        } else { // (1,+Infinity]
            // This page is way off-screen to the right.
            view.setAlpha(0);
        }
    }
}