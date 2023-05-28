package jpabook.jpashop.repository;

import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class OrderQueryRepository {

    private final EntityManager em;

    public List<OrderQueryDto> findOrderQueryDtos() {
        List<OrderQueryDto> result = findOrders();
        result.forEach(o -> {
            List<OrderItemQueryDto> orderItems = findOrderItems(o.getOrderId());
            o.setOrderItems(orderItems);
        });

        return result;
    }

    public List<OrderQueryDto> findAllByDto_optimization() {
        // query 1번
        List<OrderQueryDto> result = findOrders();

        List<Long> orderIds = toOrderIds(result);

        // query 1번
        Map<Long, List<OrderItemQueryDto>> orderItemMap = findOrderItemMap(orderIds);

        result.forEach(o -> o.setOrderItems(orderItemMap.get(o.getOrderId())));
        return result;
    }

    public List<OrderFlatDto> findAllByDto_flat() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.OrderFlatDto(o.id, m.name, o.orderDate, o.status, d.address, i.name, oi.orderPrice, oi.count)"
                                + " from Order o"
                                + " join o.member m"
                                + " join o.delivery d"
                                + " join o.orderItems oi"
                                + " join oi.item i", OrderFlatDto.class)
                .getResultList();
    }

    private List<OrderQueryDto> findOrders() {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.OrderQueryDto(o.id, m.name, o.orderDate, o.status, d.address)"
                                + " from Order o"
                                + " join o.member m"
                                + " join o.delivery d", OrderQueryDto.class)
                .getResultList();
    }

    private List<OrderItemQueryDto> findOrderItems(Long orderId) {
        return em.createQuery(
                        "select new jpabook.jpashop.repository.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                                + " from OrderItem oi"
                                + " join oi.item i"
                                + " where oi.order.id = :orderId", OrderItemQueryDto.class)
                .setParameter("orderId", orderId)
                .getResultList();
    }

    private List<Long> toOrderIds(List<OrderQueryDto> result) {
        return result.stream()
                .map(OrderQueryDto::getOrderId)
                .toList();
    }

    private Map<Long, List<OrderItemQueryDto>> findOrderItemMap(List<Long> orderIds) {
        List<OrderItemQueryDto> orderItems = em.createQuery(
                        "select new jpabook.jpashop.repository.OrderItemQueryDto(oi.order.id, i.name, oi.orderPrice, oi.count)"
                                + " from OrderItem oi"
                                + " join oi.item i"
                                + " where oi.order.id in :orderIds", OrderItemQueryDto.class)
                .setParameter("orderIds", orderIds)
                .getResultList();

        return orderItems.stream()
                .collect(Collectors.groupingBy(OrderItemQueryDto::getOrderId));
    }
}
