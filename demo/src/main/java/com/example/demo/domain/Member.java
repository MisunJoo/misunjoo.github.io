package my.example.breadshop.domain;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "member")
@Getter @Setter
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20, nullable = false)
    private String name;

    @Column(length = 20, nullable = false, unique = true)
    private String member_id;

    @Column(length = 20, nullable = false)
    private String password;

    @Column(nullable = false)
    private String gender; // 본인의 성별 입

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false, unique = true)
    private String phone;

    @Column(length = 255, nullable = false)
    private String zipcode;

    @Column(length = 255, nullable = false)
    private String address;

    @Column(length = 255)
    private String addressDetail;

    @Column(nullable = false)
    private boolean receivceEmail;

    @Column(nullable = false)
    private boolean receiveSMS;

    @Column(name = "point")
    private int point = 0;

    @OneToMany(mappedBy = "member")
    private Set<Cart> carts;

    @OneToMany(mappedBy = "member")
    private Set<History> histories;

    @OneToMany(mappedBy = "member")
    private Set<WishList> wishLists;

    @OneToMany(mappedBy = "member")
    private Set<MemberCoupon> memberCoupons;

}
