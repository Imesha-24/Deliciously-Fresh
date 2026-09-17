package lk.iu.deliciously_fresh.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.databinding.ActivitySignInBinding;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;
    private FirebaseAuth firebaseAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignInBinding.inflate(getLayoutInflater());

        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();

        binding.signInBtnSignup.setOnClickListener(view -> {
            Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
            startActivity(intent);
            finish();
        });


        binding.signInBtnSignIn.setOnClickListener(view -> {

            String email = binding.signInInputEmail.getText().toString().trim();
            String password = binding.signInInputPassword.getText().toString().trim();

            if (email.isEmpty()) {
                binding.signInInputEmail.setError("Email is required");
                binding.signInInputEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.signInInputEmail.setError("Enter valid email");
                binding.signInInputEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.signInInputPassword.setError("Password is required");
                binding.signInInputPassword.requestFocus();
                return;
            }


            firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        updateUI(firebaseAuth.getCurrentUser());
                    } else {
                        Toast.makeText(SignInActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });


    }

    private void updateUI(FirebaseUser user) {
        Intent intent = new Intent(SignInActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
