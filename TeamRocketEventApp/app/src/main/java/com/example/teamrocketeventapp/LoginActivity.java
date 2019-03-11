package com.example.teamrocketeventapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.AuthResult;
import com.google.android.gms.tasks.OnCompleteListener;
import android.support.annotation.NonNull;
import android.util.Log;


import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private FirebaseAuth Auth;
    private EditText passwordText;
    private EditText emailText;
    private FirebaseUser user;
    public static final String EXTRA_MESSAGE = "com.example.teamrocketeventapp.MESSAGE";




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailText = (EditText) findViewById(R.id.enterEmail);
        passwordText = (EditText) findViewById(R.id.enterPassword);
        Auth = FirebaseAuth.getInstance();
        user=Auth.getCurrentUser();

    }
    public void cancel (View view){
        //go back to main page when cancel is pressed

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

    }

    public void login(View view){

        String pass = passwordText.getText().toString().trim();
        String email = emailText.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();  //Toast is popup msg at bottom
            return; //Return to stop registration
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show();
            return;
        }

        Auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information


                            //FirebaseUser currUser = Auth.getCurrentUser();

                            //String node = "users/" + user.getUid();
                            //user=currUser;

                            Log.w(TAG, "createUserWithEmail:success");
                            Toast.makeText(LoginActivity.this, "Login Success",
                                    Toast.LENGTH_SHORT).show();

                            updateUI(null, user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    //method id called upon sucessful login
    public void updateUI (View view, FirebaseUser user){
        //go to event page after sucessful login
        //TODO change MainActivity to the userprofile page
        Intent intent = new Intent(this, EventIndexActivity.class); //temporary change for search testing
        intent.putExtra(EXTRA_MESSAGE, user.getUid());
        startActivity(intent);
    }



}
