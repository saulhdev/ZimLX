package org.zimmob.zimlx.pageindicator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import org.zimmob.zimlx.manager.Setup;
import org.zimmob.zimlx.util.Tool;
import org.zimmob.zimlx.viewutil.SmoothPagerAdapter;
import org.zimmob.zimlx.widget.SmoothViewPager;

import static org.zimmob.zimlx.config.Config.INDICATOR_ARROW;
import static org.zimmob.zimlx.config.Config.INDICATOR_DOTS;
import static org.zimmob.zimlx.config.Config.INDICATOR_LINE;

/**
 * Created by saul on 04-25-18.
 * Project ZimLX
 * henriquez.saul@gmail.com
 */
public class PageIndicator extends View implements SmoothViewPager.OnPageChangeListener {
    private static float _pad;
    private boolean _alphaFade;
    private boolean _alphaShow;
    private Paint _arrowPaint = new Paint(1);
    private Path _arrowPath;
    private final Runnable _delayShow;
    private Paint _dotPaint = new Paint(1);
    private float _dotSize;
    private int _mode = INDICATOR_DOTS;
    private SmoothViewPager _pager;
    private int _prePageCount;
    private int _previousPage = -1;
    private int _realPreviousPage;
    private float _scaleFactor = 1.0f;
    private float _scaleFactor2 = 1.5f;
    private float _scrollOffset;
    private int _scrollPagePosition;
    private Paint _lineBgPaint =  new Paint(1);
    private Paint _linePaint = new Paint(1);
    private Paint _linePaint2 = new Paint(1);
    public PageIndicator(Context context) {
        this(context, null);
    }

    public PageIndicator(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setPad((float) Tool.toPx(3));
        setWillNotDraw(false);
        _dotPaint.setColor(-1);
        _dotPaint.setStrokeWidth((float) Tool.toPx(2));
        _dotPaint.setAntiAlias(true);

        _linePaint.setColor(-1);
        _linePaint.setStrokeWidth((float) Tool.toPx(4));
        _linePaint.setAntiAlias(true);
        _lineBgPaint.setColor(Color.parseColor("#73ECEFF7"));
        _lineBgPaint.setStrokeWidth((float) Tool.toPx(4));
        _lineBgPaint.setAntiAlias(true);
        _linePaint2.setColor(Color.BLACK);
        _linePaint2.setStrokeWidth((float) Tool.toPx(4));
        _linePaint2.setAntiAlias(true);

        _arrowPaint.setColor(-1);
        _arrowPaint.setAntiAlias(true);
        _arrowPaint.setStyle(Style.STROKE);
        _arrowPaint.setStrokeWidth(this.getPad() / 1.5f);
        _arrowPaint.setStrokeJoin(Join.ROUND);
        _arrowPath = new Path();
        _mode = Setup.appSettings().getDesktopIndicatorMode();
        _delayShow = () -> {
            _alphaFade = true;
            _alphaShow = false;
            invalidate();
        };
    }

    private float getPad() {
        return PageIndicator._pad;
    }

    private void setPad(float v) {
        PageIndicator._pad = v;
    }

