//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package no.exam.android.cropUtils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.MotionEvent;
import android.view.View;

import com.edmodo.cropper.cropwindow.edge.Edge;
import com.edmodo.cropper.cropwindow.handle.Handle;

import no.exam.android.R;


public class CropOverlayView2 extends View {
    private static final int SNAP_RADIUS_DP = 6;
    private static final float DEFAULT_SHOW_GUIDELINES_LIMIT = 100.0F;
    private static final float DEFAULT_CORNER_THICKNESS_DP = 1;//PaintUtil.getCornerThickness();
    private static final float DEFAULT_LINE_THICKNESS_DP = 1;//PaintUtil.getLineThickness();
    private static final float DEFAULT_CORNER_OFFSET_DP;
    private static final float DEFAULT_CORNER_EXTENSION_DP;
    private static final float DEFAULT_CORNER_LENGTH_DP = 20.0F;
    private static final int GUIDELINES_OFF = 0;
    private static final int GUIDELINES_ON_TOUCH = 1;
    private static final int GUIDELINES_ON = 2;
    private Paint mBorderPaint;
    private Paint mGuidelinePaint;
    private Paint mCornerPaint;
    private Paint mBackgroundPaint;
    private Rect mBitmapRect;
    private float mHandleRadius;
    private float mSnapRadius;
    private Pair<Float, Float> mTouchOffset;
    private Handle mPressedHandle;
    private boolean mFixAspectRatio = false;
    private int mAspectRatioX = 1;
    private int mAspectRatioY = 1;
    private float mTargetAspectRatio;
    private int mGuidelines;
    private boolean initializedCropWindow;
    private float mCornerExtension;
    private float mCornerOffset;
    private float mCornerLength;

    public CropOverlayView2(Context context) {
        super(context);
        this.mTargetAspectRatio = (float)this.mAspectRatioX / (float)this.mAspectRatioY;
        this.initializedCropWindow = false;
        this.init(context);
    }

    public CropOverlayView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mTargetAspectRatio = (float)this.mAspectRatioX / (float)this.mAspectRatioY;
        this.initializedCropWindow = false;
        this.init(context);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.initCropWindow(this.mBitmapRect);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        this.drawBackground(canvas, this.mBitmapRect);
        if (showGuidelines()) {
            if (this.mGuidelines == 2) {
                this.drawRuleOfThirdsGuidelines(canvas);
            } else if (this.mGuidelines == 1) {
                if (this.mPressedHandle != null) {
                    this.drawRuleOfThirdsGuidelines(canvas);
                }
            } else if (this.mGuidelines == 0) {
            }
        }

