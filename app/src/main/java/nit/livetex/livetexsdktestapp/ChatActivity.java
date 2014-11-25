package nit.livetex.livetexsdktestapp;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.ActionBar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import livetex.sdk.models.DialogState;
import livetex.sdk.models.TextMessage;
import livetex.sdk.models.TypingMessage;
import nit.livetex.livetexsdktestapp.adapter.ChatAdapter;


public class ChatActivity extends BaseActivity {

    public static void show(Activity activity) {
        Intent intent = new Intent(activity, ChatActivity.class);
        activity.startActivity(intent);
    }

    private ListView mListView;
    private ChatAdapter mAdapter;
    private TextView mOperatorNameTV;
    private ImageView mOperatorAvaIV;
    private String mOperatorName;
    private int mOperatorAvaVisibility = View.INVISIBLE;
    private boolean isTyping = false;
    private Handler mHandler = new Handler();
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mOperatorNameTV != null) {
                isTyping = false;
                if (mOperatorName != null)
                    mOperatorNameTV.setText(mOperatorName);
                else
                    mOperatorNameTV.setText("");
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initActionBar();
        initViews();
        getMsgHistory();
    }

    private void initActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        View v = LayoutInflater.from(this).inflate(R.layout.chat_ab, null);
        Resources res = getResources();
        Bitmap src = BitmapFactory.decodeResource(res, R.drawable.avatarka);
        int bmpSize = getResources().getDimensionPixelSize(R.dimen.ava_size);
        src = Bitmap.createScaledBitmap(src, bmpSize, bmpSize, true);
        RoundedBitmapDrawable dr = RoundedBitmapDrawableFactory.create(res, src);
        dr.setCornerRadius(bmpSize / 2.0f);
        mOperatorNameTV = (TextView) v.findViewById(R.id.operator_name);
        mOperatorAvaIV = (ImageView) v.findViewById(R.id.ava);
        mOperatorAvaIV.setImageDrawable(dr);
        if (mOperatorName != null) {
            mOperatorNameTV.setText(mOperatorName);
        }
        mOperatorAvaIV.setVisibility(mOperatorAvaVisibility);
        actionBar.setCustomView(v);
    }

    public void setActionBarText(String name) {
        if (mOperatorNameTV == null) return;
        mHandler.removeCallbacks(runnable);
        SpannableString s = new SpannableString(name + "\n" + "печатает...");
        s.setSpan(new RelativeSizeSpan(0.7f), name.length() + 1, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        isTyping = true;
        mOperatorNameTV.setText(s);
        mHandler.postDelayed(runnable, 5000);
    }

    private void initViews() {
        mListView = (ListView) findViewById(android.R.id.list);
        mAdapter = new ChatAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.setSelector(android.R.color.transparent);
        findViewById(R.id.send_msg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMsg();
            }
        });
        ((ImageView) findViewById(R.id.vote_down)).setColorFilter(getResources().getColor(R.color.material_red_500));
        ((ImageView) findViewById(R.id.vote_up)).setColorFilter(getResources().getColor(R.color.material_green_500));
        findViewById(R.id.vote_down).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vote(false);
            }
        });
        findViewById(R.id.vote_up).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vote(true);
            }
        });
    }

    private void vote(boolean isLike) {
        showProgressDialog("Оценка");
        MainApplication.vote(isLike);
    }

    private void sendMsg() {
        String text = ((EditText) findViewById(R.id.input_msg)).getText().toString();
        if (TextUtils.isEmpty(text)) return;
        MainApplication.sendMsg(text);

    }

    private void getMsgHistory() {
        showProgressDialog("Получение истории сообщений");
        MainApplication.getMsgHistory(20, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_claim) {
            ClaimActivity.show(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onMsgHistoryGetted(List<TextMessage> msg) {
        mAdapter.addAllMsgs(msg);
    }

    @Override
    protected void onMsgSended(TextMessage msg) {
        if (mAdapter != null) {
            ((EditText) findViewById(R.id.input_msg)).setText("");
            mAdapter.addMsg(msg);
            mListView.setSelection(mAdapter.getCount() - 1);
        }
    }

    @Override
    protected void onMsgRecieved(TextMessage msg) {
        if (mAdapter != null) {
            mAdapter.addMsg(msg);
            mListView.setSelection(mAdapter.getCount() - 1);
        }
    }

    @Override
    protected void onVoted() {
        showToast("Оценка отправлена");
    }

    @Override
    protected void onTyping(TypingMessage typingMessage) {
        if (mOperatorName != null)
            setActionBarText(mOperatorName);
        else
            setActionBarText("Оператор");
    }

    @Override
    protected void onUpdateDialogState(DialogState state) {
        String operatorName = "";
        int avaVisibility = View.VISIBLE;
        if (state.conversation != null && state.employee != null) {
            operatorName = state.employee.firstname + " " + state.employee.lastname;
            avaVisibility = View.VISIBLE;
        } else if (state.conversation != null) {
            operatorName = "Ожидайте ответа оператора";
            avaVisibility = View.GONE;
        } else if (state.employee == null) {
            operatorName = "Диалог закрыт";
            avaVisibility = View.GONE;
        }
        mOperatorName = operatorName;
        if (mOperatorNameTV != null) {
            if (isTyping)
                setActionBarText(operatorName);
            else
                mOperatorNameTV.setText(operatorName);
        }
        if (mOperatorAvaIV != null) {
            mOperatorAvaIV.setVisibility(avaVisibility);
        }
    }
}
