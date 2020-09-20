package com.task.worldmap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.task.worldmap.model.Locality;
import com.task.worldmap.storage.DBHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PlaceAutoSuggestAdapter extends ArrayAdapter implements Filterable {

    ArrayList<Locality> results = new ArrayList<>();
    int resource;
    Context context;
    DBHelper dbHelper;

    HandlePlaceApi placeApi = new HandlePlaceApi();

    public PlaceAutoSuggestAdapter(Context context, int simple_list_item_1) {
        super(context, simple_list_item_1);
        this.context = context;
        this.resource = simple_list_item_1;
        dbHelper = new DBHelper(context);
    }


    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View convertView, ViewGroup parent) {

        View layout =  LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_1, parent, false);
        TextView tvLocalityName = (TextView) layout.findViewById(R.id.tv_locality_name);
        ImageView ivSavedOne = (ImageView) layout.findViewById(R.id.iv_image);
        tvLocalityName.setText(results.get(position).getLocality());
        if(results.get(position).getSavedOneOrNot() == 1){
            ivSavedOne.setVisibility(View.VISIBLE);
        }else {
            ivSavedOne.setVisibility(View.GONE);
        }
        return layout;
    }


    @Override
    public int getCount() {
        return results.size();
    }

    @Override
    public String getItem(int pos) {
        String searchResult = "";
        if (results.size() > pos) {
            searchResult = results.get(pos).getLocality() != null ? results.get(pos).getLocality() : "";
        }
        return searchResult;
    }

    @Override
    public Filter getFilter() {
        Filter filter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if (constraint != null) {
                    Set<Locality>items = new HashSet<>();
                    List<String> savedList = dbHelper.getSearchedLocationIfExist((String) constraint);
                    for (int i = 0; i< savedList.size(); i++){
                        items.add(new Locality(1, savedList.get(i)));
                    }

                    List<String>  searchedSuggestions = placeApi.autoComplete(constraint.toString());
                    for (int i = 0; i< searchedSuggestions.size(); i++){
                        if(savedList != null && savedList.size() > 0) {
                            for (int j = 0; j < savedList.size(); j++) {
                                if (!searchedSuggestions.get(i).contains(savedList.get(j)))
                                    items.add(new Locality(0, searchedSuggestions.get(i)));
                            }
                        }else{
                            items.add(new Locality(0, searchedSuggestions.get(i)));
                        }
                    }

                    results = new ArrayList<>(items);
                    filterResults.values = results;
                    if (results != null && results.size() > 0) {
                        filterResults.count = results.size();
                    }
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }

            }
        };
        return filter;
    }


}
