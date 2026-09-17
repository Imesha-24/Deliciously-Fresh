package lk.iu.deliciously_fresh.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.adapter.WishlistAdapter;
import lk.iu.deliciously_fresh.model.WishlistItem;

public class WishlistFragment extends Fragment {

    private RecyclerView rvWishlist;
    private TextView tvWishlistEmpty;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private CollectionReference wishlistRef;
    private ListenerRegistration listenerRegistration;

    private final java.util.List<WishlistItem> items = new java.util.ArrayList<>();
    private WishlistAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_wishlist, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvWishlist = view.findViewById(R.id.rv_wishlist);
        tvWishlistEmpty = view.findViewById(R.id.tv_wishlist_empty);

        rvWishlist.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new WishlistAdapter(requireContext(), items, new WishlistAdapter.WishlistListener() {
            @Override
            public void onOpen(@NonNull WishlistItem item) {
                openDetails(item.getFruitId());
            }

            @Override
            public void onRemove(@NonNull WishlistItem item) {
                removeFromWishlist(item.getFruitId());
            }

            @Override
            public void onAddToCart(@NonNull WishlistItem item) {
                addToCart(item);
            }
        });
        rvWishlist.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in to view wishlist", Toast.LENGTH_SHORT).show();
            tvWishlistEmpty.setVisibility(View.VISIBLE);
            return;
        }

        wishlistRef = db.collection("users").document(user.getUid()).collection("wishlist");

        listenerRegistration = wishlistRef.addSnapshotListener((snapshots, e) -> {
            if (!isAdded()) return;
            if (e != null) {
                tvWishlistEmpty.setVisibility(View.VISIBLE);
                return;
            }

            items.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    items.add(parseWishlistItem(doc));
                }
            }
            adapter.notifyDataSetChanged();
            tvWishlistEmpty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (listenerRegistration != null) {
            listenerRegistration.remove();
            listenerRegistration = null;
        }
    }

    private WishlistItem parseWishlistItem(@NonNull DocumentSnapshot doc) {
        WishlistItem.WishlistItemBuilder b = WishlistItem.builder();
        b.fruitId(doc.getString("fruitId") != null ? doc.getString("fruitId") : doc.getId());
        b.title(doc.getString("title"));
        b.image(doc.getString("image"));

        Object priceObj = doc.get("price");
        b.price(priceObj instanceof Number ? ((Number) priceObj).doubleValue() : 0.0);

        Object ratingObj = doc.get("rating");
        b.rating(ratingObj instanceof Number ? ((Number) ratingObj).floatValue() : 0f);

        Object stockObj = doc.get("stockCount");
        b.stockCount(stockObj instanceof Number ? ((Number) stockObj).intValue() : 0);

        return b.build();
    }

    private void openDetails(String fruitId) {
        if (fruitId == null || fruitId.isEmpty()) return;
        FruitDetailsFragment detailFragment = new FruitDetailsFragment();
        Bundle args = new Bundle();
        args.putString("fruitId", fruitId);
        detailFragment.setArguments(args);

        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
                .addToBackStack(null)
                .commit();
    }

    private void removeFromWishlist(String fruitId) {
        if (wishlistRef == null || fruitId == null || fruitId.isEmpty()) return;
        wishlistRef.document(fruitId).delete();
    }

    private void addToCart(@NonNull WishlistItem item) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in to add to cart", Toast.LENGTH_SHORT).show();
            return;
        }
        if (item.getStockCount() <= 0) {
            Toast.makeText(requireContext(), "Out of stock", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference cartItemRef = db.collection("users")
                .document(user.getUid())
                .collection("cart")
                .document(item.getFruitId());

        db.runTransaction(transaction -> {
            DocumentSnapshot existing = transaction.get(cartItemRef);
            int existingQty = 0;
            Object qtyObj = existing.get("quantity");
            if (qtyObj instanceof Number) existingQty = ((Number) qtyObj).intValue();

            int newQty = Math.min(existingQty + 1, item.getStockCount());

            java.util.Map<String, Object> cartData = new java.util.HashMap<>();
            cartData.put("fruitId", item.getFruitId());
            cartData.put("title", item.getTitle());
            cartData.put("image", item.getImage());
            cartData.put("price", item.getPrice());
            cartData.put("quantity", newQty);
            cartData.put("updatedAt", FieldValue.serverTimestamp());
            transaction.set(cartItemRef, cartData);
            return null;
        }).addOnSuccessListener(aVoid ->
                Toast.makeText(requireContext(), "Added to cart", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show()
        );
    }
}