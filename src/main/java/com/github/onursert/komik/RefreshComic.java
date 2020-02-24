package com.github.onursert.komik;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class RefreshComic {

    Context context;
    CustomAdapter customAdapter;
    List<List> comicList;

    public RefreshComic(Context context, CustomAdapter customAdapter, List<List> comicList) {
        this.context = context;
        this.customAdapter = customAdapter;
        this.comicList = comicList;
    }

    //Custom Shared Preferences
    public static final String myPref = "preferenceName";
    public String getFromPreferences(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(myPref, 0);
        String str = sharedPreferences.getString(key, "null");
        return str;
    }
    public void setToPreferences(String key, String thePreference) {
        SharedPreferences.Editor editor = context.getSharedPreferences(myPref, 0).edit();
        editor.putString(key, thePreference);
        editor.commit();
    }

    //Read File From Internal Storage
    public void readFileFromInternalStorage() throws IOException {
        FileInputStream fileInputStream = context.openFileInput("comicList.txt");
        InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream);
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            String[] arrOfLine = line.split("½½");
            List comicInfo = new LinkedList();
            comicInfo.add(arrOfLine[0]); //comicTitle
            comicInfo.add(arrOfLine[1]); //comicCover
            comicInfo.add(arrOfLine[2]); //comicPath
            comicInfo.add(arrOfLine[3]); //importTime
            comicInfo.add(arrOfLine[4]); //openTime
            comicInfo.add(arrOfLine[5]); //currentPage

            comicList.add(comicInfo);
        }
        fileInputStream.close();
        inputStreamReader.close();
        bufferedReader.close();
    }

    /*Search Begin*/
    File file;
    File fileImages;

    FileOutputStream fileOutputStream;
    FileOutputStream fileOutputStreamImages;
    OutputStreamWriter writer;

    Bitmap bitmap;

    public void SearchComic() throws IOException {
        file = new File(context.getFilesDir(), "comicList.txt");
        fileImages = new File(context.getFilesDir() + File.separator + "comicImages");

        if (!file.exists()) {
            file.createNewFile();
        }
        fileImages.mkdirs();

        fileOutputStream = new FileOutputStream(file, false);
        writer = new OutputStreamWriter(fileOutputStream);

        bitmap = null;

        isRemoved(0);
        FindComic(Environment.getExternalStorageDirectory());
        sortByPreferences(comicList);

        writer.close();
        if (fileOutputStream != null) {
            fileOutputStream.flush();
            fileOutputStream.close();
        }

        if (fileOutputStreamImages != null) {
            fileOutputStreamImages.flush();
            fileOutputStreamImages.close();
        }
    }
    public void isRemoved(int turn) {
        for (int i = turn; i < comicList.size(); i++) {
            File file = new File((String) comicList.get(i).get(2));
            if (!file.exists()) {
                comicList.remove(i);
                removedCount++;
                isRemoved(i);
            }
        }
    }
    public void FindComic(File dir) throws IOException {
        File listFile[] = dir.listFiles();
        if (listFile != null) {
            for (int i = 0; i < listFile.length; i++) {
                if (listFile[i].isDirectory()) {
                    FindComic(listFile[i]);
                } else {
                    if (listFile[i].getName().endsWith(".cbz") || listFile[i].getName().endsWith(".CBZ")) {
                        if (!isExist(listFile[i].getAbsolutePath())) {
                            String title = listFile[i].getName().substring(0, listFile[i].getName().length() - 4);
                            String imageName = title + ".jpeg";
                            File imageItem = new File(fileImages, imageName);
                            if (!imageItem.exists()) {
                                bitmap = (Bitmap) FindCover(listFile[i].getAbsolutePath());
                                if (bitmap != null) {
                                    fileOutputStreamImages = new FileOutputStream(imageItem);
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStreamImages);
                                } else {
                                    File fileNull = new File("null");
                                    imageItem = fileNull;
                                }
                            }

                            String importTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

                            List comicInfo = new LinkedList();
                            comicInfo.add(title); //comicTitle
                            comicInfo.add(imageItem); //comicCover
                            comicInfo.add(listFile[i].getAbsolutePath()); //comicPath
                            comicInfo.add(importTime); //importTime
                            comicInfo.add("19200423_000000"); //openTime
                            comicInfo.add(0); //currentPage

                            if (fileOutputStreamImages != null) {
                                fileOutputStreamImages.flush();
                                fileOutputStreamImages.close();
                            }

                            addedCount++;
                            comicList.add(comicInfo);
                        }
                    }
                }
            }
        }
    }
    public boolean isExist(String path) {
        for (int i = 0; i < comicList.size(); i++) {
            if (comicList.get(i).get(2).equals(path)) {
                return true;
            }
        }
        return false;
    }
    public Bitmap FindCover(String srcDir) throws IOException {
        ZipFile zipFile = new ZipFile(srcDir);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        Bitmap photo = null;
        ZipEntry zipEntry = entries.nextElement();
        photo = BitmapFactory.decodeStream(zipFile.getInputStream(zipEntry));
        while (photo == null && entries.hasMoreElements()) {
            zipEntry = entries.nextElement();
            photo = BitmapFactory.decodeStream(zipFile.getInputStream(zipEntry));
        }
        zipFile.close();
        return photo;
    }
    public void sortByPreferences(List<List> comicList) throws IOException {
        if (getFromPreferences("sort").equals("sortTitle")) {
            sortTitle(comicList);
        } else if (getFromPreferences("sort").equals("sortImportTime")) {
            sortImportTime(comicList);
        } else if (getFromPreferences("sort").equals("sortOpenTime")) {
            sortOpenTime(comicList);
        } else {
            sortTitle(comicList);
        }
    }
    /*Search End*/

    //Menu Refresh, Sort, Show/Hide
    int addedCount;
    int removedCount;
    final Handler handler = new Handler();
    class refreshComics extends AsyncTask<Void, Void, Void> {
        Toast toast = Toast.makeText(context, "Searching...", Toast.LENGTH_LONG);

        @Override
        protected void onPreExecute() {
            addedCount = 0;
            removedCount = 0;
            customAdapter.refreshingDoneBool = false;
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    toast.show();
                    customAdapter.notifyDataSetChanged();
                    handler.postDelayed(this, 1000);
                }
            }, 1000);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                SearchComic();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            handler.removeCallbacksAndMessages(null);
            toast.cancel();
            customAdapter.notifyDataSetChanged();
            customAdapter.refreshingDone(comicList);
            Toast.makeText(context, addedCount + " comic(s) added and " + removedCount + " comic(s) removed", Toast.LENGTH_SHORT).show();
        }
    }
    public void sortTitle(List<List> comicList) throws IOException {
        setToPreferences("sort", "sortTitle");
        Collections.sort(comicList, new Comparator<List>() {
            @Override
            public int compare(List o1, List o2) {
                try {
                    return o1.get(0).toString().compareTo(o2.get(0).toString());
                } catch (NullPointerException e) {
                    return 0;
                }
            }
        });
        comicListChanged(comicList);
    }
    public void sortImportTime(List<List> comicList) throws IOException {
        setToPreferences("sort", "sortImportTime");
        Collections.sort(comicList, new Comparator<List>() {
            @Override
            public int compare(List o1, List o2) {
                try {
                    if (o2.get(3).toString().compareTo(o1.get(3).toString()) == 0) {
                        return o1.get(0).toString().compareTo(o2.get(0).toString());
                    } else {
                        return o2.get(3).toString().compareTo(o1.get(3).toString());
                    }
                } catch (NullPointerException e) {
                    return 0;
                }
            }
        });
        comicListChanged(comicList);
    }
    public void sortOpenTime(List<List> comicList) throws IOException {
        setToPreferences("sort", "sortOpenTime");
        Collections.sort(comicList, new Comparator<List>() {
            @Override
            public int compare(List o1, List o2) {
                try {
                    return o2.get(4).toString().compareTo(o1.get(4).toString());
                } catch (NullPointerException e) {
                    return 0;
                }
            }
        });
        comicListChanged(comicList);
    }

    //Write ComicList To Cache
    public void comicListChanged(List<List> comicList) throws IOException {
        file = new File(context.getFilesDir(), "comicList.txt");
        if (!file.exists()) {
            file.createNewFile();
        }
        fileOutputStream = new FileOutputStream(file, false);
        writer = new OutputStreamWriter(fileOutputStream);
        updateCache(comicList);
        writer.close();
        if (fileOutputStream != null) {
            fileOutputStream.flush();
            fileOutputStream.close();
        }
    }
    public void updateCache(List<List> comicList) throws IOException {
        for (int i = 0; i < comicList.size(); i++) {
            writer.append(comicList.get(i).get(0) + "½½" + comicList.get(i).get(1) + "½½" + comicList.get(i).get(2) + "½½" + comicList.get(i).get(3) + "½½" + comicList.get(i).get(4) + "½½" + comicList.get(i).get(5) + "\r\n");
        }
    }

    //Functions Which Come From Another Class
    public void addOpenTime(List<List> comicList, String path, String openTime) throws IOException {
        if (comicList != null) {
            final int comicListSize = comicList.size();
            for (int i = 0; i < comicListSize; i++) {
                if (comicList.get(i).get(2).equals(path)) {
                    List comicInfo = new LinkedList();
                    comicInfo.add(comicList.get(i).get(0)); //comicTitle
                    comicInfo.add(comicList.get(i).get(1)); //comicCover
                    comicInfo.add(comicList.get(i).get(2)); //comicPath
                    comicInfo.add(comicList.get(i).get(3)); //importTime
                    comicInfo.add(openTime); //openTime
                    comicInfo.add(comicList.get(i).get(5)); //currentPage
                    comicList.set(i, comicInfo);
                    break;
                }
            }
            sortByPreferences(comicList);
            customAdapter.notifyDataSetChanged();
        }
    }
    public void editComic(List<List> comicList, String title, String path) throws IOException {
        if (comicList != null) {
            final int comicListSize = comicList.size();
            for (int i = 0; i < comicListSize; i++) {
                if (comicList.get(i).get(2).equals(path)) {
                    List comicInfo = new LinkedList();
                    comicInfo.add(title); //comicTitle
                    comicInfo.add(comicList.get(i).get(1)); //comicCover
                    comicInfo.add(comicList.get(i).get(2)); //comicPath
                    comicInfo.add(comicList.get(i).get(3)); //importTime
                    comicInfo.add(comicList.get(i).get(4)); //openTime
                    comicInfo.add(comicList.get(i).get(5)); //currentPage
                    comicList.set(i, comicInfo);
                    break;
                }
            }
            sortByPreferences(comicList);
            customAdapter.notifyDataSetChanged();
        }
    }
    public void deleteComic(List<List> comicList, String path, Boolean deleteDevice) throws IOException {
        if (comicList != null) {
            final int comicListSize = comicList.size();
            for (int i = 0; i < comicListSize; i++) {
                if (comicList.get(i).get(2).equals(path)) {
                    comicList.remove(i);
                    if (deleteDevice) {
                        File file = new File(path);
                        file.delete();
                    }
                    break;
                }
            }
            sortByPreferences(comicList);
            customAdapter.notifyDataSetChanged();
        }
    }
    public void addCurrentPage(List<List> comicList, String path, Integer currentPage) throws IOException {
        if (comicList != null) {
            final int comicListSize = comicList.size();
            for (int i = 0; i < comicListSize; i++) {
                if (comicList.get(i).get(2).equals(path)) {
                    List comicInfo = new LinkedList();
                    comicInfo.add(comicList.get(i).get(0)); //comicTitle
                    comicInfo.add(comicList.get(i).get(1)); //comicCover
                    comicInfo.add(comicList.get(i).get(2)); //comicPath
                    comicInfo.add(comicList.get(i).get(3)); //importTime
                    comicInfo.add(comicList.get(i).get(4)); //openTime
                    comicInfo.add(currentPage); //currentPage
                    comicList.set(i, comicInfo);
                    break;
                }
            }
            sortByPreferences(comicList);
            customAdapter.notifyDataSetChanged();
        }
    }
}
