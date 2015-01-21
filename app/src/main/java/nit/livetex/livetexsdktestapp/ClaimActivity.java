package nit.livetex.livetexsdktestapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import java.util.regex.Pattern;


public class ClaimActivity extends BaseActivity {

    public static void show(Activity activity) {
        Intent intent = new Intent(activity, ClaimActivity.class);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claim);
        initActionBar();
        findViewById(R.id.send_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                String name = ((EditText) findViewById(R.id.input_name)).getText().toString();
                if (TextUtils.isEmpty(name) || !Pattern.matches("[0-9]+", name) || name.length() < 7) {
                    showToast("Введите номер телефона");
                    return;
                }
                String msg = ((EditText) findViewById(R.id.input_msg)).getText().toString();
                if (TextUtils.isEmpty(msg)) {
                    showToast("Введите сообщение");
                    return;
                }
                showProgressDialog("Отправляется жалоба");
                MainApplication.abuse(name, msg);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("Жалоба");
    }

    @Override
    protected void onVoted() {
        showToast("Ваша жалоба отправлена");
        onBackPressed();
    }
}
