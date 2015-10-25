package org.ykmr.bowlliard;

import java.util.Date;

public class BowlliardDataItem {
	String id;
	Date date;
	ScoreModel score;
	String comment;
	
	public BowlliardDataItem() {
		id = null;
		date = new Date();
		score = new ScoreModel();
		comment = "";
	}
	public String getId(){
		return id;
	}
	public Date getDate(){
		return date;
	}
	
	public ScoreModel getScore(){
		return score;
	}
	
	public String getDateSerialized(){
		return date.toGMTString();
	}
	public String getScoreSerialized(){
		return score.serialize();
	}
	public String getComment(){
		return comment;
	}
	public void setId(String id){
		this.id = id;
	}
	public void setDate(Date d){
		date = (Date) d.clone();
	}
	public void setScore(ScoreModel m){
		score = new ScoreModel();
		score.deserialize(m.serialize());
	}
	public void setDateSerialized(String s){
		date = new Date(Date.parse(s));
	}
	public void setScoreSerialized(String s){
		score.deserialize(s);
	}
	public void setComment(String s){
		comment = s;
	}

	public String[] getSerialized(){
		return new String[]{id, getDateSerialized(), getScoreSerialized(), comment};
	}
	
	public BowlliardDataItem copy(){
		BowlliardDataItem res = new BowlliardDataItem();
		res.setId(getId());
		res.setDate(getDate());
		res.setScoreSerialized(getScoreSerialized());
		res.setComment(getComment());
		return res;
	}
}
