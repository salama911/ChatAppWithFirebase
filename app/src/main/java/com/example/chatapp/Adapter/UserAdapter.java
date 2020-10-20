package com.example.chatapp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.chatapp.MessageActivity;
import com.example.chatapp.Model.User;
import com.example.chatapp.R;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.viewHolder> {

    private Context mcontext;
    private List<User>mUsers;
    private boolean ischat;

    public UserAdapter(Context mcontext, List<User> mUsers,boolean ischat) {
        this.mcontext = mcontext;
        this.mUsers = mUsers;
        this.ischat = ischat;

    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(mcontext).inflate(R.layout.user_item,parent,false);

        return new UserAdapter.viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        final User user=mUsers.get(position);
        holder.username.setText(user.getUsername());
        if(user.getImageurl().equals("default")){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }
        else
        {
            Glide.with(mcontext).load(user.getImageurl()).into(holder.profile_image);
        }

        if (ischat){

            if (user.getStatus().equals("online")){
                holder.img_on.setVisibility(View.VISIBLE);
                holder.img_off.setVisibility(View.GONE);
            }
            else {
                holder.img_on.setVisibility(View.GONE);
                holder.img_off.setVisibility(View.VISIBLE);
            }

        }else{
            holder.img_on.setVisibility(View.GONE);
            holder.img_off.setVisibility(View.GONE);
        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mcontext, MessageActivity.class);
                intent.putExtra("userid",user.getId());
                mcontext.startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }


    public  class viewHolder extends RecyclerView.ViewHolder {

        public ImageView profile_image;
        public TextView username;
        private ImageView img_on;
        private ImageView img_off;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            profile_image=itemView.findViewById(R.id.user_image);
            username=itemView.findViewById(R.id.usernamee);
            img_on=itemView.findViewById(R.id.img_on);
            img_off=itemView.findViewById(R.id.img_off);
        }
    }
}
