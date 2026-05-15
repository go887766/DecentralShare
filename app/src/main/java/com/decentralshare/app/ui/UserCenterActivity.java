package com.decentralshare.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.decentralshare.app.R;
import com.decentralshare.app.adapter.PostAdapter;
import com.decentralshare.app.data.DataManager;
import com.decentralshare.app.databinding.ActivityUserCenterBinding;
import com.decentralshare.app.model.Post;
import com.decentralshare.app.model.User;
import java.util.List;

public class UserCenterActivity extends AppCompatActivity {

    private ActivityUserCenterBinding binding;
    private DataManager dataManager;
    private PostAdapter adapter;
    private List<Post> myPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserCenterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = DataManager.getInstance(this);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        User user = dataManager.getCurrentUser();
        if (user != null) {
            binding.tvAvatar.setText(user.getNickname().substring(0, 1).toUpperCase());
            binding.tvNickname.setText(user.getNickname());
            binding.tvAddress.setText(user.getAddress());
            binding.tvCoins.setText(String.valueOf(user.getCoins()));
        }

        loadMyPosts();

        binding.btnLogout.setOnClickListener(v -> {
            dataManager.logout();
            Intent intent = new Intent(UserCenterActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void loadMyPosts() {
        myPosts = dataManager.getMyPosts();
        adapter = new PostAdapter(this, myPosts, null);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
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
