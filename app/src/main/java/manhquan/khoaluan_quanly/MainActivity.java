package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;


import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import fragment.BillFragment;
import fragment.EmployeeFragment;
import fragment.FoodFragment;
import fragment.IncomeFragment;
import fragment.RestaurantFragment;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DrawerLayout drawer;
    @BindView(R.id.drawer_user_name)
    public TextView txtDrawerUserName;
    @BindView(R.id.drawer_position)
    public TextView txtPosition;
    private FirebaseFirestore db;
    private MaterialDialog create_table_dialog;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawer,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setCheckedItem(R.id.nav_restaurant);
        navigationView.setNavigationItemSelectedListener(this);

        View viewDrawerHeader = navigationView.getHeaderView(0);
        ButterKnife.bind(this,viewDrawerHeader);

        int position = getIntent().getIntExtra(QuanLyConstants.EMPLOYEE_POSITION,0);
        String emName = getIntent().getStringExtra(QuanLyConstants.EMPLOYEE_NAME);
        if(getPosition() != position){
            savePosition(position);
        }
        renderDrawerData(emName,position);


        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = new RestaurantFragment();
        fragmentManager.beginTransaction().replace(R.id.main_app_framelayout,fragment).commit();


    }

    private void renderDrawerData(String emName, int position){
        String pos;
        switch (position){
            case 1: pos = getResources().getString(R.string.manager);
                    break;
            case 2: pos = getResources().getString(R.string.cook);
                    break;
            case 3: pos = getResources().getString(R.string.waiter);
                    break;
            case 4: pos = getResources().getString(R.string.cashier);
                    break;
            default: pos = "????";
                    break;
        }
        txtDrawerUserName.setText(emName);
        txtPosition.setText(pos);
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
        if(id == android.R.id.home){
            if(drawer.isDrawerOpen(Gravity.START)){
                drawer.closeDrawer(Gravity.START);
            }
            else{
                drawer.openDrawer(GravityCompat.START);
            }
            return true;
        }
        if(id == QuanLyConstants.CREATE_TABLE_ID){
            create_table_dialog = new MaterialDialog.Builder(this)
                    .positiveText(getResources().getString(R.string.main_agree))
                    .negativeText(getResources().getString(R.string.main_disagree))
                    .positiveColor(getResources().getColor(R.color.primary_dark))
                    .negativeColor(getResources().getColor(R.color.black))
                    .title(getResources().getString(R.string.action_create_table))
                    .customView(R.layout.create_table_dialog,true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            View view = dialog.getView();
                            EditText edNumber = view.findViewById(R.id.create_table_number);
                            RadioButton rbCreate = view.findViewById(R.id.create_table_create_radio);
                            int NumberTableCurrent = Integer.parseInt(getTableNumber());
                            int NumberTableNeedChange = Integer.parseInt(edNumber.getText().toString());

                            String restaurantID = getRestaurantID();
                            if(rbCreate.isChecked()){
                                createTable(NumberTableCurrent,NumberTableNeedChange,restaurantID);
                            }
                            else{
                                if(NumberTableCurrent < NumberTableNeedChange){
                                    Toast.makeText(view.getContext(),
                                            getResources().getString(R.string.table_error_delete_too_much,
                                            new Object[]{NumberTableNeedChange,NumberTableCurrent}),Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    deleteTable(NumberTableCurrent,NumberTableNeedChange,restaurantID);
                                }
                            }
                        }
                    })
                    .build();
            create_table_dialog.show();
            //View viewDialog = create_table_dialog.getCustomView();

        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteTable(final int numberTableCurrent, int numberTableNeedChange, String restaurantID) {
        final int NumberAfterChange = numberTableCurrent - numberTableNeedChange;
        db.collection(QuanLyConstants.TABLE)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                int tableNum = Integer.parseInt(document.get(QuanLyConstants.TABLE_NUMBER).toString());
                                if(tableNum <= numberTableCurrent && tableNum > NumberAfterChange){
                                    db.collection(QuanLyConstants.TABLE)
                                            .document(document.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {

                                                }
                                            });
                                }
                            }
                            recreate();
                        }
                    }
                });
        Toast.makeText(this,"Delete Done",Toast.LENGTH_SHORT).show();
    }

    private void createTable(int numberTableCurrent, int numberTableNeedChange, String restaurantID) {
        int NumberAfterChange = numberTableCurrent + numberTableNeedChange;
        for(int i = numberTableCurrent+1; i <= NumberAfterChange; i++){
            Map<String, Object> table = new HashMap<>();
            table.put(QuanLyConstants.TABLE_NUMBER,i);
            table.put(QuanLyConstants.TABLE_ORDER_ID,1);
            table.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
            db.collection(QuanLyConstants.TABLE)
                    .add(table)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {

                        }
                    });
        }
        recreate();
        Toast.makeText(this,"Create Done",Toast.LENGTH_SHORT).show();
    }

    private void SignOut() {
        new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.action_sign_out))
                .positiveText(getResources().getString(R.string.main_agree))
                .negativeText(getResources().getString(R.string.main_disagree))
                .positiveColor(getResources().getColor(R.color.primary_dark))
                .negativeColor(getResources().getColor(R.color.black))
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

    /*
    *
    *@author: ManhLD
    *Select language for app. Till now app support 2 language is English and Vietnamese
    * */
    private void selectLanguage(){
        new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.main_language))
                .positiveText(getResources().getString(R.string.main_agree))
                .negativeText(getResources().getString(R.string.main_disagree))
                .positiveColor(getResources().getColor(R.color.primary_dark))
                .negativeColor(getResources().getColor(R.color.black))
                .widgetColor(getResources().getColor(R.color.colorAccent))
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
        String langPref = QuanLyConstants.SHARED_LANGUAGE;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
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
        String langPref = QuanLyConstants.SHARED_LANGUAGE;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(langPref, language);
        editor.apply();
    }

    public void savePosition(int position){
        String positionPref = QuanLyConstants.SHARED_POSITION;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE,
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(positionPref, position);
        editor.apply();
    }

    public int getPosition(){
        String langPref = QuanLyConstants.SHARED_POSITION;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getInt(langPref,0);
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
                fragment = new BillFragment();
                break;
            case R.id.nav_revenue:
                fragment = new IncomeFragment();
                break;
            case R.id.nav_account:
                fragment = new EmployeeFragment();
                break;
            case R.id.nav_food:
                fragment = new FoodFragment();
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

    /*
     * @author: ManhLD
     * @purpose: Get the restaurantID of the restaurant from SharedPreferences
     * */
    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

    /*
     * @author: ManhLD
     * @purpose: Get the tableNumber of the restaurant from SharedPreferences
     * */
    public String getTableNumber(){
        String langPref = QuanLyConstants.TABLE_NUMBER;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"0");
    }
}
