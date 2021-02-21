package com.example.login;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


import java.util.Objects;

public class RegistorActivity extends AppCompatActivity {

    TextInputEditText txt_password;
    EditText txt_dob,name,txt_email;

    private FirebaseAuth firebaseAuth;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registor);

        getSupportActionBar().hide();

        init();





    }

    private void init() {


        txt_dob=(EditText) findViewById(R.id.dob);
        name = findViewById(R.id.name);
        txt_email = findViewById(R.id.username);
        txt_password = findViewById(R.id.password);
        databaseReference = FirebaseDatabase.getInstance().getReference("Data");
        firebaseAuth = FirebaseAuth.getInstance();

    }




    public void login(View view) {
        Intent intent = new Intent(RegistorActivity.this,LoginActivity.class);
        startActivity(intent);

    }

    public void SignUp(View view) {



        final String fullname=name.getText().toString();
        final String email=txt_email.getText().toString();
        final String password= Objects.requireNonNull(txt_password.getText()).toString();
        final String number=txt_dob.getText().toString();


        if (fullname.isEmpty() && email.isEmpty() &&  password.isEmpty()&& number.isEmpty() ){
            name.setError("Enter your Name");
            txt_email.setError("Enter your Email");
            txt_dob.setError("Enter your Number");
            txt_password.setError("Enter your Password");
        }


        else {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Registering ...");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(RegistorActivity.this, new OnCompleteListener<AuthResult>() {


                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {


                            if (task.isSuccessful()) {
                                DataClass information = new DataClass(
                                        fullname,
                                        email,
                                        number
                                );
                                FirebaseDatabase.getInstance().getReference("Data")
                                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .setValue(information).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        Toast.makeText(RegistorActivity.this, "Registration Complete", Toast.LENGTH_SHORT).show();
                                        progressDialog.dismiss();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        RegistorActivity.this.finish();

                                    }
                                });

                            } else {

                                Toast.makeText(RegistorActivity.this, "Sorry Something Went Wrong. Please Try Again Later.", Toast.LENGTH_SHORT).show();
                                progressDialog.dismiss();
                            }

                        }
                    });

        }
    }


}
