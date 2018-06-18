package com.wordpress.hossamhassan47.newsfeed.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.wordpress.hossamhassan47.newsfeed.R;
import com.wordpress.hossamhassan47.newsfeed.model.NewsItem;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NewsAdapter extends ArrayAdapter<NewsItem> {

    public NewsAdapter(Context context, List<NewsItem> objects) {
        super(context, 0, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View listItemView = convertView;

        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.news_list_item, parent, false);
        }

        NewsItem current = getItem(position);

        // Title
        TextView txtTitle = listItemView.findViewById(R.id.text_view_webTitle);
        txtTitle.setText(current.getWebTitle());

        // Section Name
        TextView txtSection = listItemView.findViewById(R.id.text_view_sectionName);
        txtSection.setText(current.getSectionName());

        TextView txtDate = listItemView.findViewById(R.id.text_view_date);
        txtDate.setText(current.getWebPublicationDate());

        TextView txtTime = listItemView.findViewById(R.id.text_view_time);
        txtTime.setText(current.getWebPublicationDate());

        return listItemView;
    }

    /**
     * Return the formatted date string (i.e. "Mar 3, 1984") from a Date object.
     */
    private String formatDate(Date dateObject) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("LLL dd, yyyy");
        return dateFormat.format(dateObject);
    }

    /**
     * Return the formatted date string (i.e. "4:30 PM") from a Date object.
     */
    private String formatTime(Date dateObject) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");
        return timeFormat.format(dateObject);
    }
}
