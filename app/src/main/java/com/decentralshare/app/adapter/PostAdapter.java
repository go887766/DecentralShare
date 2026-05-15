package com.decentralshare.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.decentralshare.app.R;
import com.decentralshare.app.data.DataManager;
import com.decentralshare.app.model.Post;
import com.decentralshare.app.ui.PostDetailActivity;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> posts;
    private Context context;
    private DataManager dataManager;
    private OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onLikeChanged();
    }

    public PostAdapter(Context context, List<Post> posts, OnPostInteractionListener listener) {
        this.context = context;
        this.posts = posts;
        this.dataManager = DataManager.getInstance(context);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(List<Post> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        TextView tvAvatar, tvAuthor, tvAddress, tvTitle, tvContent, tvLikeCount, tvCommentCount;
        ImageView ivLike;
        LinearLayout layoutLike, layoutComment, layoutImages;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvAuthor = itemView.findViewById(R.id.tvAuthor);
            tvAddress = itemView.findViewById(R.id.tvAddress);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvContent = itemView.findViewById(R.id.tvContent);
            tvLikeCount = itemView.findViewById(R.id.tvLikeCount);
            tvCommentCount = itemView.findViewById(R.id.tvCommentCount);
            ivLike = itemView.findViewById(R.id.ivLike);
            layoutLike = itemView.findViewById(R.id.layoutLike);
            layoutComment = itemView.findViewById(R.id.layoutComment);
            layoutImages = itemView.findViewById(R.id.layoutImages);
        }

        void bind(Post post) {
            tvAvatar.setText(post.getAuthorNickname().substring(0, 1).toUpperCase());
            tvAuthor.setText(post.getAuthorNickname());
            tvAddress.setText(post.getAuthorAddress().substring(0, 12) + "...");
            tvTitle.setText(post.getTitle());
            tvContent.setText(post.getContent());
            tvLikeCount.setText(String.valueOf(post.getLikes()));
            tvCommentCount.setText(String.valueOf(post.getComments().size()));

            ivLike.setImageResource(post.isLikedByMe() ? 
                android.R.drawable.btn_star_big_on : 
                android.R.drawable.btn_star_big_off);

            layoutImages.removeAllViews();

            layoutLike.setOnClickListener(v -> {
                if (post.isLikedByMe()) {
                    dataManager.unlikePost(post);
                } else {
                    dataManager.likePost(post);
                }
                notifyItemChanged(getAdapterPosition());
                if (listener != null) {
                    listener.onLikeChanged();
                }
            });

            layoutComment.setOnClickListener(v -> {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", post.getId());
                context.startActivity(intent);
            });

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PostDetailActivity.class);
                intent.putExtra("postId", post.getId());
                context.startActivity(intent);
            });
        }
    }
}
