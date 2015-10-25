package org.ykmr.bowlliard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatHelper {
	public static interface StatItem{
		public String getName();
		public void init();
		public void input(ScoreModel model);
		public String getResultAsString();
	}
	
	public static  class RankStat implements StatItem{
		int sum = 0;
		int n = 0;
		public String getName(){
			return "Score Rank";
		}
		public String getResultAsString() {
			return ScoreModel.getRank(sum/Math.max(n, 1));
		}

		public void init() {
			sum = n = 0;
		}

		public void input(ScoreModel model) {
			if(model.getSum(9) >= 0){
				n+=1;
				sum += model.getSum(9);
			}
		}
	}

	public static  class AverageStat implements StatItem{
		int sum = 0;
		int n = 0;
		public String getName(){
			return "Average Score";
		}
		public String getResultAsString() {
			return ""+sum/Math.max(n, 1);
		}

		public void init() {
			sum = n = 0;
		}

		public void input(ScoreModel model) {
			if(model.getSum(9) >= 0){
				n+=1;
				sum += model.getSum(9);
			}
		}
	}
	
	public static  class StrikeStat implements StatItem{
		int sum = 0;
		int n = 0;
		public String getName(){
			return "Strike";
		}
		public String getResultAsString() {
			return sum+"("+(sum*100/Math.max(n,1))+"%)";
		}

		public void init() {
			sum = n = 0;
		}

		public void input(ScoreModel model){
			for(int j=0; j<12; j++){
				if(model.isInputed(j)){
					sum += model.isStrike(j) ? 1:0;
					n++;
				}
			}
		}
	}
	
	public static  class SpareStat implements StatItem{
		int sum = 0;
		int n = 0;
		public String getName(){
			return "Spare";
		}
		public String getResultAsString() {
			return sum+"("+(sum*100/Math.max(n,1))+"%)";
		}

		public void init() {
			sum = n = 0;
		}

		public void input(ScoreModel model){
			for(int j=0; j<12; j++){
				if(model.isInputed(j)){
					sum += model.isSpare(j) ? 1:0;
					n++;
				}
			}
		}
	}
	public static  class ShootAverageStat implements StatItem{
		int sum = 0;
		int n = 0;
		public String getName(){
			return "Pocket Average";
		}
		public String getResultAsString() {
			return (sum*100/Math.max(n,1))+"%";
		}

		public void init() {
			sum = n = 0;
		}

		public void input(ScoreModel model){
			for(int i=0; i<12; i++){
				if(model.isInputed(i)){
					if(model.isSpare(i)){
						sum+=10;
						n+=11;
					}else if(model.isStrike(i)){
						sum+=10;
						n+=10;
					}else{
						int temp = model.getScore(i*2) + model.getScore(i*2+1);
						sum += temp;
						n += temp+2;
					}
				}
			}
		}
	}
	
	public static View createItem(Context c, ViewGroup parent, String name, String value){
		final LayoutInflater inflator = (LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View layout = inflator.inflate(R.layout.statitem, null);
		TextView nameView = (TextView)layout.findViewById(R.id.NameText);
		TextView valueView = (TextView)layout.findViewById(R.id.ValueText);
		nameView.setText(name);
		valueView.setText(value);
		return layout;
	}
}
