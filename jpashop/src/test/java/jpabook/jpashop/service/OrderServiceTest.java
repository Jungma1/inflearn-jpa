package jpabook.jpashop.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
class OrderServiceTest {

    @PersistenceContext
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    @DisplayName("주문이 성공한다.")
    void order() throws Exception {
        // given
        Member member = createMember();
        Item item = createBook("JPA", 10000, 10);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // then
        Order order = orderRepository.findOne(orderId);

        assertThat(order.getStatus()).as("상품 주문시 상태는 ORDER 이다.").isEqualTo(OrderStatus.ORDER);
        assertThat(order.getOrderItems().size()).as("주문한 상품 종류 수가 정확해야 한다.").isEqualTo(1);
        assertThat(order.getTotalPrice()).as("주문 가격은 가격 * 수량이다.").isEqualTo(10000 * orderCount);
        assertThat(item.getStockQuantity()).as("주문 수량만큼 재고가 줄어야 한다.").isEqualTo(8);
    }

    @Test
    @DisplayName("주문이 취소된다.")
    void cancel_order() throws Exception {
        // given
        Member member = createMember();
        Item item = createBook("JPA", 10000, 10);

        int orderCount = 2;

        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order order = orderRepository.findOne(orderId);

        assertThat(order.getStatus()).as("주문 취소시 상태는 CANCEL 이다.").isEqualTo(OrderStatus.CANCEL);
        assertThat(item.getStockQuantity()).as("주문이 취소된 상품은 재고가 원복되어야 한다.").isEqualTo(10);
    }

    @Test
    @DisplayName("주문 상품의 재고 수량이 초과하면 예외가 발생한다.")
    void order_item_max_count_exception() throws Exception {
        // given
        Member member = createMember();
        Item item = createBook("JPA", 10000, 10);

        // when
        int orderCount = 11; // 재고보다 많은 수량을 주문

        // then
        assertThatThrownBy(() -> orderService.order(member.getId(), item.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("memberA");
        member.setAddress(new Address("Seoul", "GangGa", "123-123"));
        em.persist(member);
        return member;
    }

    private Item createBook(String name, int price, int stockQuantity) {
        Item book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }
}
