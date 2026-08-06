package org.telegram.messenger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.location.Location;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.util.Consumer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Fallback IMapsProvider used now that Google Maps (GMS) has been removed
 * from this build. There is no interactive tile-based map here - this shows
 * a static placeholder surface with a center pin and the raw coordinates.
 * Location sharing (including live location) still works at the data level
 * (coordinates are still tracked/sent/received normally) - what's gone is
 * the visual pan/zoom/tap-to-pick map surface.
 */
public class SimpleMapsProvider implements IMapsProvider {

    @Override
    public void initializeMaps(Context context) {
        // nothing to initialize - no map SDK involved
    }

    @Override
    public IMapView onCreateMapView(Context context) {
        return new SimpleMapView(context);
    }

    @Override
    public IMarkerOptions onCreateMarkerOptions() {
        return new SimpleMarkerOptions();
    }

    @Override
    public ICircleOptions onCreateCircleOptions() {
        return new SimpleCircleOptions();
    }

    @Override
    public ILatLngBoundsBuilder onCreateLatLngBoundsBuilder() {
        return new SimpleLatLngBoundsBuilder();
    }

    @Override
    public ICameraUpdate newCameraUpdateLatLng(LatLng latLng) {
        return new SimpleCameraUpdate(latLng, -1);
    }

    @Override
    public ICameraUpdate newCameraUpdateLatLngZoom(LatLng latLng, float zoom) {
        return new SimpleCameraUpdate(latLng, zoom);
    }

    @Override
    public ICameraUpdate newCameraUpdateLatLngBounds(ILatLngBounds bounds, int padding) {
        return new SimpleCameraUpdate(bounds.getCenter(), -1);
    }

    @Override
    public IMapStyleOptions loadRawResourceStyle(Context context, int resId) {
        return new SimpleMapStyleOptions();
    }

    @Override
    public String getMapsAppPackageName() {
        return "";
    }

    @Override
    public int getInstallMapsString() {
        return R.string.Loading;
    }

    /* * */

    public static class SimpleMapView implements IMapView {
        private final FrameLayout view;
        private final TextView coordsLabel;
        private final SimpleMap map = new SimpleMap();

        public SimpleMapView(Context context) {
            view = new FrameLayout(context);
            view.setBackgroundColor(0xFFE0E0E0);

            ImageView pin = new ImageView(context);
            try {
                pin.setImageResource(R.drawable.map_pin);
            } catch (Exception ignore) {}
            view.addView(pin, LayoutHelperCreate(48, 48, Gravity.CENTER));

            coordsLabel = new TextView(context);
            coordsLabel.setTextColor(Color.DKGRAY);
            coordsLabel.setTextSize(12);
            coordsLabel.setGravity(Gravity.CENTER);
            coordsLabel.setPadding(dp(context, 8), dp(context, 8), dp(context, 8), dp(context, 8));
            view.addView(coordsLabel, LayoutHelperCreate(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL));

            map.onPositionChanged = latLng -> {
                if (latLng == null) return;
                coordsLabel.setText(String.format(Locale.US, "%.5f, %.5f", latLng.latitude, latLng.longitude));
            };
        }

        private static int dp(Context context, int value) {
            return (int) (value * context.getResources().getDisplayMetrics().density);
        }

