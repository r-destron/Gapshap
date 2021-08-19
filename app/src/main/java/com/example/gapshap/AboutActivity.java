package com.example.gapshap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.example.gapshap.databinding.ActivityAboutBinding;

public class AboutActivity extends AppCompatActivity {

    ActivityAboutBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        //Code for animation

            ViewCompat.animate(binding.linear)
                    .translationY(-20)
                    .setStartDelay(400)
                    .setDuration(1000).setInterpolator(
                    new DecelerateInterpolator(1.1f)).start();
            ViewCompat.animate(binding.linear)
                    .translationY(10)
                    .setStartDelay(800)
                    .setDuration(1000).setInterpolator(
                    new DecelerateInterpolator(1.1f)).start();


        binding.backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}