package org.telegram.ui.Cells;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SelioConfig;
import org.telegram.ui.ActionBar.Theme;

import java.util.ArrayList;

/**
 * Renders up to 3 small round icon buttons (Share / Save to Saved Messages /
 * Translate) in the margin outside a message bubble, similar in spirit to
 * Telegram's own single share button but supporting several buttons at
 * once. Deliberately self-contained and NOT wired into ChatMessageCell's
 * existing drawSideButton system (which only supports one button) to avoid
 * touching that fragile, deeply-referenced code.
 *
 * Usage from ChatMessageCell:
 *  - call configure(messageObject) whenever the cell's message changes
 *  - call getButtonAreaWidth()/getButtonAreaHeight() to reserve layout space
 *  - call draw(canvas, left, top) from onDraw, in the reserved margin
 *  - call checkTouch(x, y) from onTouchEvent/dispatch, using coordinates
 *    relative to the same origin passed to draw()
 */
public class ChatMessageCellActionButtons {

    public static final int BUTTON_SHARE = 0;
    public static final int BUTTON_SAVE = 1;
    public static final int BUTTON_TRANSLATE = 2;

    private static final int BUTTON_SIZE_DP = 34;
    private static final int BUTTON_GAP_DP = 6;
    private static final int ICON_SIZE_DP = 18;

    public interface ClickListener {
        void onActionButtonClick(int button);
    }

    private final ArrayList<Integer> visibleButtons = new ArrayList<>(3);
    private final RectF[] hitRects = new RectF[3];
    private int pressedButton = -1;

    private static Drawable shareIcon, saveIcon, translateIcon;
    private Paint circlePaint;

    private ClickListener clickListener;

    public void setClickListener(ClickListener listener) {
        clickListener = listener;
    }

    /**
     * Decide which of the 3 buttons should be visible for this message,
     * based on message content (media vs text) and the Selio Settings
     * toggles. Call whenever the cell is bound to a (possibly different)
     * message.
     */
    public void configure(MessageObject messageObject) {
        visibleButtons.clear();
        if (messageObject == null) {
            return;
        }
        boolean hasMedia = hasMedia(messageObject);
        if (hasMedia && SelioConfig.isMessageShareButtonEnabled()) {
            visibleButtons.add(BUTTON_SHARE);
        }
        if (hasMedia && SelioConfig.isMessageSaveButtonEnabled()) {
            visibleButtons.add(BUTTON_SAVE);
        }
        if (SelioConfig.isMessageTranslateButtonEnabled()) {
            visibleButtons.add(BUTTON_TRANSLATE);
        }
    }

    private boolean hasMedia(MessageObject messageObject) {
        if (messageObject.isSponsored()) {
            return false;
        }
        return messageObject.isPhoto() || messageObject.isVideo() || messageObject.isGif()
            || messageObject.isVoice() || messageObject.isMusic() || messageObject.isDocument()
            || messageObject.isSticker() || messageObject.isRoundVideo();
    }

    public boolean isEmpty() {
        return visibleButtons.isEmpty();
    }

    public int getButtonCount() {
        return visibleButtons.size();
    }

    /** Width needed to fit all visible buttons stacked vertically (they share one horizontal slot, like Telegram's own side button). */
    public int getButtonAreaWidth() {
        if (visibleButtons.isEmpty()) {
            return 0;
        }
        return AndroidUtilities.dp(BUTTON_SIZE_DP);
    }

    /** Height needed to fit all visible buttons stacked vertically. */
    public int getButtonAreaHeight() {
        if (visibleButtons.isEmpty()) {
            return 0;
        }
        int size = AndroidUtilities.dp(BUTTON_SIZE_DP);
        int gap = AndroidUtilities.dp(BUTTON_GAP_DP);
        return visibleButtons.size() * size + (visibleButtons.size() - 1) * gap;
    }

    /**
     * Draws the visible buttons vertically stacked, top-left corner of the
     * whole stack anchored at (left, top) in the cell's own coordinate
     * space. Also updates the hit-test rects used by checkTouch().
     */
    public void draw(Canvas canvas, float left, float top) {
        if (visibleButtons.isEmpty()) {
            return;
        }
        ensureAssets();
        int size = AndroidUtilities.dp(BUTTON_SIZE_DP);
        int gap = AndroidUtilities.dp(BUTTON_GAP_DP);
        int iconSize = AndroidUtilities.dp(ICON_SIZE_DP);
        int iconInset = (size - iconSize) / 2;

        for (int i = 0; i < visibleButtons.size(); i++) {
            int button = visibleButtons.get(i);
            float top_i = top + i * (size + gap);
            RectF rect = hitRects[button] != null ? hitRects[button] : (hitRects[button] = new RectF());
            rect.set(left, top_i, left + size, top_i + size);

            int alpha = button == pressedButton ? 160 : 255;
            circlePaint.setAlpha((int) (alpha * 0.12f));
            canvas.drawCircle(rect.centerX(), rect.centerY(), size / 2f, circlePaint);

            Drawable icon = iconFor(button);
            if (icon != null) {
                icon.setAlpha(alpha);
                icon.setBounds(
                    (int) (rect.left + iconInset), (int) (rect.top + iconInset),
                    (int) (rect.left + iconInset + iconSize), (int) (rect.top + iconInset + iconSize)
                );
                icon.draw(canvas);
            }
        }
    }

    /** x/y must be in the same coordinate space passed to draw(). Returns the button const, or -1. */
    public int checkTouch(float x, float y) {
        for (int i = 0; i < visibleButtons.size(); i++) {
            int button = visibleButtons.get(i);
            RectF rect = hitRects[button];
            if (rect != null && rect.contains(x, y)) {
                return button;
            }
        }
        return -1;
    }

    public void setPressed(int button) {
        pressedButton = button;
    }

    public boolean performClick(int button) {
        if (button < 0 || !visibleButtons.contains(button)) {
            return false;
        }
        if (clickListener != null) {
            clickListener.onActionButtonClick(button);
        }
        return true;
    }

    private Drawable iconFor(int button) {
        switch (button) {
            case BUTTON_SHARE:
                return shareIcon;
            case BUTTON_SAVE:
                return saveIcon;
            case BUTTON_TRANSLATE:
                return translateIcon;
            default:
                return null;
        }
    }

    private void ensureAssets() {
        if (circlePaint == null) {
            circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            circlePaint.setColor(0xFF000000);
        }
        int color = Theme.getColor(Theme.key_chat_serviceIcon);
        PorterDuffColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN);
        if (shareIcon == null) {
            shareIcon = ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.msg_share).mutate();
        }
        if (saveIcon == null) {
            saveIcon = ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.msg_saved).mutate();
        }
        if (translateIcon == null) {
            translateIcon = ApplicationLoader.applicationContext.getResources().getDrawable(R.drawable.msg_translate).mutate();
        }
        shareIcon.setColorFilter(filter);
        saveIcon.setColorFilter(filter);
        translateIcon.setColorFilter(filter);
    }
}
