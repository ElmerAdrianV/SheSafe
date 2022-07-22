package com.elmeradrianv.shesafe.auxiliar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.SystemClock;
import android.view.animation.BounceInterpolator;

import com.elmeradrianv.shesafe.R;
import com.elmeradrianv.shesafe.database.TypeOfCrime;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;

public class PinAnimation {
    public static void focusTheReport(Marker marker, Context context) {
        int height = 80;
        int width = 80;
        BitmapDrawable bitmapdraw = getBitmapDrawableColorByLevelOfRisk((Integer) marker.getTag(), context);
        Bitmap bitmap = bitmapdraw.getBitmap();
        for (int i = 80; i <= 120; i++) {
            height = i;
            width = i;
            Bitmap biggerMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(biggerMarker));
        }
    }

    public static void unfocusedTheReport(Marker marker, Context context) {
        int height = 120;
        int width = 120;
        BitmapDrawable bitmapdraw = getBitmapDrawableColorByLevelOfRisk((Integer) marker.getTag(), context);
        Bitmap bitmap = bitmapdraw.getBitmap();
        for (int i = 120; i >= 80; i--) {
            height = i;
            width = i;
            Bitmap biggerMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);
            marker.setIcon(BitmapDescriptorFactory.fromBitmap(biggerMarker));
        }
    }
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

    public static BitmapDescriptor getNewIconWithLevelOfRisk(int levelOfRisk, Context context) {
        int height = 80;
        int width = 80;
        BitmapDrawable bitmapdraw = getBitmapDrawableColorByLevelOfRisk(levelOfRisk, context);
        Bitmap bitmap = bitmapdraw.getBitmap();
        Bitmap normalMarker = Bitmap.createScaledBitmap(bitmap, width, height, false);
        return BitmapDescriptorFactory.fromBitmap(normalMarker);
    }

    private static BitmapDrawable getBitmapDrawableColorByLevelOfRisk(int levelOfRisk, Context context) {
        switch (levelOfRisk) {
            case TypeOfCrime.LOW_RISK:
                return (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_ss_marker_yellow);
            case TypeOfCrime.MEDIUM_LOW_RISK:
                return (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_ss_marker_orange);
            case TypeOfCrime.MEDIUM_RISK:
                return (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_ss_marker_red);
            case TypeOfCrime.MEDIUM_HIGH_RISK:
                return (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_ss_marker_blue);
            case TypeOfCrime.HIGH_RISK:
                return (BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_ss_marker_purple);
            default:
                return null;
        }
    }
}

