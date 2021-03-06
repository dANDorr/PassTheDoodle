package com.main.passthedoodle;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

//Activity which displays a registration screen to the user

public class RegisterActivity extends Activity {
	
	private UserRegisterTask mAuthTask = null;
	private UserLoginTask login = null;
	
	//Values for user name, password, and confirmation of password for registration attempt
	private String userName, password, confirmPassword, email, logMeIn;
	
	//UI References
	private EditText newNameView, newPasswordView, newConfirmPasswordView, newEmailView;
	private CheckBox logMeInView;
	private View registerStatusView, registerFormView;
	private TextView registerStatusMessageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_register);
        getWindow().setLayout(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        //Set up register form
        newNameView = (EditText) findViewById(R.id.new_user);
        newPasswordView = (EditText) findViewById(R.id.new_password);
        newConfirmPasswordView = (EditText) findViewById(R.id.new_confirm_password);
        newEmailView = (EditText) findViewById(R.id.new_email);
        logMeInView = (CheckBox) findViewById(R.id.logmein_checkbox);
        newEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id,
                    KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
                    attemptRegister();
                    return true;
                }
                return false;
            }
        });
        
        registerFormView = findViewById(R.id.register_form);
        registerStatusView = findViewById(R.id.register_status);
        registerStatusMessageView = (TextView) findViewById(R.id.register_status_message);
        
        findViewById(R.id.register_button).setOnClickListener(
        		new View.OnClickListener() {
        			@Override
        			public void onClick(View view) {
        				attemptRegister();
        			}
        		});
	}
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.register, menu);
        return true;
    }
	
	/*Attempts to register account as specified in forms. If form
	 * errors or inability to connect to server cause attempt to
	 * fail, user is informed.
	 */
	public void attemptRegister(){
		if (mAuthTask != null) {
            return;
        }
		
		//Reset errors
		newNameView.setError(null);
		newPasswordView.setError(null);
		newConfirmPasswordView.setError(null);
		newEmailView.setError(null);
		
		//Store values at time of registration attempt
		userName = newNameView.getText().toString();
		password = newPasswordView.getText().toString();
		confirmPassword = newConfirmPasswordView.getText().toString();
		email = newEmailView.getText().toString();
		logMeIn = "" + logMeInView.isChecked();
		
		int minimumLength = 0; //minimum length for password
		boolean cancel = false;
        View focusView = null;
        
        //check for omitted fields, password too short
        if(TextUtils.isEmpty(userName)) {
        	newNameView.setError(getString(R.string.error_field_required));
        	focusView = newNameView;
        	cancel = true;
        }
        if(TextUtils.isEmpty(password)) {
        	newPasswordView.setError(getString(R.string.error_field_required));
        	focusView = newPasswordView;
        	cancel = true;
        } else if(password.length()<minimumLength) {
        	newPasswordView.setError(getString(R.string.error_short_password));
        	focusView = newPasswordView;
        	cancel = true;
        }
        if(TextUtils.isEmpty(confirmPassword)) {
        	newConfirmPasswordView.setError(getString(R.string.error_field_required));
        	focusView = newConfirmPasswordView;
        	cancel = true;
        }
        
        //check for mismatched password
        if(!TextUtils.equals(password, confirmPassword)) {
        	newConfirmPasswordView.setError(getString(R.string.error_mismatched_password));
        	focusView = newConfirmPasswordView;
        	cancel = true;
        }
        
        if(cancel)
        {
        	// There was an error; don't attempt registration and focus the first
            // form field with an error
        	focusView.requestFocus();
        } else {
        	// Show a progress spinner, and kick off a background task to
            // perform the user registration attempt.
        	registerStatusMessageView.setText(R.string.register_progress_registering);
        	showProgress(true);
        	mAuthTask = new UserRegisterTask();
        	mAuthTask.execute(userName, password, email, logMeIn);
        }
	}
	
	/**
     * Shows the progress UI and hides the register form.
     */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            registerStatusView.setVisibility(View.VISIBLE);
            registerStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            registerStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            registerFormView.setVisibility(View.VISIBLE);
            registerFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            registerFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            registerStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            registerFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
	

    /**
     * Represents an asynchronous registration task used to authenticate
     * the user.
     */
	public class UserRegisterTask extends AsyncTask<String, Void, Integer> {
		@Override
		protected Integer doInBackground(String... arg0) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://passthedoodle.com/test/mregister.php");
			
			BasicNameValuePair usernameBasicNameValuePair = new BasicNameValuePair("username", arg0[0]);
			BasicNameValuePair passwordBasicNameValuePair = new BasicNameValuePair("password", arg0[1]);
			BasicNameValuePair emailBasicNameValuePair = new BasicNameValuePair("email", arg0[2]);
			
			List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
			nameValuePairList.add(usernameBasicNameValuePair);
			nameValuePairList.add(passwordBasicNameValuePair);
			nameValuePairList.add(emailBasicNameValuePair);
			
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
            
            Log.d("Register", "statusCode: " + responseCode);
            
            if (arg0[3].equals("true")) {
                login = new UserLoginTask();
                login.execute(arg0[0], arg0[1]);
            }
            return responseCode;
        }
		
		@Override
        protected void onPostExecute(Integer headerCode) {
            mAuthTask = null;
            showProgress(false);
            
            // We could probably be more specific here about what we tell the user
            //but this should be okay for now
            if (headerCode == 201) { //Account created
            	// TODO: Alter this to send user to the login menu
                Toast.makeText(getApplicationContext(), "Registered", Toast.LENGTH_LONG).show();
                //Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else if (headerCode == 409) { //Account already exists
                Toast.makeText(getApplicationContext(), "Username already in use.", Toast.LENGTH_LONG).show();
            } else if (headerCode == 400){//password too short
            	Toast.makeText(getApplicationContext(), "Password too short.", Toast.LENGTH_LONG).show();
            } else if (headerCode >= 500 && headerCode <= 600) { //Server problem (better fix it!)
                Toast.makeText(getApplicationContext(), "Server error" + String.valueOf(headerCode), Toast.LENGTH_LONG).show();            
            } else { //Some other code (likely broken)
            	Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_LONG).show();
            }
        }
		@Override
	    protected void onCancelled() {
			mAuthTask = null;
	        showProgress(false);
	    }
	}
	
	/**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<String, Void, Integer> {
        @Override
        protected Integer doInBackground(String... arg0) {
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost("http://passthedoodle.com/test/mlogin.php");

            BasicNameValuePair usernameBasicNameValuePair = new BasicNameValuePair("username", arg0[0]);
            BasicNameValuePair passwordBasicNameValuePAir = new BasicNameValuePair("password", arg0[1]);
            
            List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
            nameValuePairList.add(usernameBasicNameValuePair);
            nameValuePairList.add(passwordBasicNameValuePAir);
            
            int responseCode = 0;
            
            try {
                // TODO: secure this or you're fired (https and cert)
                UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);
                httpPost.setEntity(urlEncodedFormEntity);
                
                // send POST data
                HttpResponse response = httpClient.execute(httpPost);
                
                // get stuff back
                responseCode = response.getStatusLine().getStatusCode();
                // move this to onPostExecute()?
                Header[] h = response.getAllHeaders();
                for (int i = 0; i < h.length; i++) {
                    if (h[i].getName().equals("Set-Cookie") && h[i].getValue().startsWith("PHPSESSID=")) {
                        // TODO: this is a bad hack for now, replace with regex later
                        String session = h[i].getValue().substring(10,36);
                        Log.d("session", session);
                        
                        // save the session id for later
                        SharedPreferences pref = getApplicationContext().getSharedPreferences("ptd", 0);
                        Editor editor = pref.edit();
                        editor.putString("session", session);
                        editor.commit();
                    }
                }
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
            mAuthTask = null;
            showProgress(false);
            
            // We could probably be more specific here about what we tell the user
            //but this should be okay for now
            if (headerCode == 202) { //Authorized
                // TODO: Alter this to send user to the game menu
                Toast.makeText(getApplicationContext(), "Logged in!", Toast.LENGTH_LONG).show();
                //Intent intent = new Intent(getApplicationContext(), GameActivity.class);
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            } else if (headerCode == 401) { //Unauthorized
                Toast.makeText(getApplicationContext(), "Incorrect username/password!", Toast.LENGTH_LONG).show();
            } else if (headerCode >= 500 && headerCode <= 600) { //Server problem (better fix it!)
                Toast.makeText(getApplicationContext(), "Server error", Toast.LENGTH_LONG).show();            
            } else { //Some other code (likely broken)
                Toast.makeText(getApplicationContext(), "Unknown error", Toast.LENGTH_LONG).show();
            }
            
            /*
            if (success) {
                finish();
            } else {
                mPasswordView
                        .setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
            */
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

}
