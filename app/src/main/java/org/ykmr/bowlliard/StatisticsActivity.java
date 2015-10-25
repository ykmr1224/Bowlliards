package org.ykmr.bowlliard;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class StatisticsActivity extends Activity {
	ScoreChartView chart;
	
	private View titleLine(String str){
        TextView text = new TextView(this);
        text.setText(str);
        text.setBackgroundColor(Color.DKGRAY);
        text.setTextColor(Color.WHITE);
        text.setTextSize(12);
        text.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        text.setPadding(10, 0, 0, 0);
        return text;
	}

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Log.d("log", "onCreate");
        Context context = getApplicationContext();

		BowlliardData data = BowlliardData.getInstance(this);
		data.sortByTime(-1);
        
        ScrollView scroll = new ScrollView(this);
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        
        layout.addView(titleLine("HISTORY"));
        
        chart = new ScoreChartView(this);
        float[] sc = new float[20];
        for(int i=0; i<20 && i<data.size(); i++){
        	sc[19-i] = data.get(i).score.sum[9];
        }
        chart.addData(sc, true);
        
        layout.addView(chart);
        
        layout.addView(createItemList(data));
        
        scroll.addView(layout);
        
        setContentView(scroll);
	}
	

	private View createItemList(BowlliardData data){
		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setOrientation(LinearLayout.VERTICAL);
        
		List<StatHelper.StatItem> stats = new ArrayList<StatHelper.StatItem>();
		stats.add(new StatHelper.RankStat());
		stats.add(new StatHelper.AverageStat());
		stats.add(new StatHelper.ShootAverageStat());
		stats.add(new StatHelper.StrikeStat());
		stats.add(new StatHelper.SpareStat());

		for(StatHelper.StatItem s : stats){s.init();}

		for(int i=0; i<data.size(); i++){
			ScoreModel model = data.get(i).getScore();
			for(StatHelper.StatItem s : stats){
				s.input(model);
			}
			if(i==4 || i == 19 || i == data.size()-1){
				if(i==data.size()-1){
			        layout.addView(titleLine("ALL GAMES"));
					layout.addView(StatHelper.createItem(getApplicationContext(), layout, "Games", ""+(i+1)));
					for(StatHelper.StatItem s : stats){
						layout.addView(StatHelper.createItem(getApplicationContext(), layout, s.getName(), s.getResultAsString()));
					}
				}else{
			        layout.addView(titleLine("LAST "+(i+1)+" GAMES"));
					for(StatHelper.StatItem s : stats){
						layout.addView(StatHelper.createItem(getApplicationContext(), layout, s.getName(), s.getResultAsString()));
					}
				}
			}
		}
		
        return layout;
	}
	
}
