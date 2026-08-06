package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;

import androidx.core.util.Consumer;

import java.util.HashMap;
import java.util.Map;

/**
 * Location provider backed by the plain Android LocationManager API
 * (GPS_PROVIDER / NETWORK_PROVIDER), replacing the GMS FusedLocationProvider.
 * No Google Play Services dependency, works on any Android device.
 */
@SuppressLint("MissingPermission")
public class NativeLocationProvider implements ILocationServiceProvider {

    private LocationManager locationManager;
    private final Map<ILocationListener, LocationListener> activeListeners = new HashMap<>();

    @Override
    public void init(Context context) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    private String bestProvider() {
        if (locationManager == null) return null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            return LocationManager.GPS_PROVIDER;
        }
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            return LocationManager.NETWORK_PROVIDER;
        }
        return null;
    }

    @Override
    public ILocationRequest onCreateLocationRequest() {
        return new NativeLocationRequest();
    }

    @Override
    public void getLastLocation(Consumer<Location> callback) {
        if (locationManager == null) {
            callback.accept(null);
            return;
        }
        try {
            Location best = null;
            for (String provider : locationManager.getAllProviders()) {
                try {
                    Location loc = locationManager.getLastKnownLocation(provider);
                    if (loc != null && (best == null || loc.getTime() > best.getTime())) {
                        best = loc;
                    }
                } catch (Exception ignore) {}
            }
            callback.accept(best);
        } catch (Exception e) {
            FileLog.e(e);
            callback.accept(null);
        }
    }

    @Override
    public void requestLocationUpdates(ILocationRequest request, ILocationListener locationListener) {
        if (locationManager == null) return;
        String provider = bestProvider();
        if (provider == null) return;

        long interval = request instanceof NativeLocationRequest ? ((NativeLocationRequest) request).interval : 1000;

        LocationListener nativeListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                locationListener.onLocationChanged(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };
        activeListeners.put(locationListener, nativeListener);

        try {
            locationManager.requestLocationUpdates(provider, interval, 0, nativeListener, Looper.getMainLooper());
        } catch (Exception e) {
            FileLog.e(e);
        }
    }

    @Override
    public void removeLocationUpdates(ILocationListener locationListener) {
        if (locationManager == null) return;
        LocationListener nativeListener = activeListeners.remove(locationListener);
        if (nativeListener != null) {
            try {
                locationManager.removeUpdates(nativeListener);
            } catch (Exception e) {
                FileLog.e(e);
            }
        }
    }

    @Override
    public void checkLocationSettings(ILocationRequest request, Consumer<Integer> callback) {
        // No GMS "resolve settings" dialog available - just report whether a
        // usable provider (GPS or network) is currently enabled on the
        // device. If not, the caller falls back to asking the user to
        // enable location in system settings, same as before.
        boolean enabled = locationManager != null &&
                (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                 locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
        callback.accept(enabled ? STATUS_SUCCESS : STATUS_SETTINGS_CHANGE_UNAVAILABLE);
    }

    @Override
    public IMapApiClient onCreateLocationServicesAPI(Context context, IAPIConnectionCallbacks connectionCallbacks, IAPIOnConnectionFailedListener failedListener) {
        return new IMapApiClient() {
            @Override
            public void connect() {
                connectionCallbacks.onConnected(null);
            }

            @Override
            public void disconnect() {
                connectionCallbacks.onConnectionSuspended(0);
            }
        };
    }

    @Override
    public boolean checkServices() {
        return locationManager != null;
    }

    public final static class NativeLocationRequest implements ILocationRequest {
        private int priority = PRIORITY_HIGH_ACCURACY;
        private long interval = 1000;
        private long fastestInterval = 1000;

        @Override
        public void setPriority(int priority) {
            this.priority = priority;
        }

        @Override
        public void setInterval(long interval) {
            this.interval = interval;
        }

        @Override
        public void setFastestInterval(long interval) {
            this.fastestInterval = interval;
        }
    }
}
