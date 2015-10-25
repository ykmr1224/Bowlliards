package org.ykmr.bowlliard;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ScoreListActivity extends Activity {
	public static final int SORT_TIME_A = 1;
	public static final int SORT_TIME_D = 2;
	public static final int SORT_SCORE_A = 3;
	public static final int SORT_SCORE_D = 4;
	
	ListView list;
	ScoreAdapter adapter;
	BowlliardData data;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("log", "onCreate");
		
		setContentView(R.layout.scorelist);
		list = (ListView)findViewById(R.id.ScoreList);

		adapter = new ScoreAdapter(getApplicationContext(), R.layout.dataitem);
		data = BowlliardData.getInstance(this);
		data.sortByTime(-1);

		list.setAdapter(adapter);

		list.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BowlliardDataItem item = (BowlliardDataItem) adapter
						.getItem(position);
				BowlliardsStatics.IntentViewGame(ScoreListActivity.this, item.getId());
			}
		});
		list.setEmptyView(findViewById(R.id.empty));
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(requestCode == BowlliardsStatics.CODE_NEWGAME){
			if(resultCode == RESULT_OK && data != null){
				String id = data.getStringExtra(ScoreEditActivity.PARAM_ID);
				if(id != null){
					BowlliardsStatics.IntentViewGame(this, id);
				}
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d("log", "onResume");
		adapter.notifyDataSetChanged();
	}

	class ScoreAdapter extends BaseAdapter {
		LayoutInflater inflator;

		public ScoreAdapter(Context context, int resid) {
			this.inflator = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = inflator.inflate(R.layout.dataitem, null);
			}
			ScoreView score = (ScoreView) convertView
					.findViewById(R.id.ListScoreView);
			TextView date = (TextView) convertView
					.findViewById(R.id.ListDateText);

			BowlliardDataItem item = (BowlliardDataItem) getItem(position);
			score.setModel(item.getScore());
			score.setEditable(false);

			date.setText(DateFormat.format(Constants.DATE_FORMAT, item.getDate()));

			return convertView;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		public int getCount() {
			return data.size();
		}

		public Object getItem(int position) {
			return data.get(position);
		}

		public long getItemId(int position) {
			return position;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflator = getMenuInflater();
		inflator.inflate(R.menu.scorelist_menu, menu);
    	return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch(item.getItemId()){
		case R.id.bytime_asc :
			data.sortByTime(1);
			break;
		case R.id.bytime_desc :
			data.sortByTime(-1);
			break;
		case R.id.byscore_asc :
			data.sortByScore(1);
			break;
		case R.id.byscore_desc :
			data.sortByScore(-1);
			break;
		case R.id.NewItem :
			BowlliardsStatics.IntentNewGame(this);
			break;
		case R.id.StatItem :
			BowlliardsStatics.IntentStatistics(this);
			break;
		case R.id.RatingItem :
			Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=org.ykmr.bowlliard"));
			startActivity(intent);
			break;
		}
		adapter.notifyDataSetChanged();
		return true;
	}
}
