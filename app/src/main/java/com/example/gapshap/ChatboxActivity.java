package com.example.gapshap;

import android.content.Intent;
import android.os.Bundle;

import com.example.gapshap.Adapters.ChatAdapter;
import com.example.gapshap.databinding.ActivityChatboxBinding;
import com.example.gapshap.models.MessagesModel;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;

import java.util.ArrayList;
import java.util.Date;

public class ChatboxActivity extends AppCompatActivity {

    ActivityChatboxBinding binding;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatboxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

//        getSupportActionBar().hide();
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
//
        final String senderId = auth.getUid(); //final makes it global since this is to be used a lot of other places
        String receiveId = getIntent().getStringExtra("userId");
        String profilePic = getIntent().getStringExtra("profilePic");
        String userName = getIntent().getStringExtra("userName");
        binding.username.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatboxActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
//
        final ArrayList<MessagesModel> messagesModels = new ArrayList<>();
        final ChatAdapter chatAdapter = new ChatAdapter(messagesModels, this);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        final String senderRoom = senderId+receiveId;
        final String receiverRoom = receiveId+senderId;
//
        database.getReference().child("Chats")
                .child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesModels.clear(); //previous snapshots(messages) should be cleared before showing in recycler view
                for(DataSnapshot snapshot1 : snapshot.getChildren()){
                    MessagesModel model = snapshot1.getValue(MessagesModel.class);
                    messagesModels.add(model);
                }
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//
        binding.sendbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.msg.getText().toString();
                if(!message.isEmpty()) {
                    final MessagesModel model = new MessagesModel(senderId, message);
                    model.setTimestamp(new Date().getTime());
                    binding.msg.setText("");

                    //make separate nodes for chats
                    database.getReference().child("Chats")
                            .child(senderRoom).push()
                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) { //if the message is sent succesfull create another node for receiver for same msg
                            database.getReference().child("Chats")
                                    .child(receiverRoom).push()
                                    .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        }
                    });

                }
            }
        });
    }
    }
