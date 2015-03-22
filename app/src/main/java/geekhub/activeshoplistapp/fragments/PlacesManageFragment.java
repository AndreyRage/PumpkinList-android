package geekhub.activeshoplistapp.fragments;

import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import geekhub.activeshoplistapp.R;
import geekhub.activeshoplistapp.activities.MapActivity;
import geekhub.activeshoplistapp.adapters.PlaceAdapter;
import geekhub.activeshoplistapp.helpers.AppConstants;
import geekhub.activeshoplistapp.helpers.ContentHelper;
import geekhub.activeshoplistapp.helpers.SqlDbHelper;
import geekhub.activeshoplistapp.model.PlacesModel;
import geekhub.activeshoplistapp.model.ShoppingContentProvider;

/**
 * Created by rage on 08.02.15. Create by task: 004
 */
public class PlacesManageFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = PlacesManageFragment.class.getSimpleName();
    private static final String ARG_MENU_ID = "argMenuId";
    private ListView shopListView;
    private View plusButton;
    private List<PlacesModel> placesList;
    private PlaceAdapter adapter;
    private int menuItemId = -1;

    public PlacesManageFragment() {
    }

    public static PlacesManageFragment newInstance(int menuItemId) {
        PlacesManageFragment fragment = new PlacesManageFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_MENU_ID, menuItemId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_places_manage, container, false);
        addToolbar(view);
        shopListView = (ListView) view.findViewById(R.id.shop_list);
        plusButton = view.findViewById(R.id.plus_button);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            menuItemId = getArguments().getInt(ARG_MENU_ID);
        }

        if (placesList == null) {
            placesList = new ArrayList<>();
        }
        getLoaderManager().initLoader(menuItemId, null, this);

        adapter = new PlaceAdapter(getActivity(), R.layout.item_shop, placesList);
        shopListView.setAdapter(adapter);
        shopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), MapActivity.class)
                        .putExtra(AppConstants.EXTRA_MENU_ITEM, menuItemId)
                        .putExtra(
                                AppConstants.EXTRA_PLACE_ID,
                                adapter.getItem(position).getDbId()
                        );
                startActivityForResult(intent, AppConstants.SHOP_RESULT_CODE);
            }
        });

        plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), MapActivity.class)
                        .putExtra(AppConstants.EXTRA_MENU_ITEM, menuItemId);
                startActivityForResult(intent, AppConstants.SHOP_RESULT_CODE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //refreshPlaces();
        adapter.notifyDataSetChanged();
    }

    /*private List<PlacesModel> getShopsList() {
        List<PlacesModel> list = new ArrayList<>();
        for (PlacesModel placesModel : ShoppingHelper.getInstance().getPlacesList()) {
            if (placesModel.getCategory() == AppConstants.PLACES_SHOP) {
                list.add(placesModel);
            }
        }
        return list;
    }

    private List<PlacesModel> getUserPlacesList() {
        List<PlacesModel> list = new ArrayList<>();
        for (PlacesModel placesModel : ShoppingHelper.getInstance().getPlacesList()) {
            if (placesModel.getCategory() == AppConstants.PLACES_USER) {
                list.add(placesModel);
            }
        }
        return list;
    }*/

    private void refreshShopsList(Cursor cursor) {
        if (placesList == null) {
            placesList = new ArrayList<>();
        }
        if (placesList.size() > 0) {
            placesList.clear();
        }
        placesList.addAll(ContentHelper.getPlaceList(cursor));
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getContentResolver().registerContentObserver(
                ShoppingContentProvider.PLACE_CONTENT_URI,
                true,
                contentObserver
        );
    }

    @Override
    public void onPause() {
        super.onPause();
        getActivity().getContentResolver().unregisterContentObserver(contentObserver);
    }

    private ContentObserver contentObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
        }
    };

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case AppConstants.MENU_SHOW_SHOPS: {
                String[] projection = {
                        SqlDbHelper.COLUMN_ID,
                        SqlDbHelper.PLACES_COLUMN_PLACES_ID,
                        SqlDbHelper.PLACES_COLUMN_CATEGORY,
                        SqlDbHelper.PLACES_COLUMN_NAME,
                        SqlDbHelper.PLACES_COLUMN_DESCRIPTION,
                        SqlDbHelper.PLACES_COLUMN_LATITUDE,
                        SqlDbHelper.PLACES_COLUMN_LONGITUDE,
                        SqlDbHelper.PLACES_COLUMN_IS_DELETE,
                        SqlDbHelper.PLACES_COLUMN_TIMESTAMP,
                };
                String orderBy = SqlDbHelper.COLUMN_ID + " DESC";
                return new CursorLoader(
                        getActivity(),
                        ShoppingContentProvider.PLACE_CONTENT_URI,
                        projection,
                        SqlDbHelper.PLACES_COLUMN_CATEGORY + "=?",
                        new String[]{Long.toString(AppConstants.PLACES_SHOP)},
                        orderBy
                );
            }
            case AppConstants.MENU_SHOW_PLACES: {
                String[] projection = {
                        SqlDbHelper.COLUMN_ID,
                        SqlDbHelper.PLACES_COLUMN_PLACES_ID,
                        SqlDbHelper.PLACES_COLUMN_CATEGORY,
                        SqlDbHelper.PLACES_COLUMN_NAME,
                        SqlDbHelper.PLACES_COLUMN_DESCRIPTION,
                        SqlDbHelper.PLACES_COLUMN_LATITUDE,
                        SqlDbHelper.PLACES_COLUMN_LONGITUDE,
                        SqlDbHelper.PLACES_COLUMN_IS_DELETE,
                        SqlDbHelper.PLACES_COLUMN_TIMESTAMP,
                };
                String orderBy = SqlDbHelper.COLUMN_ID + " DESC";
                return new CursorLoader(
                        getActivity(),
                        ShoppingContentProvider.PLACE_CONTENT_URI,
                        projection,
                        SqlDbHelper.PLACES_COLUMN_CATEGORY + "=?",
                        new String[]{Long.toString(AppConstants.PLACES_USER)},
                        orderBy
                );
            }
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        refreshShopsList(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
