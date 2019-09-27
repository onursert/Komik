package com.github.onursert.komik;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.List;

public class ImageAdapter extends PagerAdapter {

    Context context;
    List<Bitmap> pages;
    Toolbar toolbar;
    Window window;
    RelativeLayout relativeLayout;

    ImageAdapter(Context context, List<Bitmap> pages, Toolbar toolbar, Window window, RelativeLayout relativeLayout) {
        this.context = context;
        this.pages = pages;
        this.toolbar = toolbar;
        this.window = window;
        this.relativeLayout = relativeLayout;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object o) {
        return view == o;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        CustomImageView imageView = new CustomImageView(context, toolbar, window, relativeLayout);
        imageView.setImageBitmap(pages.get(position));
        container.addView(imageView, 0);
        return imageView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((ImageView) object);
    }
}
