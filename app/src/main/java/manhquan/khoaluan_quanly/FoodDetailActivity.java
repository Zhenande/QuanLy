package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.DecimalFormat;
import java.text.Normalizer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import util.GlideApp;

import static util.GlobalVariable.closeLoadingDialog;
import static util.GlobalVariable.showLoadingDialog;

public class FoodDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private String foodName;
    @BindView(R.id.food_detail_name)
    public EditText txtName;
    @BindView(R.id.food_detail_price)
    public EditText txtPrice;
    @BindView(R.id.food_detail_description)
    public EditText txtDescription;
    @BindView(R.id.food_detail_food_type)
    public AutoCompleteTextView txtType;
    @BindView(R.id.food_detail_button_add)
    public Button buttonCreate;
    @BindView(R.id.food_detail_gallary_pick)
    public Button buttonGallary;
    @BindView(R.id.food_detail_camera)
    public Button buttonCamera;
    @BindView(R.id.food_detail_image)
    public ImageView imageFood;
    private Uri uri;
    private StorageReference mStorage;
    private Image image;
    private String TAG = "FoodDetailActivity";
    private String foodID;
    private String imageName;
    private boolean createDone = false;
    private boolean available = false;
    private FirebaseFirestore db;
    private String restaurantID;
    private ArrayList<String> listFoodType = new ArrayList<>();
    private MenuItem item;
    // Use when update food type of the food and the food type that just have 1 food
    private String oldFoodType = "";
    private String current = "";
    private NumberFormat numberFormat = new DecimalFormat("###,###");
    private boolean flagNewFoodType = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        db = FirebaseFirestore.getInstance();

        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);

        restaurantID = getRestaurantID();
        ButterKnife.bind(this);
        GetListFoodType();

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mStorage = FirebaseStorage.getInstance().getReference();

        foodName = getIntent().getStringExtra(QuanLyConstants.INTENT_FOOD_DETAIL_NAME);
        if(!TextUtils.isEmpty(foodName)){
            GetFoodNeedUpdate();
            closeEdit();
        }

        txtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current)){
                    txtPrice.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[$,.]", "");
                    if(!TextUtils.isEmpty(cleanString)) {
                        double parsed = Double.parseDouble(cleanString);
                        String formatted = numberFormat.format(parsed);

                        current = formatted;
                        txtPrice.setText(formatted);
                        txtPrice.setSelection(formatted.length());

                    }
                    txtPrice.addTextChangedListener(this);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        buttonCamera.setOnClickListener(this);
        buttonGallary.setOnClickListener(this);
        buttonCreate.setOnClickListener(this);
    }



    private void GetListFoodType() {
        db.collection(QuanLyConstants.RESTAURANT)
                .document(restaurantID)
                .collection(QuanLyConstants.RESTAURANT_FOOD_TYPE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                listFoodType.add(document.get(QuanLyConstants.FOOD_TYPE_NAME).toString());
                            }
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(FoodDetailActivity.this,
                                    android.R.layout.simple_dropdown_item_1line,listFoodType);
                            txtType.setAdapter(adapter);
                        }
                    }
                });
    }

    /*
    * @author: ManhLD
    * @purpose: Render the food selected by the user
    * */
    private void GetFoodNeedUpdate() {
        showLoadingDialog(this);
        db.collection(QuanLyConstants.FOOD)
                .whereEqualTo(QuanLyConstants.RESTAURANT_ID,restaurantID)
                .whereEqualTo(QuanLyConstants.FOOD_NAME,foodName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                String imageResource = document.get(QuanLyConstants.FOOD_IMAGE_NAME).toString();
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                StorageReference imageRef = storageReference.child(QuanLyConstants.FOOD_PATH_IMAGE + imageResource);
                                GlideApp.with(getApplicationContext())
                                        .load(imageRef)
                                        .into(imageFood);
                                txtName.setText(document.get(QuanLyConstants.FOOD_NAME).toString());
                                txtPrice.setText(document.get(QuanLyConstants.FOOD_PRICE).toString());
                                txtDescription.setText(document.get(QuanLyConstants.FOOD_DESCRIPTION).toString());
                                txtType.setText(document.get(QuanLyConstants.FOOD_TYPE).toString());
                                oldFoodType = txtType.getText().toString();
                                foodID = document.getId();
                                available = (boolean)document.get(QuanLyConstants.FOOD_AVAILABLE);
                                imageName = document.get(QuanLyConstants.FOOD_IMAGE_NAME).toString();
                            }
                            if(getPosition()==2){
                                if(available){
                                    // Food is ran out. set Food To False
                                    item.setTitle(getResources().getString(R.string.food_detail_report_out_of_food));
                                }
                                else{
                                    item.setTitle(getResources().getString(R.string.food_detail_report_enable_food));
                                }
                            }
                            closeLoadingDialog();
                        }
                    }
                });
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        if(viewID == R.id.food_detail_button_add){
            if(validateForm()){
                if(buttonCreate.getText().toString().equals(getResources().getString(R.string.detail_menu_update))){
                    showLoadingDialog(this);
                    updateButtonClick();
                }
                else{
                    showLoadingDialog(this);
                    createButtonClick();
                }
            }
        }
        else if(viewID == R.id.food_detail_gallary_pick){
            ImagePicker.create(this)
                    .folderMode(true)
                    .single()
                    .start();

        }
        else if(viewID == R.id.food_detail_camera){
            ImagePicker.cameraOnly().start(this);
        }
    }

    private void updateButtonClick() {
        db = FirebaseFirestore.getInstance();
        Map<String, Object> food = new HashMap<>();
        food.put(QuanLyConstants.FOOD_NAME,txtName.getText().toString());
        food.put(QuanLyConstants.FOOD_PRICE,txtPrice.getText().toString().replaceAll(",",""));
        food.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
        food.put(QuanLyConstants.FOOD_DESCRIPTION,txtDescription.getText().toString());
        food.put(QuanLyConstants.FOOD_TYPE,txtType.getText().toString());
        food.put(QuanLyConstants.FOOD_IMAGE_NAME,imageName);
        db.collection(QuanLyConstants.FOOD).document(foodID)
                .set(food, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        db.collection(QuanLyConstants.FOOD)
                                .whereEqualTo(QuanLyConstants.RESTAURANT_ID, restaurantID)
                                .whereEqualTo(QuanLyConstants.FOOD_TYPE, oldFoodType)
                                .get()
                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                        if(task.isSuccessful()){
                                            if(task.getResult().isEmpty()){
                                                removeFoodType(oldFoodType);
                                            }
                                            else{
                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.string_done),Toast.LENGTH_SHORT).show();
                                                closeLoadingDialog();
                                                createDone = true;
                                                onBackPressed();
                                            }
                                        }
                                    }
                                });
