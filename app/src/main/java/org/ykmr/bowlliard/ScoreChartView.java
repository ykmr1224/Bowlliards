package org.ykmr.bowlliard;

import java.util.ArrayList;
import java.util.List;

import org.ykmr.Util;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

public class ScoreChartView extends View{
	int X=2;
	int Y=2;
	int maxW=Integer.MAX_VALUE;
	int maxH=Integer.MAX_VALUE;
	int W=400;
	int H=30;
	int bgColor = Color.DKGRAY;
	int frmColor = Color.LTGRAY;
	int txtColor = Color.LTGRAY;
	int csrColor = 0xff777744;
	
	float max = 10;
	List<float[]> data = new ArrayList<float[]>();
	List<Boolean> type = new ArrayList<Boolean>();
	
	boolean editable = true;
	
	public ScoreChartView(Context context, AttributeSet attributes){
		super(context, attributes);
	}

	public ScoreChartView(Context context) {
		super(context);
		Log.d("log", "BowlardScoreView");
	}
	
	public void setMaxSize(int w, int h){
		maxW = w;
		maxH = h;
	}
	
	public void setEditable(boolean b){
		this.editable = b;
	}

	public void addData(float[] d, boolean t){
		this.data.add(d);
		this.type.add(t);
		for(int i=0; i<d.length; i++)
			max = Math.max(d[i], max);
	}
	
	protected void drawBarGraph(Canvas c, Paint p, RectF rect, float[] val, float max){
		float X = rect.left;
		float Y = rect.top;
		float W = rect.right-rect.left;
		float H = rect.bottom-rect.top;
		float w0 = W/val.length;
		float ratio = H/max;
		
		for(int j=0; j<val.length; j++){
			float v = val[j];
			if(v<0) v = 0;

			p.setColor(0xffaaaaaa);
			c.drawRect(X+w0*j, Y+H-v*ratio, X+w0*(j+1)-w0/2, Y+H, p);
			
			p.setColor(0xff777777);
			Path path0 = new Path();
			path0.moveTo(X+w0*j, Y+H-v*ratio);
			path0.lineTo(X+w0*j+5, Y+H-v*ratio-5);
			path0.lineTo(X+w0*(j+1)-w0/2+5, Y+H-v*ratio-5);
			path0.lineTo(X+w0*(j+1)-w0/2, Y+H-v*ratio);
			c.drawPath(path0, p);
			
			p.setColor(0xff444444);
			Path path1 = new Path();
			path1.moveTo(X+w0*(j+1)-w0/2, Y+H);
			path1.lineTo(X+w0*(j+1)-w0/2, Y+H-v*ratio);
			path1.lineTo(X+w0*(j+1)-w0/2+5, Y+H-v*ratio-5);
			path1.lineTo(X+w0*(j+1)-w0/2+5, Y+H-5);
			c.drawPath(path1, p);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d("log", "onDraw : ");
		
		W = getWidth()-4;
		H = getHeight()-4;
		
		Paint p = new Paint();
        p.setAntiAlias(true);
        p.measureText("H");
		p.setColor(frmColor);
	
		int unit = (int)Math.pow(10, (int)Math.log10(max));
		float upper = (((int)(max-1)/unit)+1)*unit;
		
		RectF area = new RectF(30, 10, W-10, H-10);
		int n = data.get(0).length;
		float r = (float)area.height()/upper;
		float w0 = W/(float)n;
		
		
		Log.d("log", "unit:"+unit+", upper:"+upper+", r:"+r+", w0:"+w0);
		Log.d("log", Util.join(data.get(0)));
		
		p.setColor(0xffaaaaaa);
		for(int i=unit; i<=upper; i+=unit){
			canvas.drawLine(area.left, area.bottom-i*r, area.right, area.bottom-i*r, p);

			String text = ""+i;
			Rect bounds = new Rect();
			p.getTextBounds(text, 0, text.length(), bounds);
			canvas.drawText(text, area.left-bounds.width()-3, area.bottom-i*r+bounds.height()/2, p);
		}

		canvas.drawLine(area.left, area.top, area.left, area.bottom, p);
		canvas.drawLine(area.left, area.bottom, area.right, area.bottom, p);

		for(int i=0; i<data.size(); i++){
			float[] arr = data.get(i);
			drawBarGraph(canvas, p, area, arr, upper);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        
        setMeasuredDimension(Math.min(widthSize, maxW), Math.min(widthSize*2/5, maxH));
	}
}