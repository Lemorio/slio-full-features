package org.telegram.ui;

/**
 * Chromecast (Google Cast SDK) support has been removed from this build.
 * This class is now a no-op stub kept only so PhotoViewer / MediaController
 * (which call CastSync.isActive()/setPlaying()/syncPosition()/etc. to keep
 * a cast session's playback in sync with local playback) keep compiling
 * unchanged. isActive() always returns false, so nothing in those callers
 * ever treats a cast session as active and no cast-sync network traffic
 * happens.
 */
public class CastSync {

    public static final int TYPE_PHOTOVIEWER = 0;
    public static final int TYPE_MUSIC = 1;

    public static int type;

    public static void check(int type) {
        CastSync.type = type;
    }

    public static void stop() {
    }

    public static boolean isActive() {
        return false;
    }

    public static long getPosition() {
        return -1;
    }

    public static void seekTo(long position) {
    }

    public static void syncPosition(long position) {
    }

    public static void setVolume(float volume) {
    }

    public static float getVolume() {
        return 0.5f;
    }

    public static boolean isPlaying() {
        return false;
    }

    public static void setPlaying(boolean play) {
    }

    public static void setSpeed(float speed) {
    }

    public static boolean isUpdatePending() {
        return false;
    }

    public static float getSpeed() {
        return 1.0f;
    }

    public static void syncInterface() {
    }

    public static float getDeviceVolume() {
        return 0.5f;
    }
}
