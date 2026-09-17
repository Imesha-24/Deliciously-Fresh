package lk.iu.deliciously_fresh.fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.databinding.FragmentProfileBinding;
import lk.iu.deliciously_fresh.model.User;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String savedImagePath;

    public ProfileFragment() {
    }

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {

                    Uri uri = result.getData().getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().getContentResolver(), uri);

                        String fileName = "profile_" + System.currentTimeMillis() + ".png";
                        File file = new File(requireActivity().getFilesDir(), fileName);

                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();
                        fos.close();

                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        if (currentUser == null) {
                            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        savedImagePath = file.getAbsolutePath();

                        SharedPreferences prefs = requireActivity()
                                .getSharedPreferences("user_prefs", Activity.MODE_PRIVATE);
                        prefs.edit()
                                .putString("profile_image_" + currentUser.getUid(), savedImagePath)
                                .apply();

                        Glide.with(requireContext())
                                .load(file)
                                .placeholder(R.drawable.profile)
                                .circleCrop()
                                .into(binding.profileAvatar);

                        Toast.makeText(requireContext(), "Profile image updated", Toast.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(requireContext(), "Failed to save image", Toast.LENGTH_SHORT).show();
                    }
                }
            });

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        loadProfileData();
        loadLocalProfileImage();

        binding.profileBtnChangePhoto.setOnClickListener(v -> openGallery());
        binding.profileBtnSave.setOnClickListener(v -> saveProfileData());
    }

    private void loadProfileData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseFirestore.collection("users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);

                        if (user != null) {
                            binding.profileInputName.setText(user.getName());
                            binding.profileInputEmail.setText(user.getEmail());
                            binding.profileInputUid.setText(user.getUid());
                            binding.profileTxtName.setText(user.getName());
                        }
                    } else {
                        Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(
                        e -> Toast.makeText(requireContext(), "Failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void loadLocalProfileImage() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Glide.with(requireContext())
                    .load(R.drawable.profile)
                    .circleCrop()
                    .into(binding.profileAvatar);
            return;
        }

        SharedPreferences prefs = requireActivity()
                .getSharedPreferences("user_prefs", Activity.MODE_PRIVATE);

        savedImagePath = prefs.getString("profile_image_" + currentUser.getUid(), null);

        if (savedImagePath != null && !savedImagePath.isEmpty()) {
            File file = new File(savedImagePath);

            if (file.exists()) {
                Glide.with(requireContext())
                        .load(file)
                        .placeholder(R.drawable.profile)
                        .circleCrop()
                        .into(binding.profileAvatar);
                return;
            }
        }

        Glide.with(requireContext())
                .load(R.drawable.profile)
                .circleCrop()
                .into(binding.profileAvatar);
    }

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        imagePickerLauncher.launch(intent);
    }

    private void saveProfileData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = binding.profileInputName.getText() != null
                ? binding.profileInputName.getText().toString().trim()
                : "";

        if (TextUtils.isEmpty(name)) {
            binding.profileLayoutName.setError("Name is required");
            return;
        } else {
            binding.profileLayoutName.setError(null);
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);

        firebaseFirestore.collection("users")
                .document(currentUser.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    binding.profileTxtName.setText(name);
                    Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast
                        .makeText(requireContext(), "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadLocalProfileImage();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
