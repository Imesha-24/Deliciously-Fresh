package lk.iu.deliciously_fresh.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HomeProduct {
    private String fruitId;
    private String title;
    private String image;
    private double pricePerKg;
    private float rating;
    private int stockCount;
    private long soldCount;

    private boolean hasOffer;
    private double offerPricePerKg;
    private int offerPercent;
}

