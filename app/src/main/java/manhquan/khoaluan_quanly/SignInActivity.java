package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import model.Restaurant;

public class SignInActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private EditText mEmailField;
    private EditText mPasswordField;

    private FirebaseAuth mAuth;
    private FirebaseFirestore mStore;
    private int position;
    private MaterialDialog dialogLoading;
    private Restaurant restaurant;

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        mEmailField.setText("lieumanh96@gmail.com");
        mPasswordField.setText("abcdef");
        updateUI(null);
    }
// [END on_start_check_user]

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mEmailField = findViewById(R.id.sign_in_editUsername);
        mPasswordField = findViewById(R.id.sign_in_editPassword);
        Button buttonSignIn = findViewById(R.id.sign_in_button_action);


        buttonSignIn.setOnClickListener(this);
        mAuth = FirebaseAuth.getInstance();
        mStore = FirebaseFirestore.getInstance();

    }

    private void signIn(final String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm()) {
            return;
        }
        

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(SignInActivity.this, getResources().getString(R.string.sign_in_faild_input),
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    public Restaurant getRestaurant(){
        restaurant = new Restaurant();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("restaurant")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        FirebaseAuth mAuth = FirebaseAuth.getInstance();
                        String managerUID = mAuth.getUid();
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                if(managerUID.equals(document.get("ManagerUID").toString())){
                                    restaurant.setId(document.getId());
                                    restaurant.setAddress(document.get("Address").toString());
                                    restaurant.setCity(document.get("City").toString());
                                    restaurant.setContact(document.get("Contact").toString());
                                    restaurant.setDistrict(document.get("District").toString());
                                    restaurant.setName(document.get("Name").toString());
                                    restaurant.setTimeOpenClose(document.get("TimeOpenClose").toString());
                                }
                            }
                        }
                    }
                });
        return restaurant;
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Required.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Required.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        return valid;
    }

    private void updateUI(FirebaseUser user) {
        final FirebaseUser finalUser = user;
        if (user != null) {
            mStore.collection("employee")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if(task.isSuccessful()){
                                for(DocumentSnapshot document : task.getResult()){
                                    if(document.get("Username").toString().equals(finalUser.getEmail())){
                                        position = Integer.parseInt(document.get("Position").toString());
                                        Intent i = new Intent(SignInActivity.this,MainActivity.class);
                                        i.putExtra("position",position);
                                        saveRestaurantID(document.get("RestaurantID").toString());
                                        closeLoadingDialog();
                                        startActivity(i);
                                    }
                                }
                            }
                            else{
                                Log.i(TAG, task.getException().getMessage());
                                Toast.makeText(getApplicationContext(),"Task Failed",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            Toast.makeText(getApplicationContext(),"....",Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRestaurantID(String restaurantID){
        String restaurantPref = "restaurantID";
        SharedPreferences sharedPreferences = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(restaurantPref, restaurantID);
        editor.apply();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.sign_in_button_action){
            if(validateForm()){
                showLoadingDialog();
                signIn(mEmailField.getText().toString().trim(),mPasswordField.getText().toString().trim());
            }
        }
    }

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(this)
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }
}
