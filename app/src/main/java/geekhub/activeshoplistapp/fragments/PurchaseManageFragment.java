package geekhub.activeshoplistapp.fragments;

import android.app.Activity;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Build;
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
import android.widget.GridView;

import geekhub.activeshoplistapp.R;
import geekhub.activeshoplistapp.adapters.PurchaseListAdapter;
import geekhub.activeshoplistapp.helpers.ContentHelper;
import geekhub.activeshoplistapp.helpers.SqlDbHelper;
import geekhub.activeshoplistapp.model.ShoppingContentProvider;

/**
 * Created by rage on 08.02.15. Create by task: 004
 */
public class PurchaseManageFragment extends BaseFragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = PurchaseManageFragment.class.getSimpleName();

    private GridView purchaseView;
    private OnPurchaseListMainFragmentListener purchaseListMainFragmentListener;
    private PurchaseListAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_purchase_manage, container, false);
        addToolbar(view);
        purchaseView = (GridView) view.findViewById(R.id.purchase_view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            adapter = new PurchaseListAdapter(getActivity(), null, R.layout.item_purchase_view);
        } else {
            adapter = new PurchaseListAdapter(getActivity(), null, 0, R.layout.item_purchase_view);
        }
        purchaseView.setAdapter(adapter);
        purchaseView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                purchaseListMainFragmentListener.onPurchaseListMainFragmentClickListener(
                        ContentHelper.getDbId((Cursor) purchaseView.getItemAtPosition(position))
                );
            }
        });
        getLoaderManager().initLoader(0, null, this);

        //Show edit fragment
        view.findViewById(R.id.plus_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                purchaseListMainFragmentListener.onPurchaseListMainFragmentClickListener();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            purchaseListMainFragmentListener = (OnPurchaseListMainFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnLoginFragmentListener");
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        getActivity().getContentResolver().registerContentObserver(
                ShoppingContentProvider.PURCHASE_ITEM_CONTENT_URI,
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
        String[] projection = {
                SqlDbHelper.COLUMN_ID,
                SqlDbHelper.PURCHASE_LIST_COLUMN_LIST_ID,
                SqlDbHelper.PURCHASE_LIST_COLUMN_LIST_NAME,
                SqlDbHelper.PURCHASE_LIST_COLUMN_USER_ID,
                SqlDbHelper.PURCHASE_LIST_COLUMN_SHOP_ID,
                SqlDbHelper.PURCHASE_LIST_COLUMN_PLACE_ID,
                SqlDbHelper.PURCHASE_LIST_COLUMN_DONE,
                SqlDbHelper.PURCHASE_LIST_COLUMN_MAX_DISTANCE,
                SqlDbHelper.PURCHASE_LIST_COLUMN_IS_ALARM,
                SqlDbHelper.PURCHASE_LIST_COLUMN_TIME_ALARM,
                SqlDbHelper.PURCHASE_LIST_COLUMN_TIME_CREATE,
                SqlDbHelper.PURCHASE_LIST_COLUMN_TIMESTAMP,
        };
        String orderBy = SqlDbHelper.COLUMN_ID + " DESC";
        return new CursorLoader(
                getActivity(),
                ShoppingContentProvider.PURCHASE_LIST_CONTENT_URI,
                projection,
                null,
                null,
                orderBy
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.changeCursor(null);
    }

    public interface OnPurchaseListMainFragmentListener {
        void onPurchaseListMainFragmentClickListener();
        void onPurchaseListMainFragmentClickListener(long id);
    }
}
