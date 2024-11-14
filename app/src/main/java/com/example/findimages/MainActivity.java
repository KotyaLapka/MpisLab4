package com.example.findimages;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.findimages.model.Image;
import com.example.findimages.model.ImageApi;
import com.example.findimages.model.ImageResponse;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ImageView imageView;
    private String currentImageUrl;
    private String imagePageUrl = "https://unsplash.com";
    private ImageApi imageApi;
    private String query = "random";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = findViewById(R.id.imageView);
        Button searchBtn = findViewById(R.id.searchBtn);
        Button likeBtn = findViewById(R.id.likeBtn);
        Button dislikeBtn = findViewById(R.id.dislikeBtn);
        Button downloadBtn = findViewById(R.id.downloadBtn);
        Button openInBrowserBtn = findViewById(R.id.openInBrowserBtn);
        Button authorBtn = findViewById(R.id.author);
        EditText searchEditText = findViewById(R.id.searchEditText);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.unsplash.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        imageApi = retrofit.create(ImageApi.class);

        searchBtn.setOnClickListener(v -> searchImages());
        likeBtn.setOnClickListener(v -> likeImage());
        dislikeBtn.setOnClickListener(v -> dislikeImage());
        downloadBtn.setOnClickListener(v -> downloadImage());
        openInBrowserBtn.setOnClickListener(v -> openInBrowser());
        authorBtn.setOnClickListener(v -> showToast("Разработала Смердина Анастасия"));

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            query = searchEditText.getText().toString();
            searchImages();
            return true;
        });
    }

    private void searchImages() {
        Call<ImageResponse> call = imageApi.searchImages(query, "3d_ukmumfwmwcsFxEWfgB8yepI45aw-mtyTQklpzBqI", 1, 30);
        call.enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (!response.isSuccessful() || response.body() == null || response.body().getResults().isEmpty()) {
                    showToast("Error: No images found");
                    return;
                }

                List<Image> images = response.body().getResults();
                Random random = new Random();
                int randomIndex = random.nextInt(images.size());

                currentImageUrl = images.get(randomIndex).getUrls().getSmall();
                imagePageUrl = "https://unsplash.com/photos/" + images.get(randomIndex).getId();

                Glide.with(MainActivity.this).load(currentImageUrl).into(imageView);
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        });
    }

    private void likeImage() {
        if (currentImageUrl != null) {
            showToast("You liked the image!");
        }
    }

    private void dislikeImage() {
        if (currentImageUrl != null) {
            showToast("You disliked the image!");
        }
    }

    private void downloadImage() {
        if (currentImageUrl != null) {
            DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
            Uri uri = Uri.parse(currentImageUrl);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            downloadManager.enqueue(request);
            showToast("Downloading image...");
        } else {
            showToast("No image to download");
        }
    }

    private void openInBrowser() {
        if (imagePageUrl != null) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(imagePageUrl));
            startActivity(browserIntent);
        }
    }

    private void showToast(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
    }
}