package com.elmeradrianv.shesafe.auxiliar;

import android.os.SystemClock;
import android.view.animation.BounceInterpolator;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.database.TypeOfCrime;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

public class PinAnimation {
    public static void dropPinEffect(Marker marker) {
        // Handler allows us to repeat a code block after a specified delay
        final android.os.Handler handler = new android.os.Handler();
        final long start = SystemClock.uptimeMillis();
        final long duration = 1500;
        // Use the bounce interpolator
        final android.view.animation.Interpolator interpolator =
                new BounceInterpolator();
        // Animate marker with a bounce updating its position every 15ms
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                // Calculate t for bounce based on elapsed time
                float t = Math.max(
                        1 - interpolator.getInterpolation((float) elapsed
                                / duration), 0);
                // Set the anchor
                marker.setAnchor(0.5f, 1.0f + 14 * t);

                if (t > 0.0) {
                    // Post this event again 15ms from now.
                    handler.postDelayed(this, 15);
                } else { // done elapsing, show window
                    marker.showInfoWindow();
                }
            }
        });
    }

    public static BitmapDescriptor getNewIconWithLevelOfRisk(int levelOfRisk) {
        BitmapDescriptor icon;
        switch (levelOfRisk) {
            case TypeOfCrime.LOW_RISK:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_yellow);
                break;
            case TypeOfCrime.MEDIUM_LOW_RISK:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_orange);
                break;
            case TypeOfCrime.MEDIUM_RISK:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_red);
                break;
            case TypeOfCrime.MEDIUM_HIGH_RISK:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_blue);
                break;
            case TypeOfCrime.HIGH_RISK:
                icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_ss_marker_purple);
                break;
            default:
                icon = null;
                break;
        }
        return icon;
    }
}
