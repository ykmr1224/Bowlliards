package org.ykmr.bowlliard;

import java.io.IOException;
import java.util.Date;

import org.ykmr.bowlliard.R;
import org.ykmr.bowlliard.ScoreInput.OnInputListener;
import org.ykmr.bowlliard.ScoreView.OnFrameChangedListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ScoreEditActivity extends Activity implements OnDateSetListener, OnTimeSetListener{
	String id = null;
	ScoreView score;
	ScoreModel model;
	
	Button dateButton;
	Button timeButton;
	NumListAdapter adapter;
	GridView grid;
	EditText comment;
	ScoreInput input;

	Date date = new Date();
	
	public static final String MODE = "MODE";
	public static final String PARAM_ID = "ID";
	public static final String PREF_ID = "ID";
	public static final String PREF_SCORE = "SCORE";
	public static final String PREF_DATE = "DATE";
	public static final String PREF_COMMENT = "COMMENT";
	public static final String PREF_CURSOR = "CURSOR";
	
	private void constructUI(){
        setContentView(R.layout.scoreedit);
        
        dateButton = (Button)findViewById(R.id.DateButton);
        timeButton = (Button)findViewById(R.id.TimeButton);
        dateButton.setOnClickListener(new OnClickListener() {
        	public void onClick(View v){
        		DatePickerDialog dialog = new DatePickerDialog(ScoreEditActivity.this, ScoreEditActivity.this, date.getYear()+1900, date.getMonth(), date.getDate());
        		dialog.show();
			}
		});
        timeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
        		TimePickerDialog dialog = new TimePickerDialog(ScoreEditActivity.this, ScoreEditActivity.this, date.getHours(), date.getMinutes(), true);
        		dialog.show();
			}
		});
        refleshDate();

        score = (ScoreView)findViewById(R.id.ScoreView);
        model = new ScoreModel();
        score.setModel(model);
        
//        grid = (GridView)findViewById(R.id.NumInputGrid);
//        genInputView(getApplicationContext(), grid);
        input = (ScoreInput)findViewById(R.id.ScoreInput);
        input.setOnInputListener(new OnInputListener() {
			public void onInput(int i) {
				score.input(i);
			}
		});
        
        score.setOnFrameChangedListener(new OnFrameChangedListener() {
			public void onFrameChanged(int frm) {
				input.setMax(score.getInputMax());
			}
		});
        
        comment = (EditText)findViewById(R.id.CommentEdit);
	}
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("log", "onCreate");

        constructUI();
        
        String intent_id = getIntent().getStringExtra(PARAM_ID);

        if(savedInstanceState != null && savedInstanceState.getString(PREF_DATE)!=null){
	        id = savedInstanceState.getString(PREF_ID);
	        
	        String serial = savedInstanceState.getString(PREF_SCORE);
	        if(serial != null)
	        	model.deserialize(serial);
	
	        String dateStr = savedInstanceState.getString(PREF_DATE);
	        if(dateStr != null)
				date.setTime(Date.parse(dateStr));
			refleshDate();
			
			String commentText = savedInstanceState.getString(PREF_COMMENT);
			if(commentText != null)
				comment.setText(commentText);
			
			int cursor = savedInstanceState.getInt(PREF_CURSOR, 0);
			score.setCursor(cursor);
        }else if(intent_id != null){
        	BowlliardDataItem item = BowlliardData.getInstance(this).getById(intent_id);
        	id = item.getId();
        	date.setTime(item.getDate().getTime());
        	refleshDate();
        	comment.setText(item.getComment());
        	model.deserialize(item.getScoreSerialized());
			score.setCursor(20);//set cursor to the last frame
        }
    }

	private View genInputView(Context c, GridView grid){
//    	grid.setNumColumns(6);
    	Integer[] list = new Integer[11];
    	for(int i=0; i<=10; i++) list[i] = i;
    	grid.setAdapter(adapter = new NumListAdapter(c, R.layout.listitem, list));
        grid.setOnItemClickListener(new OnItemClickListener(){
        	public void onItemClick(AdapterView<?> parent, View view, int position,	long id) {
        		GridView listView = (GridView)parent;
        		Integer item = (Integer)listView.getItemAtPosition(position);
        		score.input(item);
        	}
        });
        return grid;
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		Log.d("log", "onSaveInstanceState");
		String serial = model.serialize();
		outState.putString(PREF_ID, id);
		outState.putString(PREF_SCORE, serial);
		outState.putString(PREF_DATE, date.toGMTString());
		outState.putString(PREF_COMMENT, comment.getText().toString());
		outState.putInt(PREF_CURSOR, score.cursor);
	}
	
    class NumListAdapter extends ArrayAdapter<Integer>{
		private LayoutInflater inflator;
		int maxEnabled = 10;

		public NumListAdapter(Context context, int textViewResourceId, Integer[] list) {
			super(context, textViewResourceId, list);
			this.inflator = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}
		
		public void setMaxEnabled(int max){
			this.maxEnabled = max;
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Log.d("debug", "getView("+position+",..)");
			if(convertView == null){
				convertView = inflator.inflate(R.layout.listitem, null);
			}
			TextView text = (TextView)convertView.findViewById(R.id.TextView01);
			int i = getItem(position);
			text.setText(""+getItem(position));
			
			if(i>maxEnabled) convertView.setEnabled(false);

			return convertView;
		}
    }
    
    public void refleshDate(){
		dateButton.setText(DateFormat.format(Constants.DAY_FORMAT, date));
    	timeButton.setText(DateFormat.format(Constants.TIME_FORMAT, date));
    }

	public void onDateSet(DatePicker view, int year, int monthOfYear,int dayOfMonth) {
		date.setYear(year-1900);
		date.setMonth(monthOfYear);
		date.setDate(dayOfMonth);
		refleshDate();
	}

	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		date.setHours(hourOfDay);
		date.setMinutes(minute);
		refleshDate();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		save();
	}
	
	private void save(){
		BowlliardDataItem item = new BowlliardDataItem();
		item.setComment(comment.getText().toString());
		item.setDate(date);
		item.setScore(model);
		try {
			if(id != null){
				item.setId(id);
				BowlliardData.getInstance(this).modify(item);
			}else{
				this.id = BowlliardData.getInstance(this).append(item);
			}
			Toast.makeText(this, "Saved", Toast.LENGTH_LONG);
		} catch (IOException e) {
			Toast.makeText(this, "Save Failed", Toast.LENGTH_LONG);
			e.printStackTrace();
		}
		BowlliardData.getInstance(this).sortByTime(-1);
	}
	
	public void onSave(){
		save();
		Intent result = new Intent();
		result.putExtra(PARAM_ID, this.id);
		setResult(RESULT_OK, result);
		finish();
	}
	
	public void onClear(){
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Confirm Clear");
		b.setMessage("Do you really want to clear?");
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				model.clearScore();
				score.setCursor(0);
				comment.setText("");
				date = new Date();
				refleshDate();
				dialog.dismiss();
			}
		});
		b.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		b.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflator = getMenuInflater();
		inflator.inflate(R.menu.scoreedit_menu, menu);
    	return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
		case R.id.SendItem:
			BowlliardsStatics.intentSend(this, model);
			break;
		case R.id.SaveItem:
			onSave();
			break;
		case R.id.ClearItem:
			onClear();
			break;
		case R.id.TwitterItem:
			BowlliardsStatics.IntentTweet(this, id);
			break;
		}
		return true;
	}

}
