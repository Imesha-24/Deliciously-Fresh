package lk.iu.deliciously_fresh.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.adapter.CartItemsAdapter;
import lk.iu.deliciously_fresh.model.CartItem;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.Address;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.Item;
import lk.payhere.androidsdk.model.StatusResponse;

public class CheckoutFragment extends Fragment {
    private static final int PAYHERE_REQUEST = 11010;
    private static final boolean PAYHERE_SANDBOX = true;

    // TODO: Replace these with your PayHere merchant credentials (Sandbox for testing).
    private static final String PAYHERE_MERCHANT_ID = "1226893";
    // TODO: Replace with your PayHere notify URL (server endpoint).
    private static final String PAYHERE_NOTIFY_URL = "https://eokwyobr35ggdi5.m.pipedream.net";

    private static final String PAYHERE_CURRENCY = "LKR";

    private RecyclerView rvCheckoutItems;
    private TextView tvCheckoutEmpty;
    private TextView tvCheckoutTotal;
    private MaterialButton btnPlaceOrder;

    private TextInputEditText etCheckoutAddress;
    private TextInputEditText etCheckoutMobile;
    private TextInputEditText etCheckoutEmail;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private CollectionReference itemsRef;
    private ListenerRegistration cartListenerRegistration;

    private final List<CartItem> checkoutItems = new ArrayList<>();
    private CartItemsAdapter adapter;

    // Snapshot used for order creation after PayHere result callback.
    private final List<CartItem> pendingItems = new ArrayList<>();
    private double pendingTotal = 0.0;
    private String pendingUid;
    private String pendingOrderId;
    private String pendingPaymentNo;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_checkout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvCheckoutItems = view.findViewById(R.id.rv_checkout_items);
        tvCheckoutEmpty = view.findViewById(R.id.tv_checkout_empty);
        tvCheckoutTotal = view.findViewById(R.id.tv_checkout_total);
        btnPlaceOrder = view.findViewById(R.id.btn_place_order);

        etCheckoutAddress = view.findViewById(R.id.et_checkout_address);
        etCheckoutMobile = view.findViewById(R.id.et_checkout_mobile);
        etCheckoutEmail = view.findViewById(R.id.et_checkout_email);

        rvCheckoutItems.setLayoutManager(new LinearLayoutManager(requireContext()));

