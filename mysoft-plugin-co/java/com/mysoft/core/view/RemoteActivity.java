package com.mysoft.core.view;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.mysoft.core.L;
import com.mysoft.core.MConstant;
import com.mysoft.core.util.PrefsUtils;
import com.mysoft.core.util.ResourceUtils;

/**
 * @Author Jay
 */
public class RemoteActivity extends Activity {
    private static final String REMOTE_URL = "settings_remote_url";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(ResourceUtils.layout(this, "activity_remote"));
        L.i("RemoteActivity", "onCreate " + getTaskId());
        final Button mSettingBtn = (Button) findViewById(ResourceUtils.id(this, "bt_setting"));
        final EditText mAddrEditText = (EditText) findViewById(ResourceUtils.id(this, "et_address"));
        String lastInputUrl = PrefsUtils.getString(this, REMOTE_URL, "");
        mAddrEditText.setText(lastInputUrl);
        mSettingBtn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                String inputString = mAddrEditText.getText().toString();
                if (checkInput(inputString)) {
                    Intent intent = new Intent(RemoteActivity.this, DaemonActivity.class);
                    String launchUrl = inputString;
                    intent.putExtra(MConstant.LAUNCH_URL, launchUrl);
                    PrefsUtils.putString(getApplicationContext(), REMOTE_URL, inputString);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(RemoteActivity.this, "输入格式不正确", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkInput(String inputString) {
        if (!TextUtils.isEmpty(inputString) && inputString.trim().length() > 0) {
            return inputString.startsWith("http");
        }
        return false;
    }
}
