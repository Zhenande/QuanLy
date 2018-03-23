package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.firebase.auth.FirebaseAuth;


import java.util.Locale;

import fragment.EmployeeFragment;
import fragment.IncomeFragment;
import fragment.RestaurantFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int position = getIntent().getIntExtra("position",0);
        Toast.makeText(this,"Position is " + position, Toast.LENGTH_SHORT).show();

        Toolbar toolbar = findViewById(R.id.toolbar);
        mAuth = FirebaseAuth.getInstance();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new RestaurantFragment();
        fragmentManager.beginTransaction().replace(R.id.main_app_framelayout,fragment).commit();


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_language) {
            selectLanguage();
            return true;
        }
        if(id == R.id.action_SignIn){
            SignOut();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void SignOut() {
        new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.action_sign_out))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        //Intent i = new Intent(MainActivity.this, SignInActivity.class);
                        mAuth.signOut();
                        onBackPressed();
                    }
                })
                .positiveText(getResources().getString(R.string.main_agree))
                .negativeText(getResources().getString(R.string.main_disagree))
                .show();
    }


    // Not Done
    // Does not know how to restart activity
    private void selectLanguage(){
        new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.main_language))
                .items(getResources().getStringArray(R.array.main_array_language))
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which, CharSequence text) {
                        Configuration config;
                        config = new Configuration(getResources().getConfiguration());
                        if(which == 0){
                            Locale locale = new Locale("en");
                            config.locale = locale;
                            changeLanguage(locale.toString());
                        }
                        else{
                            Locale locale = new Locale("vi");
                            config.locale = locale;
                            changeLanguage(locale.toString());
                        }
                        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
                        recreate();
                        return true;
                    }
                })
                .positiveText(getResources().getString(R.string.main_agree))
                .negativeText(getResources().getString(R.string.main_disagree))
                .show();
    }

    public void loadLocale(){
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        String language = prefs.getString(langPref,"");
        changeLanguage(language);
    }

    private void changeLanguage(String language) {
        if(language.equalsIgnoreCase("")){
            return;
        }
        Locale myLocale = new Locale(language);
        saveLanguage(language);
        Locale.setDefault(myLocale);
        android.content.res.Configuration config = new android.content.res.Configuration();
        config.locale = myLocale;
        getBaseContext().getResources().updateConfiguration(config,getBaseContext().getResources().getDisplayMetrics());
    }

    public void saveLanguage(String language){
        String langPref = "Language";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, language);
        editor.apply();
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        Fragment fragment;

        int id = item.getItemId();

        switch (id){
            case R.id.nav_restaurant:
                fragment = new RestaurantFragment();
                break;
            case R.id.nav_bill:
                fragment = new IncomeFragment();
                break;
            case R.id.nav_account:
                fragment = new EmployeeFragment();
                break;
            default:
                fragment = new RestaurantFragment();
                break;
        }

        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_app_framelayout,fragment).commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
