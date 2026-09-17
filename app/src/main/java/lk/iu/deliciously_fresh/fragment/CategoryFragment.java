package lk.iu.deliciously_fresh.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.adapter.CategoryAdapter;
import lk.iu.deliciously_fresh.databinding.FragmentCategoryBinding;
import lk.iu.deliciously_fresh.db.CategoryDatabaseHelper;
import lk.iu.deliciously_fresh.model.Category;

public class CategoryFragment extends Fragment {

    private RecyclerView rvCategories;
    private ProgressBar progressBar;
    private TextView tvEmpty;
    private CategoryAdapter adapter;
    private List<Category> categoryList;
    private FirebaseFirestore db;
    private CategoryDatabaseHelper sqliteHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_category, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCategories  = view.findViewById(R.id.rv_categories);
        progressBar   = view.findViewById(R.id.progress_category);
        tvEmpty       = view.findViewById(R.id.tv_empty);

        db            = FirebaseFirestore.getInstance();
        sqliteHelper  = new CategoryDatabaseHelper(requireContext());
        categoryList  = new ArrayList<>();

        adapter = new CategoryAdapter(requireContext(), categoryList, category -> {
            // Pass categoryId to ProductFragment
            FruitListFragment fruitListFragment = new FruitListFragment();
            Bundle args = new Bundle();
            args.putString("categoryId",   category.getCategoryId());
            args.putString("categoryName", category.getName());
            fruitListFragment.setArguments(args);

            requireActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fruitListFragment)
                    .addToBackStack(null)
                    .commit();
        });

        rvCategories.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        rvCategories.setAdapter(adapter);

        loadCategories();
    }

    private void loadCategories() {
        progressBar.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);

        db.collection("categories")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    categoryList.clear();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String categoryId = doc.getString("categoryId");
                        String name       = doc.getString("name");
                        String imageUrl   = doc.getString("imageUrl");

                        Category category = Category.builder()
                                .categoryId(categoryId)
                                .name(name)
                                .imageUrl(imageUrl)
                                .build();

                        categoryList.add(category);
                    }

                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);

                    if (categoryList.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Failed to load categories");
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sqliteHelper != null) sqliteHelper.close();
    }
}