package com.example.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class LoginActivity extends AppCompatActivity {


    private static final int RC_SIGN_IN = 65;
    TextView registor,btnReset;
    EditText id,password;
    Button login;
    ImageButton google,git;
    View progressOverlay;
    private FirebaseAuth mAuth;
    GoogleSignInClient mGoogleSignInClient;
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

       getSupportActionBar().hide();


       //  Initialization
        init();

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = id.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    id.requestFocus();
                    Toast.makeText(getApplication(), "Enter your registered email id", Toast.LENGTH_SHORT).show();
                    return;
                }

                progressOverlay.setVisibility(View.VISIBLE);

                mAuth.sendPasswordResetEmail(email)

                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(LoginActivity.this, "We have sent you instructions to reset your password!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(LoginActivity.this, "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                }

                                progressOverlay.setVisibility(View.GONE);
                            }
                        });
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });




// Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso);




        git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(id.getText().toString())){
                    Toast.makeText(getApplication(), "Enter your email id", Toast.LENGTH_SHORT).show();
                }
                else {
                    SignInWithGithubProvider(
                            OAuthProvider.newBuilder("github.com")
                            .addCustomParameter("login",id.getText().toString()).setScopes(
                                    new ArrayList<String>(){
                                        {
                                            add("user:email");
                                        }
                            })

                            .build()
                    );
                }
            }
        });





    }

    private void SignInWithGithubProvider(OAuthProvider login) {
        Task<AuthResult> pendingAuthTask = mAuth.getPendingAuthResult();
        if (pendingAuthTask != null){
            pendingAuthTask.addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(LoginActivity.this, "User Exist ", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }else {
            mAuth.startActivityForSignInWithProvider(this,login).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                @Override
                public void onSuccess(AuthResult authResult) {
                    Toast.makeText(LoginActivity.this,"Login Successfull", Toast.LENGTH_SHORT).show();
                    Log.d("TAG", "signInWithCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();

                    String fullname =user.getDisplayName();
                    String email =user.getEmail();
                    String number =user.getPhoneNumber();

                    DataClass dataClass = new DataClass(
                            fullname,
                            email,
                            number
                    );


                    database.getReference("Data").child(user.getUid()).setValue(dataClass);

                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    startActivity(intent);
                    Toast.makeText(LoginActivity.this, "Sign in with Github", Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                Log.d("TAG", "firebaseAuthWithGoogle:" + account.getId());
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w("TAG", "Google sign in failed", e);
                // ...
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {



                            // Sign in success, update UI with the signed-in user's information
                            Log.d("TAG", "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                            String fullname =user.getDisplayName();
                            String email =user.getEmail();
                            String number =user.getPhoneNumber();

                            DataClass dataClass = new DataClass(
                                    fullname,
                                    email,
                                    number
                            );


                            database.getReference("Data").child(user.getUid()).setValue(dataClass);

                            Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                            startActivity(intent);
                            Toast.makeText(LoginActivity.this, "Sign in with Google", Toast.LENGTH_SHORT).show();
                          //  updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w("TAG", "signInWithCredential:failure", task.getException());
                          //  Snackbar.make(mBinding.mainLayout, "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }

                        // ...
                    }
                });
    }



    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
            startActivity(new Intent(getApplicationContext(),MainActivity.class));
            LoginActivity.this.finish();
        }
        else {
            Toast.makeText(getApplicationContext(),"Login To Continue",Toast.LENGTH_SHORT).show();
        }
    }

    private void init() {

        registor = (TextView) findViewById(R.id.registor);
        id = (EditText) findViewById(R.id.username);
        password= (EditText) findViewById(R.id.password);
        login = (Button) findViewById(R.id.login_btn);
        git =  findViewById(R.id.sign_fb);
        google =(ImageButton) findViewById(R.id.signgoogle);
        progressOverlay = findViewById(R.id.progress_overlay);
        mAuth = FirebaseAuth.getInstance();
        btnReset= findViewById(R.id.for_pass);
        database = FirebaseDatabase.getInstance();



    }

    public void Registor(View view) {
        Intent intent = new Intent(LoginActivity.this,RegistorActivity.class);
        startActivity(intent);
    }

    public void Login(View view) {
        String email = id.getText().toString();
        String pass = password.getText().toString();

        if (email.isEmpty() && pass.isEmpty()) {
            id.setError("Please enter Email");
            password.setError("Please enter Password");
            id.requestFocus();
        }
        else if (email.isEmpty()) {
            id.setError("Please enter Email");
            id.requestFocus();
        }
        else if (pass.isEmpty()) {
            password.setError("Please enter Password");
            password.requestFocus();
        }
        else if (pass.length() < 6) {
            password.setError("Length Too Short");
            password.requestFocus();
        }
        else   {
            progressOverlay.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {


                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            progressOverlay.setVisibility(View.GONE);
                            if (task.isSuccessful()) {

                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                LoginActivity.this.finish();

                            } else {
                                Toast.makeText(getApplicationContext(), "Login Failed or User Not Exist", Toast.LENGTH_SHORT).show();

                            }

                        }
                    });
        }
    }
}