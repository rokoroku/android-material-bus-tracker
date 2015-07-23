/*
 *
 *  * Copyright 2015 Atanas Dimitrov <atanas@naughtyspirit.co>
 *  *                 NaughtySpirit 2014
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package co.naughtyspirit.showcaseview;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.Html;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import co.naughtyspirit.showcaseview.targets.Target;
import co.naughtyspirit.showcaseview.targets.TargetView;
import co.naughtyspirit.showcaseview.utils.PositionsUtil;


/**
 * Created by Seishin <atanas@naughtyspirit.co>
 * on 2/10/15.
 *
 * NaughtySpirit 2015
 */
public class ShowcaseView extends RelativeLayout implements View.OnTouchListener {

    public static final String PREFERENCE_NAME = "TUTORIAL";
    private static final String TAG = "ShowcaseView";

    private Context ctx;

    private Bitmap bgBitmap;
    private Canvas tempCanvas;
    private Paint backgroundPaint;
    private Paint transparentPaint;
    private Paint borderPaint;
    private Target target;
    private Button buttonView;
    private TextView descriptionView;
    private ImageView descriptionImageView;

    private int targetBorderSize;
    private int targetMargin;
    private String showcaseTag;
    private OnHideListener onHideListener;
    private boolean hideOnAction = false;
    private boolean isHiding = false;

    public ShowcaseView(Context ctx) {
        this(ctx, null, R.style.ShowcaseView);
    }

    public ShowcaseView(Context ctx, AttributeSet attrs) {
        this(ctx, attrs, R.style.ShowcaseView);
    }
    
    public ShowcaseView(Context ctx, AttributeSet attrs, int defStyle) {
        super(ctx, attrs, defStyle);
        this.ctx = ctx;

        final TypedArray styled = ctx.getTheme()
                .obtainStyledAttributes(attrs, R.styleable.ShowcaseView, R.attr.showcaseViewStyle,
                        R.style.ShowcaseView);

        initDrawTools();
        initUI();

        updateStyle(styled, false);
    }

    private void initDrawTools() {
        tempCanvas = new Canvas();

        backgroundPaint = new Paint();
        PorterDuffXfermode porterDuffXfermode = new PorterDuffXfermode(PorterDuff.Mode.CLEAR);

        transparentPaint = new Paint();
        transparentPaint.setAntiAlias(true);
        transparentPaint.setColor(getResources().getColor(android.R.color.transparent));
        transparentPaint.setXfermode(porterDuffXfermode);

        borderPaint = new Paint();
        borderPaint.setAntiAlias(true);

        setWillNotDraw(false);
    }

