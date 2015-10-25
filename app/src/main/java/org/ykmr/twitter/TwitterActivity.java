package org.ykmr.twitter;

import java.io.File;
import java.io.OutputStream;

import org.ykmr.bowlliard.BowlliardData;
import org.ykmr.bowlliard.BowlliardDataItem;
import org.ykmr.bowlliard.Constants;
import org.ykmr.bowlliard.R;
import org.ykmr.bowlliard.ScoreDrawer;
import org.ykmr.bowlliard.ScoreModel;
import org.ykmr.bowlliard.R.id;
import org.ykmr.bowlliard.R.layout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Bitmap.Config;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;

public class TwitterActivity extends Activity
		implements OnClickListener, OnCheckedChangeListener, TextWatcher, Runnable{
	String id;
	BowlliardDataItem data;
	ScoreModel model;
	EditText messageEdit;
	CheckBox imageCheck;
	Button tweetButton;
	TextView charCountText;
	String message;
	Bitmap bmp;
	File image;
	TwitterHelper tw;

	public static final String PARAM_ID = "ID";
	public static final String PARAM_STATUS = "STATUS";
	public static final String PARAM_TOKEN = "TOKEN";
	public static final String PARAM_TOKENSECRET = "TOKENSECRET";
	
	public TwitterActivity() {
		super();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.twitter);
		if(savedInstanceState != null && savedInstanceState.getString(PARAM_ID)!=null){
        		id = savedInstanceState.getString(PARAM_ID);
//        		status = savedInstanceState.getInt(PARAM_STATUS);
        }else{
	    		id = getIntent().getExtras().getString(PARAM_ID);
	    		if (id == null) finish();
        }
		tw = TwitterHelper.getInstance(getApplicationContext());
//		if(!tw.needAuthorize())
//		status = tw.getStatus();//STATUS_AUTHORIZED;
		data = BowlliardData.getInstance(this).getById(id);
		if (data == null) finish();
		model = data.getScore();
		
		int r = model.getResult();
		message = "Bowlliards: " + r + "[" + ScoreModel.getRank(r) + "] "
				+ "(" + model.createScoreString() + ")";
		buildUI();
		
//		changeStatus(status);
		updateStatus();
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(PARAM_ID, id);
//		outState.putInt(PARAM_STATUS, status);
//		outState.putString(PARAM_TOKEN, tw.getRequestToken());
//		outState.putString(PARAM_TOKENSECRET, tw.getRequestTokenSecret());
	}

	private void buildUI() {
		((Button)findViewById(R.id.TwitterLoginButton))
				.setOnClickListener(this);
		(tweetButton = (Button) findViewById(R.id.TweetButton))
				.setOnClickListener(this);
		((Button) findViewById(R.id.PINVerifyButton))
				.setOnClickListener(this);
		((Button) findViewById(R.id.PINCancelButton))
				.setOnClickListener(this);
		((Button) findViewById(R.id.ResetButton))
				.setOnClickListener(this);
		imageCheck = (CheckBox) findViewById(R.id.ImageCheck);
		imageCheck.setOnCheckedChangeListener(this);
		charCountText = (TextView) findViewById(R.id.CharCountText);
		messageEdit = (EditText) findViewById(R.id.MessageEdit);
		messageEdit.addTextChangedListener(this);
		if (message != null)
			messageEdit.setText(message);
		
		updateCharCount();
//		if (image == null)
//			imageCheck.setVisibility(View.INVISIBLE);
	}

	public void updateStatus() {
//		this.status = status;
		ViewFlipper flipper = (ViewFlipper) findViewById(R.id.ViewFlipper01);
		switch (tw.getStatus()) {
		case TwitterHelper.STATUS_INITIAL:
			flipper.setDisplayedChild(0);
			messageEdit.setEnabled(false);
			imageCheck.setEnabled(false);
			tweetButton.setEnabled(false);
			break;
		case TwitterHelper.STATUS_START:
			flipper.setDisplayedChild(1);
			EditText pin = (EditText) findViewById(R.id.TwitPINEdit);
			pin.setText("");
			messageEdit.setEnabled(false);
			imageCheck.setEnabled(false);
			tweetButton.setEnabled(false);
			break;
		case TwitterHelper.STATUS_AUTHORIZED:
			flipper.setDisplayedChild(2);
			TextView id = (TextView) findViewById(R.id.LoginNameText);
			String name = tw.getUserName();
			id.setText(name==null?"????":name);
			messageEdit.setEnabled(true);
			messageEdit.setText(message);
			imageCheck.setEnabled(true);
			tweetButton.setEnabled(true);
			if(name == null)
				showError("Can't retrieve account name. Maybe offline.");
			break;
		}
	}

	ProgressDialog dialog;
	PostingThread th;
	
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.TwitterLoginButton:
			if(tw.startAuthorization(this)){
				updateStatus();
			}else{
				Toast.makeText(this, "ERROR : Login failed.", Toast.LENGTH_LONG).show();
			}
			break;
		case R.id.PINVerifyButton:
			if (tw.verifyPIN(this, ((EditText) findViewById(R.id.TwitPINEdit)).getText().toString()))
				updateStatus();
			else
				Toast.makeText(this, "Authorization failed.",
						Toast.LENGTH_SHORT).show();
			break;
		case R.id.PINCancelButton:
			tw.clearAuth();
			updateStatus();
			break;
		case R.id.ResetButton:
			tw.clearAuth();
			updateStatus();
			break;
		case R.id.TweetButton:
			String msg = messageEdit.getText().toString();
			dialog = ProgressDialog.show(this, "", "Now posting the message.", true);
			th = new PostingThread(new Handler(), msg, imageCheck.isChecked()?image:null);
			th.start();
		}
	}
	
	public void run() {
		dialog.dismiss();
		if(th.result){
			showMessage(th.resultMessage);
			finish();
		}else{
			showError(th.resultMessage);
		}
	}
	
    private class PostingThread extends Thread {
        Handler mHandler;
        String msg;
        File image;
        boolean result;
        String resultMessage;
       
        PostingThread(Handler h, String msg, File image) {
            mHandler = h;
            this.msg = msg;
            this.image = image;
        }
       
        public void run() {
			if(image != null){
				String url = tw.postImage(image, msg);
				if (url != null) {
					msg = msg + " " + url;
				}else{
					result = false;
					resultMessage = "ERROR : Failed to post the image.";
					mHandler.post(TwitterActivity.this);
					return;
				}
			}
			if(tw.tweet(msg)){
				result = true;
				resultMessage = "The message is posted.";
			}else{
				result = false;
				resultMessage = "ERROR : Failed to post the message.";
			}
			mHandler.post(TwitterActivity.this);
        }
    }

	private static final String TEMP = "temp.png";

	private File saveImage(Bitmap source) {
		File res = null;
		try {
			res = getFileStreamPath(TEMP);
			OutputStream imageOut = openFileOutput(TEMP,
					Context.MODE_PRIVATE);
			try {
				source.compress(Bitmap.CompressFormat.PNG, 70, imageOut);
			} finally {
				imageOut.close();
			}
		} catch (Exception e) {
			res.delete();
		}
		return res;
	}

	static final int IMAGE_WIDTH = 400;
	static final int IMAGE_HEIGHT = 50;

	private Bitmap createImage(ScoreModel model, String comment) {
		Bitmap bitmap = Bitmap.createBitmap(IMAGE_WIDTH, IMAGE_HEIGHT + 15,
				Config.ARGB_4444);
		Canvas canvas = new Canvas(bitmap);
		ScoreDrawer drawer = new ScoreDrawer(canvas, 0, 0, IMAGE_WIDTH,
				IMAGE_HEIGHT);

		Paint paint = new Paint();
		paint.setSubpixelText(true);
		paint.setAntiAlias(true);
		paint.setColor(0xff777777);
		paint.setTextSize(12);
		canvas.drawText(comment, 10, IMAGE_HEIGHT + 13, paint);
		String copy = "Powered by Bowlliards Score Book";
		Rect temp = new Rect();
		paint.getTextBounds(copy, 0, copy.length(), temp);
		canvas.drawText(copy, IMAGE_WIDTH - temp.width() - 10,
				IMAGE_HEIGHT + 13, paint);

		drawer.drawScore(model, false, 0);
		return bitmap;
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if(isChecked){
			if(bmp == null){
				bmp = createImage(model,
						DateFormat.format(Constants.DATE_FORMAT, data.getDate()).toString());
			}
			if(bmp != null){
				image = saveImage(bmp);
//				Toast.makeText(this,	image == null ? "ERROR(saveImage)" : "Image saved : "
//								+ image.toString(), Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this,	"ERROR : save image failed.", Toast.LENGTH_SHORT).show();
			}
		}else{
			bmp = null;
		}
		((ImageView)findViewById(R.id.PostImage)).setImageBitmap(bmp);
		updateCharCount();
	}
	
	private int restChars(){
		return 140 - messageEdit.getText().length() - (imageCheck.isChecked() ? 24 : 0);
	}
	
	public void updateCharCount(){
		int n = restChars();
		charCountText.setTextColor(n >= 0 ? 0xffaaaaaa : 0xffffaaaa);
		((TextView)findViewById(R.id.CharCountText)).setText(""+n);
		tweetButton.setEnabled(n>=0);
	}
	
	public void showMessage(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void showError(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	public void afterTextChanged(Editable s) {
		updateCharCount();
	}

	public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
	public void onTextChanged(CharSequence s, int start, int before, int count) {}
}