    protected void onDraw(Canvas canvas) {
        _dotSize = getHeight() - _pad * 1.25f;
        switch (_mode) {
            case INDICATOR_DOTS: {
                if (_pager != null) {
                    _dotPaint.setAlpha(255);
                    float circlesWidth = _pager.getAdapter().getCount() * (_dotSize + _pad * 2);
                    canvas.translate(getWidth() / 2 - circlesWidth / 2, 0f);

                    if (_realPreviousPage != _pager.getCurrentItem()) {
                        _scaleFactor = 1f;
                        _realPreviousPage = _pager.getCurrentItem();
                    }
                    for (int i = 0; i < _pager.getAdapter().getCount(); i++) {
                        float targetFactor = 1.5f;
                        float targetFactor2 = 1f;
                        float increaseFactor = 0.05f;
                        if (i == _previousPage && i != _pager.getCurrentItem()) {
                            _scaleFactor2 = Tool.clampFloat(_scaleFactor2 - increaseFactor, targetFactor2, targetFactor);
                            Tool.print(_scaleFactor2);
                            canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * i, (float) (getHeight() / 2), _scaleFactor2 * _dotSize / 2, _dotPaint);
                            if (_scaleFactor2 != targetFactor2)
                                invalidate();
                            else {
                                _scaleFactor2 = 1.5f;
                                _previousPage = -1;
                            }
                        } else if (_pager.getCurrentItem() == i) {
                            if (_previousPage == -1)
                                _previousPage = i;
                            _scaleFactor = Tool.clampFloat(_scaleFactor + increaseFactor, targetFactor2, targetFactor);
                            canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * i, (float) (getHeight() / 2), _scaleFactor * _dotSize / 2, _dotPaint);
                            if (_scaleFactor != targetFactor)
                                invalidate();
                        } else {
                            canvas.drawCircle(_dotSize / 2 + _pad + (_dotSize + _pad * 2) * i, (float) (getHeight() / 2), _dotSize / 2, _dotPaint);
                        }
                    }
                }
                break;
            }
            case INDICATOR_ARROW: {
                if (_pager != null) {
                    _arrowPath.reset();
                    _arrowPath.moveTo(getWidth() / 2 - _dotSize * 1.5f, (float) (getHeight()) - _dotSize / 3 - _pad / 2);
                    _arrowPath.lineTo((getWidth() / 2f), _pad / 2);
                    _arrowPath.lineTo(getWidth() / 2 + _dotSize * 1.5f, (float) (getHeight()) - _dotSize / 3 - _pad / 2);
                    canvas.drawPath(_arrowPath, _arrowPaint);
                    float lineWidth = getWidth() / _pager.getAdapter().getCount();
                    float currentStartX = _scrollPagePosition * lineWidth;
                    float myX = currentStartX + _scrollOffset * lineWidth;
                    if (myX % lineWidth != 0f)
                        invalidate();
                    if (_alphaFade) {
                        _dotPaint.setAlpha(Tool.clampInt(_dotPaint.getAlpha() - 10, 0, 255));
                        if (_dotPaint.getAlpha() == 0)
                            _alphaFade = false;
                        invalidate();
                    }
                    if (_alphaShow) {
                        _dotPaint.setAlpha(Tool.clampInt(_dotPaint.getAlpha() + 10, 0, 255));
                        if (_dotPaint.getAlpha() == 255) {
                            _alphaShow = false;
                        }
                        invalidate();
                    }
                    canvas.drawLine(myX, (float) getHeight(), myX + lineWidth, (float) getHeight(), _dotPaint);
                }
            }
            break;
            case INDICATOR_LINE: {
                if (_pager != null) {
                    float lineWidth = 90;
                    float pagesCount = _pager.getAdapter().getCount();
                    float sep = (pagesCount-1)*30;
                    float center =getWidth()/2;
                    if (pagesCount % 2 == 0f){
                        float lineInit= center-(pagesCount/2)*lineWidth-sep/2;
                        float lineEnd=lineInit+90;
                        for(int i=0; i < pagesCount;i++){
                            canvas.drawLine(lineInit, 30, lineEnd,30, _lineBgPaint);
                            lineInit=lineEnd+30;
                            lineEnd+=120;
                        }
                        drawSelectedLine(_scrollPagePosition,pagesCount,canvas,center, lineWidth);
                    }
                    else{
                        float lineInit= center-((pagesCount-1)/2)*lineWidth - sep/2 -45;
                        float lineEnd=lineInit+90;
                        for(int i=0; i < pagesCount;i++){
                            canvas.drawLine(lineInit, 30, lineEnd,30, _lineBgPaint);
                            lineInit=lineEnd+30;
                            lineEnd+=120;
                        }
                        drawSelectedLine(_scrollPagePosition,pagesCount,canvas,center, lineWidth);
                    }
                }
                break;
            }
        }
    }

    private void drawSelectedLine(int scrollPosition, float pagesCount, Canvas canvas, float center,float lineWidth) {
        float sep = (pagesCount-1)*30;
        if(scrollPosition>0){
            float selectedInit=center-(pagesCount/2)*lineWidth - sep/2 + _scrollPagePosition*(lineWidth+30);
            float selectedEnd = selectedInit + 90;
            canvas.drawLine(selectedInit, 30, selectedEnd,30, _linePaint);
        }
        else {
            float selectedInit = center - (pagesCount / 2) * lineWidth - sep / 2 + _scrollPagePosition * lineWidth;
            float selectedEnd = selectedInit + 90;
            canvas.drawLine(selectedInit, 30, selectedEnd, 30, _linePaint);
        }
    }

    public final void setMode(int mode) {
        _mode = mode;
        invalidate();
    }

    public final void setViewPager(@Nullable SmoothViewPager pager) {
        if (pager == null && _pager != null) {
            _pager.removeOnPageChangeListener(this);
            _pager = null;
        } else {
            _pager = pager;
            _prePageCount = pager != null ? pager.getAdapter().getCount() : 0;
            if (pager != null) {
                pager.addOnPageChangeListener(this);
            }
        }
        invalidate();
    }

    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        SmoothPagerAdapter adapter = _pager.getAdapter();
        if (_prePageCount != adapter.getCount()) {
            _prePageCount = _pager.getAdapter().getCount();
        }
        _scrollOffset = positionOffset;
        _scrollPagePosition = position;
        invalidate();
    }

    public void onPageSelected(int position) {
    }

    public final void showNow() {
        removeCallbacks(_delayShow);
        _alphaShow = true;
        _alphaFade = false;
        invalidate();
    }

    public final void hideDelay() {
        postDelayed(_delayShow, 500);
    }

    public void onPageScrollStateChanged(int state) {
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        _dotSize = ((float) getHeight()) - (this.getPad() * 1.25f);
        super.onLayout(changed, left, top, right, bottom);
    }

    public void setActiveMarker(int activePage) {
    }

    public void addMarker() {
        _prePageCount++;
        onPageCountChanged();
    }

    public void removeMarker() {
        _prePageCount--;
        onPageCountChanged();
    }

    public void setMarkersCount(int numMarkers) {
        _prePageCount = numMarkers;
        onPageCountChanged();
    }

    protected void onPageCountChanged() {
    }
}