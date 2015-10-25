package org.ykmr.bowlliard;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.Path.FillType;

public class ScoreDrawer {
	Canvas canvas;
	int W, H;
	int X = 0;
	int Y = 0;
	float w0, h0;

	int bgColor = Color.DKGRAY;
	int frmColor = Color.LTGRAY;
	int txtColor = Color.LTGRAY;
	int csrColor = 0xff777744;
	
	public ScoreDrawer(Canvas canvas, int X, int Y, int W, int H){
		this.canvas = canvas;
		this.X = X;
		this.Y = Y;
		this.W = W;
		this.H = H;
		w0 = W/21f;
		h0 = H/5f;
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
	private void drawText(Canvas c, Paint p, float x, float y, float w, float h, String text, float textSize){
		Rect bounds = new Rect();
		p.setTextSize(textSize);
		p.getTextBounds(text, 0, text.length(), bounds);
		c.drawText(text, x+(w-bounds.width())/2, y+(h+bounds.height())/2, p);
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

	public void drawScore(ScoreModel m, boolean editable, int cursor){
		
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
		canvas.drawLine(X, Y+h0, X+W, Y+h0, p);
		canvas.drawLine(X, Y+h0*3, X+W, Y+h0*3, p);
		
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
}
