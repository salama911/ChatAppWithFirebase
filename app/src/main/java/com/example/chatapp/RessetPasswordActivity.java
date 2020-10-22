package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class RessetPasswordActivity extends AppCompatActivity {

    EditText sendemail;
    Button reset;

    FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resset_password);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Reset");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setTitleTextColor(Color.WHITE);

        sendemail=findViewById(R.id.send_email_edit);
        reset=findViewById(R.id.btn_reset);
        firebaseAuth=FirebaseAuth.getInstance();
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               String email= sendemail.getText().toString();

               if(email.equals("")){
                   Toast.makeText(RessetPasswordActivity.this, "This Field is required ", Toast.LENGTH_SHORT).show();
               }
               else{
                   firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                       @Override
                       public void onComplete(@NonNull Task<Void> task) {
                           if(task.isSuccessful()){
                               Toast.makeText(RessetPasswordActivity.this, "Please Cheack Your Email", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RessetPasswordActivity.this,LoginActivity.class));
                           }else{
                               String error=task.getException().getMessage();
                               Toast.makeText(RessetPasswordActivity.this, ""+error, Toast.LENGTH_SHORT).show();
                           }

                       }
                   });
               }
            }
        });
    }
}