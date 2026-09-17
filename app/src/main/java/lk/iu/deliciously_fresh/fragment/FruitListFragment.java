package lk.iu.deliciously_fresh.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.adapter.FruitAdapter;
import lk.iu.deliciously_fresh.db.FruitDatabaseHelper;
import lk.iu.deliciously_fresh.model.Fruit;
import lk.iu.deliciously_fresh.util.ImageLoader;

public class FruitListFragment extends Fragment {

    private RecyclerView rvFruits;
    private ProgressBar progressBar;
    private TextView tvEmpty, tvCategoryTitle, tvFruitCount;
    private FruitAdapter        adapter;
    private List<Fruit>       fruitList;
    private FirebaseFirestore db;
    private FruitDatabaseHelper sqliteHelper;
    private ExecutorService imageExecutor;

    private String categoryId;
    private String categoryName;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fruit_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            categoryId   = getArguments().getString("categoryId", "");
            categoryName = getArguments().getString("categoryName", "Fruits");
        }

        rvFruits        = view.findViewById(R.id.rv_fruits);
        progressBar     = view.findViewById(R.id.progress_fruits);
        tvEmpty         = view.findViewById(R.id.tv_empty_fruits);
        tvCategoryTitle = view.findViewById(R.id.tv_category_title);
        tvFruitCount    = view.findViewById(R.id.tv_fruit_count);

        tvCategoryTitle.setText(categoryName);

        view.findViewById(R.id.btn_back).setOnClickListener(v ->
                requireActivity().getSupportFragmentManager().popBackStack());

        db        = FirebaseFirestore.getInstance();
        fruitList = new ArrayList<>();
        sqliteHelper = new FruitDatabaseHelper(requireContext());
        imageExecutor = Executors.newFixedThreadPool(3);

        adapter = new FruitAdapter(requireContext(), fruitList, fruit -> {
            FruitDetailsFragment detailFragment = new FruitDetailsFragment();
            Bundle args = new Bundle();
            args.putString("fruitId", fruit.getFruitId());
            detailFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvFruits.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvFruits.setAdapter(adapter);

        loadFruits();
    }

    @SuppressWarnings("unchecked")
    private void loadFruits() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("fruit")
                .whereEqualTo("categoryId", categoryId)
                .whereEqualTo("status", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    fruitList.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String docId       = doc.getId();
                        String title       = doc.getString("title");
                        String description = doc.getString("description");
                        double price       = doc.getDouble("price") != null
                                ? doc.getDouble("price") : 0;
                        int    stockCount  = doc.getLong("stockCount") != null
                                ? doc.getLong("stockCount").intValue() : 0;
                        boolean status     = Boolean.TRUE.equals(doc.getBoolean("status"));
                        float   rating     = doc.getDouble("rating") != null
                                ? doc.getDouble("rating").floatValue() : 0f;

                        List<Fruit.Attribute> attrs = new ArrayList<>();
                        List<Map<String,Object>> rawAttrs =
                                (List<Map<String,Object>>) doc.get("attributes");
                        if (rawAttrs != null) {
                            for (Map<String,Object> m : rawAttrs) {
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
                            cachedImage = sqliteHelper.getImageUrl(docId);
                        }

                        Fruit product = Fruit.builder()
                                .fruitId(docId)
                                .title(title)
                                .description(description)
                                .price(price)
                                .categoryId(categoryId)
                                .stockCount(stockCount)
                                .status(status)
                                .rating(rating)
                                .attributes(attrs)
                                .build();
                        product.setImageUrl(
                                cachedImage != null && !cachedImage.isEmpty()
                                        ? cachedImage
                                        : imageUrl
                        );

                        fruitList.add(product);

                        final int index = fruitList.size() - 1;
                        if ((cachedImage == null || cachedImage.isEmpty())
                                && imageUrl != null
                                && !imageUrl.isEmpty()) {
                            final String originalUrl = imageUrl;

                            if (ImageLoader.looksLikeBase64(originalUrl)) {
                                try {
                                    imageExecutor.submit(() -> {
                                        if (sqliteHelper == null) return;
                                        sqliteHelper.upsertImage(docId, originalUrl);
                                    });
                                } catch (Exception ignored) {
                                }
                            }

                            else if (originalUrl.startsWith("http://") || originalUrl.startsWith("https://")) {
                                try {
                                    imageExecutor.submit(() -> {
                                        String base64 = ImageLoader.downloadUrlToBase64(originalUrl);
                                        if (base64 == null || base64.isEmpty()) return;
                                        if (sqliteHelper == null) return;
                                        sqliteHelper.upsertImage(docId, base64);

                                        if (!isAdded()) return;
                                        requireActivity().runOnUiThread(() -> {
                                            if (!isAdded()) return;
                                            fruitList.get(index).setImageUrl(base64);
                                            adapter.notifyItemChanged(index);
                                        });
                                    });
                                } catch (Exception ignored) {
                                }
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    tvFruitCount.setText(fruitList.size() + " fruits available");

                    if (fruitList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Failed to load fruits");
                });
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