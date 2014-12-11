package nit.livetex.livetexsdktestapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;

import livetex.sdk.models.DialogState;

/**
 * Created by sergey.so on 02.12.2014.
 *
 */
public class ReinitActivity extends BaseActivity {

    private boolean isFirstRun = false;
    private boolean isReInit = false;
    private boolean isLivetexStopLocked = false;
    private BroadcastReceiver mInternetStateReciever;
    private boolean isInetWasActive = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isFirstRun = true;
        mInternetStateReciever = new InternetStateReciever();
    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean isInetActive = isInetActive();
        isInetWasActive = isInetActive;
        registerReceiver(mInternetStateReciever, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        if (isInetActive)
            init();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(mInternetStateReciever);
        deinit();
    }

    private boolean isInetActive(){
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void init(){
        if (!isFirstRun)
            checkState();
        isFirstRun = false;
    }

    private void deinit(){
        if (!isLivetexStopLocked)
            MainApplication.stopLivetex();
    }

    protected void livetexLockStop() {
        isLivetexStopLocked = true;
    }

    private void checkState() {
        showProgressDialog("Восстановление соединения");
        String appId = DataKeeper.restoreAppId(this);
        if (appId == null) {
            showInitActivity();
        } else {
            isReInit = true;
            MainApplication.initLivetex(appId);
        }
    }

    protected void showInitActivity() {
        unregister();
        InitActivity.show(this);
        finish();
    }

    protected void unregister() {
        try {
            unregisterReceiver(mReciever);
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    protected void initComplete() {
        getDialogState();
        showProgressDialog("инициализация");
    }

    protected void getDialogState() {
//        showProgressDialog("Получение состояния диалога");
        MainApplication.getDialogState();
    }

    @Override
    protected void onDialogStateGetted(DialogState state) {
        if (state == null) return;
        if (!isReInit) return;

        isReInit = false;
        if (state.conversation == null && (this instanceof ChatActivity)) {
            showWelcomeActivity();
        } else if (state.conversation != null && (this instanceof WelcomeActivity)) {
            showChatActivity();
        }
    }

    protected void showChatActivity(){
        unregister();
        livetexLockStop();
        ChatActivity.show(this);
        finish();
    }

    protected void showWelcomeActivity() {
        unregister();
        livetexLockStop();
        InitActivity.show(this);
        finish();
    }

    @Override
    protected void onError(String request) {
        if (!request.equals(MainApplication.REQUEST_INIT)) return;
        showInitActivity();
    }

    private static boolean firstConnect = true;

    private class InternetStateReciever extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            final NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
            Log.d("Livetex_sdk", "InternetStateRecieve. " + activeNetInfo + " " + firstConnect);
            if (activeNetInfo != null) {
                if (firstConnect && !isInetWasActive) {
                    Log.d("Livetex_sdk", "INET ACTIVE. START INIT");
                    firstConnect = false;
                    init();
                }
                isInetWasActive = true;
            } else {
                if (firstConnect){
                    Log.d("Livetex_sdk", "INET INACTIVE. START DEINIT");
                    deinit();
                }
                firstConnect = true;
                isInetWasActive = false;
            }
        }
    }
}
