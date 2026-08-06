package org.telegram.messenger.chromecast;

import java.io.File;
import java.util.Objects;

/**
 * Chromecast (Google Cast SDK) support has been removed from this build -
 * this class is now a no-op stub kept only so PhotoViewer/MediaController/
 * AudioPlayerAlert (which call getInstance().isCasting() /
 * setCurrentMediaAndCastIfNeeded() / setCover()) keep compiling unchanged.
 * isCasting() always returns false, so every "cast to device" code path in
 * those callers is simply never taken - there is no GMS Cast session, no
 * route selector, and no network traffic to a cast device.
 */
public class ChromecastController {

    private ChromecastController() {
    }

    public boolean isCasting() {
        return false;
    }

    public void setCurrentMediaAndCastIfNeeded(ChromecastMediaVariations newMedia) {
        // no-op: casting removed
    }

    public String setCover(File file) {
        return null;
    }

    public boolean isPlaying(ChromecastMediaVariations media) {
        return false;
    }

    public static boolean eq(ChromecastMediaVariations a, ChromecastMediaVariations b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        if (a.getVariationsCount() != b.getVariationsCount()) return false;
        for (int i = 0; i < a.getVariationsCount(); ++i) {
            if (!eq(a.getVariation(i), b.getVariation(i)))
                return false;
        }
        return true;
    }

    public static boolean eq(ChromecastMedia a, ChromecastMedia b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return (
            Objects.equals(a.mimeType, b.mimeType) &&
            Objects.equals(a.title, b.title) &&
            Objects.equals(a.subtitle, b.subtitle) &&
            Objects.equals(a.internalUri, b.internalUri) &&
            Objects.equals(a.externalPath, b.externalPath) &&
            a.width == b.width &&
            a.height == b.height
        );
    }

    /* * */

    private static volatile ChromecastController Instance = null;

    public static ChromecastController getInstance() {
        ChromecastController localInstance = Instance;
        if (localInstance == null) {
            synchronized (ChromecastController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ChromecastController();
                }
            }
        }
        return localInstance;
    }
}
