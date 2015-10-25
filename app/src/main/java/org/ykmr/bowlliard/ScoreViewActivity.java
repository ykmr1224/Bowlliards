package org.ykmr.bowlliard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.ykmr.bowlliard.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ScoreViewActivity extends Activity{
	String id;
	ScoreView score;
	ScoreModel model;
	TextView dateText;
	TextView commentText;

	public static final String PARAM_ID = "ID";
	
	@SuppressWarnings("deprecation")
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("log", "onCreate");
        
        id = getIntent().getExtras().getString(PARAM_ID);
        Log.d("log", "id : "+ id);
        if(id == null)
        	finish();

        BowlliardDataItem data = BowlliardData.getInstance(this).getById(id);

        setContentView(R.layout.scoreview);
        
        dateText = (TextView)findViewById(R.id.PlayDateText);
		dateText.setText(DateFormat.format(Constants.DATE_FORMAT, data.getDate()));

        score = (ScoreView)findViewById(R.id.ScoreView);
        model = data.getScore();
        score.setModel(model);
        score.setEditable(false);
        
        commentText = (TextView)findViewById(R.id.CommentText);
        commentText.setText(data.getComment());
    }
	
/*	@Override
	public void onAttachedToWindow() {
	    super.onAttachedToWindow();
	    openOptionsMenu();
	}*/

	@Override
	protected void onResume() {
		super.onResume();
        BowlliardDataItem data = BowlliardData.getInstance(this).getById(id);
		dateText.setText(DateFormat.format(Constants.DATE_FORMAT, data.getDate()));
        model = data.getScore();
        score.setModel(model);
        commentText.setText(data.getComment());

        LinearLayout linear = (LinearLayout)findViewById(R.id.StatisticsLinear);
        linear.removeAllViews();
		List<StatHelper.StatItem> stats = new ArrayList<StatHelper.StatItem>();
		stats.add(new StatHelper.RankStat());
		stats.add(new StatHelper.ShootAverageStat());
		stats.add(new StatHelper.StrikeStat());
		stats.add(new StatHelper.SpareStat());

		for(StatHelper.StatItem s : stats){
			s.init();
			s.input(model);
			linear.addView(StatHelper.createItem(getApplicationContext(), linear, s.getName(), s.getResultAsString()));
		}
	}
	
	protected void onEdit(){
		Intent intent = new Intent(getApplicationContext(), ScoreEditActivity.class);
		intent.putExtra(ScoreEditActivity.PARAM_ID, id);
		startActivity(intent);
	}
	
	protected void onDelete(){
		AlertDialog.Builder b = new AlertDialog.Builder(this);
		b.setTitle("Confirm Delete");
		b.setMessage("Do you really want to delete?");
		b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				try {
					BowlliardData.getInstance(ScoreViewActivity.this).delete(id);
				} catch (IOException e) {
					e.printStackTrace();
				}
				finish();
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
		inflator.inflate(R.menu.scoreview_menu, menu);
    	return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
		case R.id.SendItem:
			BowlliardsStatics.intentSend(this, model);
			break;
		case R.id.TwitterItem:
			BowlliardsStatics.IntentTweet(this, id);
			break;
		case R.id.EditItem:
			onEdit();
			break;
		case R.id.DeleteItem:
			onDelete();
			break;
		}
		return true;
	}

}
