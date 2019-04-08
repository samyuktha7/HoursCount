package countedhours.hourscount.Fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import countedhours.hourscount.Database.SheetsData;
import countedhours.hourscount.Database.SqLiteDatabaseHelper;
import countedhours.hourscount.R;


public class Sheets_Fragment extends Fragment {

    private String TAG = "HC_"+Sheets_Fragment.class.getSimpleName();

    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private SqLiteDatabaseHelper dbhelper;
    private Cursor mCursor;
    public List<SheetsData> data;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_sheets, container, false);
        Log.d(TAG, "onCreateView");

        dbhelper = SqLiteDatabaseHelper.getInstance(this.getActivity());

        /*
        Retrieves Information from Sheets database - which stores the week end date and number of
        hours worked during that week. Fetches the cursor and changes into list, which is updated
        to the adapter of recylcer view.
         */
        mCursor = dbhelper.retrieveSheetsInfo();
        if (mCursor != null) {
            data = new ArrayList<>();
            while (mCursor.moveToNext()) {
                SheetsData sheet = new SheetsData();
                sheet.setWeekEnd(mCursor.getString(0));
                sheet.setHours(mCursor.getFloat(1));
                data.add(sheet);
                Log.d(TAG, "sheet added");
            }

            recyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
            // use a linear layout manager
            layoutManager = new LinearLayoutManager(this.getActivity());
            recyclerView.setLayoutManager(layoutManager);

            MyAdapter adapter = new MyAdapter();
            recyclerView.setAdapter(adapter);

        } else {
            Log.w(TAG, "Sheets Cursor is null");
        }
        return v;
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyviewHolder> {

        @NonNull
        @Override
        public MyAdapter.MyviewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            Log.d(TAG, "onCreateViewHolder");
            View v= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.eachlayout, viewGroup, false);
            return new MyviewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull MyAdapter.MyviewHolder myviewHolder, int i) {
            Log.d(TAG, "onBindViewHolder");
            if (data != null) {
                SheetsData sheetData = data.get(i);
                myviewHolder.weekNumber.setText(sheetData.getWeekEnd());
                myviewHolder.noOfHours.setText(String.valueOf(sheetData.getHours()));
            } else {
                Log.d(TAG, "onBindViewHolder() : sheets data is null");
            }
        }

        @Override
        public int getItemCount() {
            int count = data.size();
            Log.d(TAG, "getItemCount() = " +count);
            return count;
        }

        public class MyviewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            private TextView weekNumber, noOfHours;
            private MyviewHolder(View itemView) {
                super(itemView);
                weekNumber = itemView.findViewById(R.id.weekName);
                noOfHours = itemView.findViewById(R.id.noOfHours);
            }
        }

    }

}
