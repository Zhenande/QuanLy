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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import model.Restaurant;

public class AddAccountActivity extends AppCompatActivity {

    private static final String TAG = "AddAccountActivity";
    private Spinner spinnerPosition;
    private EditText txtName;
    private EditText txtUsername;
    private EditText txtPassword;
    private EditText txtContactNumber;
    private MaterialDialog dialogLoading;
    private boolean createOK = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_account);

        txtName = findViewById(R.id.add_account_editEmployeeName);
        txtUsername = findViewById(R.id.add_account_editEmployeeUsername);
        txtPassword = findViewById(R.id.add_account_editEmployeePassword);
        txtContactNumber = findViewById(R.id.add_account_editEmployeeContactNumber);
        Button buttonCreate = findViewById(R.id.add_account_button_create);

        spinnerPosition = findViewById(R.id.add_account_spinnerEmployeePosition);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.add_account_spinner_Position
                ,R.layout.spinner_item_text);

        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_text);
        spinnerPosition.setAdapter(spinnerAdapter);

        buttonCreate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(validateForm()){
                    showLoadingDialog();
                    createAuthForEmployee();
                }
            }
        });

    }


    // Create account for new employee
    private void createAuthForEmployee() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String currentEmail = mAuth.getCurrentUser().getEmail();
        String email = txtUsername.getText().toString().trim();
        String password = txtPassword.getText().toString().trim();
        mAuth.createUserWithEmailAndPassword(email, password);
        mAuth.signOut();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            createEmployee(getRestaurantID());
                            getBackToCurrentUser(currentEmail);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(AddAccountActivity.this, getResources().getString(R.string.sign_in_faild_input),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    // Sign in again for the manager
    private void getBackToCurrentUser(final String currentEmail) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("employee")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                if(document.get("Username").toString().equals(currentEmail)){
                                    String managerUsername = document.get("Username").toString();
                                    String managerPassword = document.get("Password").toString();
                                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    mAuth.signInWithEmailAndPassword(managerUsername, managerPassword)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    Toast.makeText(AddAccountActivity.this, "Create Done",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    closeLoadingDialog();
                                    createOK = true;
                                    onBackPressed();
                            }
                        }
                    }
                }});
    }

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(this)
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }

    // Create employee information
    private void createEmployee(String restaurantID){
        switch (spinnerPosition.getSelectedItemPosition()){
            case 0 : createCook(restaurantID);
                    break;
            case 1 : createWaiter(restaurantID);
                    break;
            case 2 : createCashier(restaurantID);
                    break;
        }
    }

    // Create cook
    private void createCook(String restaurantID){
        Map<String, Object > cook = new HashMap<>();
        cook.put("RestaurantID",restaurantID);
        cook.put("Name",txtName.getText().toString().trim());
        cook.put("Username",txtUsername.getText().toString().trim());
        cook.put("Password",txtPassword.getText().toString().trim());
        cook.put("ContactNumber",txtContactNumber.getText().toString().trim());
        cook.put("Position",2);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("employee")
                .add(cook)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Add Success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Add Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createWaiter(String restaurantID){
        Map<String, Object > waiter = new HashMap<>();
        waiter.put("RestaurantID",restaurantID);
        waiter.put("Name",txtName.getText().toString().trim());
        waiter.put("Username",txtUsername.getText().toString().trim());
        waiter.put("Password",txtPassword.getText().toString().trim());
        waiter.put("ContactNumber",txtContactNumber.getText().toString().trim());
        waiter.put("Position",3);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("employee")
                .add(waiter)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Add Success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Add Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void createCashier(String restaurantID){
        Map<String, Object > cashier = new HashMap<>();
        cashier.put("RestaurantID",restaurantID);
        cashier.put("Name",txtName.getText().toString().trim());
        cashier.put("Username",txtUsername.getText().toString().trim());
        cashier.put("Password",txtPassword.getText().toString().trim());
        cashier.put("ContactNumber",txtContactNumber.getText().toString().trim());
        cashier.put("Position",4);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("employee")
                .add(cashier)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(getApplicationContext(), "Add Success", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "Add Failed", Toast.LENGTH_SHORT).show();
                    }
                });
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

        String username = txtUsername.getText().toString();
        if (TextUtils.isEmpty(username)) {
            txtUsername.setError("Required.");
            valid = false;
        } else {
            txtUsername.setError(null);
        }

        String password = txtPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            txtPassword.setError("Required.");
            valid = false;
        } else {
            txtPassword.setError(null);
        }

        String contactNumber = txtContactNumber.getText().toString();
        if (TextUtils.isEmpty(contactNumber)) {
            txtContactNumber.setError("Required.");
            valid = false;
        } else {
            txtContactNumber.setError(null);
        }

        return valid;
    }


    @Override
    public void finish(){
        Intent dataBack = new Intent();
        dataBack.putExtra("flag",createOK);
        this.setResult(Activity.RESULT_OK,dataBack);
        super.finish();
    }

    public String getRestaurantID(){
        String langPref = "restaurantID";
        SharedPreferences prefs = getSharedPreferences("CommonPrefs", Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }
}
