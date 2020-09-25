package com.example.wallappwallpaper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.UiModeManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.lang.reflect.Array;
import java.util.ArrayList;

import static android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private WallPaperAdapter wallPaperAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private WallPaperDB testDB;
    private FirebaseAuth mAuth;

    private WallPaperAlarm wallPaperAlarm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        testDB = new WallPaperDB();
        Service wallPaperService = new Service(testDB);
        WallPaperFetcher wallPaperFetcher = new WallPaperFetcher(wallPaperService);

        UiModeManager uiManager = (UiModeManager) getApplicationContext().getSystemService(Context.UI_MODE_SERVICE);
        uiManager.setNightMode(UiModeManager.MODE_NIGHT_NO);

        recyclerView = findViewById(R.id.wallpapers_list);
        recyclerView.setHasFixedSize(true);

        layoutManager = new GridLayoutManager(getApplicationContext(), 2);
        recyclerView.setLayoutManager(layoutManager);

        wallPaperAdapter = new WallPaperAdapter(testDB);
        recyclerView.setAdapter(wallPaperAdapter);

        // Getting wallpapers
        try {
            wallPaperFetcher.PopulateServer((WallPaperAdapter) wallPaperAdapter,this);
        } catch (Exception e) {
            e.printStackTrace();
        }



    }

    // Search view and other menu stuff...
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.example_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)searchItem.getActionView();
        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                wallPaperAdapter.getFilter().filter(newText);
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.about:
                Toast.makeText(this,"About action...", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, AboutActivity.class);
                this.startActivity(intent);

                return true;

        }

        return super.onOptionsItemSelected(item);
    }

    public void autoChangeWallpaper(){

        Intent intent = new Intent(MainActivity.this, WallpaperAutoChange.class);

        ArrayList<String> myUrls = new ArrayList<>();

        for(WallPaper wallPaper : testDB.GetAllWallPapers()){
            String url = wallPaper.getImagePath();
            myUrls.add(url);
        }

        intent.putStringArrayListExtra("list", myUrls);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

        long timeAtStart = System.currentTimeMillis();
        long tenSecondsInMillis = 1000 * 10;

        alarmManager.set(AlarmManager.RTC_WAKEUP, tenSecondsInMillis, pendingIntent);

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

}