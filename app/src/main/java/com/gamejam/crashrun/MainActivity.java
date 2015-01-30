
package com.gamejam.crashrun;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.gamejam.crashrun.ViewMapFragment.onCameraListener;
import com.google.android.gms.maps.model.LatLng;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

@EActivity
public class MainActivity
    extends ActionBarActivity
    implements onCameraListener, ActionBar.TabListener
{
	/*TODO 
	 * ADD CREDITS FOR GMAPS AND OSM
	 * TODO one point per round?
	 */
	
    static long a = 300000; //time remaining
    static long orb_value = 60000;

	public static String TAG = "BathroomFinder";
//	static ArrayList <OSMNode> allTapItem = new ArrayList<OSMNode>();
//	static ArrayList <OSMNode> allToiletItem = new ArrayList<OSMNode>();
//	static ArrayList <OSMNode> allFoodItem = new ArrayList<OSMNode>();
	
    Fragment mMapFragment;
    Fragment mListFragment;
    TextView timerText;
    TextView roundText;
    static CountDownTimer cdt;

    int rounds = 0;
	static boolean paused = false;
	private Menu _abs_menu;
	View LL ;
	static boolean DEMO = false;

    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        _abs_menu = menu;
        getMenuInflater().inflate(R.menu.activity_main, menu);
        LL = LayoutInflater.from(this).inflate(R.layout.text_counters, null);
        getSupportActionBar().setCustomView(LL);
        timerText = (TextView) LL.findViewById(R.id.textTimeronTheActionBar);
        //timerText.setText("00");
        
        roundText = (TextView) LL.findViewById(R.id.textRounds);
       // roundText.setText("00");

        getSupportActionBar().setDisplayShowCustomEnabled(true);
        
        //Workaround enable location layer on map after menu load
      	ViewMapFragment mMapFragment = (ViewMapFragment) getSupportFragmentManager().findFragmentByTag("map");
		if (mMapFragment.mMap != null) {
      	mMapFragment.mMap.setMyLocationEnabled(true);
		}
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
      if (item.getItemId() == R.id.credits) {
        startActivity(new Intent(this, LegalNoticesActivity.class));

        return(true);
      }else if(item.getItemId() == R.id.satellite){
    	ViewMapFragment mMapFragment = (ViewMapFragment) getSupportFragmentManager().findFragmentByTag("map");
  		mMapFragment.changeView(ViewMapFragment.MapType.Satellite);
    	  
      }else if(item.getItemId() == R.id.hybrid){
      	ViewMapFragment mMapFragment = (ViewMapFragment) getSupportFragmentManager().findFragmentByTag("map");
    		mMapFragment.changeView(ViewMapFragment.MapType.Hybrid);
      }else if(item.getItemId() == R.id.map_only){
      	ViewMapFragment mMapFragment = (ViewMapFragment) getSupportFragmentManager().findFragmentByTag("map");
    		mMapFragment.changeView(ViewMapFragment.MapType.Map);
      }else if(item.getItemId() == R.id.terrain){
      	ViewMapFragment mMapFragment = (ViewMapFragment) getSupportFragmentManager().findFragmentByTag("map");
    		mMapFragment.changeView(ViewMapFragment.MapType.Terrain);
      }else if(item.getItemId() == R.id.menu_quit){
        	finish();
        	
      }else if(item.getItemId() == R.id.pauseBtn){
      	if(paused == true)
      	{
      		cdt = null;
     		Countdown();
          	MenuItem arrowtoggle = _abs_menu.findItem(R.id.pauseBtn);
          	arrowtoggle.setIcon(getResources().getDrawable(R.drawable.ic_media_pause));
          	paused  = false;
          	
            roundText = (TextView) LL.findViewById(R.id.textRounds);
            
    		roundText.setText("Round " + rounds);
    		
          	ViewMapFragment mMapFragment = (ViewMapFragment) getSupportFragmentManager().findFragmentByTag("map");
          	mMapFragment.checkForNearbyItems();
    		
      	} else {
     		cdt.cancel();
          	MenuItem arrowtoggle = _abs_menu.findItem(R.id.pauseBtn);
          	arrowtoggle.setIcon(getResources().getDrawable(R.drawable.ic_media_play));
          	paused = true;
          	
            roundText = (TextView) LL.findViewById(R.id.textRounds);
            
    		roundText.setText("Game Paused");

      	}
      	
      }else if(item.getItemId() == R.id.help){
          showSimplePopUp(this.getString(R.string.help1), this.getString(R.string.help_text));
      }else if(item.getItemId() == R.id.share){
    	  Intent s = new Intent(android.content.Intent.ACTION_SEND);

          s.setType("text/plain");
          s.putExtra(Intent.EXTRA_SUBJECT, "I just ran " + rounds + " rounds in CrashCourse!");
          s.putExtra(Intent.EXTRA_TEXT, "How many can you do? Get the game at http://globalgamejam.org/2013/crashcourse");

          startActivity(Intent.createChooser(s, "Quote"));

      }else if(item.getItemId() == R.id.demo_mode){
    	  if(DEMO == false) {
    		  DEMO = true;
    		  }
    	  else{
    		  DEMO = false;
    	  }

      }else if(item.getItemId() == R.id.add_new){
        	ViewMapFragment mMapFragment = (ViewMapFragment) getSupportFragmentManager().findFragmentByTag("map");
      		mMapFragment.newRound();
      		
    		rounds = 1;
    		roundText.setText("Round " + rounds);
   		
      }
      

    
      return super.onOptionsItemSelected(item);
    }
    public void showSimplePopUp(String title, String text) {

      	 AlertDialog alertDialog = new AlertDialog.Builder(this).create();
      	 alertDialog.setTitle(title);
      	 alertDialog.setMessage(text);
       //alertDialog.setIcon(R.drawable.icon);
      	 alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
      	  
      		 public void onClick(DialogInterface dialog, int which) {
      	     // Do nothing but close the dialog
      	    }
      	   });

      	 // Remember, create doesn't show the dialog
      	alertDialog.show();
      	}
    @Override
    public void onResume(){
    	super.onResume();
    }
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        if(savedInstanceState != null){
//        	allTapItem = (ArrayList<OSMNode>) savedInstanceState.get("allTapItem");
//        	allToiletItem = (ArrayList<OSMNode>) savedInstanceState.get("allToiletItem");
//        	allFoodItem = (ArrayList<OSMNode>) savedInstanceState.get("allFoodItem");
        }
        //TODO Semi-Transparent Action Bar
       // requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);       
       // getSupportActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.ab_bg_black));
        
        setContentView(R.layout.activity_main);
    	configureActionBar();
    	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mMapFragment = getSupportFragmentManager().findFragmentByTag("map");
		if (mMapFragment == null) {
			// If not, instantiate and add it to the activity
			mMapFragment = new ViewMapFragment_();
			ft.add(R.id.containerFrag, mMapFragment, "map").commit();
		} else {
			// If it exists, simply attach it in order to show it
			ft.show(mMapFragment).commit();
		}
		//timerText = (TextView) findViewById(R.id.timerText);
    }
    
    @Override
    public void onSaveInstanceState(Bundle outState){
//    	outState.putParcelableArrayList("allTapItem", allTapItem);
//    	outState.putParcelableArrayList("allToiletItem", allToiletItem);
//    	outState.putParcelableArrayList("allFoodItem", allFoodItem);
    	super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	
    }


    private void configureActionBar() {
/*        ActionBar actionBar = getSupportActionBar();
        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        Tab tab = actionBar.newTab()
                .setText("Map") //TODO String
                .setTabListener(this);
        actionBar.addTab(tab);

        tab = actionBar.newTab()
            .setText("List") //TODO String
            .setTabListener(this);
        actionBar.addTab(tab);*/
        
    }
	@Override
	public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
		
		if(tab.getPosition() == 0){
			// Check if the fragment is already initialized
			mMapFragment = getSupportFragmentManager().findFragmentByTag("map");
			if (mMapFragment == null) {
				// If not, instantiate and add it to the activity
				mMapFragment = new ViewMapFragment_();
				ft.add(R.id.containerFrag, mMapFragment, "map");
			} else {
				// If it exists, simply attach it in order to show it
				ft.show(mMapFragment);
			}
		}
		if(tab.getPosition() == 1){
			// Check if the fragment is already initialized
			mMapFragment = getSupportFragmentManager().findFragmentByTag("list");
			if (mListFragment == null) {
				// If not, instantiate and add it to the activity
			//	mListFragment = new RestroomListFragment_();
				ft.add(R.id.containerFrag, mListFragment, "list");
			} else {
				// If it exists, simply attach it in order to show it
				ft.show(mListFragment);
			}
		}

	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
		if(tab.getPosition() == 0){
			if (mMapFragment != null) {
				// Detach the fragment, because another one is being attached
				ft.hide(mMapFragment);
			}
		}
		if(tab.getPosition() == 1){
			if (mListFragment != null) {
				// Detach the fragment, because another one is being attached
				ft.hide(mListFragment);
			}
		}
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onCameraLocationChange(LatLng loc) {
      //  setProgressBarIndeterminateVisibility(true); 
		//updatePOIs(loc);
	}

	@UiThread
	public void stopProgressbar() {
	  //  setProgressBarIndeterminateVisibility(false); 
		
	}

	@Override
	public void onMyLocationChange(Location location) {
		Log.d(TAG, "onMyLocationChange()");

	}
	
	@UiThread
	public void Countdown()
	{		
		if(cdt == null)
		{
			cdt = new CountDownTimer(a, 1000) {


				public void onTick(long millisUntilFinished) 
				{
					long s = 0;
					long m = 0;
					
					//timerText.setText("" + millisUntilFinished / 1000);
					a = millisUntilFinished;
					//Log.d(TAG, "&" + millisUntilFinished);
					m = millisUntilFinished/60000;
					s = millisUntilFinished/1000 - m * 60;
					String sec = String.valueOf(s);
					if (s == 0){
						sec = "00";
					}
					timerText.setText("" + m + ":" + sec);
					setProgressBarIndeterminateVisibility(true); 
				}
			
				public void onFinish() 
				{
					timerText.setText("Game over!");
					setProgressBarIndeterminateVisibility(false);
					
					// Get instance of Vibrator from current Context
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					 
					int dot = 300;
					int short_gap = 200;    // Length of Gap Between dots/dashes
					long[] pattern = {
					    0,  // Start immediately
					    dot, short_gap, dot, short_gap, dot
					};
					 
					// Only perform this pattern one time (-1 means "do not repeat")
					v.vibrate(pattern, -1);
					
				}
			}.start();
		}
	}

	@Override
	public void onOrbGet()
	{
		Log.d(TAG, ""+ a);
		cdt.cancel();
		cdt = null;
		a += orb_value;
		Countdown();
		// Get instance of Vibrator from current Context
		Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		 
		int dot = 300;
		int short_gap = 100;    // Length of Gap Between dots/dashes
		long[] pattern = {
		    0,  // Start immediately
		    dot, short_gap, dot
		};
		 
		// Only perform this pattern one time (-1 means "do not repeat")
		v.vibrate(pattern, -1);
	}

	@Override
	public void onNewRound() {
		// TODO Auto-generated method stub
		rounds = rounds + 1;
		Log.d(TAG, "rounds: " + rounds);
        timerText = (TextView) LL.findViewById(R.id.textTimeronTheActionBar);
        roundText = (TextView) LL.findViewById(R.id.textRounds);
        
		roundText.setText("Round " + rounds);
		timerText.setText("5:00");
  		Countdown();
	}	

}
