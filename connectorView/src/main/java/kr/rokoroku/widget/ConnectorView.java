package kr.rokoroku.widget;

/**
 * Created by rok on 2015. 5. 12..
 */

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

import kr.rokoroku.connectorview.R;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static android.graphics.PorterDuff.Mode.CLEAR;

public final class ConnectorView extends View {

    private final Paint iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint topLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint centerLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint bottomLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private static final PorterDuffXfermode CLEAR_XFER_MODE = new PorterDuffXfermode(CLEAR);

    public enum ConnectorType {
        START, NODE, END, NONE
    }

    public enum IconType {
        NONE, DOT, CIRCLE, DRAWABLE
    }

    private ConnectorType connectorType;
    private IconType iconType;
    private Bitmap cache;
    private Drawable drawable;
    private float strokeSize;

    public ConnectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttribute(context, attrs);
    }

    public ConnectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttribute(context, attrs);
    }

    @TargetApi(21)
    public ConnectorView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initAttribute(context, attrs);
    }

    public void initAttribute(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ConnectorView);

        int centerLineColor = a.getColor(R.styleable.ConnectorView_lineColor, Color.LTGRAY);
        int bottomLineColor = a.getColor(R.styleable.ConnectorView_topLineColor, centerLineColor);
        int topLineColor = a.getColor(R.styleable.ConnectorView_bottomLineColor, centerLineColor);
        int iconColor = a.getColor(R.styleable.ConnectorView_iconColor, Color.LTGRAY);
        int fillColor = a.getColor(R.styleable.ConnectorView_fillColor, Color.TRANSPARENT);
        iconPaint.setColor(iconColor);
        topLinePaint.setColor(topLineColor);
        centerLinePaint.setColor(centerLineColor);
        bottomLinePaint.setColor(bottomLineColor);
        fillPaint.setColor(fillColor);
        if (fillColor == Color.TRANSPARENT) {
            fillPaint.setXfermode(CLEAR_XFER_MODE);
        }

        int connectorType = a.getInteger(R.styleable.ConnectorView_connectorType, 0);
        switch (connectorType) {
            case 0:
            default:
                this.connectorType = ConnectorType.NODE;
                break;
            case 1:
                this.connectorType = ConnectorType.START;
                break;
            case 2:
                this.connectorType = ConnectorType.END;
                break;
            case 3:
                this.connectorType = ConnectorType.NONE;
                break;
        }

        int iconType = a.getInteger(R.styleable.ConnectorView_iconType, 2);
        switch (iconType) {
            case 0:
                this.iconType = IconType.NONE;
                break;
            case 1:
                this.iconType = IconType.DOT;
                break;
            case 2:
            default:
                this.iconType = IconType.CIRCLE;
                break;
            case 3:
                this.iconType = IconType.DRAWABLE;
                drawable = a.getDrawable(R.styleable.ConnectorView_iconDrawable);
                if (drawable == null) this.iconType = IconType.NONE;
                break;
        }

        strokeSize = a.getDimension(R.styleable.ConnectorView_strokeWidth, 4);

        a.recycle();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        if (cache != null && (cache.getWidth() != width || cache.getHeight() != height)) {
            cache.recycle();
            cache = null;
        }

        if (cache == null) {
            cache = Bitmap.createBitmap(width, height, ARGB_8888);

            Canvas cacheCanvas = new Canvas(cache);

            float halfWidth = width / 2f;
            float halfHeight = height / 2f;
            float thirdWidth = width / 3f;

            iconPaint.setStrokeWidth(strokeSize);
            centerLinePaint.setStrokeWidth(strokeSize);
            topLinePaint.setStrokeWidth(strokeSize);
            bottomLinePaint.setStrokeWidth(strokeSize);

            switch (connectorType) {
                case NODE:
                default:
                    cacheCanvas.drawLine(halfWidth, 0, halfWidth, halfHeight, topLinePaint);
                    cacheCanvas.drawLine(halfWidth, halfHeight, halfWidth, height, bottomLinePaint);
                    break;
                case START:
                    cacheCanvas.drawLine(halfWidth, halfHeight, halfWidth, height, bottomLinePaint);
                    cacheCanvas.drawCircle(halfWidth, halfHeight, strokeSize / 2, centerLinePaint);
                    break;
                case END:
                    cacheCanvas.drawLine(halfWidth, 0, halfWidth, halfHeight, topLinePaint);
                    cacheCanvas.drawCircle(halfWidth, halfHeight, strokeSize / 2, centerLinePaint);
                    break;
                case NONE:
                    break;
            }
            switch (iconType) {
                case CIRCLE:
                    cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth, iconPaint);
                    cacheCanvas.drawCircle(halfWidth, halfHeight, halfWidth - strokeSize, fillPaint);
                    break;
                case DOT:
                    cacheCanvas.drawCircle(halfWidth, halfHeight, thirdWidth, iconPaint);
                    break;
                case DRAWABLE:
                    if (drawable != null) {
                        int size = width - getPaddingLeft() - getPaddingRight();
                        int halfSize = size / 2;
                        drawable.setBounds(
                                getPaddingLeft(),
                                (int) (halfHeight - halfSize),
                                getPaddingRight(),
                                (int) (halfHeight + halfSize));
                        drawable.setColorFilter(iconPaint.getColorFilter());
                        drawable.draw(cacheCanvas);
                    }
                    break;
                case NONE:
                default:
                    break;
            }
        }
        canvas.drawBitmap(cache, 0, 0, null);
    }

    public void setConnectorType(ConnectorType connectorType) {
        if (connectorType != this.connectorType) {
            this.connectorType = connectorType;
            if (cache != null) {
                cache.recycle();
                cache = null;
            }
            invalidate();
        }
    }

    public void setIconType(IconType iconType) {
        if (iconType != this.iconType) {
            this.iconType = iconType;
            if (cache != null) {
                cache.recycle();
                cache = null;
            }
            invalidate();
        }
    }

    public void setTopLineColor(int color) {
        topLinePaint.setColor(color);
        if (cache != null) {
            cache.recycle();
            cache = null;
        }
        invalidate();
    }

    public void setBottomLineColor(int color) {
        bottomLinePaint.setColor(color);
        if (cache != null) {
            cache.recycle();
            cache = null;
        }
        invalidate();
    }
}