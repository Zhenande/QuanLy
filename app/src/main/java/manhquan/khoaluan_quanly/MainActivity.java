package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import adapter.NotificationAdapter;
import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import fragment.BillFragment;
import fragment.EmployeeFragment;
import fragment.FoodFragment;
import fragment.IncomeFragment;
import fragment.RestaurantFragment;
import model.NotiContent;
import model.Notification;
import util.GlobalVariable;
import util.NotificationTouchHelper;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, NotificationTouchHelper.CallBackRemoveItem {

    private static final String TAG = "MainActivity";
    private FirebaseAuth mAuth;
    private DrawerLayout drawer;
    @BindView(R.id.drawer_user_name)
    public TextView txtDrawerUserName;
    @BindView(R.id.drawer_position)
    public TextView txtPosition;
    @BindView(R.id.drawer_notification)
    public ImageButton buttonNoti;
    private FirebaseFirestore db;
    private NavigationView navigationView;
    private boolean doubleBackToSignOutPressedOnce = false;
    private MaterialDialog dialogChoose;
    private NotificationAdapter adapter;
    private List<Notification> listNotification = new ArrayList<>();
    private RecyclerView recyclerView;
    private boolean isDialogNotificationShowUp = false;
    private boolean isFirst = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        loadLocale();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        onChangeListener(GlobalVariable.employeeID);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
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
        int position = getIntent().getIntExtra(QuanLyConstants.EMPLOYEE_POSITION,0);
        setRoleOfApp(position);
        View viewDrawerHeader = navigationView.getHeaderView(0);
        ButterKnife.bind(this,viewDrawerHeader);

        String emName = getIntent().getStringExtra(QuanLyConstants.EMPLOYEE_NAME);
        if(getPosition() != position){
            savePosition(position);
        }
        renderDrawerData(emName,position);

        onNavigationItemSelected(navigationView.getMenu().getItem(0));
    }


    private void setRoleOfApp(int position) {
        switch (position){
            case 1: // Manager
                    setLayoutForManager();
                    break;
            case 2: // Cook
                    setLayoutForCook();
                    break;
            case 3: // Waiter
                    setLayoutForWaiter();
                    break;
            case 4: // Cashier
                    setLayoutForCashier();
                    break;
        }
    }

    private void setLayoutForCashier() {
        navigationView.getMenu().removeItem(R.id.nav_order);
        navigationView.getMenu().removeItem(R.id.nav_revenue);
        navigationView.getMenu().removeItem(R.id.nav_account);
    }

    private void setLayoutForWaiter() {
        navigationView.getMenu().removeItem(R.id.nav_restaurant);
        navigationView.getMenu().removeItem(R.id.nav_bill);
        navigationView.getMenu().removeItem(R.id.nav_order);
        navigationView.getMenu().removeItem(R.id.nav_revenue);
        navigationView.getMenu().removeItem(R.id.nav_account);
    }

    private void setLayoutForCook() {

        navigationView.getMenu().removeItem(R.id.nav_restaurant);
        navigationView.getMenu().removeItem(R.id.nav_bill);
        navigationView.getMenu().removeItem(R.id.nav_account);
        navigationView.getMenu().removeItem(R.id.nav_revenue);
    }

    private void setLayoutForManager() {
        navigationView.getMenu().removeItem(R.id.nav_restaurant);
        navigationView.getMenu().removeItem(R.id.nav_order);
    }

    private void renderDrawerData(String emName, int position){
        String pos;
        switch (position){
            case 1: pos = getResources().getString(R.string.manager);
                    break;
            case 2: pos = getResources().getString(R.string.cook);
                    break;
            case 3: pos = getResources().getString(R.string.waiter);
                    buttonNoti.setOnClickListener(this);
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
            if (doubleBackToSignOutPressedOnce) {
                Intent i = new Intent(this,SignInActivity.class);
                startActivity(i);
            }

            this.doubleBackToSignOutPressedOnce = true;
            Toast.makeText(this, getResources().getString(R.string.sign_out_noti), Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToSignOutPressedOnce=false;
                }
            }, 2000);
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
            MaterialDialog create_table_dialog = new MaterialDialog.Builder(this)
                    .positiveText(getResources().getString(R.string.main_agree))
                    .negativeText(getResources().getString(R.string.main_disagree))
                    .positiveColor(getResources().getColor(R.color.primary_dark))
                    .negativeColor(getResources().getColor(R.color.black))
                    .title(getResources().getString(R.string.action_create_table))
                    .customView(R.layout.create_table_dialog, true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            View view = dialog.getView();
                            EditText edNumber = view.findViewById(R.id.create_table_number);
                            RadioButton rbCreate = view.findViewById(R.id.create_table_create_radio);
                            if (edNumber.getText().toString().matches("[0-9]*")) {
                                int NumberTableCurrent = Integer.parseInt(getTableNumber());
                                int NumberTableNeedChange = Integer.parseInt(edNumber.getText().toString());

                                String restaurantID = getRestaurantID();
                                if (rbCreate.isChecked()) {
                                    createTable(NumberTableCurrent, NumberTableNeedChange, restaurantID);
                                } else {
                                    if (NumberTableCurrent < NumberTableNeedChange) {
                                        Toast.makeText(view.getContext(),
                                                getResources().getString(R.string.table_error_delete_too_much,
                                                        NumberTableNeedChange, NumberTableCurrent), Toast.LENGTH_SHORT).show();
                                    } else {
                                        deleteTable(NumberTableCurrent, NumberTableNeedChange, restaurantID);
                                    }
                                }
                            } else {
                                Toast.makeText(view.getContext(), getResources().getString(R.string.table_error_input_letter), Toast.LENGTH_SHORT).show();
                            }
                        }
                    })
                    .build();
            create_table_dialog.show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteTable(final int numberTableCurrent, int numberTableNeedChange, final String restaurantID) {
        final int NumberAfterChange = numberTableCurrent - numberTableNeedChange;
        db.collection(QuanLyConstants.TABLE)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                final int tableNum = Integer.parseInt(document.get(QuanLyConstants.TABLE_NUMBER).toString());
                                final String tableID = document.getId();
                                if(tableNum <= numberTableCurrent && tableNum > NumberAfterChange){
                                    db.collection(QuanLyConstants.TABLE)
                                            .document(tableID)
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    db.collection(QuanLyConstants.COOK)
                                                        .document(restaurantID)
                                                        .collection(QuanLyConstants.TABLE)
                                                        .document(tableID)
                                                        .delete();
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

    private void createTable(int numberTableCurrent, int numberTableNeedChange, final String restaurantID) {
        int NumberAfterChange = numberTableCurrent + numberTableNeedChange;
        for(int i = numberTableCurrent+1; i <= NumberAfterChange; i++){
            Map<String, Object> table = new HashMap<>();
            table.put(QuanLyConstants.TABLE_NUMBER,i+"");
            table.put(QuanLyConstants.TABLE_ORDER_ID,"1");
            table.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
            final int num = i;
            db.collection(QuanLyConstants.TABLE)
                    .add(table)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            String tableID = documentReference.getId();
                            Map<String, Object> cook = new HashMap<>();
                            cook.put(QuanLyConstants.TABLE_NUMBER, num);
                            cook.put(QuanLyConstants.FOOD_NAME,"");
                            cook.put(QuanLyConstants.ORDER_TIME,"99:99");
                            db.collection(QuanLyConstants.COOK)
                                .document(restaurantID)
                                .collection(QuanLyConstants.TABLE)
                                .document(tableID)
                                .set(cook);
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
                        Intent i = new Intent(MainActivity.this, SignInActivity.class);
                        mAuth.signOut();
                        startActivity(i);
                    }
                })
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
            case R.id.nav_order:
                fragment = new OrderFragment();
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


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.drawer_notification){
            dialogChoose = new MaterialDialog.Builder(this)
                    .customView(R.layout.notification_dialog, false)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        }
                    })
                    .build();
            dialogChoose.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    clickOnItemListView(dialogChoose);
                }
            });

            dialogChoose.show();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(adapter!=null){
            adapter.onSaveInstanceState(outState);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(adapter!=null){
            adapter.onRestoreInstanceState(savedInstanceState);
        }
    }

    private void clickOnItemListView(final MaterialDialog dialogChoose) {
        View view = dialogChoose.getView();
//        listNotification.clear();
        if(listNotification.size()==0){
            dialogChoose.dismiss();
            Toast.makeText(this, getResources().getString(R.string.notification_error), Toast.LENGTH_SHORT).show();
            return;
        }
        recyclerView = view.findViewById(R.id.recyclerView_notification);
        db.collection(QuanLyConstants.NOTIFICATION)
            .document(GlobalVariable.employeeID)
            .collection(QuanLyConstants.TABLE)
            .orderBy(QuanLyConstants.ORDER_TIME, Query.Direction.ASCENDING)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        if(listNotification.size() == 0) {
                            for (DocumentSnapshot document : task.getResult()) {
                                String[] content = document.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                                List<NotiContent> listNoti = new ArrayList<>();
                                for (String aContent : content) {
                                    listNoti.add(new NotiContent(aContent));
                                }

                                String time = document.get(QuanLyConstants.ORDER_TIME).toString();
                                String title = document.get(QuanLyConstants.TABLE_NUMBER).toString();

                                Notification notification = new Notification(title, listNoti);
                                notification.setTime(time);
                                listNotification.add(notification);
                            }
                        }

                        LinearLayoutManager layoutManager = new LinearLayoutManager(MainActivity.this);

                        adapter = new NotificationAdapter(MainActivity.this,listNotification);
                        recyclerView.setLayoutManager(layoutManager);
                        recyclerView.setAdapter(adapter);

                        NotificationTouchHelper nth = new NotificationTouchHelper(adapter, MainActivity.this);
                        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(nth);
                        itemTouchHelper.attachToRecyclerView(recyclerView);

                        isDialogNotificationShowUp = true;
                    }
                }
            });

    }

    private void onChangeListener(String docID) {
        db.collection(QuanLyConstants.NOTIFICATION)
            .document(docID)
            .collection(QuanLyConstants.TABLE)
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if( e != null){
                        Log.w(TAG, "Listen failed.", e);
                        return;
                    }

                    String source = documentSnapshots != null && documentSnapshots.getMetadata().hasPendingWrites()
                            ? "Local" : "Server";

                    if(documentSnapshots != null && !documentSnapshots.isEmpty()){
                        for(DocumentSnapshot document : documentSnapshots){

                            List<NotiContent> listNoti = new ArrayList<>();
                            String title = document.get(QuanLyConstants.TABLE_NUMBER).toString();
                            for(int i = 0; i < listNotification.size(); i++){
                                if(listNotification.get(i).getTitle().equals(title)){
                                    listNotification.remove(i);
                                    break;
                                }
                            }
                            String[] content = document.get(QuanLyConstants.FOOD_NAME).toString().split(";");

                            for (String aContent : content) {
                                listNoti.add(new NotiContent(aContent));
                            }
                            String time = document.get(QuanLyConstants.ORDER_TIME).toString();

                            Notification notification = new Notification(title, listNoti);
                            notification.setTime(time);
                            listNotification.add(notification);

                            if(isDialogNotificationShowUp){
                                adapter.notifyDataSetChanged();
                            }
                        }

                        if(!isFirst){
                            try {
                                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                                Ringtone r = RingtoneManager.getRingtone(MainActivity.this, notification);
                                r.play();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        isFirst = false;
                    }else{
                        Log.w(TAG, source + " data: null");
                    }
                }
            });

    }

    @Override
    public void onRemoveItem(int position, int parentPos) {
        if(parentPos == -1){
            removeTableFollow(position);
        }
        else{
//            listNotification.get(parentPos).getItems().remove(position-parentPos-1);
            removeFoodOfTable(position, parentPos);
//            if(listNotification.get(parentPos).getItems().size()==0){
//                listNotification.remove(parentPos);
//            }
        }
    }

    private void removeFoodOfTable(final int position, final int parentPos) {
        db.collection(QuanLyConstants.NOTIFICATION)
            .document(GlobalVariable.employeeID)
            .collection(QuanLyConstants.TABLE)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            Notification noti = listNotification.get(parentPos);
                            String titleDoc = document.get(QuanLyConstants.TABLE_NUMBER).toString();
                            if(noti.getTitle().equals(titleDoc)){
                                String[] content = document.get(QuanLyConstants.FOOD_NAME).toString().split(";");
                                int posRemove = position - parentPos-1;
                                if(content.length==1){
                                    document.getReference().delete();
                                    adapter.notifyItemRangeRemoved(position - 1,2);
                                    listNotification.remove(position-1);
                                }
                                else {
                                    StringBuilder builder = new StringBuilder();
                                    for (int i = 0; i < content.length; i++) {
                                        if (i != posRemove) {
                                            builder.append(content[i]);
                                            builder.append(";");
                                        }
                                    }
                                    document.getReference()
                                            .update(QuanLyConstants.FOOD_NAME, builder.toString());
                                    adapter.notifyItemRemoved(position);
                                    listNotification.get(parentPos).getItems().remove(position-parentPos-1);
                                }
                                break;
                            }
                        }
                        if(listNotification.size()==0){
                            dialogChoose.dismiss();
                        }
                    }
                }
            });
    }

    private void removeTableFollow(final int parentPos) {
        db.collection(QuanLyConstants.NOTIFICATION)
            .document(GlobalVariable.employeeID)
            .collection(QuanLyConstants.TABLE)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()){
                        for(DocumentSnapshot document : task.getResult()){
                            Notification noti = listNotification.get(parentPos);
                            String titleDoc = document.get(QuanLyConstants.TABLE_NUMBER).toString();
                            if(noti.getTitle().equals(titleDoc)){
                                document.getReference().delete();
                                int childCountGroup = adapter.getGroups().get(parentPos).getItemCount();
                                while(adapter.getGroups().get(parentPos).getItemCount()!=0){
                                    adapter.getGroups().get(parentPos).getItems().remove(0);
                                }
                                listNotification.remove(parentPos);
                                adapter.notifyItemRangeRemoved(parentPos, childCountGroup+1);
                                break;
                            }
                        }
                        if(listNotification.size()==0){
                            dialogChoose.dismiss();
                        }
                    }
                }
            });
    }
}
