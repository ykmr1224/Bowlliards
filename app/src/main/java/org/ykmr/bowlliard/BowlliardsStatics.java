package org.ykmr.bowlliard;

import java.io.File;
import java.io.OutputStream;

import org.ykmr.twitter.TwitterActivity;
import org.ykmr.twitter.TwitterHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

public class BowlliardsStatics {
	public static void IntentStatistics(Activity a){
		Intent intent = new Intent(a.getApplicationContext(),
				StatisticsActivity.class);
		a.startActivity(intent);
	}
	
	public static final int CODE_NEWGAME = 1001;

	public static void IntentNewGame(Activity a){
		Intent intent = new Intent(a.getApplicationContext(),
				ScoreEditActivity.class);
		a.startActivityForResult(intent, CODE_NEWGAME);
	}
	
	public static void IntentViewGame(Activity a, String id){
		Intent intent = new Intent(a.getApplicationContext(),
				ScoreViewActivity.class);
		intent.putExtra(ScoreViewActivity.PARAM_ID, id);
		a.startActivity(intent);
	}

	public static void IntentTweet(Activity a, String id){
		BowlliardDataItem item = BowlliardData.getInstance(a).getById(id);
		if(item == null) return;
		ScoreModel model = item.getScore();
		if(model.getSum(9)<0){
			AlertDialog.Builder b = new AlertDialog.Builder(a);
			b.setTitle("Not Completed.");
			b.setMessage("This score can't be shared. Because this game is not completed yet.");
			b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			b.show();
		}else{
			Intent intent = new Intent(a.getApplicationContext(),
			TwitterActivity.class);
			intent.putExtra(ScoreViewActivity.PARAM_ID, id);
			a.startActivity(intent);
		}
	}

	public static void intentSend(Activity a, ScoreModel model){
		if(model.getSum(9)<0){
			AlertDialog.Builder b = new AlertDialog.Builder(a);
			b.setTitle("Not Completed.");
			b.setMessage("This score can't be shared. Because this game is not completed yet.");
			b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			b.show();
		}else{
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT, "Scored " + model.getResult() + "P " + model.createScoreString() + "(via Bowlliards Score Book) #billiards");
			a.startActivity(Intent.createChooser(intent, "Share this score with..."));
		}
	}

}
