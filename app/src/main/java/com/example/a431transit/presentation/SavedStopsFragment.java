package com.example.a431transit.presentation;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import androidx.fragment.app.Fragment;

import com.example.a431transit.R;
import com.example.a431transit.application.AppConstants;
import com.example.a431transit.objects.bus_stop.BusStop;
import com.example.a431transit.presentation.Dialogs.CategoryDialogs;
import com.example.a431transit.presentation.front_end_objects.CategoryList;
import com.example.a431transit.api.transit_api.TransitAPIService;
import com.example.a431transit.util.bus_stop_expandable_list.BusStopGridViewItemClickInterface;
import com.example.a431transit.util.bus_stop_expandable_list.CategoryExpandableListAdapter;
import com.example.a431transit.util.storage_managers.CategoriesManager;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class SavedStopsFragment extends Fragment implements BusStopGridViewItemClickInterface {
    private TransitAPIService transitService;
    private List<String> categoryNames;
    private HashMap<String, List<BusStop>> categoryChildren;
    //todo communicate to logic layer instead
    private CategoriesManager categoriesManager;
    private CategoryExpandableListAdapter categoryExpandableListAdapter;
    private CategoryList categoryList;

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

        //get user categories and saved stops from storage
        categoriesManager = new CategoriesManager(getContext());
        prepareListData();

        initComponents(rootView);

        //Expand the categories in which the user has left expanded from the previous usage of this app
        categoryList.loadExpandedState();

        return rootView;
    }

    //Load categories and bus stops from storage
    private void prepareListData() {
        //get most recent version of the json file
        categoriesManager.update();

        //todo communicate through logic layer
        //get category list and order them by the earliest creation date
        categoryNames = categoriesManager.getAllCategories();
        Collections.reverse(categoryNames);

        categoryChildren = new HashMap<>();

        //Since the category json stores pointers of the bus stop data, get actual bus stop instances
        for (String category : categoryNames) {
            categoryChildren.put(category, categoriesManager.getBusStopsFromCategory(category));
        }
    }

    @Override
    //Once the user has left an Arrivals activity, update the expandable list and its components
    //This is because a user can modify categories or unsave a bus stop
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == AppConstants.getRequestCode()) {
            if (resultCode == Activity.RESULT_OK) {
                prepareListData();
                categoryExpandableListAdapter.setData(categoryNames, categoryChildren);
            }
        }
    }

    @Override
    //If a user has clicked on a bus stop, show them its arrivals screen
    public void onGridViewBusStopClick(BusStop busStop) {
        MainActivity.startIntent(getContext(), busStop);
    }

    private void addCategoryDialog() {
        CategoryDialogs.showAddCategoryDialog(getContext(), categoriesManager, this::updateCategoryList);
    }

    private void removeCategoriesDialog() {
        CategoryDialogs.showRemoveCategoriesDialog(getContext(), categoriesManager, this::updateCategoryList);
    }

    private void updateCategoryList() {
        prepareListData();
        categoryExpandableListAdapter.setData(categoryNames, categoryChildren);
        categoryList.loadExpandedState();
        categoryList.saveExpandedState();
    }
    private void initComponents(View rootView) {
        //Get Components
        ImageButton editCategoryBtn = rootView.findViewById(R.id.SavedEditCategoriesBtn);

        ExpandableListView categoryListView = rootView.findViewById(R.id.SavedStopsExpandableList);
        categoryExpandableListAdapter = new CategoryExpandableListAdapter(getActivity(), categoryNames, categoryChildren, this, transitService);
        categoryListView.setAdapter(categoryExpandableListAdapter);
        categoryList = new CategoryList(categoryListView, categoryExpandableListAdapter, getContext());

        //Set listeners for the buttons
        categoryListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                // This way the expander cannot be collapsed
                // on click event of group item
                return false;
            }
        });

        // Move PopupMenu logic to Dialogs class
        editCategoryBtn.setOnClickListener(view -> CategoryDialogs.showEditCategoryMenu(
                getContext(),
                view,
                this::addCategoryDialog,
                this::removeCategoriesDialog
        ));

        //save the expanded state when a group is expanded
        categoryListView.setOnGroupExpandListener((groupPosition) -> categoryList.saveExpandedState());

        //save the expanded state when a group is collapsed
        categoryListView.setOnGroupCollapseListener((groupPosition) -> categoryList.saveExpandedState());
    }
}