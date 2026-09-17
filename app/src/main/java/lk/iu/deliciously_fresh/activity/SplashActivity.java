package lk.iu.deliciously_fresh.activity;
//
//import android.content.Intent;
//import android.os.Build;
//import android.os.Bundle;
//import android.os.Handler;
//import android.os.Looper;
//import android.view.View;
//import android.view.WindowInsets;
//import android.view.WindowInsetsController;
//import android.view.WindowManager;
//import android.widget.ImageView;
//
//import androidx.activity.EdgeToEdge;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.core.graphics.Insets;
//import androidx.core.view.ViewCompat;
//import androidx.core.view.WindowInsetsCompat;
//
//import com.bumptech.glide.Glide;
//
//import lk.iu.deliciously_fresh.R;
//
//public class SplashActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        EdgeToEdge.enable(this);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            getWindow().setDecorFitsSystemWindows(false);
//
//            WindowInsetsController controller = getWindow().getInsetsController();
//            if (controller != null) {
//                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
//            }
//
//        } else {
//            getWindow().setFlags(
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                    WindowManager.LayoutParams.FLAG_FULLSCREEN
//            );
//        }
//
//        setContentView(R.layout.activity_splash);
//
//        ImageView imageView = findViewById(R.id.splashLogo);
//        ImageView imageView2 = findViewById(R.id.splashLogo1);
//
//        Glide.with(this)
//                .asBitmap()
//                .load(R.drawable.fruits)
//                .override(1000)
//                .into(imageView);
//
//        Glide.with(this)
//                .asBitmap()
//                .load(R.drawable.fruits2)
//                .override(1200)
//                .into(imageView2);
//
//
//        new Handler(Looper.getMainLooper())
//                .postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        Intent intent = new Intent(SplashActivity.this, SignUpActivity.class);
//                        startActivity(intent);
//                        finish();
//                    }
//                }, 3000);
//
//
//    }
//}

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.activity.SignUpActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);

            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars() | WindowInsets.Type.navigationBars());
            }

        } else {
            getWindow().setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
            );
        }

        setContentView(R.layout.activity_splash);

        ImageView imageView = findViewById(R.id.splashLogo);

        Glide.with(this)
                .asBitmap()
                .load(R.drawable.deliciouslyfresh)
                .override(600)
                .into(imageView);


        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.splashProgressBar).setVisibility(View.VISIBLE);
                    }
                }, 1000);


        new Handler(Looper.getMainLooper())
                .postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.splashProgressBar).setVisibility(View.INVISIBLE);
                        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, 5000);


    }
}