        adapter = new CartItemsAdapter(requireContext(), checkoutItems, false, null);
        rvCheckoutItems.setAdapter(adapter);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in to checkout", Toast.LENGTH_SHORT).show();
            btnPlaceOrder.setEnabled(false);
            return;
        }

        pendingUid = user.getUid();

        itemsRef = db.collection("users")
                .document(user.getUid())
                .collection("cart");

        cartListenerRegistration = itemsRef.addSnapshotListener((snapshots, e) -> {
            if (!isAdded()) return;
            if (e != null) {
                Toast.makeText(requireContext(), "Failed to load cart: " + e.getMessage(), Toast.LENGTH_LONG).show();
                return;
            }

            checkoutItems.clear();
            if (snapshots != null) {
                for (DocumentSnapshot doc : snapshots.getDocuments()) {
                    checkoutItems.add(parseCartItem(doc));
                }
            }
            adapter.notifyDataSetChanged();
            updateUi();
        });

        btnPlaceOrder.setOnClickListener(v -> {
            if (pendingUid == null) return;
            startPayNowFlow(pendingUid);
        });

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

    private void updateUi() {
        double total = 0.0;
        for (CartItem item : checkoutItems) {
            total += item.getPrice() * item.getQuantity();
        }
        pendingTotal = total;

        tvCheckoutTotal.setText(String.format(Locale.getDefault(), "LKR %.2f", total));
        tvCheckoutEmpty.setVisibility(checkoutItems.isEmpty() ? View.VISIBLE : View.GONE);
        btnPlaceOrder.setEnabled(!checkoutItems.isEmpty());
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

    private void startPayNowFlow(@NonNull String uid) {
        if (checkoutItems.isEmpty()) {
            Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        String address = getText(etCheckoutAddress);
        String mobile = getText(etCheckoutMobile);
        String email = getText(etCheckoutEmail);

        if (TextUtils.isEmpty(address) || TextUtils.isEmpty(mobile) || TextUtils.isEmpty(email)) {
            Toast.makeText(requireContext(), "Please fill address, mobile and email", Toast.LENGTH_LONG).show();
            return;
        }

        if (!email.contains("@")) {
            Toast.makeText(requireContext(), "Please enter a valid email", Toast.LENGTH_LONG).show();
            return;
        }

        btnPlaceOrder.setEnabled(false);

        // Save checkout contact details to users/{uid}
        Map<String, Object> updates = new HashMap<>();
        updates.put("address", address);
        updates.put("mobile", mobile);
        updates.put("email", email);

        db.collection("users").document(uid)
                .set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    if (!isAdded()) return;
                    launchPayHerePayment(uid, address, mobile, email);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    btnPlaceOrder.setEnabled(true);
                    Toast.makeText(requireContext(), "Failed to save details: " + (e != null ? e.getMessage() : ""), Toast.LENGTH_LONG).show();
                });
    }

    private void launchPayHerePayment(@NonNull String uid,
                                       @NonNull String address,
                                       @NonNull String mobile,
                                       @NonNull String email) {

        pendingItems.clear();
        pendingItems.addAll(checkoutItems);
        pendingPaymentNo = null;

        pendingOrderId = "ORDER_" + System.currentTimeMillis();


        InitRequest req = new InitRequest();
        req.setMerchantId(PAYHERE_MERCHANT_ID);
        req.setNotifyUrl(PAYHERE_NOTIFY_URL);
        req.setCurrency(PAYHERE_CURRENCY);
        req.setAmount(pendingTotal);
        req.setOrderId(pendingOrderId);
        req.setItemsDescription("DeliciouslyFresh Cart");


        req.getCustomer().setFirstName("User");
        req.getCustomer().setLastName("");
        req.getCustomer().setEmail(email);
        req.getCustomer().setPhone(mobile);


        Address addr = req.getCustomer().getAddress();
        if (addr != null) {
            addr.setAddress(address);
            addr.setCity("Colombo");
            addr.setCountry("Sri Lanka");
        }

        req.getItems().clear();
        Item payhereItem = new Item();
        payhereItem.setId("CART");
        payhereItem.setName("Cart");
        payhereItem.setQuantity(1);
        payhereItem.setAmount(pendingTotal);
        req.getItems().add(payhereItem);

        if (PAYHERE_SANDBOX) {
            PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        } else {
            PHConfigs.setBaseUrl(PHConfigs.LIVE_URL);
        }

        Intent intent = new Intent(requireContext(), PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        startActivityForResult(intent, PAYHERE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode != PAYHERE_REQUEST) return;
        if (data == null || !data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            btnPlaceOrder.setEnabled(true);
            return;
        }

        PHResponse response = (PHResponse) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
        if (response == null) {
            btnPlaceOrder.setEnabled(true);
            Toast.makeText(requireContext(), "Payment failed (no response)", Toast.LENGTH_LONG).show();
            return;
        }

        if (resultCode == Activity.RESULT_OK) {
            Object payhereData = response.getData();
            StatusResponse status = (payhereData instanceof StatusResponse) ? (StatusResponse) payhereData : null;


            int statusVal = status != null ? status.getStatus() : -1;
            boolean success = (statusVal == StatusResponse.Status.SUCCESS.value())
                    || (statusVal == StatusResponse.Status.HOLD.value());

            if (success) {

                long paymentNo = status != null ? status.getPaymentNo() : 0L;
                pendingPaymentNo = paymentNo > 0L ? String.valueOf(paymentNo) : pendingOrderId;
                createOrderAndClearCart(pendingUid, pendingPaymentNo);
            } else {
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(requireContext(), "Payment cancelled/failed", Toast.LENGTH_LONG).show();
            }
        } else {
            btnPlaceOrder.setEnabled(true);
            Toast.makeText(requireContext(), "Payment dismissed", Toast.LENGTH_LONG).show();
        }
    }

    private void createOrderAndClearCart(@Nullable String uid, @NonNull String paymentNo) {
        if (!isAdded()) return;
        if (uid == null) return;
        if (itemsRef == null) return;

        WriteBatch batch = db.batch();

        DocumentReference orderRef = db.collection("users")
                .document(uid)
                .collection("orders")
                .document();

        List<Map<String, Object>> orderItems = new ArrayList<>();
        for (CartItem item : pendingItems) {
            double lineTotal = item.getPrice() * item.getQuantity();
            Map<String, Object> line = new HashMap<>();
            line.put("fruitId", item.getFruitId());
            line.put("title", item.getTitle());
            line.put("image", item.getImage());
            line.put("price", item.getPrice());
            line.put("quantity", item.getQuantity());
            line.put("lineTotal", lineTotal);
            orderItems.add(line);
        }

        Map<String, Object> order = new HashMap<>();
        order.put("items", orderItems);
        order.put("total", pendingTotal);
        order.put("status", "paid");
        order.put("paymentNo", paymentNo);
        order.put("orderId", pendingOrderId);
        order.put("createdAt", FieldValue.serverTimestamp());

        batch.set(orderRef, order);

        itemsRef.get().addOnSuccessListener(snapshots -> {
            if (!isAdded()) return;

            for (DocumentSnapshot doc : snapshots.getDocuments()) {
                batch.delete(doc.getReference());
            }

            batch.commit().addOnSuccessListener(aVoid -> {
                if (!isAdded()) return;
                Toast.makeText(requireContext(), "Order placed successfully", Toast.LENGTH_SHORT).show();

                requireActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, new OrdersFragment())
                        .commit();
            }).addOnFailureListener(e -> {
                if (!isAdded()) return;
                btnPlaceOrder.setEnabled(true);
                Toast.makeText(requireContext(), "Failed to finalize order", Toast.LENGTH_LONG).show();
            });
        }).addOnFailureListener(e -> {
            if (!isAdded()) return;
            btnPlaceOrder.setEnabled(true);
            Toast.makeText(requireContext(), "Failed to clear cart: " + (e != null ? e.getMessage() : ""), Toast.LENGTH_LONG).show();
        });
    }

    @NonNull
    private static String getText(@Nullable TextInputEditText editText) {
        if (editText == null || editText.getText() == null) return "";
        return editText.getText().toString().trim();
    }
}