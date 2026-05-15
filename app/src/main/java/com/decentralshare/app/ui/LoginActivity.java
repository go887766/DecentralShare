package com.decentralshare.app.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.decentralshare.app.R;
import com.decentralshare.app.data.DataManager;
import com.decentralshare.app.databinding.ActivityLoginBinding;
import com.decentralshare.app.model.User;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private DataManager dataManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        dataManager = DataManager.getInstance(this);

        if (dataManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        binding.tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

    private void login() {
        String address = binding.etAddress.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(address)) {
            binding.etAddress.setError("请输入用户地址");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            binding.etPassword.setError("请输入密码");
            return;
        }

        User user = dataManager.login(address, password);
        if (user != null) {
            Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Toast.makeText(this, "地址或密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
