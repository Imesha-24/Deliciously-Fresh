package lk.iu.deliciously_fresh.model;

import com.google.firebase.Timestamp;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {
    private String id;
    private String type;
    private String text;
    private String toUserId;
    private Timestamp createdAt;
}
