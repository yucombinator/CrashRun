package com.gamejam.crashrun;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesUtil;

public class LegalNoticesActivity extends ActionBarActivity {
  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_legal);

    TextView legal=(TextView)findViewById(R.id.legal);
    legal.setText("By Steven Dahdah and Yu Chen Hou. \n Concordia Global Gamejam 2013 \n \n");
    legal.append(GooglePlayServicesUtil.getOpenSourceSoftwareLicenseInfo(this));

    getSupportActionBar().setHomeButtonEnabled(true);
    ActionBar actionBar = getSupportActionBar();
    actionBar.setDisplayHomeAsUpEnabled(true);
  }
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
	  switch (item.getItemId()) {

	  case android.R.id.home:
		  // app icon in action bar clicked; go home
		  finish();
		  return true;

	  default:
		  return super.onOptionsItemSelected(item);
	  }
  }
}