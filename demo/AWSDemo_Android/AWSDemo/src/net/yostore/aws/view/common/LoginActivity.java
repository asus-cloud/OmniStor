package net.yostore.aws.view.common;


import com.ecareme.asuswebstorage.ASUSWebStorage;

import net.yostore.aws.api.ApiConfig;
import net.yostore.aws.asyntask.LoginTask;
import net.yostore.aws.dialog.MessageDialog;
import net.yostore.aws.menu.ActivityMenu;
import net.yostore.utility.MD5;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private Activity actLogin;

	private final String TAG = "Login";
	private ApiConfig apiCfg = null;

	private EditText txtUid = null;
	private EditText txtPwd = null;
	private EditText txtSecure = null;

	private LinearLayout llAuth, llOTP;

	private int preStatus = 999;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		actLogin = this;
		apiCfg = ASUSWebStorage.apiCfg;

		txtUid = (EditText) findViewById(R.id.edtUid);
		txtPwd = (EditText) findViewById(R.id.edtPwd);
		txtSecure = (EditText) findViewById(R.id.edtSecureCode);
		
		llAuth = (LinearLayout) findViewById(R.id.llAuth);
		llOTP = (LinearLayout) findViewById(R.id.llOTP);
		
	}

	public void loginFunction(View v) {
		StringBuilder msg = new StringBuilder();

		String uid = txtUid.getText().toString();
		String pwd = txtPwd.getText().toString();
		String sec = txtSecure.getText().toString();

		if (uid.trim().length() == 0 || pwd.trim().length() == 0) {
			msg.delete(0, msg.length());
			msg.append("Login info could not be empty!");

			Log.e(TAG, msg.toString());

			Toast.makeText(actLogin, msg.toString(), Toast.LENGTH_LONG).show();

			return;
		}

		if ((preStatus == 504 || preStatus == 508) && sec.trim().length() == 0) {
			msg.delete(0, msg.length());
			if (preStatus == 504)
				msg.append("Please input OTP security code!");
			else
				msg.append("Please input CAPTCHA code!");

			Log.e(TAG, msg.toString());

			Toast.makeText(actLogin, msg.toString(), Toast.LENGTH_LONG).show();
			return;
		}

		apiCfg.userid = uid.trim();

		// Hash password :
		// 1. make lower case of password
		// 2. Doing hash by MD5
		// 3. Converting to be Hex Text
		apiCfg.password = MD5.encode(pwd.trim().toLowerCase());

		// Switch to my space activity
		Intent intent = new Intent();
		intent.setClass(actLogin, MyBrowseActivity.class);

		LoginTask loginTask = new LoginTask(actLogin, apiCfg, intent,
				llOTP.getVisibility() == View.VISIBLE ? sec != null
						&& sec.trim().length() > 0 ? sec : null : null) {

			@Override
			protected void loginFail(Integer result) {
				txtUid.setText("");
				txtPwd.setText("");
				txtSecure.setText("");

				if (result != 999)
					MessageDialog.show(actLogin, getString(R.string.app_name),
							"Login fail");
				else {
					MessageDialog.show(actLogin, getString(R.string.app_name),
							"System Error (" + result + ")");
					finish();
					return;
				}

				if (llAuth.getVisibility() != View.VISIBLE)
					switch2Auth();
			}

			@Override
			protected void goOTP(Integer result) {
				preStatus = result;

				if (result == 505) {
					MessageDialog.show(actLogin, getString(R.string.app_name),
							"Your OTP has been LOCKED!");
					finish();
				} else if (llOTP.getVisibility() != View.VISIBLE)
					switch2OTP();
			}

		};
		loginTask.execute((Void) null);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inf = getMenuInflater();
		inf.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean bRtn = super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.mmuAbout:
			ActivityMenu.openOptionsDialog(actLogin);
			break;
		case R.id.mmuExit:
			finish();
		}
		return bRtn;
	}

	private void switch2OTP() {
		llOTP.setVisibility(View.VISIBLE);
		llAuth.setVisibility(View.GONE);
	}

	private void switch2Auth() {
		llAuth.setVisibility(View.VISIBLE);
		llOTP.setVisibility(View.GONE);
	}
}