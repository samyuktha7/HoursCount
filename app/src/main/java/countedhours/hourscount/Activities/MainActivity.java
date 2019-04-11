package countedhours.hourscount.Activities;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import countedhours.hourscount.Fragments.Sheets_Fragment;
import countedhours.hourscount.Fragments.Today_Fragment;
import countedhours.hourscount.Fragments.Week_Fragment;
import countedhours.hourscount.R;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.FrameLayout;

/*
This activity is the main activity with a frame layout, which accomodates three fragments to view
Today, Weekly, Sheets.
 */
public class MainActivity extends AppCompatActivity {

    private String TAG = "HC_"+MainActivity.class.getSimpleName();

    private Fragment mFragment;


    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() ");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView mBottomNavigation = (BottomNavigationView)findViewById(R.id.navigationBar);

        final Today_Fragment todayFragment = new Today_Fragment();
        final Week_Fragment weekFragment = new Week_Fragment();
        final Sheets_Fragment sheetsFragment = new Sheets_Fragment();
        addOrReplaceFragment(todayFragment);

        mBottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.todayTAB:
                        addOrReplaceFragment(todayFragment);
                        break;
                    case R.id.weekTAB:
                        addOrReplaceFragment(weekFragment);
                        break;
                    case R.id.sheetsTAB:
                        addOrReplaceFragment(sheetsFragment);
                        break;
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private void addOrReplaceFragment(Fragment fragment) {
        Log.d(TAG, "addOrReplaceFragment "+fragment + "  "+mFragment);
        android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
        android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if(mFragment == null) {
            fragmentTransaction.add(R.id.frameLayout, fragment);
        } else if (!mFragment.equals(fragment)) {
            fragmentTransaction.replace(R.id.frameLayout , fragment);
        }
        fragmentTransaction.commit();
        mFragment = fragment;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.profile , menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected() ");
        if(item.getItemId() == R.id.profilebutton) {
            Intent profileNavigationIntent = new Intent(this , ProfileActivity.class);
            startActivity(profileNavigationIntent);
        }
        return true;
    }
}
