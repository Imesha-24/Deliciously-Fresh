package lk.iu.deliciously_fresh.util;

import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import lk.iu.deliciously_fresh.R;

public class ImageLoader {

    private ImageLoader() {  }


    public static void load(Object context, String imageData, ImageView target) {
        if (imageData == null || imageData.isEmpty()) {
            target.setImageResource(R.drawable.placeholder_fruit);
            return;
        }

        if (looksLikeBase64(imageData)) {
            Bitmap bmp = decodeBitmap(imageData);
            if (bmp != null) {
                Glide.with(target.getContext())
                        .load(bmp)
                        .placeholder(R.drawable.placeholder_fruit)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .centerCrop()
                        .into(target);
            } else {
                target.setImageResource(R.drawable.placeholder_fruit);
            }
        } else {
            Glide.with(target.getContext())
                    .load(imageData)
                    .placeholder(R.drawable.placeholder_fruit)
                    .error(R.drawable.placeholder_fruit)
                    .centerCrop()
                    .into(target);
        }
    }

    public static void loadWithRadius(Object context, String imageData, ImageView target, int radius) {
        if (imageData == null || imageData.isEmpty()) {
            target.setImageResource(R.drawable.placeholder_fruit);
            return;
        }

        if (looksLikeBase64(imageData)) {
            Bitmap bmp = decodeBitmap(imageData);
            if (bmp != null) {
                Glide.with(target.getContext())
                        .load(bmp)
                        .placeholder(R.drawable.placeholder_fruit)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .centerCrop()
                        .transform(new com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
                        .into(target);
            } else {
                target.setImageResource(R.drawable.placeholder_fruit);
            }
        } else {
            Glide.with(target.getContext())
                    .load(imageData)
                    .placeholder(R.drawable.placeholder_fruit)
                    .error(R.drawable.placeholder_fruit)
                    .centerCrop()
                    .transform(new com.bumptech.glide.load.resource.bitmap.RoundedCorners(radius))
                    .into(target);
        }
    }

    public static boolean looksLikeBase64(String data) {
        if (data == null) return false;
        String s = data.trim();
        if (s.isEmpty()) return false;

        if (s.startsWith("data:image")) return true;

        if (s.startsWith("http://") || s.startsWith("https://") || s.startsWith("gs://")) {
            return false;
        }

        if (s.length() <= 100) return false;
        return s.matches("^[A-Za-z0-9+/=\\r\\n]+$");
    }

    private static Bitmap decodeBitmap(String base64Data) {
        try {
            String pure = base64Data;
            if (pure.contains(",")) {
                pure = pure.substring(pure.indexOf(',') + 1);
            }
            byte[] bytes = Base64.decode(pure.trim(), Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } catch (Exception e) {
            return null;
        }
    }

    public static String downloadUrlToBase64(String urlString) {
        if (urlString == null || urlString.isEmpty()) return null;
        if (!(urlString.startsWith("http://") || urlString.startsWith("https://"))) return null;

        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlString);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(20000);
            conn.setInstanceFollowRedirects(true);

            int code = conn.getResponseCode();
            if (code < 200 || code >= 300) return null;

            try (InputStream is = conn.getInputStream();
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    baos.write(buffer, 0, read);
                }
                byte[] bytes = baos.toByteArray();
                return Base64.encodeToString(bytes, Base64.NO_WRAP);
            }
        } catch (Exception e) {
            Log.w("ImageLoader", "downloadUrlToBase64 failed: " + e.getMessage());
            return null;
        } finally {
            if (conn != null) conn.disconnect();
        }
    }
}
