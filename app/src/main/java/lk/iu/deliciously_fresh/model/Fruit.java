package lk.iu.deliciously_fresh.model;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Fruit {
    private String fruitId;
    private String title;
    private String imageUrl;
    private String description;
    private double price;
    private String categoryId;
    private int stockCount;
    private boolean status;
    private float rating;
    private List<Attribute> attributes;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Attribute {
        private String name;
        private String type;
        private String quality;
    }
}