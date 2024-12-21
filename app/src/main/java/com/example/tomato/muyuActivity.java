package com.example.tomato;


import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.TextView;
import android.view.MotionEvent;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.media.MediaPlayer;
import android.media.SoundPool;

public class muyuActivity extends AppCompatActivity {
    private Animation muyubig;
    private Animation muyusmall;
    private ImageButton muyu;
    private TextView textView;
    private RelativeLayout mLayout;
    private int counter=0;
    private SoundPool soundPool;
    private int soundId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.muyu);

        muyu = findViewById(R.id.imageButton);
        mLayout = findViewById(R.id.relative);
        textView = findViewById(R.id.textView2);

        //設定木魚動畫
        muyubig = AnimationUtils.loadAnimation(this, R.drawable.anime_big_small);
        muyusmall = AnimationUtils.loadAnimation(this, R.drawable.anime_small);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // 初始化聲音
        initSound();

        //設定木魚觸發放大縮小
        muyu.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    playSound();// 播放音效
                    // 按下时放大
                    v.startAnimation(muyubig);
                    counter++;
                    showFloatingText();
                    textView.setText(String.valueOf(counter));

                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    // 放开时恢复
                    v.startAnimation(muyusmall);
                    break;
            }
            return true; // 消耗触摸事件
        });

        // Set bottom navigation view item selected listener
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // 根據選擇的項目啟動對應的 Activity
                if(item.getItemId()==R.id.nav_home){
                    // 啟動首頁 Activity
                    Intent homeIntent = new Intent(muyuActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                    return true;
                }
                else if (item.getItemId()==R.id.nav_search) {
                    // 啟動搜尋 Activity
                    Intent searchIntent = new Intent(muyuActivity.this, RecordActivity.class);
                    startActivity(searchIntent);
                    return true;
                }
                else if (item.getItemId()==R.id.nav_notifications) {
                    // Activity
                    Intent searchIntent = new Intent(muyuActivity.this, muyuActivity.class);
                    //startActivity(searchIntent);
                    return true;
                }
                else{
                    return false;
                }
            }
        });
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_notifications);
        }
    }

    //設定木魚聲音
    // 初始化SoundPool
    private void initSound() {
        soundPool = new SoundPool.Builder()
                .setMaxStreams(10) // 最大同時播放的音效數量
                .build();
        // 載入音效
        soundId = soundPool.load(this, R.raw.muyusoundonly, 1);
    }

    // 播放音效
    private void playSound() {
        // 播放音效
        if (soundPool != null) {
            soundPool.play(soundId, 1f, 1f, 0, 0, 1f);  // 播放音效，1f 是音量，0 是循環次數
        }
    }

    protected void onResume() {
        super.onResume();
        // 重置 BottomNavigationView 的選中項目
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_notifications); // 回到預設選項
    }

    private void showFloatingText() {
        // 動態建立一個 TextView
        TextView textView = new TextView(this);
        textView.setText("功德 +1");
        textView.setTextSize(20);
        textView.setTextColor(Color.WHITE);

        // 設定初始位置 (在木魚按鈕的上方)
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
        );
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        textView.setLayoutParams(params);
        textView.setY(muyu.getY() - 100); // 設定初始 Y 位置

        // 將文字加到 Layout 中
        mLayout.addView(textView);

        // 隨機生成左右漂移的位移量 (範圍 -1000 到 1000)
        float randomShiftX = (float) (Math.random() * 1000-500);

        // 動畫設定
        ObjectAnimator translateY = ObjectAnimator.ofFloat(textView, "translationY", textView.getY(), 0f);
        ObjectAnimator translateX = ObjectAnimator.ofFloat(textView, "translationX", textView.getX(), textView.getX() + randomShiftX);
        ObjectAnimator alpha = ObjectAnimator.ofFloat(textView, "alpha", 1f, 0f);

        AnimatorSet animatorSet = new AnimatorSet();

        //三個一起執行
        animatorSet.playTogether(translateY, translateX, alpha);

        //過程時間
        animatorSet.setDuration(3500);
        animatorSet.start();

        // 動畫完成後移除 TextView
        animatorSet.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                mLayout.removeView(textView);
            }
        });
    }

}
