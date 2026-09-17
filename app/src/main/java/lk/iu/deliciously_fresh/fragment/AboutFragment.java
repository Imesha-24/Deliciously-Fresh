package lk.iu.deliciously_fresh.fragment;

import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import lk.iu.deliciously_fresh.R;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.material.button.MaterialButton;

/**
 * About screen shows demo store locations on a Google Map and emergency calling numbers.
 */
public class AboutFragment extends Fragment {

    private GoogleMap googleMap;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton btnPolice = view.findViewById(R.id.btn_emergency_police);
        MaterialButton btnAmbulance = view.findViewById(R.id.btn_emergency_ambulance);
        MaterialButton btnFire = view.findViewById(R.id.btn_emergency_fire);

        btnPolice.setOnClickListener(v -> dial("0763282740"));
        btnAmbulance.setOnClickListener(v -> dial("0774111982"));
        btnFire.setOnClickListener(v -> dial("0726862392"));

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_stores);

        if (mapFragment == null) {
            Toast.makeText(requireContext(), "Map failed to load", Toast.LENGTH_SHORT).show();
            return;
        }

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;
                addStoreMarkers();
            }
        });
    }

    private void addStoreMarkers() {
        if (googleMap == null) return;

        // Demo coordinates around Colombo.
        LatLng center = new LatLng(6.9271, 79.8612);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, 12f));

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(6.9157, 79.8623))
                .title("DeliciouslyFresh - Colombo Fort")
                .snippet("Fresh fruits & deliveries")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(6.9275, 79.8473))
                .title("DeliciouslyFresh - Kollupitiya")
                .snippet("Pickup available")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE)));

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(6.9893, 79.9170))
                .title("DeliciouslyFresh - Dehiwala")
                .snippet("Open daily")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));

        googleMap.getUiSettings().setZoomControlsEnabled(true);
    }

    private void dial(String number) {
        try {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:" + number));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Cannot open dialer", Toast.LENGTH_SHORT).show();
        }
    }
}