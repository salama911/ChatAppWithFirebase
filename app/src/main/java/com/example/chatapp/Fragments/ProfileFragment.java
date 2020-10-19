package com.example.chatapp.Fragments;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;

import java.util.HashMap;

import javax.xml.transform.Result;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.app.Activity.RESULT_OK;


public class ProfileFragment extends Fragment {

   CircleImageView profile_image;
   TextView username;

   DatabaseReference reference;
   FirebaseUser fuser;

   StorageReference storageReference;
   private static  final int IMAGE_REQUEST=1;
   private Uri imageuri;
   private StorageTask uploadtask;

    public ProfileFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


       View view=inflater.inflate(R.layout.fragment_profile, container, false);

       profile_image=view.findViewById(R.id.profile_image_prof);
       username=view.findViewById(R.id.username_prof);

       storageReference= FirebaseStorage.getInstance().getReference("uploads");

       fuser= FirebaseAuth.getInstance().getCurrentUser();
       reference= FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
       reference.addValueEventListener(new ValueEventListener() {
           @Override
           public void onDataChange(@NonNull DataSnapshot snapshot) {
               User user=snapshot.getValue(User.class);
               username.setText(user.getUsername());
               if(user.getImageurl().equals("default")){
                   profile_image.setImageResource(R.mipmap.ic_launcher);
               }else{
                   Glide.with(getActivity()).load(user.getImageurl()).into(profile_image);

               }
           }

           @Override
           public void onCancelled(@NonNull DatabaseError error) {

           }
       });

       profile_image.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               openImage();
           }


       });

       return view;
    }



    private void openImage() {
        Intent intent=new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,IMAGE_REQUEST);
    }
    private String getFileextension(Uri uri){

        ContentResolver contentResolver=getContext().getContentResolver();
        MimeTypeMap mimeTypeMap=MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }
    private void Uploadimage(){
        final ProgressDialog pd=new ProgressDialog(getContext());
        pd.setMessage("uploading");
        pd.show();

        if(imageuri!=null){
            final StorageReference filerefrence=storageReference.child(System.currentTimeMillis() +"."+getFileextension(imageuri));

            uploadtask=filerefrence.putFile(imageuri);
            uploadtask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if(!task.isSuccessful()){
                        throw   task.getException();
                    }
                    return filerefrence.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Uri downloaduri=task.getResult();
                        String myurl=downloaduri.toString();

                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());

                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("imageurl",""+myurl);
                        reference.updateChildren(hashMap);
                        pd.dismiss();
                    }else{
                        Toast.makeText(getContext(), "Failed", Toast.LENGTH_SHORT).show();
                                                }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(getContext(),e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            Toast.makeText(getContext(), "No Image Selected", Toast.LENGTH_SHORT).show();
        }


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==IMAGE_REQUEST&& resultCode == RESULT_OK
               &&data!=null &&data.getData()!=null ) {
            imageuri=data.getData();
            if (uploadtask!=null&&uploadtask.isInProgress()){
                Toast.makeText(getContext(), "upload in progress", Toast.LENGTH_SHORT).show();
            }else {
                Uploadimage();
            }

        }
    }
}