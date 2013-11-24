package com.main.passthedoodle;

import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;

public class MainActivity extends Activity {
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
    
    public void goLogin(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
    
    public void goRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }
    
    public void goPaint(View view) {
        Intent intent = new Intent(this, DrawingActivity.class);
        intent.putExtra("isLocal", true);
        startActivity(intent);
    }
    
    public void goWrite(View view) {
        Intent intent = new Intent(this, TextActivity.class);
        startActivity(intent);
    }
    
    public void goLocalPlay(View view) {
        Intent intent = new Intent(this, DrawingActivity.class);
        intent.putExtra("isLocal", false);
        startActivity(intent);
    }
    
    public void goGame(View view) {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