//                        closeLoadingDialog();
//                        Toast.makeText(getApplicationContext(),"Update Success",Toast.LENGTH_SHORT).show();
//                        createDone = true;
                    }
                });
        CheckIsNewFoodType();
    }

    /*
    * @author: ManhLD
    * Delete food when click on menu
    * */
    private void deleteFood(){
        new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.detail_menu_delete))
                .content(getResources().getString(R.string.food_detail_content_delete))
                .positiveText(getResources().getString(R.string.main_agree))
                .negativeText(getResources().getString(R.string.main_disagree))
                .positiveColor(getResources().getColor(R.color.primary_dark))
                .negativeColor(getResources().getColor(R.color.black))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        showLoadingDialog(FoodDetailActivity.this);
                        db.collection(QuanLyConstants.FOOD).document(foodID)
                                .delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        final String foodType = txtType.getText().toString().toLowerCase();
                                        db.collection(QuanLyConstants.FOOD)
                                                .whereEqualTo(QuanLyConstants.RESTAURANT_ID, restaurantID)
                                                .whereEqualTo(QuanLyConstants.FOOD_TYPE, foodType)
                                                .get()
                                                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                        if(task.isSuccessful()){
                                                            if(task.getResult().isEmpty()){
                                                                removeFoodType(foodType);
                                                            }
                                                            else{
                                                                Toast.makeText(getApplicationContext(),getResources().getString(R.string.string_done),Toast.LENGTH_SHORT).show();
                                                                closeLoadingDialog();
                                                                createDone = true;
                                                                onBackPressed();
                                                            }
                                                        }
                                                    }
                                                });
                                    }
                                });
                        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
                        StorageReference imageRef = mStorage.child(QuanLyConstants.FOOD_PATH_IMAGE + imageName);
                        imageRef.delete();
                    }
                })
                .canceledOnTouchOutside(false)
                .show();
    }

    private void removeFoodType(String foodType) {
        db.collection(QuanLyConstants.RESTAURANT)
                .document(restaurantID)
                .collection(QuanLyConstants.RESTAURANT_FOOD_TYPE)
                .whereEqualTo(QuanLyConstants.FOOD_NAME, foodType)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                document.getReference().delete();
                            }
                            flagNewFoodType = true;
                            Toast.makeText(getApplicationContext(),getResources().getString(R.string.string_done),Toast.LENGTH_SHORT).show();
                            closeLoadingDialog();
                            createDone = true;
                            onBackPressed();
                        }
                    }
                });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        if(getPosition()==2){
            item = menu.add(0,QuanLyConstants.REPORT_OUT_OF_FOOD,0,getResources().getString(R.string.action_create_table));
        }
        else{
            getMenuInflater().inflate(R.menu.detail, menu);
        }
        return true;
    }

    public int getPosition(){
        String langPref = QuanLyConstants.SHARED_POSITION;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getInt(langPref,0);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_update) {
            openEdit();
            return true;
        }
        if(id == R.id.action_delete){
            deleteFood();
            return true;
        }
        if(id == QuanLyConstants.REPORT_OUT_OF_FOOD){
            setOutOfFood();
            return true;
        }
        if(id == android.R.id.home){
            this.onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void setOutOfFood() {
        DocumentReference docRef = db.collection(QuanLyConstants.FOOD).document(foodID);
        if(available){
            docRef.update(QuanLyConstants.FOOD_AVAILABLE,false);
        }
        else{
            docRef.update(QuanLyConstants.FOOD_AVAILABLE,true);
        }

    }

    private void createButtonClick() {
        if(uri != null){
            // Upload image of food to storage -- Start
            String newPath = changeFileName(image.getPath());
            uri = Uri.fromFile(new File(newPath));
            StorageReference imageRef = mStorage.child(QuanLyConstants.FOOD_PATH_IMAGE + uri.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(uri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    //Uri downloadURL = taskSnapshot.getDownloadUrl();
                }
            });
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getApplicationContext(),"Upload image fail",Toast.LENGTH_SHORT).show();
                }
            });

            // -------------------------------- End of upload image of food

            // Create a document food on FireStore -- Start

            String[] getImageName = newPath.split("/");
            String imageName = getImageName[getImageName.length-1];
            String cleanPrice = txtPrice.getText().toString().replaceAll(",","");
            Map<String, Object> food = new HashMap<>();
            food.put(QuanLyConstants.FOOD_NAME,txtName.getText().toString());
            food.put(QuanLyConstants.FOOD_PRICE,cleanPrice);
            food.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
            food.put(QuanLyConstants.FOOD_DESCRIPTION,txtDescription.getText().toString());
            food.put(QuanLyConstants.FOOD_TYPE,txtType.getText().toString().toLowerCase());
            food.put(QuanLyConstants.FOOD_IMAGE_NAME,imageName);
            food.put(QuanLyConstants.FOOD_AVAILABLE, true);
            db.collection(QuanLyConstants.FOOD)
                    .add(food)
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, e.getMessage());
                            Toast.makeText(getApplicationContext(), "Add Fail", Toast.LENGTH_SHORT).show();
                        }
                    });
            // ------------------------------------- End of create a document food
            CheckIsNewFoodType();
        }
    }

    private void CheckIsNewFoodType() {
        // If Restaurant add new food have new food type, we will insert new food type -- Start
        if(!listFoodType.contains(txtType.getText().toString().toLowerCase())){
            Map<String, Object> foodType = new HashMap<>();
            foodType.put(QuanLyConstants.FOOD_TYPE_NAME, txtType.getText().toString().toLowerCase());
            db.collection(QuanLyConstants.RESTAURANT)
                    .document(restaurantID)
                    .collection(QuanLyConstants.RESTAURANT_FOOD_TYPE)
                    .add(foodType)
                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            Toast.makeText(getApplicationContext(), "Add Success", Toast.LENGTH_SHORT).show();
                            closeLoadingDialog();
                            createDone = true;
                            flagNewFoodType = true;
                            onBackPressed();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    });
        }
        else{
            Toast.makeText(getApplicationContext(), "Add Success", Toast.LENGTH_SHORT).show();
            closeLoadingDialog();
            createDone = true;
            onBackPressed();
        }
        // -------------------------------------- End
    }

    /*
    * @author: ManhLD
    * @purpose: Validate form input
    * */
    private boolean validateForm() {
        boolean valid = true;
        //String patternFoodName = "\\w{1,100}";

        String name = txtName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            txtName.setError(getResources().getString(R.string.required));
            valid = false;
        }
