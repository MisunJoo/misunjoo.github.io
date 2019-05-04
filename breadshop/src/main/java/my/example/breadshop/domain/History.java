package my.example.breadshop.domain;

import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

public class PurchaseHistory {
    private Long id; //주문번호
    private Long user_id;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member member;


}
