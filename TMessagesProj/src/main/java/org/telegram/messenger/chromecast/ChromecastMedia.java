package org.telegram.messenger.chromecast;

import android.net.Uri;

/**
 * Plain data holder for a piece of media that could be handed off to a cast
 * session. Chromecast (Google Cast SDK) support has been removed from this
 * build, so this class no longer depends on any com.google.android.gms.cast
 * types - it just keeps the fields other code (VideoPlayer, PhotoViewer,
 * MediaController) already builds and reads.
 */
public class ChromecastMedia {
    public static final String IMAGE_JPEG = "image/jpeg";
    public static final String IMAGE_PNG = "image/png";
    public static final String VIDEO_MP4 = "video/mp4";
    public static final String APPLICATION_X_MPEG_URL = "application/x-mpegURL";

    public final String mimeType;
    public final String title;
    public final String subtitle;

    public final Uri internalUri;
    public final String externalPath;

    public final int width;
    public final int height;

    private ChromecastMedia(ChromecastMedia.Builder b) {
        this.mimeType = b.mimeType;
        this.title = b.title;
        this.subtitle = b.subtitle;
        this.internalUri = b.internalUri;
        this.externalPath = b.externalPath;
        this.width = b.width;
        this.height = b.height;
    }

    public String getExternalUri (String host) {
        return ChromecastFileServer.getUrlToSource(host, externalPath);
    }

    /* */

    public static class Builder {
        private final String mimeType;
        private final Uri internalUri;
        private final String externalPath;

        private int width;
        private int height;
        private String title;
        private String subtitle;

        private Builder (String mime, Uri internalUri, String externalPath) {
            this.mimeType = mime;
            this.internalUri = internalUri;
            this.externalPath = externalPath;
        }

        public static Builder fromUri (Uri internalUri, String externalPath, String mimeType) {
            return new Builder(mimeType, internalUri, externalPath);
        }

        public Builder setTitle (String title) {
            this.title = title;
            return this;
        }

        public Builder setSubtitle (String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder setSize (int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public ChromecastMedia build () {
            return new ChromecastMedia(this);
        }
    }
}
