package org.telegram.ui.Components.Paint;

import android.graphics.PointF;

/**
 * Face-anchored mask sticker placement data. GMS Vision face detection has
 * been removed from this build, so nothing constructs instances of this
 * class anymore (LPhotoPaintView/PaintView's face lists always stay empty) -
 * this shell is kept only so the surrounding "ArrayList<PhotoFace>" code in
 * those files still compiles.
 */
public class PhotoFace {

    private float width;
    private float angle;

    private PointF foreheadPoint;

    private PointF eyesCenterPoint;
    private float eyesDistance;

    private PointF mouthPoint;
    private PointF chinPoint;

    public boolean isSufficient() {
        return eyesCenterPoint != null;
    }

    public PointF getPointForAnchor(int anchor) {
        switch (anchor) {
            case 0:
                return foreheadPoint;
            case 1:
                return eyesCenterPoint;
            case 2:
                return mouthPoint;
            case 3:
                return chinPoint;
            default:
                return null;
        }
    }

    public float getWidthForAnchor(int anchor) {
        if (anchor == 1) {
            return eyesDistance;
        }
        return width;
    }

    public float getAngle() {
        return angle;
    }
}
