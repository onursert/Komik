package com.github.onursert.komik;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;

import java.io.IOException;

public class DeleteComicDialog extends Dialog implements View.OnClickListener {

    public Activity activity;
    private String comicName;
    private String comicPath;
    RefreshComic refreshComic;
    CustomAdapter customAdapter;

    public TextView name;
    public CheckBox deleteDevice;
    public Button delete;
    public Button cancel;

    public DeleteComicDialog(Activity activity, String comicName, String comicPath, RefreshComic refreshComic, CustomAdapter customAdapter) {
        super(activity);
        this.activity = activity;
        this.comicName = comicName;
        this.comicPath = comicPath;
        this.refreshComic = refreshComic;
        this.customAdapter = customAdapter;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.delete_comic_dialog);
        
        name = (TextView) findViewById(R.id.comicNameTextView);
        name.setText("Do you want to delete " + comicName);

        deleteDevice = (CheckBox) findViewById(R.id.deleteCheckBox);

        delete = (Button) findViewById(R.id.deleteButton);
        delete.setOnClickListener(this);
        cancel = (Button) findViewById(R.id.cancelDeleteButton);
        cancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.deleteButton:
                try {
                    refreshComic.deleteComic(refreshComic.comicList, comicPath, deleteDevice.isChecked());
                    refreshComic.deleteComic(customAdapter.searchedComicList, comicPath, deleteDevice.isChecked());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;

            case R.id.cancelDeleteButton:
                dismiss();
                break;

            default:
                break;
        }
        dismiss();
    }
}
