package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    TextView name,email,phone;
    DatabaseReference host;
    String id = FirebaseAuth.getInstance().getCurrentUser().getUid();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name= findViewById(R.id.name);
        email= findViewById(R.id.email);
        phone= findViewById(R.id.phone);
        host = FirebaseDatabase.getInstance().getReference().child("Data");
        read();



    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        startActivity(new Intent(MainActivity.this,LoginActivity.class));
        MainActivity.this.finish();
    }
    private void read() {

        host.child(id).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String fullname = dataSnapshot.child("Fullname").getValue(String.class);
                String number = dataSnapshot.child("Number").getValue(String.class);
                String mail = dataSnapshot.child("Email").getValue(String.class);

                if(number != null) {

                    name.setText(fullname);
                    phone.setText(number);
                    email.setText(mail);
                }else{
                    name.setText(fullname);
                    email.setText(mail);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}