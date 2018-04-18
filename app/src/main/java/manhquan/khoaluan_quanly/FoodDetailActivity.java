package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import fragment.FoodFragment;
import util.GlideApp;

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
    private MaterialDialog dialogLoading;
    private String imageName;
    private boolean createDone = false;
    private boolean available = false;
    private FirebaseFirestore db;
    private String restaurantID;
    private ArrayList<String> listFoodType = new ArrayList<>();
    private MenuItem item;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        db = FirebaseFirestore.getInstance();
        restaurantID = getRestaurantID();
        ButterKnife.bind(this);
        GetListFoodType();

        mStorage = FirebaseStorage.getInstance().getReference();

        foodName = getIntent().getStringExtra(QuanLyConstants.INTENT_FOOD_DETAIL_NAME);
        if(!TextUtils.isEmpty(foodName)){
            GetFoodNeedUpdate();
            closeEdit();
        }

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
        showLoadingDialog();
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
                    showLoadingDialog();
                    updateButtonClick();
                }
                else{
                    showLoadingDialog();
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
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> food = new HashMap<>();
        food.put(QuanLyConstants.FOOD_NAME,txtName.getText().toString());
        food.put(QuanLyConstants.FOOD_PRICE,txtPrice.getText().toString());
        food.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
        food.put(QuanLyConstants.FOOD_DESCRIPTION,txtDescription.getText().toString());
        food.put(QuanLyConstants.FOOD_TYPE,txtType.getText().toString());
        food.put(QuanLyConstants.FOOD_IMAGE_NAME,imageName);
        db.collection(QuanLyConstants.FOOD).document(foodID)
                .set(food, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        closeLoadingDialog();
                        Toast.makeText(getApplicationContext(),"Update Success",Toast.LENGTH_SHORT).show();
                        createDone = true;
                    }
                });
        CheckIsNewFoodType();
    }

    /*
    * @author: ManhLD
    * Delete food when click on menu
    * */
    private void deleteFood(){
        showLoadingDialog();
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
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection(QuanLyConstants.FOOD).document(foodID)
                                .delete();
                        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
                        StorageReference imageRef = mStorage.child(QuanLyConstants.FOOD_PATH_IMAGE + imageName);
                        imageRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.employee_detail_delete_success),Toast.LENGTH_SHORT).show();
                                        closeLoadingDialog();
                                        createDone = true;
                                        onBackPressed();
                                    }
                                });
                    }
                })
                .canceledOnTouchOutside(false)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        menu.clear();
        getMenuInflater().inflate(R.menu.detail, menu);
        if(getPosition()==2){
            item = menu.add(0,QuanLyConstants.REPORT_OUT_OF_FOOD,0,getResources().getString(R.string.action_create_table));
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
            Map<String, Object> food = new HashMap<>();
            food.put(QuanLyConstants.FOOD_NAME,txtName.getText().toString());
            food.put(QuanLyConstants.FOOD_PRICE,txtPrice.getText().toString());
            food.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
            food.put(QuanLyConstants.FOOD_DESCRIPTION,txtDescription.getText().toString());
            food.put(QuanLyConstants.FOOD_TYPE,txtType.getText().toString().toLowerCase());
            food.put(QuanLyConstants.FOOD_IMAGE_NAME,imageName);
            food.put(QuanLyConstants.FOOD_AVAILABLE, true);
            db.collection(QuanLyConstants.FOOD)
                    .add(food)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Toast.makeText(getApplicationContext(), "Add Success", Toast.LENGTH_SHORT).show();
                            closeLoadingDialog();
                            createDone = true;
                            onBackPressed();
                        }
                    })
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
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, e.getMessage());
                        }
                    });
        }
        // -------------------------------------- End
    }

    /*
    * @author: ManhLD
    * @purpose: Validate form input
    * */
    private boolean validateForm() {
        boolean valid = true;

        String name = txtName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            txtName.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
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
        } else {
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

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(this)
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
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

        this.setResult(Activity.RESULT_OK, data);
        super.finish();
    }

}
