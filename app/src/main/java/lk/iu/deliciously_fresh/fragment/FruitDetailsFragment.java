package lk.iu.deliciously_fresh.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.db.FruitDatabaseHelper;
import lk.iu.deliciously_fresh.model.Fruit;
import lk.iu.deliciously_fresh.util.ImageLoader;

public class FruitDetailsFragment extends Fragment {
    private static final String TAG = "FruitDetailsFragment";

    private ImageView      ivHero;
    private ImageView      ivFavouriteIcon;
    private TextView       tvCategoryBadge, tvStockBadge;
    private TextView       tvFruitTitle;
    private RatingBar      rbRating;
    private TextView       tvRatingValue, tvRatingCount;
    private TextView       tvFruitPrice;
    private TextView       tvFruitDescription;
    private LinearLayout   llAttributesContainer;
    private TextView       tvQuantity, tvTotalPrice;
    private View           btnDecreaseQty, btnIncreaseQty;
    private View           btnBack, btnFavourite;
    private MaterialButton btnAddToCart;


    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;
    private FruitDatabaseHelper sqliteHelper;
    private ExecutorService imageExecutor;

    private String fruitId;
    private Fruit  currentFruit;
    private int    quantity = 1;

    public FruitDetailsFragment() { }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fruit_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        if (getArguments() != null) {
            fruitId = getArguments().getString("fruitId", "");
        }

        bindViews(view);
        setupBackButton();
        setupQuantityStepper();
        setUiLoading(true);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        sqliteHelper = new FruitDatabaseHelper(requireContext());
        imageExecutor = Executors.newFixedThreadPool(2);

        loadFruitDetails();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FruitDatabaseHelper helperToClose = sqliteHelper;
        sqliteHelper = null;
        ExecutorService execToShutdown = imageExecutor;
        imageExecutor = null;

