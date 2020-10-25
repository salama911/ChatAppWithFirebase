package com.example.chatapp.Fragments;

import com.example.chatapp.Notifications.MyResponse;
import com.example.chatapp.Notifications.Sender;

import retrofit2.Call;
import retrofit2.http.Body;

import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface Apiservice {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAqn5gAnk:APA91bEmDZxQ3fKze8e_tmOyAu5YAFc98DLrrjZctRcVZUvVfBnFlMKjveKzZdqeQ54K2EqjxQkuBmd1uo-o4ibECswYZC-bqrCXJxYcilzBSCEEtZiN0iT4XpYYeR1ILXJGkvaDWozt"
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotification(@Body Sender body);
}