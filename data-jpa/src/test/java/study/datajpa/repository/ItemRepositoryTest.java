package study.datajpa.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import study.datajpa.entity.Item;

@SpringBootTest
class ItemRepositoryTest {

    @Autowired
    ItemRepository itemRepository;

    @Test
    void save() {
        Item item = new Item("A");

        // CrudRepository - SimpleJpaRepository - save()
        // 엔티티 ID가 없으면 저장하고, 있으면 병합한다.
        // - 식별자가 객체일 때 null 로 판단
        // - 식별자가 자바 기본 타입일 때 0 으로 판단
        // - *Persistable 인터페이스를 구현해서 판단 로직 변경 가능
        itemRepository.save(item);
    }
}