        canvas.drawRect(Edge.LEFT.getCoordinate(), Edge.TOP.getCoordinate(), Edge.RIGHT.getCoordinate(), Edge.BOTTOM.getCoordinate(), this.mBorderPaint);
        this.drawCorners(canvas);
    }

    public boolean onTouchEvent(MotionEvent event) {

        Log.i("AndroidLifeCycle", "ontouchevent");

        if (!this.isEnabled()) {
            return false;
        } else {
            switch(event.getAction()) {
            case 0:
                this.onActionDown(event.getX(), event.getY());
                return true;
            case 1:
            case 3:
                this.getParent().requestDisallowInterceptTouchEvent(false);
                this.onActionUp();
                return true;
            case 2:
                this.onActionMove(event.getX(), event.getY());
                this.getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            default:
                return false;
            }
        }
    }

    public void setBitmapRect(Rect bitmapRect) {
        this.mBitmapRect = bitmapRect;
        this.initCropWindow(this.mBitmapRect);
    }

    public void resetCropOverlayView() {
        if (this.initializedCropWindow) {
            this.initCropWindow(this.mBitmapRect);
            this.invalidate();
        }

    }

    public void setGuidelines(int guidelines) {
        if (guidelines >= 0 && guidelines <= 2) {
            this.mGuidelines = guidelines;
            if (this.initializedCropWindow) {
                this.initCropWindow(this.mBitmapRect);
                this.invalidate();
            }

        } else {
            throw new IllegalArgumentException("Guideline value must be set between 0 and 2. See documentation.");
        }
    }

    public void setFixedAspectRatio(boolean fixAspectRatio) {
        this.mFixAspectRatio = fixAspectRatio;
        if (this.initializedCropWindow) {
            this.initCropWindow(this.mBitmapRect);
            this.invalidate();
        }

    }

    public void setAspectRatioX(int aspectRatioX) {
        if (aspectRatioX <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else {
            this.mAspectRatioX = aspectRatioX;
            this.mTargetAspectRatio = (float)this.mAspectRatioX / (float)this.mAspectRatioY;
            if (this.initializedCropWindow) {
                this.initCropWindow(this.mBitmapRect);
                this.invalidate();
            }

        }
    }

    public void setAspectRatioY(int aspectRatioY) {
        if (aspectRatioY <= 0) {
            throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
        } else {
            this.mAspectRatioY = aspectRatioY;
            this.mTargetAspectRatio = (float)this.mAspectRatioX / (float)this.mAspectRatioY;
            if (this.initializedCropWindow) {
                this.initCropWindow(this.mBitmapRect);
                this.invalidate();
            }

        }
    }

    public void setInitialAttributeValues(int guidelines, boolean fixAspectRatio, int aspectRatioX, int aspectRatioY) {
        if (guidelines >= 0 && guidelines <= 2) {
            this.mGuidelines = guidelines;
            this.mFixAspectRatio = fixAspectRatio;
            if (aspectRatioX <= 0) {
                throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
            } else {
                this.mAspectRatioX = aspectRatioX;
                this.mTargetAspectRatio = (float)this.mAspectRatioX / (float)this.mAspectRatioY;
                if (aspectRatioY <= 0) {
                    throw new IllegalArgumentException("Cannot set aspect ratio value to a number less than or equal to 0.");
                } else {
                    this.mAspectRatioY = aspectRatioY;
                    this.mTargetAspectRatio = (float)this.mAspectRatioX / (float)this.mAspectRatioY;
                }
            }
        } else {
            throw new IllegalArgumentException("Guideline value must be set between 0 and 2. See documentation.");
        }
    }

    private void init(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        this.mHandleRadius = 10; //HandleUtil.getTargetRadius(context);
        this.mSnapRadius = 10;//TypedValue.applyDimension(1, 6.0F, displayMetrics);
        this.mBorderPaint = PaintUtil.newBorderPaint(context.getResources());
        this.mGuidelinePaint = PaintUtil.newGuidelinePaint(context.getResources());

        final Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth(context.getResources().getDimension(R.dimen.guideline_thickness));
        paint.setColor(context.getResources().getColor(R.color.guideline));

        this.mBackgroundPaint = paint;//PaintUtil.newBackgroundPaint(context);
        this.mCornerPaint = PaintUtil.newCornerPaint(context.getResources());
        this.mCornerOffset = 1;//TypedValue.applyDimension(1, DEFAULT_CORNER_OFFSET_DP, displayMetrics);
        this.mCornerExtension = 1;//TypedValue.applyDimension(1, DEFAULT_CORNER_EXTENSION_DP, displayMetrics);
        this.mCornerLength = 1;//TypedValue.applyDimension(1, 20.0F, displayMetrics);
        this.mGuidelines = 1;
    }

    private void initCropWindow(Rect bitmapRect) {
        if (!this.initializedCropWindow) {
            this.initializedCropWindow = true;
        }

        RectF rec = new RectF();
        rec.roundOut(bitmapRect);

        float centerX;
        float cropWidth;
        if (this.mFixAspectRatio) {
            float halfCropWidth;
            if (AspectRatioUtil.calculateAspectRatio(rec) > this.mTargetAspectRatio) {
                Edge.TOP.setCoordinate((float)bitmapRect.top);
                Edge.BOTTOM.setCoordinate((float)bitmapRect.bottom);
                centerX = (float)this.getWidth() / 2.0F;
                cropWidth = Math.max(40.0F, AspectRatioUtil.calculateWidth(Edge.TOP.getCoordinate(), Edge.BOTTOM.getCoordinate()));
                if (cropWidth == 40.0F) {
                    this.mTargetAspectRatio = 40.0F / (Edge.BOTTOM.getCoordinate() - Edge.TOP.getCoordinate());
                }

                halfCropWidth = cropWidth / 2.0F;
                Edge.LEFT.setCoordinate(centerX - halfCropWidth);
                Edge.RIGHT.setCoordinate(centerX + halfCropWidth);
            } else {
                Edge.LEFT.setCoordinate((float)bitmapRect.left);
                Edge.RIGHT.setCoordinate((float)bitmapRect.right);
                centerX = (float)this.getHeight() / 2.0F;
                cropWidth = Math.max(40.0F, AspectRatioUtil.calculateHeight(Edge.LEFT.getCoordinate(), Edge.RIGHT.getCoordinate()));
                if (cropWidth == 40.0F) {
                    this.mTargetAspectRatio = (Edge.RIGHT.getCoordinate() - Edge.LEFT.getCoordinate()) / 40.0F;
                }

                halfCropWidth = cropWidth / 2.0F;
                Edge.TOP.setCoordinate(centerX - halfCropWidth);
                Edge.BOTTOM.setCoordinate(centerX + halfCropWidth);
            }
        } else {
            centerX = 0.1F * (float)bitmapRect.width();
            cropWidth = 0.1F * (float)bitmapRect.height();
            Edge.LEFT.setCoordinate((float)bitmapRect.left + centerX);
            Edge.TOP.setCoordinate((float)bitmapRect.top + cropWidth);
            Edge.RIGHT.setCoordinate((float)bitmapRect.right - centerX);
            Edge.BOTTOM.setCoordinate((float)bitmapRect.bottom - cropWidth);
        }

    }

    public static boolean showGuidelines() {
        return !(Math.abs(Edge.LEFT.getCoordinate() - Edge.RIGHT.getCoordinate()) < 100.0F) && !(Math.abs(Edge.TOP.getCoordinate() - Edge.BOTTOM.getCoordinate()) < 100.0F);
    }

    private void drawRuleOfThirdsGuidelines(Canvas canvas) {
        float left = Edge.LEFT.getCoordinate();
        float top = Edge.TOP.getCoordinate();
        float right = Edge.RIGHT.getCoordinate();
        float bottom = Edge.BOTTOM.getCoordinate();
        float oneThirdCropWidth = Edge.getWidth() / 3.0F;
        float x1 = left + oneThirdCropWidth;
        canvas.drawLine(x1, top, x1, bottom, this.mGuidelinePaint);
        float x2 = right - oneThirdCropWidth;
        canvas.drawLine(x2, top, x2, bottom, this.mGuidelinePaint);
        float oneThirdCropHeight = Edge.getHeight() / 3.0F;
        float y1 = top + oneThirdCropHeight;
        canvas.drawLine(left, y1, right, y1, this.mGuidelinePaint);
        float y2 = bottom - oneThirdCropHeight;
        canvas.drawLine(left, y2, right, y2, this.mGuidelinePaint);
    }

    private void drawBackground(Canvas canvas, Rect bitmapRect) {
        float left = Edge.LEFT.getCoordinate();
        float top = Edge.TOP.getCoordinate();
        float right = Edge.RIGHT.getCoordinate();
        float bottom = Edge.BOTTOM.getCoordinate();
        canvas.drawRect((float)bitmapRect.left, (float)bitmapRect.top, (float)bitmapRect.right, top, this.mBackgroundPaint);
        canvas.drawRect((float)bitmapRect.left, bottom, (float)bitmapRect.right, (float)bitmapRect.bottom, this.mBackgroundPaint);
        canvas.drawRect((float)bitmapRect.left, top, left, bottom, this.mBackgroundPaint);
        canvas.drawRect(right, top, (float)bitmapRect.right, bottom, this.mBackgroundPaint);
    }

    private void drawCorners(Canvas canvas) {
        float left = Edge.LEFT.getCoordinate();
        float top = Edge.TOP.getCoordinate();
        float right = Edge.RIGHT.getCoordinate();
        float bottom = Edge.BOTTOM.getCoordinate();
        canvas.drawLine(left - this.mCornerOffset, top - this.mCornerExtension, left - this.mCornerOffset, top + this.mCornerLength, this.mCornerPaint);
        canvas.drawLine(left, top - this.mCornerOffset, left + this.mCornerLength, top - this.mCornerOffset, this.mCornerPaint);
        canvas.drawLine(right + this.mCornerOffset, top - this.mCornerExtension, right + this.mCornerOffset, top + this.mCornerLength, this.mCornerPaint);
        canvas.drawLine(right, top - this.mCornerOffset, right - this.mCornerLength, top - this.mCornerOffset, this.mCornerPaint);
        canvas.drawLine(left - this.mCornerOffset, bottom + this.mCornerExtension, left - this.mCornerOffset, bottom - this.mCornerLength, this.mCornerPaint);
        canvas.drawLine(left, bottom + this.mCornerOffset, left + this.mCornerLength, bottom + this.mCornerOffset, this.mCornerPaint);
        canvas.drawLine(right + this.mCornerOffset, bottom + this.mCornerExtension, right + this.mCornerOffset, bottom - this.mCornerLength, this.mCornerPaint);
        canvas.drawLine(right, bottom + this.mCornerOffset, right - this.mCornerLength, bottom + this.mCornerOffset, this.mCornerPaint);
    }

    private void onActionDown(float x, float y) {
        float left = Edge.LEFT.getCoordinate();
        float top = Edge.TOP.getCoordinate();
        float right = Edge.RIGHT.getCoordinate();
        float bottom = Edge.BOTTOM.getCoordinate();
        this.mPressedHandle = HandleUtil.getPressedHandle(x, y, left, top, right, bottom, this.mHandleRadius);
        if (this.mPressedHandle != null) {
            this.mTouchOffset = com.edmodo.cropper.util.HandleUtil.getOffset(this.mPressedHandle, x, y, left, top, right, bottom); //HandleUtil.getOffset(this.mPressedHandle, x, y, left, top, right, bottom, new PointF());
            this.invalidate();
        }
    }

    private void onActionUp() {
        if (this.mPressedHandle != null) {
            this.mPressedHandle = null;
            this.invalidate();
        }
    }

    private void onActionMove(float x, float y) {
        if (this.mPressedHandle != null) {
            x += (Float)this.mTouchOffset.first;
            y += (Float)this.mTouchOffset.second;
            if (this.mFixAspectRatio) {
                this.mPressedHandle.updateCropWindow(x, y, this.mTargetAspectRatio, this.mBitmapRect, this.mSnapRadius);
            } else {
                this.mPressedHandle.updateCropWindow(x, y, this.mBitmapRect, this.mSnapRadius);
            }

            this.invalidate();
        }
    }

    static {
        DEFAULT_CORNER_OFFSET_DP = DEFAULT_CORNER_THICKNESS_DP / 2.0F - DEFAULT_LINE_THICKNESS_DP / 2.0F;
        DEFAULT_CORNER_EXTENSION_DP = DEFAULT_CORNER_THICKNESS_DP / 2.0F + DEFAULT_CORNER_OFFSET_DP;
    }
}
