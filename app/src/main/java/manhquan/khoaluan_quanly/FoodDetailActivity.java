package manhquan.khoaluan_quanly;

import android.app.Activity;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;
import fcm.MyFirebaseMessagingService;
import model.Food;
import util.GlideApp;

public class FoodDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private String foodName;
    private boolean flag = false; // meaning on create
    @BindView(R.id.food_detail_name)
    public EditText txtName;
    @BindView(R.id.food_detail_price)
    public EditText txtPrice;
    @BindView(R.id.food_detail_description)
    public EditText txtDescription;
    @BindView(R.id.food_detail_food_type)
    public EditText txtType;
    @BindView(R.id.food_detail_button_add)
    public Button buttonCreate;
    @BindView(R.id.food_detail_gallary_pick)
    public Button buttonGallary;
    @BindView(R.id.food_detail_camera)
    public Button buttonCamera;
    public ImageView imageFood;
    private Uri uri;
    private StorageReference mStorage;
    private Image image;
    private String TAG = "FoodDetailActivity";
    private String foodID;
    private MaterialDialog dialogLoading;
    private String imageName;
    private boolean createDone = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ButterKnife.bind(this);

        mStorage = FirebaseStorage.getInstance().getReference();

        foodName = getIntent().getStringExtra(QuanLyConstants.INTENT_FOOD_DETAIL_NAME);
        if(!TextUtils.isEmpty(foodName)){
            flag = true; // meaning we are on updated
            showLoadingDialog();
            GetFoodNeedUpdate();
            closeEdit();
        }

        buttonCamera.setOnClickListener(this);
        buttonGallary.setOnClickListener(this);
        buttonCreate.setOnClickListener(this);
    }

    private void GetFoodNeedUpdate() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("food")
                .whereEqualTo("RestaurantID",getRestaurantID())
                .whereEqualTo("Name",foodName)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                String imageResource = document.get("ImageName").toString();
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                                StorageReference imageRef = storageReference.child("images/" + imageResource);
                                GlideApp.with(getApplicationContext())
                                        .load(imageRef)
                                        .into(imageFood);
                                txtName.setText(document.get("Name").toString());
                                txtPrice.setText(document.get("Price").toString());
                                txtDescription.setText(document.get("Description").toString());
                                txtType.setText(document.get("Type").toString());
                                foodID = document.getId();
                                imageName = document.get("ImageName").toString();
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
                if(buttonCreate.getText().toString().equals("Update")){
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
        food.put("Name",txtName.getText().toString());
        food.put("Price",txtPrice.getText().toString());
        food.put("RestaurantID",getRestaurantID());
        food.put("Description",txtDescription.getText().toString());
        food.put("Type",txtType.getText().toString());
        food.put("ImageName",imageName);
        db.collection("food").document(foodID)
                .set(food, SetOptions.merge())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        closeLoadingDialog();
                        Toast.makeText(getApplicationContext(),"Update Success",Toast.LENGTH_SHORT).show();
                        createDone = true;
                        onBackPressed();
                    }
                });
    }

    private void deleteFood(){
        new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.detail_menu_delete))
                .content(getResources().getString(R.string.employee_detail_content_delete))
                .positiveText(getResources().getString(R.string.main_agree))
                .negativeText(getResources().getString(R.string.main_disagree))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        db.collection("food").document(foodID)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.employee_detail_delete_success)
                                                ,Toast.LENGTH_SHORT).show();
                                    }
                                });
                        StorageReference mStorage = FirebaseStorage.getInstance().getReference();
                        StorageReference imageRef = mStorage.child("images/" + imageName);
                        imageRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),"Delete Success",Toast.LENGTH_SHORT).show();
                                        closeLoadingDialog();
                                        createDone = true;
                                        onBackPressed();
                                    }
                                });
                    }
                })
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.detail, menu);
        return true;
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
            showLoadingDialog();
            deleteFood();
            return true;
        }
        if(id == R.id.action_test){
            MyFirebaseMessagingService messaging = new MyFirebaseMessagingService();
            messaging.sendNotification("I don't know what to write - Test 1");
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createButtonClick() {
        if(uri != null){
            // Upload image of food to storage -- Start
            String newPath = changeFileName(image.getPath());
            uri = Uri.fromFile(new File(newPath));
            StorageReference imageRef = mStorage.child("images/" + uri.getLastPathSegment());
            UploadTask uploadTask = imageRef.putFile(uri);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadURL = taskSnapshot.getDownloadUrl();
                    Log.i("DownloadURL", downloadURL.getPath());
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
            food.put("Name",txtName.getText().toString());
            food.put("Price",txtPrice.getText().toString());
            food.put("RestaurantID",getRestaurantID());
            food.put("Description",txtDescription.getText().toString());
            food.put("Type",txtType.getText().toString());
            food.put("ImageName",imageName);
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            db.collection("food")
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
        }
    }

    // Check the input form
    private boolean validateForm() {
        boolean valid = true;

        String name = txtName.getText().toString();
        if (TextUtils.isEmpty(name)) {
            txtName.setError("Required.");
            valid = false;
        } else {
            txtName.setError(null);
        }

        String price = txtPrice.getText().toString();
        if (TextUtils.isEmpty(price)) {
            txtPrice.setError("Required.");
            valid = false;
        } else {
            txtPrice.setError(null);
        }

        String description = txtDescription.getText().toString();
        if (TextUtils.isEmpty(description)) {
            txtDescription.setError("Required.");
            valid = false;
        } else {
            txtDescription.setError(null);
        }

        String type = txtType.getText().toString();
        if (TextUtils.isEmpty(type)) {
            txtType.setError("Required.");
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
            imageFood.setImageURI(uri);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public String getRestaurantID(){
        String langPref = "restaurantID";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

    public String changeFileName(String inputFilePath){
        StringBuilder result = new StringBuilder();
        String foodName = Normalizer.normalize(txtName.getText().toString(), Normalizer.Form.NFD).replaceAll("[^a-zA-Z]", "");
        String[] oldPath = inputFilePath.split("/");
        for(int i = 0; i < oldPath.length; i++){
            if(i+1 == oldPath.length){
                String[] getImageType = oldPath[i].split("\\.");
                result.append(getRestaurantID() + "_" + foodName + "." +getImageType[getImageType.length-1]);
            }
            else{
                result.append(oldPath[i]+"/");
            }
        }
        File oldFile = new File(image.getPath());
        File newFile = new File(result.toString());
        if(oldFile.renameTo(newFile)){
            Toast.makeText(getApplicationContext(),"Rename Done",Toast.LENGTH_SHORT).show();
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
        buttonCreate.setText("Update");
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
