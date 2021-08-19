package com.example.gapshap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.example.gapshap.Adapters.ChatAdapter;
import com.example.gapshap.databinding.ActivityChatboxBinding;
import com.example.gapshap.models.MessagesModel;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.View;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class ChatboxActivity extends AppCompatActivity {

    ActivityChatboxBinding binding;
    FirebaseDatabase database;
    FirebaseStorage storage;
    FirebaseAuth auth;
    FirebaseUser user;
    ProgressDialog progressDialog;
    String senderId ;
    String receiveId ;
    String senderRoom ;
    String receiverRoom ;
    ArrayList<MessagesModel> messagesModels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatboxBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Uploading");
        progressDialog.setCancelable(false);

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        storage = FirebaseStorage.getInstance();

        String profilePic = getIntent().getStringExtra("profilePic");
        String userName = getIntent().getStringExtra("userName");
        binding.username.setText(userName);
        Picasso.get().load(profilePic).placeholder(R.drawable.avatar).into(binding.profileImage);

        receiveId = getIntent().getStringExtra("userId");
        senderId = auth.getUid();
        receiverRoom = receiveId + senderId;
        senderRoom = senderId + receiveId;

        binding.backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
//
         messagesModels = new ArrayList<>();


        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        database.getReference().child("Chats")
                .child(senderRoom).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagesModels.clear(); //previous snapshots(messages) should be cleared before showing in recycler view
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    MessagesModel model = snapshot1.getValue(MessagesModel.class);
                    messagesModels.add(model);
                }
                final ChatAdapter chatAdapter = new ChatAdapter(messagesModels, ChatboxActivity.this);
                binding.chatRecyclerView.setAdapter(chatAdapter);
                chatAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
//
        binding.sendImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/* video/*");
                startActivityForResult(intent, 25);
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
                    model.setType("text");
                    binding.msg.setText("");

                    //make separate nodes for chats
                    database.getReference().child("Chats")
                            .child(senderRoom).push()
                            .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) { //if the message is sent successfull create another node for receiver for same msg
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 25){
            if(data != null){
                    Uri selectedImg = data.getData();
                    Calendar calendar = Calendar.getInstance();
                    StorageReference ref = storage.getReference().child("Chats").child(user
                    .getUid()).child(calendar.getTimeInMillis()+" ");
                    progressDialog.show();
                    ref.putFile(selectedImg).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            progressDialog.dismiss();
                            if(task.isSuccessful()){
                                ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String filePath = uri.toString();

                                        String message = "Sent an Image";
                                            final MessagesModel model = new MessagesModel(senderId, message);
                                            model.setImageUrl(filePath);
                                            model.setTimestamp(new Date().getTime());
                                            model.setType("image");
                                            binding.msg.setText(" ");

                                            //make separate nodes for chats
                                            database.getReference().child("Chats")
                                                    .child(senderRoom).push()
                                                    .setValue(model).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) { //if the message is sent succesfullu create another node for receiver for same msg
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
                                });
                            }
                        }
                    });
            }
        }
    }
}
