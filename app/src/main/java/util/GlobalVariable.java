package util;


import android.annotation.SuppressLint;
import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;

import manhquan.khoaluan_quanly.R;

/**
 * Created by LieuDucManh on 4/11/2018.
 */

public class GlobalVariable {

    public static String tableChoose = "0";
    public static String tableCusID = "";
    public static String employeeID = "";
    public static String employeeName = "";

    public static MaterialDialog dialogLoading;

    @SuppressLint("ResourceAsColor")
    public static void showLoadingDialog(Context context){
        dialogLoading = new MaterialDialog.Builder(context)
                .customView(R.layout.loading_dialog,true)
                .backgroundColor(context.getResources().getColor(R.color.primary_darker))
                .canceledOnTouchOutside(false)
                .show();
    }

    public static void closeLoadingDialog(){
        if(dialogLoading.isShowing()){
            dialogLoading.dismiss();
        }
    }
}
