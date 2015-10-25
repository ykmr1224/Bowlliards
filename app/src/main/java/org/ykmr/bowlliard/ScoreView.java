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

public class ScoreView extends View{
	int X=2;
	int Y=2;
	int W=400;
	int H=50;
	float w0=W/21;
	float h0=H/5;
	int cursor = 0;
	ScoreModel m;
	int bgColor = Color.DKGRAY;
	int frmColor = Color.LTGRAY;
	int txtColor = Color.LTGRAY;
	int csrColor = 0xff777744;
	
	boolean editable = true;
	
	public ScoreView(Context context, AttributeSet attributes){
		super(context, attributes);
		init(new ScoreModel());
	}

	public ScoreView(Context context, ScoreModel model) {
		super(context);
		Log.d("log", "BowlardScoreView");
		init(model);
	}
	
	private void init(ScoreModel model){
		m = model;
	}
	
	public ScoreView(Context context){
		this(context, new ScoreModel());
	}
	
	public void setModel(ScoreModel model){
		this.m = model;
	}
	
	public void setEditable(boolean b){
		this.editable = b;
	}

	protected void drawSum(Canvas c, Paint p, int frm, int sum, float size){
		if(sum>=0){
			if(frm == 9){
				drawText(c, p, X+w0*2*frm, Y+h0*3, w0*3, h0*2, ""+sum, size);
			}else{
				drawText(c, p, X+w0*2*frm, Y+h0*3, w0*2, h0*2, ""+sum, size);
			}
		}
	}
	protected void drawScore(Canvas c, Paint p, int frm, int score, float size){
		if(score == 0 && frm%2==0)
			drawText(c, p, X+w0*frm, Y+h0, w0, h0*2, "G", size);
		else if(score >= 0)
			drawText(c, p, X+w0*frm, Y+h0, w0, h0*2, ""+score, size);
	}
	protected void drawStrike(Canvas c, Paint p, int frm){
		Path path = new Path();
		path.moveTo(X+w0*frm, Y+h0);
		path.lineTo(X+w0*(frm+1), Y+h0*3);
		path.lineTo(X+w0*(frm+1), Y+h0);
		path.lineTo(X+w0*frm, Y+h0*3);
		path.setFillType(FillType.EVEN_ODD);
		c.drawPath(path, p);
	}
	protected void drawSpare(Canvas c, Paint p, int frm){
		Path path = new Path();
		path.moveTo(X+w0*(frm+1), Y+h0);
		path.lineTo(X+w0*(frm+1), Y+h0*3);
		path.lineTo(X+w0*frm, Y+h0*3);
		path.setFillType(FillType.EVEN_ODD);
		c.drawPath(path, p);
	}
	
