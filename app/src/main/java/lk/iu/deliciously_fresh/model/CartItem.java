package lk.iu.deliciously_fresh.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem {
    private String fruitId;
    private String title;
    private String image;
    private double price;
    private int quantity;
}