        private static FrameLayout.LayoutParams LayoutHelperCreate(int w, int h, int gravity) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                    w == FrameLayout.LayoutParams.WRAP_CONTENT ? w : dpStatic(w),
                    h == FrameLayout.LayoutParams.WRAP_CONTENT ? h : dpStatic(h));
            params.gravity = gravity;
            return params;
        }

        private static int dpStatic(int value) {
            return (int) (value * android.content.res.Resources.getSystem().getDisplayMetrics().density);
        }

        @Override
        public View getView() {
            return view;
        }

        @Override
        public void getMapAsync(Consumer<IMap> callback) {
            view.post(() -> callback.accept(map));
        }

        @Override
        public void onResume() {}

        @Override
        public void onPause() {}

        @Override
        public void onCreate(Bundle savedInstance) {}

        @Override
        public void onDestroy() {}

        @Override
        public void onLowMemory() {}

        @Override
        public void setOnDispatchTouchEventInterceptor(ITouchInterceptor touchInterceptor) {}

        @Override
        public void setOnInterceptTouchEventInterceptor(ITouchInterceptor touchInterceptor) {}

        @Override
        public void setOnLayoutListener(Runnable callback) {
            view.addOnLayoutChangeListener((v, l, t, r, b, ol, ot, or_, ob) -> {
                if (callback != null) callback.run();
            });
        }
    }

    public static class SimpleMap implements IMap {
        private CameraPosition cameraPosition = new CameraPosition(new LatLng(0, 0), 15);
        private Runnable onCameraIdleListener;
        private Consumer<Location> onMyLocationChangeListener;
        Consumer<LatLng> onPositionChanged;

        @Override
        public void setMapType(int mapType) {}

        @Override
        public void animateCamera(ICameraUpdate update) {
            animateCamera(update, null);
        }

        @Override
        public void animateCamera(ICameraUpdate update, ICancelableCallback callback) {
            applyCameraUpdate(update);
            if (callback != null) callback.onFinish();
            if (onCameraIdleListener != null) onCameraIdleListener.run();
        }

        @Override
        public void animateCamera(ICameraUpdate update, int duration, ICancelableCallback callback) {
            animateCamera(update, callback);
        }

        @Override
        public void moveCamera(ICameraUpdate update) {
            applyCameraUpdate(update);
            if (onCameraIdleListener != null) onCameraIdleListener.run();
        }

        private void applyCameraUpdate(ICameraUpdate update) {
            if (update instanceof SimpleCameraUpdate) {
                SimpleCameraUpdate u = (SimpleCameraUpdate) update;
                float zoom = u.zoom >= 0 ? u.zoom : cameraPosition.zoom;
                cameraPosition = new CameraPosition(u.target, zoom);
                if (onPositionChanged != null) onPositionChanged.accept(u.target);
            }
        }

        @Override
        public float getMaxZoomLevel() {
            return 20f;
        }

        @Override
        public float getMinZoomLevel() {
            return 2f;
        }

        @Override
        public void setMyLocationEnabled(boolean enabled) {}

        @Override
        public IUISettings getUiSettings() {
            return new SimpleUISettings();
        }

        @Override
        public void setOnCameraIdleListener(Runnable callback) {
            this.onCameraIdleListener = callback;
        }

        @Override
        public void setOnCameraMoveStartedListener(OnCameraMoveStartedListener onCameraMoveStartedListener) {}

        @Override
        public CameraPosition getCameraPosition() {
            return cameraPosition;
        }

        @Override
        public void setOnMapLoadedCallback(Runnable callback) {
            if (callback != null) callback.run();
        }

        @Override
        public IProjection getProjection() {
            return latLng -> new Point(0, 0);
        }

        @Override
        public void setPadding(int left, int top, int right, int bottom) {}

        @Override
        public void setMapStyle(IMapStyleOptions style) {}

        @Override
        public IMarker addMarker(IMarkerOptions markerOptions) {
            SimpleMarkerOptions o = (SimpleMarkerOptions) markerOptions;
            SimpleMarker marker = new SimpleMarker(o.position);
            if (o.position != null && onPositionChanged != null) {
                onPositionChanged.accept(o.position);
            }
            return marker;
        }

        @Override
        public void setOnMyLocationChangeListener(Consumer<Location> callback) {
            this.onMyLocationChangeListener = callback;
        }

        @Override
        public void setOnMarkerClickListener(OnMarkerClickListener markerClickListener) {}

        @Override
        public void setOnCameraMoveListener(Runnable callback) {}

        @Override
        public ICircle addCircle(ICircleOptions circleOptions) {
            return new SimpleCircle((SimpleCircleOptions) circleOptions);
        }
    }

    public static class SimpleUISettings implements IUISettings {
        @Override
        public void setZoomControlsEnabled(boolean enabled) {}

        @Override
        public void setMyLocationButtonEnabled(boolean enabled) {}

        @Override
        public void setCompassEnabled(boolean enabled) {}
    }

    public static class SimpleMarker implements IMarker {
        private LatLng position;
        private Object tag;

        SimpleMarker(LatLng position) {
            this.position = position;
        }

        @Override
        public Object getTag() {
            return tag;
        }

        @Override
        public void setTag(Object tag) {
            this.tag = tag;
        }

        @Override
        public LatLng getPosition() {
            return position;
        }

        @Override
        public void setPosition(LatLng latLng) {
            this.position = latLng;
        }

        @Override
        public void setRotation(int rotation) {}

        @Override
        public void setIcon(Bitmap bitmap) {}

        @Override
        public void setIcon(int resId) {}

        @Override
        public void remove() {}
    }

    public static class SimpleMarkerOptions implements IMarkerOptions {
        private LatLng position;

        @Override
        public IMarkerOptions position(LatLng latLng) {
            this.position = latLng;
            return this;
        }

        @Override
        public IMarkerOptions icon(Bitmap bitmap) {
            return this;
        }

        @Override
        public IMarkerOptions icon(int resId) {
            return this;
        }

        @Override
        public IMarkerOptions anchor(float lat, float lng) {
            return this;
        }

        @Override
        public IMarkerOptions title(String title) {
            return this;
        }

        @Override
        public IMarkerOptions snippet(String snippet) {
            return this;
        }

        @Override
        public IMarkerOptions flat(boolean flat) {
            return this;
        }
    }

    public static class SimpleCircle implements ICircle {
        private final SimpleCircleOptions options;

        SimpleCircle(SimpleCircleOptions options) {
            this.options = options;
        }

        @Override
        public void setStrokeColor(int color) {}

        @Override
        public void setFillColor(int color) {}

        @Override
        public void setRadius(double radius) {
            options.radius = radius;
        }

        @Override
        public double getRadius() {
            return options.radius;
        }

        @Override
        public void setCenter(LatLng latLng) {
            options.center = latLng;
        }

        @Override
        public void remove() {}
    }

    public static class SimpleCircleOptions implements ICircleOptions {
        private LatLng center;
        private double radius;

        @Override
        public ICircleOptions center(LatLng latLng) {
            this.center = latLng;
            return this;
        }

        @Override
        public ICircleOptions radius(double radius) {
            this.radius = radius;
            return this;
        }

        @Override
        public ICircleOptions strokeColor(int color) {
            return this;
        }

        @Override
        public ICircleOptions fillColor(int color) {
            return this;
        }

        @Override
        public ICircleOptions strokePattern(List<PatternItem> patternItems) {
            return this;
        }

        @Override
        public ICircleOptions strokeWidth(int width) {
            return this;
        }
    }

    public static class SimpleLatLngBoundsBuilder implements ILatLngBoundsBuilder {
        private final List<LatLng> points = new ArrayList<>();

        @Override
        public ILatLngBoundsBuilder include(LatLng latLng) {
            points.add(latLng);
            return this;
        }

        @Override
        public ILatLngBounds build() {
            double lat = 0, lng = 0;
            for (LatLng p : points) {
                lat += p.latitude;
                lng += p.longitude;
            }
            final LatLng center = points.isEmpty()
                    ? new LatLng(0, 0)
                    : new LatLng(lat / points.size(), lng / points.size());
            return () -> center;
        }
    }

    public static class SimpleCameraUpdate implements ICameraUpdate {
        final LatLng target;
        final float zoom;

        SimpleCameraUpdate(LatLng target, float zoom) {
            this.target = target;
            this.zoom = zoom;
        }
    }

    public static class SimpleMapStyleOptions implements IMapStyleOptions {}
}
