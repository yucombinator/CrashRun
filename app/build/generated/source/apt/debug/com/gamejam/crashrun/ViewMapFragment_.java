//
// DO NOT EDIT THIS FILE, IT HAS BEEN GENERATED USING AndroidAnnotations 3.2.
//


package com.gamejam.crashrun;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.gamejam.crashrun.game.RandomPointProvider;
import com.google.android.gms.maps.model.LatLng;
import org.androidannotations.api.BackgroundExecutor;
import org.androidannotations.api.builder.FragmentBuilder;
import org.androidannotations.api.view.HasViews;
import org.androidannotations.api.view.OnViewChangedNotifier;

public final class ViewMapFragment_
    extends com.gamejam.crashrun.ViewMapFragment
    implements HasViews
{

    private final OnViewChangedNotifier onViewChangedNotifier_ = new OnViewChangedNotifier();
    private View contentView_;
    private Handler handler_ = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate(Bundle savedInstanceState) {
        OnViewChangedNotifier previousNotifier = OnViewChangedNotifier.replaceNotifier(onViewChangedNotifier_);
        init_(savedInstanceState);
        super.onCreate(savedInstanceState);
        OnViewChangedNotifier.replaceNotifier(previousNotifier);
    }

    @Override
    public View findViewById(int id) {
        if (contentView_ == null) {
            return null;
        }
        return contentView_.findViewById(id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        contentView_ = super.onCreateView(inflater, container, savedInstanceState);
        return contentView_;
    }

    @Override
    public void onDestroyView() {
        contentView_ = null;
        super.onDestroyView();
    }

    private void init_(Bundle savedInstanceState) {
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onViewChangedNotifier_.notifyViewChanged(this);
    }

    public static ViewMapFragment_.FragmentBuilder_ builder() {
        return new ViewMapFragment_.FragmentBuilder_();
    }

    @Override
    public void addPoly(final RandomPointProvider mRPP) {
        handler_.post(new Runnable() {


            @Override
            public void run() {
                ViewMapFragment_.super.addPoly(mRPP);
            }

        }
        );
    }

    @Override
    public void UiAddMarker(final com.google.android.gms.maps.model.MarkerOptions MarkerOptions) {
        handler_.post(new Runnable() {


            @Override
            public void run() {
                ViewMapFragment_.super.UiAddMarker(MarkerOptions);
            }

        }
        );
    }

    @Override
    public void UiAddOverlay(final com.google.android.gms.maps.model.GroundOverlayOptions GroundOverlayOptions) {
        handler_.post(new Runnable() {


            @Override
            public void run() {
                ViewMapFragment_.super.UiAddOverlay(GroundOverlayOptions);
            }

        }
        );
    }

    @Override
    public void updateLocation(final LatLng location) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {


            @Override
            public void execute() {
                try {
                    ViewMapFragment_.super.updateLocation(location);
                } catch (Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

        }
        );
    }

    @Override
    public void addMarker(final LatLng Node, final int type) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {


            @Override
            public void execute() {
                try {
                    ViewMapFragment_.super.addMarker(Node, type);
                } catch (Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

        }
        );
    }

    @Override
    public void generatePoint(final LatLng location) {
        BackgroundExecutor.execute(new BackgroundExecutor.Task("", 0, "") {


            @Override
            public void execute() {
                try {
                    ViewMapFragment_.super.generatePoint(location);
                } catch (Throwable e) {
                    Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                }
            }

        }
        );
    }

    public static class FragmentBuilder_
        extends FragmentBuilder<ViewMapFragment_.FragmentBuilder_, com.gamejam.crashrun.ViewMapFragment>
    {


        @Override
        public com.gamejam.crashrun.ViewMapFragment build() {
            ViewMapFragment_ fragment_ = new ViewMapFragment_();
            fragment_.setArguments(args);
            return fragment_;
        }

    }

}
