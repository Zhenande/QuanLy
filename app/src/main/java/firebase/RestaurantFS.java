package firebase;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import model.Restaurant;

/**
 * Created by ABC on 3/21/2018.
 */

public class RestaurantFS {

    private FirebaseFirestore db;
    private Restaurant restaurant;

    public Restaurant getRestaurant(){
        restaurant = new Restaurant();
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
}
