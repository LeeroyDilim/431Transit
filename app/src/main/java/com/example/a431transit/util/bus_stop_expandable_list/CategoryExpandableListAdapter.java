package com.example.a431transit.util.bus_stop_expandable_list;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.a431transit.R;
import com.example.a431transit.model.stops.BusStop;
import com.example.a431transit.util.api_communication.TransitAPIService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CategoryExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private List<String> categoryHeaders;
    private HashMap<String, List<BusStop>> categoryChildren;
    private LayoutInflater layoutInflater;
    private BusStopGridViewItemClickInterface busStopGridViewItemClickListener;
    private TransitAPIService transitAPIService;
    private final int COLUMN_COUNT = 2;

    public CategoryExpandableListAdapter(Context context, List<String> categoryHeaders, HashMap<String, List<BusStop>> categoryChildren,
                                         BusStopGridViewItemClickInterface busStopGridViewItemClickInterface, TransitAPIService transitAPIService)
    {
        this.context = context;
        this.categoryHeaders = categoryHeaders;
        this.categoryChildren = categoryChildren;
        this.busStopGridViewItemClickListener = busStopGridViewItemClickInterface;
        this.layoutInflater = LayoutInflater.from(context);
        this.transitAPIService = transitAPIService;
    }
    @Override
    public int getGroupCount() {
        return categoryHeaders.size();
    }

    public void setData(List<String> categoryHeaders, HashMap<String, List<BusStop>> categoryChildren)
    {
        this.categoryHeaders = categoryHeaders;
        this.categoryChildren = categoryChildren;
        notifyDataSetChanged();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return categoryHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return categoryChildren.get(this.categoryHeaders.get(groupPosition)).size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String categoryTitle = (String) getGroup(groupPosition);

        if(convertView == null)
        {
            convertView = layoutInflater.inflate((R.layout.expandable_list_header),null);
        }

        TextView categoryNameView = convertView.findViewById(R.id.expandable_list_header_name);
        categoryNameView.setText(categoryTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        convertView = layoutInflater.inflate(R.layout.expandable_list_item, null);
        final List<BusStop> items = categoryChildren.get(categoryHeaders.get(groupPosition));

        if (items.size() > 0) {
            BusStopGridView gridView = (BusStopGridView) convertView.findViewById(R.id.bus_stop_gridView);
            gridView.setNumColumns(COLUMN_COUNT);

            BusStopGridAdapter adapter = new BusStopGridAdapter(context, items, transitAPIService);
            gridView.setAdapter(adapter);

            int totalHeight = 0;

            // This is to get the actual size of gridView at runtime while filling the items into it
            for (int size = 0; size < adapter.getCount(); size++) {
                RelativeLayout relativeLayout = (RelativeLayout) adapter.getView(size, null, gridView);
                relativeLayout.measure(0, 0);

                //will fix this later!
                int itemHeight = relativeLayout.getMeasuredHeight() + 50; //this method is not completely accurate

                if (size % COLUMN_COUNT == 0) {
                    // New row starts, add the height of the first item
                    totalHeight += itemHeight;
                } else {
                    // Add the height of subsequent items in the same row
                    totalHeight = Math.max(totalHeight, itemHeight);
                }
            }

            gridView.setGridViewItemHeight(totalHeight);

            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    busStopGridViewItemClickListener.onGridViewBusStopClick(items.get(position));
                }
            });
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
