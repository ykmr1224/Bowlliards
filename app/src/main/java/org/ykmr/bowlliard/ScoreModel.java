package org.ykmr.bowlliard;

import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

public class ScoreModel{
	int[] score = new int[24];
	int[] sum = new int[10];
	public ScoreModel() {
		clearScore();
	}
	public void clearScore(){
		Arrays.fill(score, -1);
		Arrays.fill(sum, -1);
	}
	public int setScore(int i, int s){
		if(i<0 || 22<i) return 0;
		if(s<0 || 10<s) return 0;
		if(i==20 && score[18]!=10&&score[18]+score[19]!=10) return 0;
		if(i==21 && score[18]!=10) return 0;
		if(i==22 && (score[18]!=10||score[20]!=10)) return 0;
		int res = 1;
		if(i>=18) for(int j=i+1; j<24; j++) score[j]=-1;
		if(i%2==0){
			score[i] = s;
			if(score[i+1]>=0) score[i+1] = 0;
			if(s==10){
				score[i+1] = 0;
				res = 2;
			}
		}else{
			if(score[i-1]==10 || score[i-1]+s>10) return 0;
			else score[i] = s;
		}
		recalc();
		return res;
	}
	public int getScore(int i){
		return score[i];
	}
	public int getSum(int i){
		return sum[i];
	}
	public int getResult(){
		return Math.max(0, sum[9]);
	}
	public boolean isStrike(int frm){
		return score[frm*2] == 10;
	}
	public boolean isSpare(int frm){
		return score[frm*2]!=10 && score[frm*2]+score[frm*2+1] == 10;
	}
	public boolean isInputed(int frm){
		return score[frm*2]!=-1;
	}
	private void recalc(){
		Arrays.fill(sum, -1);
		int s = 0;
		for(int i=0; i<10 && score[i] != -1; i++){
			if(score[2*i]<0) break;
			if(score[2*i]==10){//strike
				if(score[2*i+2]==10){//double
					if(score[2*i+4]>=0){
						sum[i] = s+20+score[2*i+4];
					}else{
						break;
					}
				}else if(score[2*i+2]>=0){
					if(score[2*i+3]>=0){
						sum[i] = s+10+score[2*i+2]+score[2*i+3];
					}else{
						break;
					}
				}else{
					break;
				}
			}else if(score[2*i+1]<0){
				break;
			}else if(score[2*i]+score[2*i+1]==10){//spare
				if(score[2*i+2]<0){
					break;
				}else{
					sum[i] = s+10+score[2*i+2];
				}
			}else{
				sum[i] = s+score[2*i]+score[2*i+1];
			}
			s = sum[i];
		}
	}
	public String serialize(){
		StringBuffer res = new StringBuffer();
		for(int i:score){
			res.append(i);
			res.append(" ");
		}
		return res.toString();
	}
	public void deserialize(String serial){
		ArrayList<Integer> list = new ArrayList<Integer>(24);
		int a = 0;
		Log.d("log", "deserialize:"+serial);
		for(int i=0; i<serial.length(); i++){
			if(serial.charAt(i)==' '){
				list.add(Integer.valueOf(serial.substring(a, i)));
				a = i+1;
			}
		}
		if(list.size()==24){
			for(int i=0; i<24; i++){
				score[i] = list.get(i);
			}
			recalc();
		}else{
			Log.d("log", "deserialize: failed");
		}
	}
	
	private String createFrameFirstString(int score){
		if(score == 10) return "X";
		else if(score == 0) return "G";
		else return ""+score;
	}
	private String createFrameString(int i){
		if(isStrike(i)){
			return "X";
		}else{
			StringBuffer res = new StringBuffer();

			if(getScore(i*2) == 0) res.append("G");
			else res.append(getScore(i*2));
			
			if(isSpare(i)) res.append("/");
			else if(getScore(i*2+1) == 0) res.append("-");
			else res.append(getScore(i*2+1));
			
			return res.toString();
		}
	}
	public String createScoreString(){
		StringBuffer res = new StringBuffer();
		for(int i=0; i<9; i++){
			res.append("[");
			res.append(createFrameString(i));
			res.append("]");
			res.append(getSum(i));
//			res.append(" ");
		}
		res.append("[");
		res.append(createFrameString(9));
		if(isStrike(9)){
			res.append(createFrameString(10));
			if(isStrike(10)){
				res.append(createFrameFirstString(getScore(22)));
			}else{
				res.append(createFrameString(10));
			}
		}else if(isSpare(9)){
			res.append(createFrameFirstString(getScore(20)));
		}
		res.append("]");
		res.append(getResult());
		
		return res.toString();
	}
	
	public static String getRank(int score){
		if(score <= 0){
			return "-";
		}else if(score < 25){
			return "C-";
		}else	if(score < 50){
			return "C";
		}else if(score < 75){
			return "C+";
		}else if(score < 100){
			return "B-";
		}else if(score < 125){
			return "B";
		}else if(score < 150){
			return "B+";
		}else if(score < 175){
			return "A-";
		}else if(score < 200){
			return "A";
		}else if(score < 225){
			return "A+";
		}else if(score < 250){
			return "A++";
		}else{
			return "A+++";
		}
	}
}
