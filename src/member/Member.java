package member;

import java.time.LocalDate;

import common.Grade;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import shoppingCart.ShoppingCart;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class Member {
	private int memberId;
	private String loginId;
	private String password;
	private String name;
	private LocalDate birthDate;
	private String phoneNumber;
	private String passportNum;
	private LocalDate passportExpiredDate;
	private boolean isAdult;
	private Grade grade;
	private LocalDate gradeSelectionDate;
	private ShoppingCart cart = new ShoppingCart();
	
	// 인도장에서 번호표 뽑을 때 (번호표의 정보로 들어갈)경량 Member 생성을 위한 생성자
	public Member(int memberId, String name, String passportNum, boolean isAdult, Grade grade) {
		this.memberId=memberId;
		this.name=name;
		this.passportNum=passportNum;
		this.isAdult=isAdult;
		this.grade=grade;
	}
	
	public ShoppingCart getCart() {
		return cart;
	}

}