    private void initUI() {
        descriptionView = (TextView) LayoutInflater.from(ctx).inflate(R.layout.showcase_description, null);
        descriptionImageView = (ImageView) LayoutInflater.from(ctx).inflate(R.layout.showcase_description_image, null);
        addView(descriptionView, PositionsUtil.configureParams(ctx, PositionsUtil.ItemPosition.TOP_CENTER));
        addView(descriptionImageView, PositionsUtil.configureParams(ctx, PositionsUtil.ItemPosition.TOP_CENTER));

        buttonView = (Button) LayoutInflater.from(ctx).inflate(R.layout.showcase_button, null);
        buttonView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                hide(false);
                buttonView.setOnClickListener(null);
            }
        });
        addView(buttonView, PositionsUtil.configureParams(ctx, PositionsUtil.ItemPosition.CENTER));

        setOnTouchListener(this);
    }

    public void setStyle(int style) {
        TypedArray array = getContext().obtainStyledAttributes(style, R.styleable.ShowcaseView);

        updateStyle(array, true);
    }

    public void updateStyle(TypedArray styled, boolean invalidate) {
        int showcaseBackground = styled.getColor(R.styleable.ShowcaseView_showcase_background,
                ctx.getResources().getColor(R.color.showcase_bg));

        int targetBorderColor = styled.getColor(R.styleable.ShowcaseView_showcase_target_border_color,
                ctx.getResources().getColor(R.color.showcase_target_border_color));
        targetBorderSize = styled.getDimensionPixelSize(R.styleable.ShowcaseView_showcase_target_border_size,
                ctx.getResources().getDimensionPixelSize(R.dimen.showcase_target_border_size));
        targetMargin = styled.getDimensionPixelSize(R.styleable.ShowcaseView_showcase_target_margin,
                ctx.getResources().getDimensionPixelSize(R.dimen.showcase_target_margin));

        float descTextSize = styled.getDimension(R.styleable.ShowcaseView_showcase_desc_text_size,
                ctx.getResources().getDimensionPixelSize(R.dimen.showcase_desc_text_size));
        int descTextColor = styled.getColor(R.styleable.ShowcaseView_showcase_desc_text_color,
                ctx.getResources().getColor(R.color.showcase_desc_text));

        float btnTextSize = styled.getDimension(R.styleable.ShowcaseView_showcase_btn_text_size,
                ctx.getResources().getDimension(R.dimen.showcase_btn_text));
        int btnTextColor = styled.getColor(R.styleable.ShowcaseView_showcase_btn_text_color,
                ctx.getResources().getColor(R.color.showcase_btn_text));
        int btnBackground = styled.getColor(R.styleable.ShowcaseView_showcase_btn_background,
                ctx.getResources().getColor(R.color.showcase_btn_background));

        styled.recycle();
        
        setBackgroundColor(showcaseBackground);
        setBorderColor(targetBorderColor);

        descriptionView.setTextColor(descTextColor);
        descriptionView.setTextSize(PositionsUtil.floatToSP(ctx, descTextSize));
        descriptionView.setText(TextUtils.isEmpty(descriptionView.getText().toString()) ? "" : descriptionView.getText().toString());

        buttonView.setTextColor(btnTextColor);
        buttonView.setTextSize(PositionsUtil.floatToSP(ctx, btnTextSize));
        buttonView.setText(TextUtils.isEmpty(buttonView.getText().toString()) ? "" : buttonView.getText().toString());
        buttonView.setBackgroundColor(btnBackground);

        if (invalidate) {
            invalidate();
        }
    }

    public OnHideListener getOnHideListener() {
        return onHideListener;
    }

    public void setOnHideListener(OnHideListener onHideListener) {
        this.onHideListener = onHideListener;
    }

    public void setHideOnAction(boolean hideOnAction) {
        this.hideOnAction = hideOnAction;
    }

    public void setBackgroundColor(String color) {
        backgroundPaint.setColor(Color.parseColor(color));
    }

    public void setBackgroundColor(int color) {
        backgroundPaint.setColor(color);
    }

    public void setBorderColor(String color) {
        borderPaint.setColor(Color.parseColor(color));
    }

    public void setBorderColor(int color) {
        borderPaint.setColor(color);
    }

    public void setDescription(String description, PositionsUtil.ItemPosition position) {
        if (!TextUtils.isEmpty(description)) {
            descriptionView.setText(Html.fromHtml(description));
        }
        
        descriptionView.setLayoutParams(PositionsUtil.configureParams(ctx, position));
        invalidate();
    }

    public void setDescriptionDrawable(final Drawable drawable, final PositionsUtil.ItemPosition position) {
        if(drawable != null) {
            descriptionImageView.setImageDrawable(drawable);
            if(position.equals(PositionsUtil.ItemPosition.ADJUST_HEIGHT)) {
                descriptionImageView.setVisibility(GONE);
                descriptionImageView.setTag(position);
            } else {
                descriptionImageView.setLayoutParams(PositionsUtil.configureParams(ctx, position));
            }
        }
        invalidate();
    }

    public void setShowcaseTag(String tag) {
        this.showcaseTag = tag;
    }

    public String getShowcaseTag() {
        return showcaseTag;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Target getTarget() {
        return target;
    }

    public void hide(final boolean isTargetTriggered) {
        if(!isHiding) {
            isHiding = true;

            Animation animation = new AlphaAnimation(1.0f, 0.0f);
            animation.setDuration(300);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            setVisibility(GONE);
                            if(getParent() instanceof ViewGroup) {
                                ((ViewGroup) getParent()).removeView(ShowcaseView.this);
                            }
                            if(onHideListener != null) {
                                onHideListener.onHide(isTargetTriggered);
                            }
                        }
                    });
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            startAnimation(animation);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (target == null) {
            Log.i(TAG, "No target set...");
            return;
        }

        if(bgBitmap == null) {
            bgBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_4444);
        }

        tempCanvas.setBitmap(bgBitmap);
        tempCanvas.drawRect(0, 0, tempCanvas.getWidth(), tempCanvas.getHeight(), backgroundPaint);

        if (target.getType().equals(TargetView.ShowcaseType.CIRCLE)) {
            float x = target.getLocation()[0];
            float y = target.getLocation()[1];
            float radius = target.getLocation()[2];

            tempCanvas.drawCircle(x, y, radius + targetMargin + targetBorderSize, transparentPaint);
            tempCanvas.drawCircle(x, y, radius + targetMargin + targetBorderSize, borderPaint);
            tempCanvas.drawCircle(x, y, radius + targetMargin, transparentPaint);
        } else if (target.getType().equals(TargetView.ShowcaseType.RECTANGLE)) {
            float left = target.getLocation()[0] - targetMargin;
            float top = target.getLocation()[1] - targetMargin;
            float right = target.getLocation()[2] + targetMargin;
            float bottom = target.getLocation()[3] + targetMargin;

            tempCanvas.drawRect(left - targetBorderSize, top - targetBorderSize,
                    right + targetBorderSize, bottom + targetBorderSize, transparentPaint);
            tempCanvas.drawRect(left - targetBorderSize, top - targetBorderSize,
                    right + targetBorderSize, bottom + targetBorderSize, borderPaint);
            tempCanvas.drawRect(left, top, right, bottom, transparentPaint);
        }

        canvas.drawBitmap(bgBitmap, 0, 0, backgroundPaint);

        if(descriptionImageView.getTag() != null && descriptionImageView.getTag() instanceof PositionsUtil.ItemPosition) {
            PositionsUtil.ItemPosition itemPosition = (PositionsUtil.ItemPosition) descriptionImageView.getTag();
            RelativeLayout.LayoutParams params = PositionsUtil.configureParams(ctx, itemPosition, target);
            descriptionImageView.setLayoutParams(params);
            descriptionImageView.setVisibility(VISIBLE);
            descriptionImageView.setTag(null);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN && target != null) {
            float eventX = event.getX();
            float eventY = event.getY();

            Rect rect = null;

            if (target.getType().equals(TargetView.ShowcaseType.CIRCLE)) {
                float x = target.getLocation()[0];
                float y = target.getLocation()[1];
                float radius = target.getLocation()[2];

                rect = new Rect((int) (x - radius), (int) (y - radius), (int) (x + radius), (int) (y + radius));

            } else if (target.getType().equals(TargetView.ShowcaseType.RECTANGLE)) {
                float left = target.getLocation()[0] - targetMargin;
                float top = target.getLocation()[1] - targetMargin;
                float right = target.getLocation()[2] + targetMargin;
                float bottom = target.getLocation()[3] + targetMargin;

                rect = new Rect((int) left, (int) top, (int) right, (int) bottom);
            }

            if (rect != null) {
                if (!rect.contains((int) eventX, (int) eventY)) {
                    buttonView.performClick();
                    return true;
                } else if(hideOnAction) {
                    hide(true);
                }
            }
        }
        return false;
    }

    public static class Builder {
        private final Activity activity;

        private String tag;
        private Target target;
        private String description;
        private Drawable drawable;
        private PositionsUtil.ItemPosition position;
        private SharedPreferences sharedPreferences;
        private OnHideListener onHideListener;

        private boolean isOneShot;
        private boolean hideOnAction;

        /**
         * Builder class for easier ShowcaseView creation.
         *
         * @param activity host activity reference
         * @param tag showcaseView tag
         */
        public Builder(Activity activity, String tag) {
            this.activity = activity;
            this.tag = tag;
            if(activity != null) {
                this.sharedPreferences = activity.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE);
            }
        }

        /**
         * Setting up the current ShowcaseView whether to show once or always.
         *
         * @param isOneShot
         * @return
         */
        public Builder setOneShot(boolean isOneShot) {
            this.isOneShot = isOneShot;
            return this;
        }

        /**
         * Setting up the the showcase target view.
         *
         * @param target {@link co.naughtyspirit.showcaseview.targets.TargetView} to be showcased
         */
        public Builder setTarget(Target target) {
            this.target = target;
            return this;
        }

        /**
         * Setting up the description position on the screen.
         * Default position is TOP_CENTER {@link co.naughtyspirit.showcaseview.utils.PositionsUtil.ItemPosition}
         *
         * @param position desired {@link co.naughtyspirit.showcaseview.utils.PositionsUtil.ItemPosition}
         */
        public Builder setDescription(PositionsUtil.ItemPosition position) {
            setDescription(null, position);
            return this;
        }

        /**
         * Setting up the text's text and its position on the screen.
         * Default position is TOP_CENTER {@link co.naughtyspirit.showcaseview.utils.PositionsUtil.ItemPosition}
         *
         * @param text description text
         * @param position desired {@link co.naughtyspirit.showcaseview.utils.PositionsUtil.ItemPosition}
         */
        public Builder setDescription(String text, PositionsUtil.ItemPosition position) {
            this.description = text;
            this.position = position;
            return this;
        }

        /**
         * Setting up the text's text and its position on the screen.
         * Default position is TOP_CENTER {@link co.naughtyspirit.showcaseview.utils.PositionsUtil.ItemPosition}
         *
         * @param drawable description text
         * @param position desired {@link co.naughtyspirit.showcaseview.utils.PositionsUtil.ItemPosition}
         */
        public Builder setDescriptionDrawable(Drawable drawable, PositionsUtil.ItemPosition position) {
            this.drawable = drawable;
            this.position = position;
            return this;
        }

        public Builder setOnHideListener(OnHideListener onHideListener) {
            this.onHideListener = onHideListener;
            return this;
        }

        public Builder setHideOnAction(boolean hideOnAction) {
            this.hideOnAction = hideOnAction;
            return this;
        }

        /**
         * Building and presenting the {@link ShowcaseView} on the screen.
         */
        public ShowcaseView build() {
            if (isOneShot && sharedPreferences.getBoolean(tag, false)) {
                if(onHideListener != null) onHideListener.onHide(false);
                return null;

            } else {
                if(sharedPreferences != null) {
                    sharedPreferences.edit().putBoolean(tag, isOneShot).apply();
                }
                final ShowcaseView showcaseView = new ShowcaseView(activity);
                showcaseView.setTarget(target);
                showcaseView.setShowcaseTag(tag);
                showcaseView.setHideOnAction(hideOnAction);
                showcaseView.setOnHideListener(onHideListener);
                showcaseView.setDescription(description, position);
                showcaseView.setDescriptionDrawable(drawable, position);

                ((ViewGroup) activity.getWindow().getDecorView()).addView(showcaseView);
                Animation animation = new AlphaAnimation(0.0f, 1.0f);
                animation.setDuration(300);
                showcaseView.startAnimation(animation);

                return showcaseView;
            }
        }
    }

    public static interface OnHideListener {
        public void onHide(boolean isTargetTriggered);
    }
}