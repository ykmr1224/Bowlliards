package org.ykmr.bowlliard;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class BowlliardData {
	private static BowlliardData instance = null;
	private Activity context = null;
	private List<BowlliardDataItem> list = new LinkedList<BowlliardDataItem>();
	private HashMap<String, BowlliardDataItem> map = new HashMap<String, BowlliardDataItem>();
	
	public static final String SAVE_FILE = "data.csv";
	
	public int size(){
		return list.size();
	}
	
	public BowlliardDataItem get(int i){
		return list.get(i).copy();
	}
	
	public BowlliardDataItem getById(String id){
		return map.get(id).copy();
	}
/*	
	private void sort(){
		Collections.sort(list, new Comparator<BowlliardDataItem>() {
			public int compare(BowlliardDataItem a, BowlliardDataItem b) {
				return (int)(b.date.getTime()-a.date.getTime());
			}
		});
	}*/
	
	public void save() throws IOException{
		Log.d("action", "save");
//		sort();
		OutputStream out;
		out = context.openFileOutput(SAVE_FILE, Activity.MODE_PRIVATE);
		CSVWriter writer = new CSVWriter(new OutputStreamWriter(out));
		for(BowlliardDataItem d : list)
			writer.writeNext(d.getSerialized());
		writer.close();
	}
	
	private static String createId(Date date){
		return ""+date.getTime()+Integer.toString(((int)(Math.random()*999)+1000)).substring(1);
	}
	
	public void restore() throws IOException{
		Log.d("action", "restore");
		InputStream in;
		in = context.openFileInput(SAVE_FILE);
		CSVReader reader = new CSVReader(new InputStreamReader(in));
		list.clear();
		map.clear();
		String[] d;
		while((d=reader.readNext()) != null){
			if(d.length == 3){
				BowlliardDataItem item = new BowlliardDataItem();
				item.setDateSerialized(d[0]);
				item.setScoreSerialized(d[1]);
				item.setComment(d[2]);
				item.setId(createId(item.getDate()));
				list.add(item);
				map.put(item.getId(), item);
			}else if(d.length == 4){
				BowlliardDataItem item = new BowlliardDataItem();
				item.setId(d[0]);
				item.setDateSerialized(d[1]);
				item.setScoreSerialized(d[2]);
				item.setComment(d[3]);
				list.add(item);
				map.put(item.getId(), item);
			}else{
				Log.d("log", Arrays.toString(d));
				break;
			}
		}
	}
	
	public static BowlliardData getInstance(Activity a){
		if(instance == null){
			instance = new BowlliardData();
			instance.context = a;
			try {
				instance.restore();
			} catch (IOException e) {
				Toast.makeText(a, "Score Restore Failed.", Toast.LENGTH_LONG);
				e.printStackTrace();
			}
		}
		return instance;
	}
	
	public String append(BowlliardDataItem item) throws IOException{
		if(item.id==null)
			item.id = createId(item.getDate());
		Log.d("action", "append : " + item.getId());
		list.add(item);
		map.put(item.getId(), item);
		save();
		return item.id;
	}
	
	public void delete(BowlliardDataItem item) throws IOException{
		Log.d("action", "delete : " + item.getId());
		list.remove(item);
		map.remove(item);
		save();
	}
	
	public void delete(String id) throws IOException{
		BowlliardDataItem item = map.get(id);
		delete(item);
	}
	
	public void modify(BowlliardDataItem item) throws IOException{
		Log.d("action", "modify : " + item.getId());
		BowlliardDataItem i = map.get(item.getId());
		i.setDate(item.getDate());
		i.setScoreSerialized(item.getScoreSerialized());
		i.setComment(item.getComment());
		save();
	}
	
	public void sortByTime(){
		sortByTime(1);
	}
	public void sortByTime(final int order){
		Collections.sort(list, new Comparator<BowlliardDataItem>(){
			public int compare(BowlliardDataItem item0, BowlliardDataItem item1) {
				long res = (item0.getDate().getTime() - item1.getDate().getTime());
				return order * (res > 0 ? 1 : (res < 0 ? -1 : 0));
			}
		});
	}

	public void sortByScore(){
		sortByScore(1);
	}
	public void sortByScore(final int order){
		Collections.sort(list, new Comparator<BowlliardDataItem>(){
			public int compare(BowlliardDataItem item0, BowlliardDataItem item1) {
				return order * (item0.getScore().getResult() - item1.getScore().getResult());
			}
		});
	}
}
