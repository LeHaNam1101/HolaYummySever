package com.example.holayummyserver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.holayummyserver.Common.Common;
import com.example.holayummyserver.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class SignIn extends AppCompatActivity {

    private Button btnSignIn;
    private EditText edtPhone, edtPassword;

    FirebaseDatabase db;
    DatabaseReference users;
    private void bindingView() {
        btnSignIn = findViewById(R.id.btnSignIn);
        edtPhone = findViewById(R.id.edtPhone);
        edtPassword=findViewById(R.id.edtPassword);
        db = FirebaseDatabase.getInstance();
        users = db.getReference("User");
    }
    private void bindingAction() {
        btnSignIn.setOnClickListener(this::onBtnSignInClick);
    }

    private void onBtnSignInClick(View view) {
            signInUser(edtPhone.getText().toString(),edtPassword.getText().toString());
    }

    private void signInUser(String phone, String password) {
        ProgressDialog mDialog = new ProgressDialog(SignIn.this);
        mDialog.setMessage("Please waiting...");
        mDialog.show();

        final String localPhone = phone;
        final String localPassword = password;
        Query query = users.child(localPhone);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot datasnapshot) {
                if(datasnapshot.exists()){
                        mDialog.dismiss();
                        User user = datasnapshot.getValue(User.class);
                        user.setPhone(localPhone);
                        String isStaff = (String) datasnapshot.child("IsStaff").getValue();
                        if(isStaff.equals("true"))
                        {
                            if(user.getPassword().equals(localPassword)){
                                Intent login = new Intent(SignIn.this,Home.class);
                                login.putExtra("user_name", (String) datasnapshot.child("Name").getValue());
//                                Common.currentUser = user;
                                startActivity(login);
                                finish();


                            }
                            else {
                                Toast.makeText(SignIn.this, "Wrong password", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else {
                            Toast.makeText(SignIn.this, "Please login with Staff account", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else {
                        mDialog.dismiss();
                        Toast.makeText(SignIn.this, "User not exist in Database", Toast.LENGTH_SHORT).show();

                    }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        bindingView();
        bindingAction();
    }
}