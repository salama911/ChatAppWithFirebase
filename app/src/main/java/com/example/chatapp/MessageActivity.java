package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.Adapter.MessageAdapter;
import com.example.chatapp.Model.Chat;
import com.example.chatapp.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MessageActivity extends AppCompatActivity {

  private   ImageView imageprofile;
  private   TextView  username;

  private ImageButton btn_send;
  private EditText txt_send;

    Intent intent;
    FirebaseUser fuser;
    DatabaseReference reference;

    RecyclerView recyclerView;
    MessageAdapter messageAdapter;
    List <Chat>chatList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        recyclerView=findViewById(R.id.recycler_view_ch);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        imageprofile=findViewById(R.id.profile_imagee);
        username=findViewById(R.id.usernameee);
        btn_send=findViewById(R.id.btn_send);
        txt_send=findViewById(R.id.txt_send);


        intent=getIntent();
        final String userid=intent.getStringExtra("userid");

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg=txt_send.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(),userid,msg);

                }
                else
                {
                    Toast.makeText(MessageActivity.this, "You can't send empty Message", Toast.LENGTH_SHORT).show();
                }

                txt_send.setText("");
            }
        });

        fuser=FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User user=snapshot.getValue(User.class);
                assert user != null;
                username.setText(user.getUsername());
                if(user.getImageurl().equals("default")){
                    imageprofile.setImageResource(R.mipmap.ic_launcher);
                }else{
                    Glide.with(MessageActivity.this).load(user.getImageurl()).into(imageprofile);
                }
                readMessage(fuser.getUid(),userid,user.getImageurl());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void sendMessage(String sender,String receiver,String message){

        reference=FirebaseDatabase.getInstance().getReference("Chat");

        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);

        reference.push().setValue(hashMap);

    }

    private void readMessage(final String myid, final String userid, final String imageurl){

        chatList=new ArrayList<>();

        reference=FirebaseDatabase.getInstance().getReference("Chat");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatList.clear();
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Chat chat=snapshot1.getValue(Chat.class);
                    assert chat != null;
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid)||
                          chat.getReceiver().equals(userid)&&chat.getSender().equals(myid)  )
                    {
                        chatList.add(chat);
                    }
                    messageAdapter=new MessageAdapter(MessageActivity.this,chatList,imageurl);
                    recyclerView.setAdapter(messageAdapter);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}