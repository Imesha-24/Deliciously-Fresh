package lk.iu.deliciously_fresh.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import lk.iu.deliciously_fresh.R;
import lk.iu.deliciously_fresh.adapter.HomeProductAdapter;
import lk.iu.deliciously_fresh.adapter.HomeOfferAdapter;
import lk.iu.deliciously_fresh.model.HomeProduct;

public class HomeFragment extends Fragment {

    private RecyclerView rvNewArrivals, rvMostSold, rvOffers;
    private ProgressBar progressHome;
    private TextView tvHomeEmpty;

    private FirebaseFirestore db;
    private FirebaseAuth firebaseAuth;

    private final List<HomeProduct> newArrivalList = new ArrayList<>();
    private final List<HomeProduct> mostSoldList = new ArrayList<>();
    private final List<HomeProduct> offersList = new ArrayList<>();

    private HomeProductAdapter newArrivalAdapter;
    private HomeProductAdapter mostSoldAdapter;
    private HomeOfferAdapter offersAdapter;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvNewArrivals = view.findViewById(R.id.rv_new_arrivals);
        rvMostSold = view.findViewById(R.id.rv_most_sold);
        rvOffers = view.findViewById(R.id.rv_offers);
        progressHome = view.findViewById(R.id.progress_home);
        tvHomeEmpty = view.findViewById(R.id.tv_home_empty);

