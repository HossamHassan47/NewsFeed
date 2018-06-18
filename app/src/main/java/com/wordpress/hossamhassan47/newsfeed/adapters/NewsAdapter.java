package com.wordpress.hossamhassan47.newsfeed.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.wordpress.hossamhassan47.newsfeed.R;
import com.wordpress.hossamhassan47.newsfeed.model.NewsItem;

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
        TextView txtTitle = listItemView.findViewById(R.id.text_view_title);
        txtTitle.setText(current.getWebTitle());

        // Section Name
        TextView txtSection = listItemView.findViewById(R.id.text_view_section);
        txtSection.setText(current.getSectionName());

        // Date
        TextView txtDate = listItemView.findViewById(R.id.text_view_date);
        txtDate.setText(getDate(current.getWebPublicationDate()));

        // Image
        ImageView imgThumbnail = (ImageView) listItemView.findViewById(R.id.image_view_thumbnail);

        Glide.with(getContext()).load(current.getThumbnail()).into(imgThumbnail);

        return listItemView;
    }

    private String getDate(String longDate) {
        return longDate.substring(0, longDate.lastIndexOf("T"));
    }
}
