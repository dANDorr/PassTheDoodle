
package com.main.passthedoodle;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.os.AsyncTask;

public class ChangeEmailActivity extends Activity {
	private UserChangeEmailTask mAuthTask = null;
	
	//Values for username, password, and new email
	private String userName, password, newEmail;
	
	//UI references
	private EditText userNameView, passwordView, newEmailView;
	private View changeEmailStatusView, changeEmailFormView;
	private TextView changeEmailStatusMessageView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_change_email);
		
		//Set up email change form
		userNameView = (EditText) findViewById(R.id.change_email_username);
		passwordView = (EditText) findViewById(R.id.change_email_password);
		newEmailView = (EditText) findViewById(R.id.new_email);
		newEmailView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id,
                    KeyEvent keyEvent) {
                if (id == R.id.register || id == EditorInfo.IME_NULL) {
                    attemptEmailChange();
                    return true;
                }
                return false;
            }
        });
		
		changeEmailFormView = findViewById(R.id.change_email_form);
		changeEmailStatusView = findViewById(R.id.change_email_status);
		changeEmailStatusMessageView = (TextView) findViewById(R.id.change_email_status_message);
		
		findViewById(R.id.change_email_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptEmailChange();
					}
				});
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.email, menu);
		return true;
	}
	
	public void attemptEmailChange(){
		if (mAuthTask != null) {
			return;
		}
		
		//Reset errors
		userNameView.setError(null);
		passwordView.setError(null);
		newEmailView.setError(null);
		
		//Store values at time of email change attempt
		userName = userNameView.getText().toString();
		password = passwordView.getText().toString();
		newEmail = newEmailView.getText().toString();
		
		boolean cancel = false;
		View focusView = null;
		
		//Check for omitted fields
		if(TextUtils.isEmpty(userName)) {
			userNameView.setError(getString(R.string.error_field_required));
			focusView = userNameView;
			cancel = true;
		}
		if(TextUtils.isEmpty(password)) {
			passwordView.setError(getString(R.string.error_field_required));
			focusView = passwordView;
			cancel = true;
		}
		if(TextUtils.isEmpty(newEmail)) {
			newEmailView.setError(getString(R.string.error_field_required));
			focusView = newEmailView;
			cancel = true;
		}
		
		if(cancel)
		{
			//Error; don't attempt change, inform user
			focusView.requestFocus();
		} else {
			//Show progress spinner, start background task to perform change
			changeEmailStatusMessageView.setText(R.string.email_status_changing);
			showProgress(true);
			mAuthTask = new UserChangeEmailTask();
			mAuthTask.execute(userName, password, newEmail);
		}
	}
	/**
     * Shows the progress UI and hides the email change form.
     */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(
                    android.R.integer.config_shortAnimTime);

            changeEmailStatusView.setVisibility(View.VISIBLE);
            changeEmailStatusView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 1 : 0)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            changeEmailStatusView.setVisibility(show ? View.VISIBLE
                                    : View.GONE);
                        }
                    });

            changeEmailFormView.setVisibility(View.VISIBLE);
            changeEmailFormView.animate().setDuration(shortAnimTime)
                    .alpha(show ? 0 : 1)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            changeEmailFormView.setVisibility(show ? View.GONE
                                    : View.VISIBLE);
                        }
                    });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            changeEmailStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
            changeEmailFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
	
	// Asynchronous task to authenticate user and change email
	public class UserChangeEmailTask extends AsyncTask<String, Void, Integer> {
		@Override
		protected Integer doInBackground(String...arg0) {
			HttpClient httpClient = new DefaultHttpClient();
			HttpPost httpPost = new HttpPost("http://passthedoodle.com/test/changeEmail.php");
			
			BasicNameValuePair usernameBasicNameValuePair = new BasicNameValuePair("username", arg0[0]);
			BasicNameValuePair passwordBasicNameValuePair = new BasicNameValuePair("password", arg0[1]);
			BasicNameValuePair emailBasicNameValuePair = new BasicNameValuePair("email", arg0[2]);
			
			List<NameValuePair> nameValuePairList = new ArrayList<NameValuePair>();
			nameValuePairList.add(usernameBasicNameValuePair);
			nameValuePairList.add(passwordBasicNameValuePair);
			nameValuePairList.add(emailBasicNameValuePair);
			
			int responseCode = 0;
			
			try{
				//TODO: secure this
				UrlEncodedFormEntity urlEncodedFormEntity = new UrlEncodedFormEntity(nameValuePairList);
				httpPost.setEntity(urlEncodedFormEntity);
				
				//send POST data
				HttpResponse response = httpClient.execute(httpPost);
				responseCode = response.getStatusLine().getStatusCode();
			}catch (UnsupportedEncodingException e){
				//TODO Auto-generated catch block
				e.printStackTrace();
			}catch (ClientProtocolException e){
				//TODO Auto-generated catch block
				e.printStackTrace();
			}catch (IOException e) {
				//TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.d("Register", "statusCode: " + responseCode);
			
			return responseCode;
		}
		
		@Override
		protected void onPostExecute(Integer headerCode) {
			mAuthTask = null;
			showProgress(false);
			
			if(headerCode == 200) {//Email changed
				Toast.makeText(getApplicationContext(), "Email Changed", Toast.LENGTH_LONG).show();
				Intent intent = new Intent(getApplicationContext(), MainActivity.class);
				startActivity(intent);
				finish();
			}
			else if(headerCode == 400) {//no such user/password combination
				Toast.makeText(getApplicationContext(), "No such user", Toast.LENGTH_LONG).show();	
			}else if (headerCode >= 500 && headerCode <= 600) { //Server problem (better fix it!)
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
}
