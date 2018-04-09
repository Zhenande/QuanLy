package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
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
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;

public class EmployeeDetailActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "EmployeeDetailActivity";
    @BindView(R.id.employee_detail_spinnerEmployeePosition)
    public Spinner spinnerPosition;
    @BindView(R.id.employee_detail_editEmployeeName)
    public EditText txtName;
    @BindView(R.id.employee_detail_editEmployeeUsername)
    public EditText txtUsername;
    @BindView(R.id.employee_detail_editEmployeePassword)
    public EditText txtPassword;
    @BindView(R.id.employee_detail_editEmployeeContactNumber)
    public EditText txtContactNumber;
    private MaterialDialog dialogLoading;
    @BindView(R.id.employee_detail_button_create)
    public Button buttonUpdate;
    private String RestaurantID;
    private boolean flag = false;
    private String documentID;
    private FirebaseFirestore db;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_detail);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ButterKnife.bind(this);
        db = FirebaseFirestore.getInstance();

        spinnerPosition = findViewById(R.id.employee_detail_spinnerEmployeePosition);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.add_account_spinner_Position
                ,R.layout.spinner_item_text);

        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_text);
        spinnerPosition.setAdapter(spinnerAdapter);

        RestaurantID = getRestaurantID();

        documentID = getIntent().getStringExtra(QuanLyConstants.INTENT_DOCUMENT_ID);
        if(TextUtils.isEmpty(documentID)){
            // Create New Employee
            buttonUpdate.setText(getResources().getString(R.string.button_create));
        }
        else {
            // Update Employee
            showLoadingDialog();
            buttonUpdate.setText(getResources().getString(R.string.employee_detail_button_save));
            renderUI(documentID);
        }
        buttonUpdate.setOnClickListener(this);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if(!TextUtils.isEmpty(documentID)){
            getMenuInflater().inflate(R.menu.detail, menu);
        }
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
            openEditText();
            return true;
        }
        if(id == R.id.action_delete){
            showLoadingDialog();
            deleteEmployee();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void deleteEmployee() {
        new MaterialDialog.Builder(this)
                .title(getResources().getString(R.string.detail_menu_delete))
                .content(getResources().getString(R.string.employee_detail_content_delete))
                .positiveText(getResources().getString(R.string.main_agree))
                .negativeText(getResources().getString(R.string.main_disagree))
                .positiveColor(getResources().getColor(R.color.primary))
                .negativeColor(getResources().getColor(R.color.black))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        db.collection(QuanLyConstants.EMPLOYEE).document(documentID)
                                .delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(),getResources().getString(R.string.employee_detail_delete_success)
                                                ,Toast.LENGTH_SHORT).show();
                                        closeLoadingDialog();
                                        onBackPressed();
                                    }
                                });
                    }
                })
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        closeLoadingDialog();
                    }
                })
                .canceledOnTouchOutside(false)
                .show();
    }

    public void renderUI(final String documentID){
        db.collection(QuanLyConstants.EMPLOYEE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                if(document.getId().equals(documentID)){
                                    txtName.setText(document.get(QuanLyConstants.EMPLOYEE_NAME).toString());
                                    txtUsername.setText(document.get(QuanLyConstants.EMPLOYEE_USERNAME).toString());
                                    txtPassword.setText(document.get(QuanLyConstants.EMPLOYEE_PASSWORD).toString());
                                    txtContactNumber.setText(document.get(QuanLyConstants.EMPLOYEE_CONTACT).toString());
                                    spinnerPosition.setSelection(Integer.parseInt(document.get(QuanLyConstants.EMPLOYEE_POSITION).toString())-2);
                                    closeEditText();
                                    closeLoadingDialog();
                                    flag = true;
                                }
                            }
                        }
                    }
                });
    }

    public void showLoadingDialog(){
        dialogLoading = new MaterialDialog.Builder(this)
                .customView(R.layout.loading_dialog,true)
                .show();
    }

    public void closeLoadingDialog(){
        dialogLoading.dismiss();
    }

    /*
    * @author: ManhLD
    * @purpose: Open Edittext for the user update new information of the employee
    * */
    private void openEditText(){
        txtName.setEnabled(true);
        txtUsername.setEnabled(true);
        txtPassword.setEnabled(true);
        txtContactNumber.setEnabled(true);
        spinnerPosition.setEnabled(true);
        buttonUpdate.setVisibility(View.VISIBLE);
    }

    /*
    * @author: ManhLD
    * @purpose: Close Edittext
    * */
    private void closeEditText(){
        txtName.setEnabled(false);
        txtUsername.setEnabled(false);
        txtPassword.setEnabled(false);
        txtContactNumber.setEnabled(false);
        spinnerPosition.setEnabled(false);
        buttonUpdate.setVisibility(View.INVISIBLE);
    }

    /*
     * @author: ManhLD
     * @purpose: Create Authenticate for new employee
     * */
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
                            createEmployee(RestaurantID);
                            getBackToCurrentUser(currentEmail);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(EmployeeDetailActivity.this, getResources().getString(R.string.sign_in_faild_input),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /*
     * @author: ManhLD
     * @purpose: When create an Authenticate for new employee, the manager has sign out
     *           So this method will sign in again for the manager.
     * */
    private void getBackToCurrentUser(final String currentEmail) {
        db.collection(QuanLyConstants.EMPLOYEE)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            for(DocumentSnapshot document : task.getResult()){
                                if(document.get(QuanLyConstants.EMPLOYEE_USERNAME).toString().equals(currentEmail)){
                                    String managerUsername = document.get(QuanLyConstants.EMPLOYEE_USERNAME).toString();
                                    String managerPassword = document.get(QuanLyConstants.EMPLOYEE_PASSWORD).toString();
                                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    mAuth.signInWithEmailAndPassword(managerUsername, managerPassword)
                                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    Toast.makeText(EmployeeDetailActivity.this, "Create Done",
                                                            Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                    closeLoadingDialog();
                                    flag = true;
                                    onBackPressed();
                                }
                            }
                        }
                    }});
    }

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

    /*
     * @author: ManhLD
     * @purpose: Create cook employee.
     * */
    private void createCook(String restaurantID){
        Map<String, Object > cook = new HashMap<>();
        cook.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
        cook.put(QuanLyConstants.RESTAURANT_NAME,txtName.getText().toString().trim());
        cook.put(QuanLyConstants.EMPLOYEE_USERNAME,txtUsername.getText().toString().trim());
        cook.put(QuanLyConstants.EMPLOYEE_PASSWORD,txtPassword.getText().toString().trim());
        cook.put(QuanLyConstants.EMPLOYEE_CONTACT,txtContactNumber.getText().toString().trim());
        cook.put(QuanLyConstants.EMPLOYEE_POSITION,2);
        db.collection(QuanLyConstants.EMPLOYEE)
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

    /*
     * @author: ManhLD
     * @purpose: Create waiter employee.
     * */
    private void createWaiter(String restaurantID){
        Map<String, Object > waiter = new HashMap<>();
        waiter.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
        waiter.put(QuanLyConstants.RESTAURANT_NAME,txtName.getText().toString().trim());
        waiter.put(QuanLyConstants.EMPLOYEE_USERNAME,txtUsername.getText().toString().trim());
        waiter.put(QuanLyConstants.EMPLOYEE_PASSWORD,txtPassword.getText().toString().trim());
        waiter.put(QuanLyConstants.EMPLOYEE_CONTACT,txtContactNumber.getText().toString().trim());
        waiter.put(QuanLyConstants.EMPLOYEE_POSITION,3);
        db.collection(QuanLyConstants.EMPLOYEE)
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

    /*
     * @author: ManhLD
     * @purpose: Create cashier employee.
     * */
    private void createCashier(String restaurantID){
        Map<String, Object > cashier = new HashMap<>();
        cashier.put(QuanLyConstants.RESTAURANT_ID,restaurantID);
        cashier.put(QuanLyConstants.RESTAURANT_NAME,txtName.getText().toString().trim());
        cashier.put(QuanLyConstants.EMPLOYEE_USERNAME,txtUsername.getText().toString().trim());
        cashier.put(QuanLyConstants.EMPLOYEE_PASSWORD,txtPassword.getText().toString().trim());
        cashier.put(QuanLyConstants.EMPLOYEE_CONTACT,txtContactNumber.getText().toString().trim());
        cashier.put(QuanLyConstants.EMPLOYEE_POSITION,4);
        db.collection(QuanLyConstants.EMPLOYEE)
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

    /*
     * @author: ManhLD
     * @purpose: Check input form
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

        String username = txtUsername.getText().toString();
        if (TextUtils.isEmpty(username)) {
            txtUsername.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            txtUsername.setError(null);
        }

        String password = txtPassword.getText().toString();
        if (TextUtils.isEmpty(password)) {
            txtPassword.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            txtPassword.setError(null);
        }

        String contactNumber = txtContactNumber.getText().toString();
        if (TextUtils.isEmpty(contactNumber)) {
            txtContactNumber.setError(getResources().getString(R.string.required));
            valid = false;
        } else {
            txtContactNumber.setError(null);
        }

        return valid;
    }

    public String getRestaurantID(){
        String langPref = QuanLyConstants.RESTAURANT_ID;
        SharedPreferences prefs = getSharedPreferences(QuanLyConstants.SHARED_PERFERENCE, Activity.MODE_PRIVATE);
        return prefs.getString(langPref,"");
    }

    @Override
    public void finish() {
        Intent dataBack = new Intent();
        dataBack.putExtra(QuanLyConstants.FLAG,flag);
        this.setResult(Activity.RESULT_OK,dataBack);
        super.finish();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == R.id.employee_detail_button_create){
            if(buttonUpdate.getText().equals(getResources().getString(R.string.button_create))){
                if(validateForm()){
                    showLoadingDialog();
                    createAuthForEmployee();
                }
            }
            else{
                showLoadingDialog();
                Map<String, Object > employee = new HashMap<>();
                employee.put(QuanLyConstants.RESTAURANT_ID,RestaurantID);
                employee.put(QuanLyConstants.EMPLOYEE_NAME,txtName.getText().toString().trim());
                employee.put(QuanLyConstants.EMPLOYEE_USERNAME,txtUsername.getText().toString().trim());
                employee.put(QuanLyConstants.EMPLOYEE_PASSWORD,txtPassword.getText().toString().trim());
                employee.put(QuanLyConstants.EMPLOYEE_CONTACT,txtContactNumber.getText().toString().trim());
                employee.put(QuanLyConstants.EMPLOYEE_POSITION,spinnerPosition.getSelectedItemPosition()+2);
                db.collection(QuanLyConstants.EMPLOYEE).document(documentID)
                        .set(employee, SetOptions.merge());
                Toast.makeText(getApplicationContext(),getResources().getString(R.string.employee_detail_update_success)
                        ,Toast.LENGTH_SHORT).show();
                closeLoadingDialog();
                closeEditText();
            }
        }
    }
}
