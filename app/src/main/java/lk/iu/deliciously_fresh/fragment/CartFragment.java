package lk.iu.deliciously_fresh.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.adapter.CartItemsAdapter;
import lk.iu.deliciously_fresh.model.CartItem;


public class CartFragment extends Fragment {

    private RecyclerView rvCartItems;
    private TextView tvCartEmpty;
    private TextView tvCartTotal;
    private MaterialButton btnCheckout;
    private MaterialButton btnBuyNow;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private CollectionReference itemsRef;
    private ListenerRegistration cartListenerRegistration;

    private final java.util.List<CartItem> cartItems = new java.util.ArrayList<>();
    private CartItemsAdapter adapter;

    public CartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCartItems = view.findViewById(R.id.rv_cart_items);
        tvCartEmpty = view.findViewById(R.id.tv_cart_empty);
        tvCartTotal = view.findViewById(R.id.tv_cart_total);
        btnCheckout = view.findViewById(R.id.btn_checkout);
        btnBuyNow = view.findViewById(R.id.btn_buy_now);

        rvCartItems.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new CartItemsAdapter(requireContext(), cartItems, true, new CartItemsAdapter.CartItemActionListener() {
            @Override
            public void onIncrement(@NonNull CartItem item) {
                updateQuantity(item.getFruitId(), item.getQuantity() + 1);
            }

            @Override
            public void onDecrement(@NonNull CartItem item) {
                updateQuantity(item.getFruitId(), item.getQuantity() - 1);
            }

            @Override
            public void onRemove(@NonNull CartItem item) {
                removeItem(item.getFruitId());
            }
        });
        rvCartItems.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in to use cart", Toast.LENGTH_SHORT).show();
            btnCheckout.setEnabled(false);
            return;
        }

        itemsRef = db.collection("users")
                .document(user.getUid())
                .collection("cart");

        cartListenerRegistration = itemsRef.addSnapshotListener((snapshots, e) -> {
            if (!isAdded()) return;

            if (e != null) {
                Toast.makeText(requireContext(), "Failed to load cart: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            cartItems.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    cartItems.add(parseCartItem(doc));
                }
            }

            adapter.notifyDataSetChanged();
            updateUi();
        });

        btnCheckout.setOnClickListener(v -> openCheckout());
        btnBuyNow.setOnClickListener(v -> openCheckout());
        updateUi();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (cartListenerRegistration != null) {
            cartListenerRegistration.remove();
            cartListenerRegistration = null;
        }
    }

    private void openCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        requireActivity()
                .getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, new CheckoutFragment())
                .addToBackStack(null)
                .commit();
    }

    private void updateUi() {
        double total = 0.0;
        for (CartItem item : cartItems) {
            total += item.getPrice() * item.getQuantity();
        }

        tvCartTotal.setText(String.format(java.util.Locale.getDefault(), "LKR %.2f", total));
        tvCartEmpty.setVisibility(cartItems.isEmpty() ? View.VISIBLE : View.GONE);
        boolean hasItems = !cartItems.isEmpty();
        btnCheckout.setEnabled(hasItems);
        btnBuyNow.setEnabled(hasItems);
    }

    private CartItem parseCartItem(@NonNull DocumentSnapshot doc) {
        CartItem.CartItemBuilder builder = CartItem.builder();
        builder.fruitId(doc.getString("fruitId") != null ? doc.getString("fruitId") : doc.getId());
        builder.title(doc.getString("title"));
        builder.image(doc.getString("image"));

        Object priceObj = doc.get("price");
        double price = 0.0;
        if (priceObj instanceof Number) {
            price = ((Number) priceObj).doubleValue();
        }
        builder.price(price);

        Object qtyObj = doc.get("quantity");
        int qty = 0;
        if (qtyObj instanceof Number) {
            qty = ((Number) qtyObj).intValue();
        }
        builder.quantity(qty);

        return builder.build();
    }

    private void updateQuantity(@NonNull String fruitId, int newQuantity) {
        if (itemsRef == null) return;
        DocumentReference ref = itemsRef.document(fruitId);

        int finalQty = Math.max(0, newQuantity);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(ref);
            if (!snapshot.exists()) return null;

            if (finalQty <= 0) {
                transaction.delete(ref);
            } else {
                transaction.update(ref, "quantity", finalQty, "updatedAt", FieldValue.serverTimestamp());
            }
            return null;
        });
    }

    private void removeItem(@NonNull String fruitId) {
        if (itemsRef == null) return;
        itemsRef.document(fruitId).delete();
    }
}