        db = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        LinearLayoutManager lm1 = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);
        LinearLayoutManager lm2 = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);
        LinearLayoutManager lm3 = new LinearLayoutManager(requireContext(), RecyclerView.HORIZONTAL, false);
        rvNewArrivals.setLayoutManager(lm1);
        rvMostSold.setLayoutManager(lm2);
        rvOffers.setLayoutManager(lm3);

        HomeProductAdapter.HomeProductActionListener listener = new HomeProductAdapter.HomeProductActionListener() {
            @Override
            public void onCardClick(@NonNull HomeProduct product) {
                openDetails(product.getFruitId());
            }

            @Override
            public void onAddToCartClick(@NonNull HomeProduct product) {
                addToCart(product);
            }
        };

        newArrivalAdapter = new HomeProductAdapter(requireContext(), newArrivalList, listener);
        mostSoldAdapter = new HomeProductAdapter(requireContext(), mostSoldList, listener);
        offersAdapter = new HomeOfferAdapter(requireContext(), offersList, listener);

        rvNewArrivals.setAdapter(newArrivalAdapter);
        rvMostSold.setAdapter(mostSoldAdapter);
        rvOffers.setAdapter(offersAdapter);

        loadHomeData();
    }

    private void loadHomeData() {
        progressHome.setVisibility(View.VISIBLE);
        tvHomeEmpty.setVisibility(View.GONE);

        db.collection("fruit")
                .whereEqualTo("status", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<HomeProduct> allProducts = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        HomeProduct p = parseProduct(doc);
                        if (p != null) {
                            allProducts.add(p);
                        }
                    }

                    buildSections(allProducts);
                    progressHome.setVisibility(View.GONE);
                    tvHomeEmpty.setVisibility(allProducts.isEmpty() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressHome.setVisibility(View.GONE);
                    tvHomeEmpty.setVisibility(View.VISIBLE);
                    tvHomeEmpty.setText("Failed to load products");
                });
    }

    private HomeProduct parseProduct(@NonNull DocumentSnapshot doc) {
        String fruitId = doc.getId();
        String title = doc.getString("title");
        String image = doc.getString("image");
        if (image == null || image.isEmpty()) {
            image = doc.getString("imageUrl");
        }

        Object priceObj = doc.get("price");
        double price = (priceObj instanceof Number) ? ((Number) priceObj).doubleValue() : 0.0;

        Object stockObj = doc.get("stockCount");
        int stock = (stockObj instanceof Number) ? ((Number) stockObj).intValue() : 0;

        Object ratingObj = doc.get("rating");
        float rating = (ratingObj instanceof Number) ? ((Number) ratingObj).floatValue() : 0f;

        Object soldObj = doc.get("soldCount");
        long sold = (soldObj instanceof Number) ? ((Number) soldObj).longValue() : 0L;

        Object discountObj = doc.get("discountPercent");
        int discountPercent = (discountObj instanceof Number) ? ((Number) discountObj).intValue() : 0;
        Object offerPriceObj = doc.get("offerPrice");
        double offerPrice = (offerPriceObj instanceof Number) ? ((Number) offerPriceObj).doubleValue() : 0.0;
        boolean offerFlag = Boolean.TRUE.equals(doc.getBoolean("offer"));

        if (!offerFlag && discountPercent <= 0 && offerPrice <= 0 && price > 0) {
            discountPercent = 10;
            offerPrice = price * 0.9;
            offerFlag = true;
        }

        return HomeProduct.builder()
                .fruitId(fruitId)
                .title(title != null ? title : "Fruit")
                .image(image)
                .pricePerKg(price)
                .rating(rating)
                .stockCount(stock)
                .soldCount(sold)
                .hasOffer(offerFlag)
                .offerPercent(Math.max(discountPercent, 0))
                .offerPricePerKg(offerPrice)
                .build();
    }

    private void buildSections(@NonNull List<HomeProduct> allProducts) {
        newArrivalList.clear();
        mostSoldList.clear();
        offersList.clear();

        List<HomeProduct> newSorted = new ArrayList<>(allProducts);
        newSorted.sort((a, b) -> {
            int byRating = Float.compare(b.getRating(), a.getRating());
            if (byRating != 0) return byRating;
            return safe(a.getTitle()).compareToIgnoreCase(safe(b.getTitle()));
        });
        addTop(newSorted, newArrivalList, 10);

        List<HomeProduct> soldSorted = new ArrayList<>(allProducts);
        soldSorted.sort((a, b) -> {
            int bySold = Long.compare(b.getSoldCount(), a.getSoldCount());
            if (bySold != 0) return bySold;
            return Float.compare(b.getRating(), a.getRating());
        });
        addTop(soldSorted, mostSoldList, 10);

        List<HomeProduct> offerSorted = new ArrayList<>();
        for (HomeProduct p : allProducts) {
            if (p.isHasOffer() && p.getOfferPricePerKg() > 0 && p.getOfferPricePerKg() < p.getPricePerKg()) {
                offerSorted.add(p);
            }
        }
        offerSorted.sort((a, b) -> Integer.compare(b.getOfferPercent(), a.getOfferPercent()));
        addTop(offerSorted, offersList, 10);

        newArrivalAdapter.notifyDataSetChanged();
        mostSoldAdapter.notifyDataSetChanged();
        offersAdapter.notifyDataSetChanged();
    }

    private static void addTop(@NonNull List<HomeProduct> source, @NonNull List<HomeProduct> target, int max) {
        int size = Math.min(max, source.size());
        for (int i = 0; i < size; i++) {
            target.add(source.get(i));
        }
    }

    private void openDetails(@Nullable String fruitId) {
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

    private void addToCart(@NonNull HomeProduct product) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(requireContext(), "Please sign in to add to cart", Toast.LENGTH_SHORT).show();
            return;
        }
        if (product.getStockCount() <= 0) {
            Toast.makeText(requireContext(), "Product is out of stock", Toast.LENGTH_SHORT).show();
            return;
        }

        DocumentReference cartItemRef = db.collection("users")
                .document(user.getUid())
                .collection("cart")
                .document(product.getFruitId());

        db.runTransaction(transaction -> {
            DocumentSnapshot existing = transaction.get(cartItemRef);
            int existingQty = 0;
            Object qtyObj = existing.get("quantity");
            if (qtyObj instanceof Number) {
                existingQty = ((Number) qtyObj).intValue();
            }

            int newQty = Math.min(existingQty + 1, product.getStockCount());

            Map<String, Object> cartData = new HashMap<>();
            cartData.put("fruitId", product.getFruitId());
            cartData.put("title", product.getTitle());
            cartData.put("image", product.getImage());
            cartData.put("price", product.getPricePerKg());
            cartData.put("quantity", newQty);
            cartData.put("updatedAt", FieldValue.serverTimestamp());
            transaction.set(cartItemRef, cartData);
            return null;
        }).addOnSuccessListener(aVoid ->
                Toast.makeText(requireContext(), product.getTitle() + " added to cart", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(requireContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show()
        );
    }

    @NonNull
    private static String safe(@Nullable String s) {
        return s == null ? "" : s;
    }
}