package lk.iu.deliciously_fresh.activity;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.Manifest;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import android.os.Build;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.databinding.ActivityMainBinding;
import lk.iu.deliciously_fresh.databinding.SideNavHeaderBinding;
import lk.iu.deliciously_fresh.fragment.AboutFragment;
import lk.iu.deliciously_fresh.fragment.CartFragment;
import lk.iu.deliciously_fresh.fragment.CategoryFragment;
import lk.iu.deliciously_fresh.fragment.HomeFragment;
import lk.iu.deliciously_fresh.fragment.MessageFragment;
import lk.iu.deliciously_fresh.fragment.OrdersFragment;
import lk.iu.deliciously_fresh.fragment.ProfileFragment;
import lk.iu.deliciously_fresh.fragment.SettingsFragment;
import lk.iu.deliciously_fresh.fragment.WishlistFragment;
import lk.iu.deliciously_fresh.model.User;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, BottomNavigationView.OnItemSelectedListener {

    private ActivityMainBinding binding;
    private SideNavHeaderBinding sideNavHeaderBinding;
    private DrawerLayout drawerLayout;
    private MaterialToolbar toolbar;
    private NavigationView navigationView;
    private BottomNavigationView bottomNavigationView;
    
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View headerView = binding.sideNavigationView.getHeaderView(0);

        sideNavHeaderBinding = SideNavHeaderBinding.bind(headerView);

        drawerLayout = binding.main;
        toolbar = binding.toolbar;
        navigationView = binding.sideNavigationView;
        bottomNavigationView = binding.bottomNavigationView;

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(toggle);

        toggle.syncState();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });


        navigationView.setNavigationItemSelectedListener(this);
        bottomNavigationView.setOnItemSelectedListener(this);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);
        }

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();


        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(ds -> {

                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            sideNavHeaderBinding.navHeaderName.setText(user.getName());
                            sideNavHeaderBinding.navHeaderEmail.setText(user.getEmail());


                            SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                            String imagePath = prefs.getString("profile_image", null);

                            loadHeaderProfileImage();


                        } else {
                            Log.e("Firestore", "Document does not exist");
                        }

                    }).addOnFailureListener(e -> {
                        Log.e("Firestore", "Error: " + e.getMessage());
                        Log.e("Firestore", "Error: " + e.getMessage());
                    });

            subscribeToNotifications(currentUser.getUid());


            navigationView.getMenu().findItem(R.id.side_nav_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.side_nav_profile).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_orders).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_wishlist).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_cart).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_message).setVisible(true);
            navigationView.getMenu().findItem(R.id.side_nav_logout).setVisible(true);


            sideNavHeaderBinding.navHeaderAvatar.setOnClickListener(v -> {

                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);

                activityResultLauncher.launch(intent);
            });

        }
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("FCM_TOKEN", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();

                        // Log and copy to clipboard
                        Log.d("FCM_TOKEN", "Token: " + token);
                        
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("FCM Token", token);
                        if (clipboard != null) {
                            clipboard.setPrimaryClip(clip);
                        }
                        
                        Toast.makeText(MainActivity.this, "FCM Token copied to clipboard!", Toast.LENGTH_LONG).show();
                    }
                });

    }
    
    private void subscribeToNotifications(String userId) {
        FirebaseMessaging.getInstance().subscribeToTopic("broadcast");
        if (userId != null) {
            FirebaseMessaging.getInstance().subscribeToTopic("user_" + userId);
        }
    }

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (!isGranted) {
                    Toast.makeText(this, "Notifications disabled. You might miss important updates.", Toast.LENGTH_LONG).show();
                }
            });

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK
                        && result.getData() != null
                        && result.getData().getData() != null) {

                    Uri uri = result.getData().getData();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

                        String filename = "profile_" + System.currentTimeMillis() + ".png";
                        File file = new File(getFilesDir(), filename);

                        FileOutputStream fos = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                        fos.flush();
                        fos.close();

                        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
                        if (currentUser != null) {
                            prefs.edit().putString("profile_image_" + currentUser.getUid(), file.getAbsolutePath()).apply();
                        }

                        Glide.with(MainActivity.this)
                                .load(file)
                                .placeholder(R.drawable.profile)
                                .circleCrop()
                                .into(sideNavHeaderBinding.navHeaderAvatar);

                        Toast.makeText(this, "Profile image saved!", Toast.LENGTH_SHORT).show();

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
    );


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        Menu navMenu = navigationView.getMenu();
        Menu bottomNavMenu = bottomNavigationView.getMenu();

        for (int i = 0; i < navMenu.size(); i++) {
            navMenu.getItem(i).setChecked(false);
        }

        for (int i = 0; i < bottomNavMenu.size(); i++) {
            bottomNavMenu.getItem(i).setChecked(false);
        }


        if (itemId == R.id.side_nav_home || itemId == R.id.bottom_nav_home) {
            loadFragment(new HomeFragment());
            navigationView.getMenu().findItem(R.id.side_nav_home).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_home).setChecked(true);

        } else if (itemId == R.id.side_nav_profile || itemId == R.id.bottom_nav_profile) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            loadFragment(new ProfileFragment());
            navigationView.getMenu().findItem(R.id.side_nav_profile).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_profile).setChecked(true);

        } else if (itemId == R.id.side_nav_orders) {
            loadFragment(new OrdersFragment());
            navigationView.getMenu().findItem(R.id.side_nav_orders).setChecked(true);

        } else if (itemId == R.id.side_nav_wishlist) {
            loadFragment(new WishlistFragment());
            navigationView.getMenu().findItem(R.id.side_nav_wishlist).setChecked(true);

        } else if (itemId == R.id.side_nav_cart || itemId == R.id.bottom_nav_cart) {
            if (firebaseAuth.getCurrentUser() == null) {
                Intent intent = new Intent(MainActivity.this, SignInActivity.class);
                startActivity(intent);
                finish();
            }
            loadFragment(new CartFragment());
            navigationView.getMenu().findItem(R.id.side_nav_cart).setChecked(true);
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_cart).setChecked(true);

        } else if (itemId == R.id.side_nav_message) {
            loadFragment(new MessageFragment());
            navigationView.getMenu().findItem(R.id.side_nav_message).setChecked(true);

        } else if (itemId == R.id.side_nav_settings) {
            loadFragment(new SettingsFragment());
            navigationView.getMenu().findItem(R.id.side_nav_settings).setChecked(true);

        } else if (itemId == R.id.bottom_nav_category) {
            loadFragment(new CategoryFragment());
            bottomNavigationView.getMenu().findItem(R.id.bottom_nav_category).setChecked(true);

        } else if (itemId == R.id.side_nav_login) {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);

        } else if (itemId == R.id.side_nav_logout) {
            firebaseAuth.signOut();
            loadFragment(new HomeFragment());
            navigationView.getMenu().clear();
            navigationView.inflateMenu(R.menu.side_nav_menu);

            //View headerView = navigationView.getHeaderView(0);

            navigationView.removeHeaderView(sideNavHeaderBinding.getRoot());
            navigationView.inflateHeaderView(R.layout.side_nav_header);

        } else if (itemId == R.id.side_nav_about) {
            loadFragment(new AboutFragment());
            navigationView.getMenu().findItem(R.id.side_nav_about).setChecked(true);

        }

        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    private void loadHeaderProfileImage() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Glide.with(MainActivity.this)
                    .load(R.drawable.profile)
                    .placeholder(R.drawable.profile)
                    .circleCrop()
                    .into(sideNavHeaderBinding.navHeaderAvatar);
            return;
        }

        SharedPreferences prefs = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String imagePath = prefs.getString("profile_image_" + currentUser.getUid(), null);

        if (imagePath != null && !imagePath.isEmpty()) {
            File file = new File(imagePath);

            if (file.exists()) {
                Glide.with(MainActivity.this)
                        .load(file)
                        .placeholder(R.drawable.profile)
                        .circleCrop()
                        .into(sideNavHeaderBinding.navHeaderAvatar);
                return;
            }
        }

        Glide.with(MainActivity.this)
                .load(R.drawable.profile)
                .placeholder(R.drawable.profile)
                .circleCrop()
                .into(sideNavHeaderBinding.navHeaderAvatar);
    }
    @Override
    protected void onResume() {
        super.onResume();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            firebaseFirestore.collection("users")
                    .document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(ds -> {
                        if (ds.exists()) {
                            User user = ds.toObject(User.class);
                            if (user != null) {
                                sideNavHeaderBinding.navHeaderName.setText(user.getName());
                                sideNavHeaderBinding.navHeaderEmail.setText(user.getEmail());
                            }
                        }
                        loadHeaderProfileImage();
                    })
                    .addOnFailureListener(e -> loadHeaderProfileImage());
        } else {
            Glide.with(this)
                    .load(R.drawable.profile)
                    .circleCrop()
                    .into(sideNavHeaderBinding.navHeaderAvatar);
        }
    }

}