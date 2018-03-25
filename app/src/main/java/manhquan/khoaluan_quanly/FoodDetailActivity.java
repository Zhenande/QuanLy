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
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.esafirm.imagepicker.features.ImagePicker;
import com.esafirm.imagepicker.model.Image;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.Normalizer;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import constants.QuanLyConstants;

public class FoodDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private String foodName;
    private boolean flag = false; // meaning on create
    public EditText txtName;
    public EditText txtPrice;
    public EditText txtDescription;
    public EditText txtType;
    public Button buttonCreate;
    public Button buttonGallary;
    public Button buttonCamera;
    public ImageView imageFood;
    private Uri uri;
    private StorageReference mStorage;
    private Image image;
    private String TAG = "FoodDetailActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        txtName = findViewById(R.id.food_detail_name);
        txtPrice = findViewById(R.id.food_detail_price);
        txtDescription = findViewById(R.id.food_detail_description);
        txtType = findViewById(R.id.food_detail_food_type);
        imageFood = findViewById(R.id.food_detail_image);
        buttonCreate = findViewById(R.id.food_detail_button_add);
        buttonGallary = findViewById(R.id.food_detail_gallary_pick);
        buttonCamera = findViewById(R.id.food_detail_camera);

        mStorage = FirebaseStorage.getInstance().getReference();

        foodName = getIntent().getStringExtra(QuanLyConstants.INTENT_FOOD_DETAIL_NAME);
        if(!TextUtils.isEmpty(foodName)){
            flag = true; // meaning we are on updated
        }

        buttonCamera.setOnClickListener(this);
        buttonGallary.setOnClickListener(this);
        buttonCreate.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int viewID = v.getId();
        if(viewID == R.id.food_detail_button_add){
            if(validateForm()){
                createButtonClick();
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

}