        if (execToShutdown != null) execToShutdown.shutdownNow();
        if (helperToClose != null) helperToClose.close();
    }


    private void bindViews(View v) {
        ivHero                = v.findViewById(R.id.iv_fruit_hero);
        ivFavouriteIcon       = v.findViewById(R.id.iv_favourite_icon);
        tvCategoryBadge       = v.findViewById(R.id.tv_category_badge);
        tvStockBadge          = v.findViewById(R.id.tv_stock_badge);
        tvFruitTitle          = v.findViewById(R.id.tv_fruit_title);
        rbRating              = v.findViewById(R.id.rb_fruit_rating);
        tvRatingValue         = v.findViewById(R.id.tv_rating_value);
        tvRatingCount         = v.findViewById(R.id.tv_rating_count);
        tvFruitPrice          = v.findViewById(R.id.tv_fruit_price);
        tvFruitDescription    = v.findViewById(R.id.tv_fruit_description);
        llAttributesContainer = v.findViewById(R.id.ll_attributes_container);
        tvQuantity            = v.findViewById(R.id.tv_quantity);
        tvTotalPrice          = v.findViewById(R.id.tv_total_price);
        btnDecreaseQty        = v.findViewById(R.id.btn_decrease_qty);
        btnIncreaseQty        = v.findViewById(R.id.btn_increase_qty);
        btnBack               = v.findViewById(R.id.btn_back);
        btnFavourite          = v.findViewById(R.id.btn_favourite);
        btnAddToCart          = v.findViewById(R.id.btn_add_to_cart);
    }

    private void setupBackButton() {
        btnBack.setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());
    }

    private void setupQuantityStepper() {
        btnDecreaseQty.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice();
            }
        });

        btnIncreaseQty.setOnClickListener(v -> {
            if (currentFruit != null && quantity < currentFruit.getStockCount()) {
                quantity++;
                tvQuantity.setText(String.valueOf(quantity));
                updateTotalPrice();
            } else {
                Toast.makeText(requireContext(),
                        "Maximum available stock reached", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateTotalPrice() {
        if (currentFruit == null) return;
        double total = currentFruit.getPrice() * quantity;
        tvTotalPrice.setText(
                String.format(Locale.getDefault(), "LKR %.2f", total));
    }


    @SuppressWarnings("unchecked")
    private void loadFruitDetails() {
        if (fruitId == null || fruitId.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid product", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("fruit")
                .document(fruitId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded()) return;

                    if (doc == null || !doc.exists()) {
                        setUiLoading(false);
                        Toast.makeText(requireContext(),
                                "Product not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String  title       = doc.getString("title");
                    String  description = doc.getString("description");
                    String  categoryId  = doc.getString("categoryId");
                    double  price       = doc.getDouble("price")    != null
                            ? doc.getDouble("price")    : 0.0;
                    int     stockCount  = doc.getLong("stockCount") != null
                            ? doc.getLong("stockCount").intValue() : 0;
                    boolean status      = Boolean.TRUE.equals(doc.getBoolean("status"));
                    float   rating      = doc.getDouble("rating")   != null
                            ? doc.getDouble("rating").floatValue() : 0f;


                    List<Fruit.Attribute> attrs = new ArrayList<>();
                    List<Map<String, Object>> rawAttrs =
                            (List<Map<String, Object>>) doc.get("attributes");
                    if (rawAttrs != null) {
                        for (Map<String, Object> m : rawAttrs) {
                            attrs.add(Fruit.Attribute.builder()
                                    .name(safeStr(m.get("name")))
                                    .type(safeStr(m.get("type")))
                                    .quality(safeStr(m.get("quality")))
                                    .build());
                        }
                    }


                    String imageUrl = doc.getString("image");
                    if (imageUrl == null || imageUrl.isEmpty()) {
                        imageUrl = doc.getString("imageUrl");
                    }

                    String cachedImage = null;
                    if (sqliteHelper != null) {
                        cachedImage = sqliteHelper.getImageUrl(fruitId);
                    }

                    currentFruit = Fruit.builder()
                            .fruitId(fruitId)
                            .title(title)
                            .description(description)
                            .price(price)
                            .categoryId(categoryId)
                            .stockCount(stockCount)
                            .status(status)
                            .rating(rating)
                            .attributes(attrs)
                            .build();
                    currentFruit.setImageUrl(
                            cachedImage != null && !cachedImage.isEmpty()
                                    ? cachedImage
                                    : imageUrl
                    );

                    populateUi(currentFruit);
                    loadCategoryName(categoryId);
                    setUiLoading(false);

                    if ((cachedImage == null || cachedImage.isEmpty())
                            && imageUrl != null
                            && !imageUrl.isEmpty()
                            && imageExecutor != null) {
                        final String originalUrl = imageUrl;
                        final String fruitIdFinal = fruitId;
                        try {
                            imageExecutor.submit(() -> {

                                if (ImageLoader.looksLikeBase64(originalUrl)) {
                                    if (sqliteHelper == null) return;
                                    sqliteHelper.upsertImage(fruitIdFinal, originalUrl);
                                    return;
                                }

                                if (!(originalUrl.startsWith("http://") || originalUrl.startsWith("https://"))) {
                                    return;
                                }

                                String base64 = ImageLoader.downloadUrlToBase64(originalUrl);
                                if (base64 == null || base64.isEmpty()) return;
                                if (sqliteHelper == null) return;
                                sqliteHelper.upsertImage(fruitIdFinal, base64);

                                if (!isAdded()) return;
                                requireActivity().runOnUiThread(() -> {
                                    if (!isAdded()) return;
                                    if (currentFruit == null) return;
                                    currentFruit.setImageUrl(base64);
                                    ImageLoader.load(requireContext(), base64, ivHero);
                                });
                            });
                        } catch (Exception ignored) {

                        }
                    }
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    setUiLoading(false);
                    Toast.makeText(requireContext(),
                            "Failed to load product: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void loadCategoryName(String categoryId) {
        if (categoryId == null || categoryId.isEmpty()) return;

        db.collection("categories")
                .whereEqualTo("categoryId", categoryId)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded() || querySnapshot == null || querySnapshot.isEmpty()) return;
                    String name = querySnapshot.getDocuments().get(0).getString("name");
                    if (name != null) {
                        tvCategoryBadge.setText(name.toUpperCase(Locale.getDefault()));
                    }
                });
    }


    private void populateUi(@NonNull Fruit fruit) {

        ImageLoader.load(requireContext(), fruit.getImageUrl(), ivHero);

        tvFruitTitle.setText(fruit.getTitle());

        rbRating.setRating(fruit.getRating());
        tvRatingValue.setText(
                String.format(Locale.getDefault(), "%.1f", fruit.getRating()));
        int approxReviews = (int) (fruit.getRating() * 20);
        tvRatingCount.setText(
                String.format(Locale.getDefault(), "(%d reviews)", approxReviews));

        tvFruitPrice.setText(
                String.format(Locale.getDefault(), "LKR %.2f", fruit.getPrice()));

        applyStockStatus(fruit.getStockCount());

        String desc = fruit.getDescription();
        tvFruitDescription.setText(
                (desc != null && !desc.isEmpty()) ? desc : "No description available.");

        tvTotalPrice.setText(
                String.format(Locale.getDefault(), "LKR %.2f", fruit.getPrice()));

        buildAttributeChips(fruit.getAttributes());

        btnAddToCart.setOnClickListener(v -> {
            if (currentFruit == null) return;
            addToCart(currentFruit.getFruitId(), quantity);
        });

        syncFavouriteIconState(fruit.getFruitId());
        btnFavourite.setOnClickListener(v -> toggleWishlist(fruit));
    }

    private void syncFavouriteIconState(@NonNull String fruitIdToCheck) {
        if (ivFavouriteIcon == null) return;
        FirebaseUser user = firebaseAuth != null ? firebaseAuth.getCurrentUser() : null;
        if (user == null) {
            ivFavouriteIcon.setImageResource(R.drawable.ic_favorite_border);
            return;
        }

        db.collection("users")
                .document(user.getUid())
                .collection("wishlist")
                .document(fruitIdToCheck)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!isAdded() || ivFavouriteIcon == null) return;
                    ivFavouriteIcon.setImageResource(doc != null && doc.exists()
                            ? R.drawable.favorite
                            : R.drawable.ic_favorite_border);
                });
    }

    private void toggleWishlist(@NonNull Fruit fruit) {
        if (ivFavouriteIcon == null) return;
        FirebaseUser user = firebaseAuth != null ? firebaseAuth.getCurrentUser() : null;
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in to use wishlist", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference wishRef = db.collection("users")
                .document(user.getUid())
                .collection("wishlist")
                .document(fruit.getFruitId());

        wishRef.get().addOnSuccessListener(doc -> {
            if (!isAdded() || ivFavouriteIcon == null) return;
            boolean exists = doc != null && doc.exists();
            if (exists) {
                wishRef.delete().addOnSuccessListener(aVoid -> {
                    if (!isAdded() || ivFavouriteIcon == null) return;
                    ivFavouriteIcon.setImageResource(R.drawable.ic_favorite_border);
                    Toast.makeText(requireContext(), "Removed from wishlist", Toast.LENGTH_SHORT).show();
                });
            } else {
                java.util.Map<String, Object> data = new java.util.HashMap<>();
                data.put("fruitId", fruit.getFruitId());
                data.put("title", fruit.getTitle());
                data.put("image", fruit.getImageUrl());
                data.put("price", fruit.getPrice());
                data.put("rating", fruit.getRating());
                data.put("stockCount", fruit.getStockCount());
                data.put("addedAt", FieldValue.serverTimestamp());

                wishRef.set(data).addOnSuccessListener(aVoid -> {
                    if (!isAdded() || ivFavouriteIcon == null) return;
                    ivFavouriteIcon.setImageResource(R.drawable.favorite);
                    Toast.makeText(requireContext(), "Added to wishlist", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void addToCart(@NonNull String fruitIdToAdd, int qtyToAdd) {
        FirebaseUser user = firebaseAuth != null ? firebaseAuth.getCurrentUser() : null;
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in to add to cart", Toast.LENGTH_SHORT).show();
            return;
        }
        if (qtyToAdd <= 0) return;

        DocumentReference fruitRef = db.collection("fruit").document(fruitIdToAdd);
        DocumentReference cartItemRef = db.collection("users")
                .document(user.getUid())
                .collection("cart")
                .document(fruitIdToAdd);

        setUiLoading(true);

        db.runTransaction(transaction -> {
            DocumentSnapshot fruitDoc = transaction.get(fruitRef);
            if (fruitDoc == null || !fruitDoc.exists()) {
                return null;
            }

            DocumentSnapshot existingCartDoc = transaction.get(cartItemRef);
            int existingQty = 0;
            Object existingQtyObj = existingCartDoc != null ? existingCartDoc.get("quantity") : null;
            if (existingQtyObj instanceof Number) {
                existingQty = ((Number) existingQtyObj).intValue();
            }

            int stockCount = 0;
            Object stockObj = fruitDoc.get("stockCount");
            if (stockObj instanceof Number) {
                stockCount = ((Number) stockObj).intValue();
            }

            int newQty = existingQty + qtyToAdd;
            if (stockCount > 0 && newQty > stockCount) {
                newQty = stockCount;
            }
            if (newQty <= 0) {
                transaction.delete(cartItemRef);
                return null;
            }

            String title = fruitDoc.getString("title");
            // Your Firestore field is `image` (fallback to `imageUrl` for older data)
            String imageUrl = fruitDoc.getString("image");
            if (imageUrl == null || imageUrl.isEmpty()) {
                imageUrl = fruitDoc.getString("imageUrl");
            }

            double price = 0.0;
            Object priceObj = fruitDoc.get("price");
            if (priceObj instanceof Number) {
                price = ((Number) priceObj).doubleValue();
            }

            Map<String, Object> cartData = new HashMap<>();
            cartData.put("fruitId", fruitIdToAdd);
            cartData.put("title", title);
            cartData.put("image", imageUrl);
            cartData.put("price", price);
            cartData.put("quantity", newQty);
            cartData.put("updatedAt", FieldValue.serverTimestamp());

            transaction.set(cartItemRef, cartData);
            return null;
        }).addOnSuccessListener(aVoid -> {
            if (!isAdded()) return;
            setUiLoading(false);
            Toast.makeText(requireContext(), "Added to cart", Toast.LENGTH_SHORT).show();
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            setUiLoading(false);
            String msg = (e != null && e.getMessage() != null) ? e.getMessage() : "unknown error";
            Log.e(TAG, "addToCart failed", e);
            Toast.makeText(requireContext(), "Failed to add to cart: " + msg, Toast.LENGTH_LONG).show();
        });
    }

    private void applyStockStatus(int stock) {
        if (stock > 10) {
            tvStockBadge.setText("In Stock: " + stock);
            tvStockBadge.setTextColor(
                    requireContext().getColor(android.R.color.holo_green_dark));
        } else if (stock > 0) {
            tvStockBadge.setText("Low Stock: " + stock);
            tvStockBadge.setTextColor(
                    requireContext().getColor(android.R.color.holo_orange_dark));
        } else {
            tvStockBadge.setText("Out of Stock");
            tvStockBadge.setTextColor(
                    requireContext().getColor(android.R.color.holo_red_dark));
            btnAddToCart.setEnabled(false);
            btnAddToCart.setAlpha(0.5f);
        }
    }

    private void buildAttributeChips(@Nullable List<Fruit.Attribute> attributes) {
        llAttributesContainer.removeAllViews();   // remove design-time placeholders

        if (attributes == null || attributes.isEmpty()) return;

        LayoutInflater inflater = LayoutInflater.from(requireContext());
        for (Fruit.Attribute attr : attributes) {
            View chip = inflater.inflate(
                    R.layout.item_attribute, llAttributesContainer, false);

            TextView tvName  = chip.findViewById(R.id.tv_attr_name);
            TextView tvValue = chip.findViewById(R.id.tv_attr_value);

            tvName.setText(attr.getName() != null ? attr.getName() : "");

            String value = attr.getQuality() != null
                    ? attr.getQuality()
                    : (attr.getType() != null ? attr.getType() : "—");
            tvValue.setText(value);

            llAttributesContainer.addView(chip);
        }
    }

    private void setUiLoading(boolean loading) {
        float alpha = loading ? 0.25f : 1f;
        ivHero.setAlpha(alpha);
        tvFruitTitle.setAlpha(alpha);
        tvFruitPrice.setAlpha(alpha);
        tvFruitDescription.setAlpha(alpha);
        rbRating.setAlpha(alpha);
        btnAddToCart.setAlpha(loading ? 0.4f : 1f);
        btnAddToCart.setEnabled(!loading);
    }

    private static String safeStr(Object value) {
        if (value == null) return null;
        if (value instanceof String) return (String) value;
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            return list.isEmpty() ? "" : String.valueOf(list.get(0));
        }
        return String.valueOf(value);
    }
}
