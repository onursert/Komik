package com.github.onursert.komik;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

public class EditComicDialog extends Dialog implements View.OnClickListener {

    public Activity activity;
    private String comicTitle;
    private String comicPath;
    RefreshComic refreshComic;
    CustomAdapter customAdapter;

    public Button update;
    public Button cancel;
    public EditText title;

    public EditComicDialog(Activity activity, String comicTitle, String comicPath, RefreshComic refreshComic, CustomAdapter customAdapter) {
        super(activity);
        this.activity = activity;
        this.comicTitle = comicTitle;
        this.comicPath = comicPath;
        this.refreshComic = refreshComic;
        this.customAdapter = customAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.edit_comic_dialog);

        title = (EditText) findViewById(R.id.editComicTitle);
        title.setText(comicTitle, TextView.BufferType.EDITABLE);

        update = (Button) findViewById(R.id.updateButton);
        update.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancelEditButton);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.updateButton:
                try {
                    refreshComic.editComic(refreshComic.comicList, title.getText().toString(), comicPath);
                    refreshComic.editComic(customAdapter.searchedComicList, title.getText().toString(), comicPath);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.cancelEditButton:
                dismiss();
                break;

            default:
                break;
        }
        dismiss();
    }
}
