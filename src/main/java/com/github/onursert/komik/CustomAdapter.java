package com.github.onursert.komik;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.MyViewHolder> implements Filterable {
    Context context;
    List<List> comicList;

    RefreshComic refreshComic;

    LayoutInflater inflater;
    int position;

    public List<List> searchedComicList;

    public CustomAdapter(Context context, List<List> comicList) {
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.comicList = comicList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.custom_row, parent, false);
        MyViewHolder holder = new MyViewHolder(view);
        return holder;
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        List comicInfo = comicList.get(i);
        try {
            myViewHolder.setData(comicInfo);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return comicList.size();
    }

    boolean refreshingDoneBool = false;
    public void refreshingDone(List<List> comicList) {
        searchedComicList = new ArrayList<>(comicList);
        refreshingDoneBool = true;
    }
    @Override
    public Filter getFilter() {
        return filter;
    }
    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<List> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(searchedComicList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();
                for (List infos : searchedComicList) {
                    if (infos.get(0).toString().toLowerCase().contains(filterPattern)) {
                        filteredList.add(infos);
                    }
                }
            }
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (refreshingDoneBool) {
                comicList.clear();
                comicList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        }
    };

    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView title, importTime, openTime;
        ImageView image;

        public MyViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.comicTitle);
            image = (ImageView) view.findViewById(R.id.comicCover);
            importTime = (TextView) view.findViewById(R.id.comicImportTime);
            openTime = (TextView) view.findViewById(R.id.comicOpenTime);
            refreshComic = MainActivity.getInstance().refreshComic;

            updateViews();

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    position = getLayoutPosition();
                    return false;
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intentComicViewer = new Intent(context, ComicViewer.class);
                    intentComicViewer.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intentComicViewer.putExtra("title", comicList.get(getLayoutPosition()).get(0).toString());
                    intentComicViewer.putExtra("path", comicList.get(getLayoutPosition()).get(2).toString());
                    intentComicViewer.putExtra("currentPage", comicList.get(getLayoutPosition()).get(5).toString());
                    context.startActivity(intentComicViewer);

                    String openTime = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
                    try {
                        String clickedComicPath = comicList.get(getLayoutPosition()).get(2).toString();
                        refreshComic.addOpenTime(comicList, clickedComicPath, openTime);
                        if (refreshingDoneBool) {
                            refreshComic.addOpenTime(searchedComicList, clickedComicPath, openTime);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        public void setData(List comicInfo) throws ParseException {
            title.setText(comicInfo.get(0).toString());
            Picasso.get().load("file://" + comicInfo.get(1).toString()).error(R.drawable.ic_book_black_24dp).resize(240, 320).into(image);
            importTime.setText("Import: " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new SimpleDateFormat("yyyyMMdd_HHmmss").parse(comicInfo.get(3).toString())));
            openTime.setText("Open: " + DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new SimpleDateFormat("yyyyMMdd_HHmmss").parse(comicInfo.get(4).toString())));
        }

        public void updateViews() {
            if (refreshComic.getFromPreferences("showHideImportTime").equals("Invisible")) {
                importTime.setVisibility(View.INVISIBLE);
            } else if (refreshComic.getFromPreferences("showHideImportTime").equals("Visible")) {
                importTime.setVisibility(View.VISIBLE);
            } else {
                importTime.setVisibility(View.VISIBLE);
            }
            if (refreshComic.getFromPreferences("showHideOpenTime").equals("Invisible")) {
                openTime.setVisibility(View.INVISIBLE);
            } else if (refreshComic.getFromPreferences("showHideOpenTime").equals("Visible")) {
                openTime.setVisibility(View.VISIBLE);
            } else {
                openTime.setVisibility(View.VISIBLE);
            }
        }
    }
}
