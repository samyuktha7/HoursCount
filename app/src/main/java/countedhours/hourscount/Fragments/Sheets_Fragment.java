package countedhours.hourscount.Fragments;

import android.arch.persistence.room.Room;
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
import java.util.List;

import countedhours.hourscount.Database.DatabaseHelper;
import countedhours.hourscount.Database.Sheets;
import countedhours.hourscount.R;


public class Sheets_Fragment extends Fragment {

    private String TAG = "HC_"+Sheets_Fragment.class.getSimpleName();
    public List<Sheets> data;
    private DatabaseHelper dbHelper;
    private View v;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_sheets, container, false);
        Log.d(TAG, "onCreateView");
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (this.getActivity() != null) {
            dbHelper = Room.databaseBuilder(this.getActivity(), DatabaseHelper.class, "sheets.db").allowMainThreadQueries().build();

//        Retrieves Information from Sheets database - which stores the week end date and number of
//        hours worked during that week. Fetches the list, which is updated
//        to the adapter of recylcer view.
            data = dbHelper.dao().retrieveSheetsData();

            if (data.size() != 0) {
                Log.d(TAG, "Size of Sheets List "+data.size());
                RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.my_recycler_view);
                // use a linear layout manager
                RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this.getActivity());
                recyclerView.setLayoutManager(layoutManager);

                MyAdapter adapter = new MyAdapter();
                recyclerView.setAdapter(adapter);
            } else {
                Log.d(TAG, "size is 0");
            }

        }
    }

    /*
        Setting up the Recycler view.
            *Each layout has a seperate layout xml file. which is initialized.
            *Number of views in the list are also initialized
            *Every view is then set with values.
         */
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
                Sheets tuple = data.get(i);
                float weekly = 40;
                myviewHolder.weekNumber.setText(tuple.getWeekend());
                float totalTime = tuple.getHours();
                myviewHolder.noOfHours.setText(String.valueOf(tuple.getHours()));
                if (totalTime > weekly) {
                    myviewHolder.noOfHours.setTextColor(getResources().getColor(R.color.secondary));
                    myviewHolder.weekNumber.setTextColor(getResources().getColor(R.color.secondary));
                }
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
