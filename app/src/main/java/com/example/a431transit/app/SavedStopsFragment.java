package com.example.a431transit.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.example.a431transit.R;
import com.example.a431transit.model.stops.BusStop;
import com.example.a431transit.util.api_communication.TransitAPIService;
import com.example.a431transit.util.bus_stop_expandable_list.BusStopGridViewItemClickInterface;
import com.example.a431transit.util.bus_stop_expandable_list.CategoryExpandableListAdapter;
import com.example.a431transit.util.storage_managers.CategoriesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SavedStopsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SavedStopsFragment extends Fragment implements BusStopGridViewItemClickInterface {
    private List<String> categoryNames;
    private HashMap<String, List<BusStop>> categoryChildren;
    private CategoriesManager categoriesManager;

    private ExpandableListView categoryListView;
    private CategoryExpandableListAdapter categoryExpandableListAdapter;

    private static final int REQUEST_CODE = 1;
    TransitAPIService transitService;

    public SavedStopsFragment() {
        // Required empty public constructor
    }

    public SavedStopsFragment(TransitAPIService transitService) {
        this.transitService = transitService;
    }

    public static SavedStopsFragment newInstance() {
        return new SavedStopsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_saved_stops, container, false);

        categoriesManager = new CategoriesManager(getContext());

        categoryListView = rootView.findViewById(R.id.SavedStopsExpandableList);
        prepareListData();

        categoryExpandableListAdapter = new CategoryExpandableListAdapter(getActivity(), categoryNames, categoryChildren, this, transitService);
        categoryListView.setAdapter(categoryExpandableListAdapter);

        categoryListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // This way the expander cannot be collapsed
                // on click event of group item
                return false;
            }
        });

        return rootView;
    }

    private void prepareListData() {
        categoriesManager.update();

        categoryNames = categoriesManager.getCategories();
        categoryChildren = new HashMap<>();

        for(String category : categoryNames)
        {
            categoryChildren.put(category, categoriesManager.getBusStopsFromCategory(category));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                prepareListData();
                categoryExpandableListAdapter.setData(categoryNames,categoryChildren);
            }
        }
    }

    @Override
    public void onGridViewBusStopClick(BusStop busStop) {
        Intent intent = new Intent(getContext(), BusArrivals.class);

        intent.putExtra("BUS_STOP", busStop);

        startActivityForResult(intent, REQUEST_CODE);
    }
}