	protected int calcTextSize(Paint p){
		int res = (int)(h0*2);
		while(true){
			Rect rect = new Rect();
			p.setTextSize(res);
			p.getTextBounds("000", 0, 3, rect);
//			Log.d("log", "calcTextSize : " + res + " :: "+rect.toString());
			if(rect.width() < w0*2-6 && rect.height() < h0*2-2) break;
			res--;
		}
		return res;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d("log", "onDraw : ");
		logScore();
		
		W = getWidth()-4;
		H = (W-4)/8;
		w0 = W/21f;
		h0 = H/5f;
		
		Paint p = new Paint();
        p.setAntiAlias(true);
        p.measureText("H");
        int size = calcTextSize(p);
        
		p.setColor(bgColor);
		canvas.drawRoundRect(new RectF(X, Y, X+W, Y+H), h0, h0, p);
		if(editable){
			p.setColor(csrColor);
			canvas.drawRect(X+w0*cursor, Y+h0, X+w0*(cursor+1), Y+h0*3, p);
		}
		p.setColor(frmColor);
		//draw vertical lines
		for(int i=0; i<9; i++){
			canvas.drawLine(X+w0*(2*i+1), Y+h0, X+w0*(2*i+1), Y+h0*3, p);
			canvas.drawLine(X+w0*(2*i+2), Y, X+w0*(2*i+2), Y+H, p);
		}
		canvas.drawLine(X+w0*19, Y+h0, X+w0*19, Y+h0*3, p);
		canvas.drawLine(X+w0*20, Y+h0, X+w0*20, Y+h0*3, p);
		
		//draw horizontal lines
//		canvas.drawLine(X, Y, X+W, Y, p);
		canvas.drawLine(X, Y+h0, X+W, Y+h0, p);
		canvas.drawLine(X, Y+h0*3, X+W, Y+h0*3, p);
//		canvas.drawLine(X, Y+h0*5, X+W, Y+h0*5, p);
		
		p.setSubpixelText(true);
		p.setTypeface(Typeface.SANS_SERIF);
		//draw scores
		for(int i=0; i<9; i++){
			drawText(canvas, p, X+w0*i*2, Y, w0*2, h0, ""+(i+1), size/2);
			if(m.getScore(i*2)==10){//strike
				drawStrike(canvas, p, i*2);
			}else{
				drawScore(canvas, p, i*2, m.getScore(i*2), size);
				if(m.isSpare(i)) drawSpare(canvas, p, i*2+1);
				else drawScore(canvas, p, i*2+1, m.getScore(i*2+1), size);
			}
		}
		drawText(canvas, p, X+w0*9*2, Y, w0*3, h0, "10", size/2);
		if(m.isStrike(9)){
			drawStrike(canvas, p, 18);
			if(m.isStrike(10)){
				drawStrike(canvas, p, 19);
				if(m.isStrike(11)) drawStrike(canvas, p, 20);
				else drawScore(canvas, p, 20, m.getScore(22), size);
			}else{
				drawScore(canvas, p, 19, m.getScore(20), size);
				if(m.isSpare(10)) drawSpare(canvas, p, 20);
				else drawScore(canvas, p, 20, m.getScore(21), size);
			}
		}else{
			drawScore(canvas, p, 18, m.getScore(18), size);
			if(m.isSpare(9)){
				drawSpare(canvas, p, 19);
				if(m.isStrike(10)) drawStrike(canvas, p, 20);
				else drawScore(canvas, p, 20, m.getScore(20), size);
			}else{
				drawScore(canvas, p, 19, m.getScore(19), size);
			}
		}
		for(int i=0; i<10; i++){
			drawSum(canvas, p, i, m.getSum(i), size);
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e) {
		super.onTouchEvent(e);
		if(!editable) return false;
		requestFocus();
		int x = (int) e.getX();
		int y = (int) e.getY();
		
		int touchFrm = Math.max(0, Math.min((int)((x-X)/w0), 20));
		if(touchFrm == 0){
			cursor = 0;
		}else{
			for(cursor=0; cursor<touchFrm; cursor++){
				if(m.getScore(cursor)==-1) break;
			}
		}
		fireOnFrameChanged(cursor);
		invalidate();
		return true;
	}
	
	public void setCursor(int frm){
		for(cursor=0; cursor<frm; cursor++){
			if(m.getScore(cursor)==-1) break;
		}
		fireOnFrameChanged(cursor);
	}
	
	void logScore(){
		StringBuffer buff = new StringBuffer();
		for(int i=0; i<24; i++){
			buff.append(" ");
			buff.append(m.getScore(i));
		}
		Log.d("log", "score : " + buff.toString());
	}
	
	public void input(int k){
		Log.d("log", "input : " + k);
		if(!editable) return;
		if(0 <= k && k <= 10){
			if(cursor<18)
				cursor+=m.setScore(cursor, k);
			else{
				if(cursor==18){
					m.setScore(18, k);
				}else if(cursor==19){
					if(m.isStrike(9)) m.setScore(20, k);
					else m.setScore(19, k);
				}else if(cursor==20){
					if(m.isStrike(9)){
						if(m.isStrike(10)) m.setScore(22, k);
						else m.setScore(21, k);
					}else{
						m.setScore(20, k);
					}
				}
				cursor++;
			}
		}
		if(cursor>20) cursor = 20;
		logScore();
		fireOnFrameChanged(cursor);
		invalidate();
	}
	
	public static interface OnFrameChangedListener{
		public void onFrameChanged(int frm);
	}
	
	private OnFrameChangedListener onFrameChanged = null;
	
	public void setOnFrameChangedListener(OnFrameChangedListener l){
		onFrameChanged = l;
	}
	
	private void fireOnFrameChanged(int frm){
		if(onFrameChanged != null) onFrameChanged.onFrameChanged(frm);
	}
	
	public int getInputMax(){
		if(cursor <= 18){
			if(cursor%2 == 0){//1st throw
				return 10;
			}else{
				return 10-m.getScore(cursor-1);
			}
		}else if(cursor == 19){
			if(m.isStrike(9)){
				return 10;
			}else{
				return 10-m.getScore(18);
			}
		}else if(cursor == 20){
			if(m.isStrike(9)){
				if(m.isStrike(10)) return 10;
				else return 10-m.getScore(20);
			}else if(m.isSpare(9)){
				return 10;
			}else{
				return 0;
			}
		}else{
			return -1;
		}
	}
	
	private void drawText(Canvas c, Paint p, float x, float y, float w, float h, String text, float textSize){
		Rect bounds = new Rect();
		p.setTextSize(textSize);
		p.getTextBounds(text, 0, text.length(), bounds);
		c.drawText(text, x+(w-bounds.width())/2, y+(h+bounds.height())/2, p);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		int k = keyCode - KeyEvent.KEYCODE_0;
		Log.d("log", "key : " + keyCode);
		input(k);
		invalidate();
		return true;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        setMeasuredDimension(widthSize, widthSize/8);
	}
}