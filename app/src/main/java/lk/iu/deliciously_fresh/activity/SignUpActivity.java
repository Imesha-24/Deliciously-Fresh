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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.databinding.ActivitySignUpBinding;
import lk.iu.deliciously_fresh.model.User;

public class SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        binding.signUpBtnSignIn.setOnClickListener(v -> {
            Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
            startActivity(intent);
            finish();
        });


        binding.signUpBtnSignup.setOnClickListener(view -> {

            String name = binding.signUpInputNAme.getText().toString().trim();
            String email = binding.signUpInputEmail.getText().toString().trim();
            String password = binding.signUpInputPassword.getText().toString().trim();
            String confirmPassword = binding.signUpInputConfirmPassword.getText().toString().trim();

            if (name.isEmpty()) {
                binding.signUpInputNAme.setError("Name is required");
                binding.signUpInputNAme.requestFocus();
                return;
            }

            if (email.isEmpty()) {
                binding.signUpInputEmail.setError("Email is required");
                binding.signUpInputEmail.requestFocus();
                return;
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.signUpInputEmail.setError("Enter valid email");
                binding.signUpInputEmail.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                binding.signUpInputPassword.setError("Password is required");
                binding.signUpInputPassword.requestFocus();
                return;
            }

            if (password.length() < 6) {
                binding.signUpInputPassword.setError("Password must be at least 6 characters");
                binding.signUpInputPassword.requestFocus();
                return;
            }

            if (!confirmPassword.equals(password)) {
                binding.signUpInputConfirmPassword.setError("Password and retype password must be the same");
                binding.signUpInputConfirmPassword.requestFocus();
                return;
            }


            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {

                        String uid = task.getResult().getUser().getUid();

                        User user = User.builder().uid(uid).name(name).email(email).build();

                        firebaseFirestore.collection("users").document(uid).set(user)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(getApplicationContext(), "Saved success", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(SignUpActivity.this, SignInActivity.class);
                                        startActivity(intent);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });


                    }
                }
            });


        });

    }
}