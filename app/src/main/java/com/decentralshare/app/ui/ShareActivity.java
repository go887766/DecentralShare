package com.decentralshare.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.decentralshare.app.R;
import com.decentralshare.app.data.DataManager;
import com.decentralshare.app.databinding.ActivityShareBinding;
import java.util.ArrayList;
import java.util.List;

public class ShareActivity extends AppCompatActivity {

    private ActivityShareBinding binding;
    private DataManager dataManager;
    private List<String> images;
    private List<String> videos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShareBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = DataManager.getInstance(this);
        images = new ArrayList<>();
        videos = new ArrayList<>();

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        binding.btnAddImage.setOnClickListener(v -> {
            Toast.makeText(this, "图片功能即将上线", Toast.LENGTH_SHORT).show();
        });

        binding.btnAddVideo.setOnClickListener(v -> {
            Toast.makeText(this, "视频功能即将上线", Toast.LENGTH_SHORT).show();
        });

        binding.btnPublish.setOnClickListener(v -> publish());
    }

    private void publish() {
        String title = binding.etTitle.getText().toString().trim();
        String content = binding.etContent.getText().toString().trim();

        if (TextUtils.isEmpty(title)) {
            binding.etTitle.setError("请输入标题");
            return;
        }

        if (TextUtils.isEmpty(content)) {
            binding.etContent.setError("请输入内容");
            return;
        }

        dataManager.createPost(title, content, images, videos);
        Toast.makeText(this, R.string.share_success, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
