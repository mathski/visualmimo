package com.android.visualmimo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * AnalyticsLogin Activity
 * Essentially just presents an IP:Port input screen and directly that information to an AnalyticsActivity once the user hits submit.
 */
public class AnalyticsLogin extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);
        ((Button) findViewById(R.id.analyt_submit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), AnalyticsActivity.class);
                intent.putExtra("serverCon", ( (EditText) findViewById(R.id.analyt_serverKey) ).getText().toString() );
                startActivity(intent);
            }
        });
    }



}
