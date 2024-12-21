// RecordActivity.java
package com.example.tomato;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import android.widget.Toast;
import java.util.List;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class RecordActivity extends AppCompatActivity {
    private TextView recordTextView;
    private DatabaseHelper dbHelper;
    private SQLiteDatabase database;

    private ListView recordListView;
    private ArrayAdapter<String> adapter;
    private FocusRecordManager recordManager;
    private List<String> records;// 用來儲存查詢到的記錄
    private List<Integer> recordIds;// 用來儲存每條記錄的 ID，便於刪除操作
    private String daynow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        CalendarView calendarView = findViewById(R.id.calendarView);

        recordListView = findViewById(R.id.recordListView);
        recordManager = new FocusRecordManager(this);// 用來儲存每條記錄的 ID
        daynow = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        // 初始化 ListView 和資料庫
        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getReadableDatabase();

        // 初始化記錄列表
        records = new ArrayList<>();
        recordIds = new ArrayList<>();

        // 設置 ListView 的適配器，使用 ArrayAdapter 將記錄顯示在 ListView 中
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, records);
        recordListView.setAdapter(adapter);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                String selectedDate = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                daynow = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayOfMonth);

                displayRecordsForDate(selectedDate);
            }
        });

        // Display records for the current date initially
        // 設定日期選擇器（此處省略，假設有 CalendarView）
        // 當選擇日期時顯示相應日期的記錄
        String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        displayRecordsForDate(currentDate);

        recordListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String record = records.get(position);// 這是顯示在 ListView 中的記錄
                Toast.makeText(RecordActivity.this, "選擇了: " + record, Toast.LENGTH_SHORT).show();

                // 獲取對應的 ID
                final int recordId = recordIds.get(position);
                // 顯示刪除確認對話框
                new AlertDialog.Builder(RecordActivity.this)
                        .setTitle("確認刪除")
                        .setMessage("確定刪除這條記錄嗎？")
                        .setPositiveButton("刪除", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 根據選中的記錄刪除資料
                                deleteRecord(recordId);  // 刪除該條記錄
                                Toast.makeText(RecordActivity.this, "記錄已刪除", Toast.LENGTH_SHORT).show();
                                displayRecordsForDate(daynow);// 刷新顯示
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();
            }
        });

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                // 根據選擇的項目啟動對應的 Activity
                if(item.getItemId()==R.id.nav_home){
                    // 啟動首頁 Activity
                    Intent homeIntent = new Intent(RecordActivity.this, MainActivity.class);
                    startActivity(homeIntent);
                    return true;
                }
                else if (item.getItemId()==R.id.nav_search) {
                    // 啟動搜尋 Activity
                    Intent searchIntent = new Intent(RecordActivity.this, RecordActivity.class);
                    //startActivity(searchIntent);
                    return true;
                }
                else if (item.getItemId()==R.id.nav_notifications) {
                    // Activity
                    Intent searchIntent = new Intent(RecordActivity.this, muyuActivity.class);
                    startActivity(searchIntent);
                    return true;
                }
                else{
                    return false;
                }
            }
        });
        if (savedInstanceState == null) {
            bottomNavigationView.setSelectedItemId(R.id.nav_search);
        }
    }
    protected void onResume() {
        super.onResume();

        // 重置 BottomNavigationView 的選中項目
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_search); // 回到預設選項
    }
    private void displayRecordsForDate(String date) {
        records.clear();
        recordIds.clear();
        Cursor cursor = database.query(DatabaseHelper.TABLE_NAME, null, DatabaseHelper.COLUMN_DATE + "=?", new String[]{date}, null, null, null);

        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_ID);  // 假設資料表有一個 ID 欄位
        int hoursIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_HOURS);
        int minutesIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_MINUTES);
        int secondsIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_SECONDS);
        int purposeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_PURPOSE);

        if (hoursIndex >= 0 && minutesIndex >= 0 && secondsIndex >= 0 && purposeIndex >= 0) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(idIndex);  // 獲取記錄的 ID
                int hours = cursor.getInt(hoursIndex);
                int minutes = cursor.getInt(minutesIndex);
                int seconds = cursor.getInt(secondsIndex);
                String purpose = cursor.getString(purposeIndex);

                // 格式化記錄並添加到列表
                String record = "日期: " + date + ", 時長: " + hours + "h " + minutes + "m " + seconds + "s, 目的: " + purpose;
                records.add(record);
                recordIds.add(id);// 儲存 ID 以便後續刪除
            }
        }
        cursor.close();

        // 通知適配器更新 ListView 顯示
        adapter.notifyDataSetChanged();
    }

    private void deleteRecord(int recordId) {
        SQLiteDatabase writableDatabase = dbHelper.getWritableDatabase();
        // 根據 ID 刪除記錄
        writableDatabase.delete(DatabaseHelper.TABLE_NAME, DatabaseHelper.COLUMN_ID + "=?", new String[]{String.valueOf(recordId)});
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dbHelper.close();
    }
}