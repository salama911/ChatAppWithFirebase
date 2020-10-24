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
import com.example.chatapp.Fragments.Apiservice;
import com.example.chatapp.Model.Chat;
import com.example.chatapp.Model.User;
import com.example.chatapp.Notifications.Client;
import com.example.chatapp.Notifications.Data;
import com.example.chatapp.Notifications.MyResponse;
import com.example.chatapp.Notifications.Sender;
import com.example.chatapp.Notifications.Token;
import com.google.android.gms.common.api.Api;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    String userid;

    Apiservice apiservice;

    boolean notify =false;


    ValueEventListener seenlistener;
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
                startActivity(new Intent(getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
            }
        });

       apiservice = Client.getClient("https://fcm.googleapis.com/").create(Apiservice.class);

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
         userid=intent.getStringExtra("userid");

        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
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
                    Glide.with(getApplicationContext()).load(user.getImageurl()).into(imageprofile);
                }
                readMessage(fuser.getUid(),userid,user.getImageurl());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        seenLMessage(userid);

    }

    private void seenLMessage(final String userid){
        reference=FirebaseDatabase.getInstance().getReference("Chat");
        seenlistener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot snapshot1:snapshot.getChildren()){
                    Chat chat=snapshot1.getValue(Chat.class);
                    if(chat.getReceiver().equals(fuser.getUid())&&chat.getSender().equals(userid)){

                        HashMap <String ,Object>hashMap=new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot1.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String sender, final String receiver, String message){

        reference=FirebaseDatabase.getInstance().getReference("Chat");

        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("isseen",false);


        reference.push().setValue(hashMap);

        final DatabaseReference chatRef=FirebaseDatabase.getInstance().getReference("ChatList")
                .child(fuser.getUid())
                .child(userid);
        chatRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(!snapshot.exists()){
                    chatRef.child("id").setValue(userid);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        final String msg=message;
        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                    if (notify) {
                        sendNotification(receiver, user.getUsername(), msg);
                    }
                    notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(String receiver, final String username, final String message) {

        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query = tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Token token = snapshot.getValue(Token.class);
                    Data data = new Data(fuser.getUid(), R.mipmap.ic_launcher, username+": "+message, "New Message",
                            userid);

                    Sender sender = new Sender(data, token.getToken());

                    apiservice.sendNotification(sender)
                            .enqueue(new Callback<MyResponse>() {
                                @Override
                                public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                    if (response.code() == 200){
                                        if (response.body().success != 1){
                                            Toast.makeText(MessageActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(Call<MyResponse> call, Throwable t) {

                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
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
    private void status(String status){
        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        HashMap<String,Object>hashMap=new HashMap<>();
        hashMap.put("status",status);

        reference.updateChildren(hashMap);
    }

    @Override
    protected void onResume() {
        super.onResume();
        status("online");
    }



    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenlistener);
        status("offline");
    }
}