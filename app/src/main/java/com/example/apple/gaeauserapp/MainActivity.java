package com.example.apple.gaeauserapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Animation bounce;
    ImageView imageView;
    private ViewPager viewPager;
    private MyViewPageAdapter myViewPagerAdapter;
    private LinearLayout dotsLayout;
    private TextView[] dots;
    private int[] layouts;
    private Button btnSkip, btnNext;
    private PrefManager prefManager;
    private View LogoScreen, TourScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LogoScreen = findViewById(R.id.logo_screen);
        TourScreen = findViewById(R.id.tour_screen);
        enableViews(LogoScreen);
        disableViews(TourScreen);

        imageView = findViewById(R.id.logo_img);

        bounce = AnimationUtils.loadAnimation(this,R.anim.bounce);
        imageView.setAnimation(bounce); viewPager = findViewById(R.id.view_pager);
        dotsLayout = findViewById(R.id.layout_dots);
        btnSkip = findViewById(R.id.btn_skip);
        btnNext = findViewById(R.id.btn_next);

        layouts = new int[]{
                R.layout.welcome_slide_1,
                R.layout.welcome_slide_2,
                R.layout.welcome_activity_3
        };

        addBottomDots(0);
        changeStatusBarColor();

        myViewPagerAdapter = new MyViewPageAdapter();
        viewPager.setAdapter(myViewPagerAdapter);
        viewPager.addOnPageChangeListener(viewPageListener);

        btnSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchHomeScreen();
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current = getItem(1);
                if(current < layouts.length){
                    viewPager.setCurrentItem(current);
                }
                else {
                    launchHomeScreen();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //add your code here
                        isFirstTimeUser();
                    }
                }, 2000);
            };
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
//        isFirstTimeUser();
    }

    private int getItem(int i) {
        return viewPager.getCurrentItem() + 1;
    }

    private void launchHomeScreen() {
        prefManager.setFirstTimeLaunch(false);
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void changeStatusBarColor() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    private void addBottomDots(int currentPage) {
        dots = new TextView[layouts.length];
        int[] colorsActive = getResources().getIntArray(R.array.array_dot_active);
        int[] colorsInactive = getResources().getIntArray(R.array.array_dot_inactive);

        dotsLayout.removeAllViews();

        for(int i = 0; i< dots.length ; i++){
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            dots[i].setTextColor(colorsInactive[currentPage]);
            dotsLayout.addView(dots[i]);
        }

        if(dots.length > 0){
            dots[currentPage].setTextColor(colorsActive[currentPage]);
        }
    }

    ViewPager.OnPageChangeListener viewPageListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int i) {
            addBottomDots(i);
            if(i == layouts.length - 1){
                btnNext.setText(getString(R.string.start));
                btnSkip.setVisibility(View.GONE);
            }
            else{
                btnNext.setText(getString(R.string.next));
                btnSkip.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int i) {

        }
    };

    public class MyViewPageAdapter extends PagerAdapter {

        private LayoutInflater layoutInflater;

        public MyViewPageAdapter(){}

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            layoutInflater  = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(layouts[position],container,false);
            container.addView(view);

            return view;
        }

        @Override
        public int getCount() {
            return layouts.length;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
            return view == o;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            View view = (View) object;
            container.removeView(view);
        }
    }

    private void isFirstTimeUser() {
        prefManager = new PrefManager(this);
        if(!prefManager.isFirstTimeLaunch()){
            launchLoginScreen();
            finish();
        }
        else{
            enableViews(TourScreen);
            disableViews(LogoScreen);
        }
    }

    private void launchLoginScreen() {
        Intent loginScreen = new Intent(this,LoginActivity.class);
        startActivity(loginScreen);
    }

    private void enableViews(View... views){
        for (View v : views){
            v.setVisibility(View.VISIBLE);
        }
    }

    private void disableViews(View... views){
        for (View v : views){
            v.setVisibility(View.GONE);
        }
    }
}
