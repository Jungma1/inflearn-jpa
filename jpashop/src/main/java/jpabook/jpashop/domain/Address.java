package jpabook.jpashop.domain;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor // 값 타입은 변경 불가능하게 설계해야 함
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자를 protected 로 설정하여 생성 방지
@Embeddable // 값 타입을 정의하는 곳에 표시
public class Address {

    private String city;
    private String street;
    private String zipcode;
}
