package manhquan.khoaluan_quanly;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import constants.QuanLyConstants;

public class EmployeeDetailActivity extends AppCompatActivity {

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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_detail);

        ButterKnife.bind(this);

        spinnerPosition = findViewById(R.id.employee_detail_spinnerEmployeePosition);
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getApplicationContext(),R.array.add_account_spinner_Position
                ,R.layout.spinner_item_text);

        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item_text);
        spinnerPosition.setAdapter(spinnerAdapter);

        RestaurantID = getRestaurantID();

        showLoadingDialog();
        documentID = getIntent().getStringExtra(QuanLyConstants.INTENT_DOCUMENT_ID);
        renderUI(documentID);

        buttonUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showLoadingDialog();
                FirebaseFirestore db = FirebaseFirestore.getInstance();
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
        });
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
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
                .show();
    }

    public void renderUI(final String documentID){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
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
}
