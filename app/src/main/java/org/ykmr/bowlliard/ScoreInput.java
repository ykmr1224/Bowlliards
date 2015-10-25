package org.ykmr.bowlliard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Path.FillType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class ScoreInput extends View{
	int X=2;
	int Y=2;
	int W=400;
	int H=50;
	float w0=W/11;
	float h0=H;
	int cursor = 0;
	int bgColor = Color.DKGRAY;
	int frmColor = Color.LTGRAY;
	int txtColor = Color.LTGRAY;
	int csrColor = 0xff777744;
	int max = 10;
	int selected = -1;
	
	boolean editable = true;
	
	public ScoreInput(Context context, AttributeSet attributes){
		super(context, attributes);
	}

	public ScoreInput(Context context){
		super(context);
	}
	
	protected int calcTextSize(Paint p){
		int res = (int)(h0);
		while(true){
			Rect rect = new Rect();
			p.setTextSize(res);
			p.getTextBounds("000", 0, 3, rect);
//			Log.d("log", "calcTextSize : " + res + " :: "+rect.toString());
			if(rect.width() < w0-6 && rect.height() < h0-2) break;
			res--;
		}
		return res;
	}
	
	public void setMax(int max){
		this.max = max;
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		W = getWidth()-4;
		H = getHeight()-4;
		w0 = W/11f;
		h0 = H;
		
		Paint p = new Paint();
        p.setAntiAlias(true);
        p.measureText("H");
        int size = calcTextSize(p);
        
		p.setColor(bgColor);
		
		p.setSubpixelText(true);
		p.setTypeface(Typeface.SANS_SERIF);
		//draw scores
		for(int i=0; i<=max; i++){
			p.setColor(i==selected ? csrColor : bgColor);
			canvas.drawRoundRect(new RectF(X+w0*i, Y, X+w0*(i+1)-2, Y+H), h0/5, h0/5, p);
			p.setColor(txtColor);
			drawText(canvas, p, X+w0*i, Y, w0-2, h0, ""+i, size);
		}
		for(int i=max+1; i<=10; i++){
			p.setColor(bgColor);
			canvas.drawRoundRect(new RectF(X+w0*i, Y, X+w0*(i+1)-2, Y+H), h0/5, h0/5, p);
//			p.setColor(txtColor);
//			drawText(canvas, p, X+w0*i, Y, w0-2, h0, ""+i, size);
		}
	}
	
	public static interface OnInputListener{
		public void onInput(int i);
	}
	
	OnInputListener onInput = null;

	public void setOnInputListener(OnInputListener l){
		onInput = l;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		super.onTouchEvent(e);
		int x = (int) e.getX();
		int y = (int) e.getY();
		int a = e.getAction();
		int touchFrm = Math.max(0, Math.min((int)((x-X)/w0), 10));
		if(touchFrm <= max){
			switch(a){
			case MotionEvent.ACTION_DOWN :
			case MotionEvent.ACTION_MOVE :
				selected = touchFrm;
				break;
			case MotionEvent.ACTION_UP :
				selected = -1;
				if(onInput != null)
					onInput.onInput(touchFrm);
				break;
			}
		}
		invalidate();
		return true;
	}
	
	private void drawText(Canvas c, Paint p, float x, float y, float w, float h, String text, float textSize){
		Rect bounds = new Rect();
		p.setTextSize(textSize);
		p.getTextBounds(text, 0, text.length(), bounds);
		c.drawText(text, x+(w-bounds.width())/2, y+(h+bounds.height())/2, p);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        setMeasuredDimension(widthSize, widthSize/11);
	}
}
