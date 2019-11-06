package com.github.onursert.komik;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ComicViewer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    Context context;
    SharedPreferences sharedPreferences;

    List<Bitmap> pages = new ArrayList<>();
    List<String> pagesName = new ArrayList<>();
    int pageNumber = 0;

    DrawerLayout drawer;
    NavigationView navigationViewContent;

    String path;
    String comicTitle;

    ViewPager viewPager;
    ImageAdapter imageAdapter;

    RefreshComic refreshComic;

    SeekBar seekBar;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comic_viewer);
        context = getApplicationContext();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        //Toolbar
        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Navigation Drawer
        drawer = findViewById(R.id.drawer_layout);
        navigationViewContent = findViewById(R.id.nav_view_content);
        if (navigationViewContent != null) {
            navigationViewContent.setNavigationItemSelectedListener(this);
        }

        //Show Comic
        path = getIntent().getStringExtra("path");
        comicTitle = getIntent().getStringExtra("title");
        getSupportActionBar().setTitle(comicTitle);
        try {
            OpenComic(path);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //ViewPager & ImageAdapter
        final TextView textViewPage = (TextView) findViewById(R.id.textViewPage);
        viewPager = findViewById(R.id.viewPager);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.seekLayout);
        imageAdapter = new ImageAdapter(context, pages, toolbar, getWindow(), relativeLayout);
        viewPager.setAdapter(imageAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                seekBar.setProgress(position);
            }

            @Override
            public void onPageSelected(int position) {
                pageNumber = position;
                textViewPage.setText("Page: " + navigationViewContent.getMenu().getItem(pageNumber).setChecked(true));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        if (pages.size() > 0) {
            if (sharedPreferences.getBoolean("where_i_left", false) == true) {
                if (getIntent().getStringExtra("currentPage") != null) {
                    pageNumber = Integer.parseInt(getIntent().getStringExtra("currentPage"));
                } else {
                    pageNumber = 0;
                }
            }
            viewPager.setCurrentItem(pageNumber);
            if (pageNumber == 0) {
                textViewPage.setText("Page: " + navigationViewContent.getMenu().getItem(0).setChecked(true));
            }
        } else {
            finish();
            Toast.makeText(context, "Unable to open", Toast.LENGTH_LONG).show();
        }

        refreshComic = MainActivity.getInstance().refreshComic;

        //Seekbar
        final TextView textViewNumber = (TextView) findViewById(R.id.textViewNumber);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        seekBar.setMax(pages.size() - 1);
        seekBar.setPadding(100, 0, 100, 0);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                this.progress = progress;
                textViewNumber.setText(progress + "/" + (pages.size() - 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(final SeekBar seekBar) {
                viewPager.setCurrentItem(progress);
            }
        });

        checkSharedPreferences();
    }

    //On Activity Stop
    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
    @Override
    public void onStop() {
        try {
            refreshComic.addCurrentPage(refreshComic.comicList, path, pageNumber);
            refreshComic.addCurrentPage(refreshComic.customAdapter.searchedComicList, path, pageNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        finish();
        super.onStop();
    }

    //Check Shared Preferences
    public void checkSharedPreferences() {
        if (sharedPreferences.getBoolean("keep_screen_on", false) == true) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        if (sharedPreferences.getBoolean("rotation_lock", false) == true) {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
            }
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        checkSharedPreferences();
    }

    //OpenComic
    public void OpenComic(String srcDir) throws IOException {
        srcDir = URLDecoder.decode(srcDir, "UTF-8");
        ZipFile zipFile = new ZipFile(srcDir);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        Bitmap photo = null;
        int lastAddedItem = 0;
        while (entries.hasMoreElements()) {
            final ZipEntry zipEntry = entries.nextElement();
            if (zipEntry.getName().endsWith(".jpg") || zipEntry.getName().endsWith(".JPG") || zipEntry.getName().endsWith(".jpeg") || zipEntry.getName().endsWith(".JPEG") || zipEntry.getName().endsWith(".png") || zipEntry.getName().endsWith(".PNG")) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;
                photo = BitmapFactory.decodeStream(zipFile.getInputStream(zipEntry), null, options);
                if (photo != null) {
                    pages.add(photo);
                    String[] firstSplittedLink = zipEntry.getName().split("/");
                    String[] secondSplittedLink = firstSplittedLink[firstSplittedLink.length - 1].split("\\.");
                    pagesName.add(secondSplittedLink[0]);
                    navigationViewContent.getMenu().add(secondSplittedLink[0]);
                    navigationViewContent.getMenu().getItem(lastAddedItem).setCheckable(true);
                }
                lastAddedItem++;
            }
        }
        zipFile.close();
    }

    //Navigation Item Clicked
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        for (int i = 0; i < pages.size(); i++) {
            if (pagesName.get(i).equals(menuItem.toString())) {
                viewPager.setCurrentItem(i);
                break;
            }
        }
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }
}