//        else if(!name.matches(patternFoodName)){
//            txtName.setError(getResources().getString(R.string.food_detail_name_too_long));
//            valid = false;
//        }
        else {
            txtName.setError(null);
        }

        String price = txtPrice.getText().toString();
        if (TextUtils.isEmpty(price)) {
            txtPrice.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            txtPrice.setError(null);
        }

        String description = txtDescription.getText().toString();
        if (TextUtils.isEmpty(description)) {
            txtDescription.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            txtDescription.setError(null);
        }

        String type = txtType.getText().toString();
        if (TextUtils.isEmpty(type)) {
            txtType.setError(getResources().getString(R.string.required));
            valid = false;
        }
//        else if(!name.matches(patternFoodName)){
//            txtType.setError(getResources().getString(R.string.food_detail_type_too_long));
//            valid = false;
//        }
        else {
            txtType.setError(null);
        }
        if(imageFood.getDrawable() == null){
            imageFood.setFocusable(true);
            valid = false;
        }
        return valid;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(ImagePicker.shouldHandle(requestCode,resultCode,data)){
            image = ImagePicker.getFirstImageOrNull(data);
            uri = Uri.fromFile(new File(image.getPath()));
            if(uri!= null){
                imageFood.setImageURI(uri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

    public String changeFileName(String inputFilePath){
        StringBuilder result = new StringBuilder();
        // Remove all the special character, include letter of vietnamese
        String foodName = Normalizer.normalize(txtName.getText().toString(), Normalizer.Form.NFD).replaceAll("[^a-zA-Z]", "");
        String[] oldPath = inputFilePath.split("/");
        for(int i = 0; i < oldPath.length; i++){
            if(i+1 == oldPath.length){
                String[] getImageType = oldPath[i].split("\\.");
                result.append(restaurantID)
                        .append("_").append(foodName)
                        .append(".").append(getImageType[getImageType.length - 1]);
            }
            else{
                result.append(oldPath[i]).append("/");
            }
        }
        File oldFile = new File(image.getPath());
        File newFile = new File(result.toString());
        if(oldFile.renameTo(newFile)){
            Log.i(TAG,"Rename Success");
        }
        return result.toString();
    }

    public void closeEdit(){
        txtType.setEnabled(false);
        txtDescription.setEnabled(false);
        txtPrice.setEnabled(false);
        txtName.setEnabled(false);
        buttonCreate.setVisibility(View.INVISIBLE);
        buttonGallary.setVisibility(View.INVISIBLE);
        buttonCamera.setVisibility(View.INVISIBLE);
    }

    public void openEdit(){
        txtType.setEnabled(true);
        txtDescription.setEnabled(true);
        txtPrice.setEnabled(true);
        txtName.setEnabled(true);
        buttonCreate.setText(getResources().getString(R.string.detail_menu_update));
        buttonCreate.setVisibility(View.VISIBLE);
    }

    @Override
    public void finish() {
        Intent data = new Intent();
        data.putExtra(QuanLyConstants.INTENT_FOOD_DETAIL_FLAG,createDone);
        data.putExtra(QuanLyConstants.NEW_FOOD_TYPE, flagNewFoodType);

        this.setResult(Activity.RESULT_OK, data);
        super.finish();
    }

}