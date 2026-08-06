package org.telegram.messenger;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Central storage for Selio's custom (non-stock-Telegram) feature toggles.
 * Everything here defaults to the behavior Selio ships with; toggling a
 * setting off restores the stock Telegram behavior for that specific
 * feature. Backed by its own SharedPreferences file so it's independent of
 * Telegram's own settings storage.
 */
public class SelioConfig {

    private static final String PREFS_NAME = "selio_config";

    // Poin 4 — prioritize call/videochat network traffic over background downloads
    public static final String KEY_PRIORITIZE_CALL_NETWORK = "prioritizeCallNetwork";
    // Poin 5 — hide the "user joined the video chat" bubble/toast
    public static final String KEY_HIDE_JOIN_BUBBLE = "hideJoinBubble";
    // Poin 6 — default-mute other participants (local playback only) when joining a video chat
    public static final String KEY_AUTO_MUTE_ON_JOIN = "autoMuteOnJoin";
    // Poin 8 — default the "delete for everyone" checkbox to checked
    public static final String KEY_DEFAULT_DELETE_FOR_EVERYONE = "defaultDeleteForEveryone";

    private static volatile SharedPreferences preferences;

    private static SharedPreferences prefs() {
        SharedPreferences local = preferences;
        if (local == null) {
            synchronized (SelioConfig.class) {
                local = preferences;
                if (local == null) {
                    preferences = local = ApplicationLoader.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
                }
            }
        }
        return local;
    }

    private static boolean get(String key) {
        // All Selio features default to ON (matches how this build has
        // always behaved) - turning a switch off restores stock behavior.
        return prefs().getBoolean(key, true);
    }

    private static void set(String key, boolean value) {
        prefs().edit().putBoolean(key, value).apply();
    }

    public static boolean isPrioritizeCallNetworkEnabled() {
        return get(KEY_PRIORITIZE_CALL_NETWORK);
    }

    public static void setPrioritizeCallNetworkEnabled(boolean value) {
        set(KEY_PRIORITIZE_CALL_NETWORK, value);
    }

    public static boolean isHideJoinBubbleEnabled() {
        return get(KEY_HIDE_JOIN_BUBBLE);
    }

    public static void setHideJoinBubbleEnabled(boolean value) {
        set(KEY_HIDE_JOIN_BUBBLE, value);
    }

    public static boolean isAutoMuteOnJoinEnabled() {
        return get(KEY_AUTO_MUTE_ON_JOIN);
    }

    public static void setAutoMuteOnJoinEnabled(boolean value) {
        set(KEY_AUTO_MUTE_ON_JOIN, value);
    }

    // Poin 7 (new) — "Simpan Cepat" quick-save-to-Saved-Messages entry in the message context menu
    public static final String KEY_QUICK_SAVE_CONTEXT_MENU = "quickSaveContextMenu";

    public static boolean isQuickSaveContextMenuEnabled() {
        return get(KEY_QUICK_SAVE_CONTEXT_MENU);
    }

    public static void setQuickSaveContextMenuEnabled(boolean value) {
        set(KEY_QUICK_SAVE_CONTEXT_MENU, value);
    }

    // 3 icon buttons next to messages: Share, Save to Saved Messages, Translate.
    // Share/Save only show on messages with media; Translate shows on all messages.
    public static final String KEY_MSG_SHARE_BUTTON = "msgShareButton";
    public static final String KEY_MSG_SAVE_BUTTON = "msgSaveButton";
    public static final String KEY_MSG_TRANSLATE_BUTTON = "msgTranslateButton";

    public static boolean isMessageShareButtonEnabled() {
        return get(KEY_MSG_SHARE_BUTTON);
    }

    public static void setMessageShareButtonEnabled(boolean value) {
        set(KEY_MSG_SHARE_BUTTON, value);
    }

    public static boolean isMessageSaveButtonEnabled() {
        return get(KEY_MSG_SAVE_BUTTON);
    }

    public static void setMessageSaveButtonEnabled(boolean value) {
        set(KEY_MSG_SAVE_BUTTON, value);
    }

    public static boolean isMessageTranslateButtonEnabled() {
        return get(KEY_MSG_TRANSLATE_BUTTON);
    }

    public static void setMessageTranslateButtonEnabled(boolean value) {
        set(KEY_MSG_TRANSLATE_BUTTON, value);
    }

    public static final String KEY_ACTIVE_CALL_SORT_TOP = "activeCallSortTop";

    public static boolean isActiveCallSortTopEnabled() {
        return get(KEY_ACTIVE_CALL_SORT_TOP);
    }

    public static void setActiveCallSortTopEnabled(boolean value) {
        set(KEY_ACTIVE_CALL_SORT_TOP, value);
    }

    public static boolean isDefaultDeleteForEveryoneEnabled() {
        return get(KEY_DEFAULT_DELETE_FOR_EVERYONE);
    }

    public static void setDefaultDeleteForEveryoneEnabled(boolean value) {
        set(KEY_DEFAULT_DELETE_FOR_EVERYONE, value);
    }
}
