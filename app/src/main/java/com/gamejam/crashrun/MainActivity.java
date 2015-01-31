
package com.gamejam.crashrun;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
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
import android.view.ViewAnimationUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.gamejam.crashrun.ViewMapFragment.onCameraListener;
import com.gamejam.crashrun.game.Game;
import com.google.android.gms.maps.model.LatLng;
import com.melnykov.fab.FloatingActionButton;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

@EActivity
public class MainActivity
    extends ActionBarActivity
    implements onCameraListener
{
	/*TODO 
	 * ADD CREDITS FOR GMAPS AND OSM
	 * TODO one point per round?
	 */
	
    public static long a = 300000; //time remaining
    static long orb_value = 60000;

    Game game;


    public static String TAG = "BathroomFinder";
	
    ViewMapFragment_ mMapFragment;
    Fragment mListFragment;
    TextView timerText;
    TextView roundText;
    public static CountDownTimer cdt;

    int rounds = 0;
	static boolean paused = true;
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
      }
      return super.onOptionsItemSelected(item);
    }
    public void gameToggle(final View v){

        if(paused)
        {
            game = new Game();
            mMapFragment.make(game);
            mMapFragment.startGame();
            cdt = null;
            Countdown();
            paused  = false;
            roundText = (TextView) LL.findViewById(R.id.textRounds);
            roundText.setText("Round " + rounds);
            ViewMapFragment mMapFragment = (ViewMapFragment) getSupportFragmentManager().findFragmentByTag("map");
            mMapFragment.checkForNearbyItems();
            ((FloatingActionButton)v).setImageDrawable(getResources().getDrawable(R.drawable.ic_action_av_pause));

            //TRANSITION ANIMATION!

            // previously visible view
            final View myView = findViewById(R.id.card_view);

            // get the center for the clipping circle
            int cx = (myView.getLeft() + myView.getRight()) / 2;
            int cy = (myView.getTop() + myView.getBottom()) / 2;

            // get the initial radius for the clipping circle
            int initialRadius = myView.getWidth();

            // create the animation (the final radius is zero)
            Animator anim =
                    ViewAnimationUtils.createCircularReveal(myView, cx, cy, initialRadius, 0);

            // make the view invisible when the animation is done
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    myView.setVisibility(View.INVISIBLE);
                }
            });

            // start the animation
            anim.start();
        } else {
            //Show confirmation dialog
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Stop game?");
            alertDialog.setMessage("You will lose all your progress!");
            //alertDialog.setIcon(R.drawable.icon);
            alertDialog.setButton(DialogInterface.BUTTON_POSITIVE,getResources().getString(android.R.string.ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    //Okay, then
                    // previously invisible view
                    View myView = findViewById(R.id.card_view);

                    // get the center for the clipping circle
                    int cx = (myView.getLeft() + myView.getRight()) / 2;
                    int cy = (myView.getTop() + myView.getBottom()) / 2;

                    // get the final radius for the clipping circle
                    int finalRadius = Math.max(myView.getWidth(), myView.getHeight());

                    // create the animator for this view (the start radius is zero)
                    Animator anim =
                            ViewAnimationUtils.createCircularReveal(myView, cx, cy, 0, finalRadius);

                    // make the view visible and start the animation
                    myView.setVisibility(View.VISIBLE);
                    anim.start();

                    cdt.cancel();
                    mMapFragment.stopGame();
                    paused = true;
                    roundText = (TextView) LL.findViewById(R.id.textRounds);
                    roundText.setText("Game Stopped");
                    ((FloatingActionButton)v).setImageDrawable(getResources().getDrawable(R.drawable.ic_action_av_play_arrow));
                }
            });
            alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE,getResources().getString(android.R.string.cancel), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    // Do nothing but close the dialog
                }
            });
            // Remember, create doesn't show the dialog
            alertDialog.show();

        }
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

    //Do not exit the game if it is in progress! Let's overwrite the back button
    @Override
    public void onBackPressed() {
        if(!paused) gameToggle(findViewById(R.id.fab));
        else super.onBackPressed();
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUND‌​S);
            getWindow().setStatusBarColor(getResources().getColor(R.color.primary_dark));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.primary));
        }

        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        if(savedInstanceState != null){
        //restore instances here
        }
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setContentView(R.layout.activity_main);
    	FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		mMapFragment = (ViewMapFragment_) getSupportFragmentManager().findFragmentByTag("map");

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
    //save instances here
    super.onSaveInstanceState(outState);
    }
    
    @Override
    public void onStop(){
    	super.onStop();
    	
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
                    game.scoreAdd(-5);
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

                    game.newGame();
                    a = 60*(game.levelAdd(0))*1000;
					
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
        game.scoreAdd(100);
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

		rounds = game.levelAdd(0);
		Log.d(TAG, "rounds: " + rounds);
        timerText = (TextView) LL.findViewById(R.id.textTimeronTheActionBar);

        roundText = (TextView) LL.findViewById(R.id.textRounds);
        
		roundText.setText("Round " + rounds);
		timerText.setText("5:00");

        a = 60*(game.levelAdd(0))*1000;
  		Countdown();
	}	

}
