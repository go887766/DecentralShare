package com.decentralshare.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.decentralshare.app.R;
import com.decentralshare.app.data.DataManager;
import com.decentralshare.app.databinding.ActivityRegisterBinding;
import com.decentralshare.app.model.User;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = DataManager.getInstance(this);

        binding.btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register();
            }
        });

        binding.tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void register() {
        String nickname = binding.etNickname.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(nickname)) {
            binding.etNickname.setError("请输入昵称");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("请输入密码");
            return;
        }

        if (password.length() < 6) {
            binding.etPassword.setError("密码至少6位");
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("两次密码不一致");
            return;
        }

        User user = dataManager.registerUser(nickname, password);
        Toast.makeText(this, "注册成功！您的地址：" + user.getAddress(), Toast.LENGTH_LONG).show();
        
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
