package com.decentralshare.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.decentralshare.app.R;
import com.decentralshare.app.adapter.PostAdapter;
import com.decentralshare.app.data.DataManager;
import com.decentralshare.app.databinding.ActivityMainBinding;
import com.decentralshare.app.model.Post;
import com.decentralshare.app.model.User;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DataManager dataManager;
    private PostAdapter adapter;
    private List<Post> posts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = DataManager.getInstance(this);

        if (!dataManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);

        posts = new ArrayList<>();
        adapter = new PostAdapter(this, posts, new PostAdapter.OnPostInteractionListener() {
            @Override
            public void onLikeChanged() {
                updateToolbarTitle();
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        binding.fabAdd.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, ShareActivity.class));
        });

        binding.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadPosts();
                binding.swipeRefreshLayout.setRefreshing(false);
            }
        });

        updateToolbarTitle();
        loadPosts();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPosts();
    }

    private void updateToolbarTitle() {
        User user = dataManager.getCurrentUser();
        if (user != null && getSupportActionBar() != null) {
            getSupportActionBar().setTitle(getString(R.string.app_name) + " - " + user.getCoins() + " 金币");
        }
    }

    private void loadPosts() {
        posts = dataManager.getPosts();
        adapter.updatePosts(posts);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_user_center) {
            startActivity(new Intent(this, UserCenterActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
