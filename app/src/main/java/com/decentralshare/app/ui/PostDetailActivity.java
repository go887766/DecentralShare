package com.decentralshare.app.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.decentralshare.app.R;
import com.decentralshare.app.adapter.CommentAdapter;
import com.decentralshare.app.data.DataManager;
import com.decentralshare.app.databinding.ActivityPostDetailBinding;
import com.decentralshare.app.model.Comment;
import com.decentralshare.app.model.Post;

public class PostDetailActivity extends AppCompatActivity {

    private ActivityPostDetailBinding binding;
    private DataManager dataManager;
    private Post post;
    private CommentAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = DataManager.getInstance(this);

        String postId = getIntent().getStringExtra("postId");
        post = dataManager.getPost(postId);

        if (post == null) {
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        initViews();
        loadComments();
    }

    private void initViews() {
        binding.tvAvatar.setText(post.getAuthorNickname().substring(0, 1).toUpperCase());
        binding.tvAuthor.setText(post.getAuthorNickname());
        binding.tvAddress.setText(post.getAuthorAddress().substring(0, 16) + "...");
        binding.tvTitle.setText(post.getTitle());
        binding.tvContent.setText(post.getContent());
        updateLikeButton();
        binding.tvLikeCount.setText(String.valueOf(post.getLikes()));
        binding.tvCommentCount.setText(String.valueOf(post.getComments().size()));

        binding.layoutLike.setOnClickListener(v -> {
            if (post.isLikedByMe()) {
                dataManager.unlikePost(post);
            } else {
                dataManager.likePost(post);
            }
            updateLikeButton();
            binding.tvLikeCount.setText(String.valueOf(post.getLikes()));
        });

        binding.btnSend.setOnClickListener(v -> {
            String content = binding.etComment.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                binding.etComment.setError("请输入评论内容");
                return;
            }
            Comment comment = dataManager.addComment(post, content);
            binding.etComment.setText("");
            loadComments();
            binding.tvCommentCount.setText(String.valueOf(post.getComments().size()));
            Toast.makeText(this, "评论成功，获得 2 金币", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateLikeButton() {
        binding.ivLike.setImageResource(post.isLikedByMe() ? 
            android.R.drawable.btn_star_big_on : 
            android.R.drawable.btn_star_big_off);
    }

    private void loadComments() {
        adapter = new CommentAdapter(this, post.getComments());
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
