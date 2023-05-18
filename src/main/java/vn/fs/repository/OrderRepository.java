package vn.fs.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.fs.entities.OrderEntity;

/**
 * @author DongTHD
 *
 */
@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

	@Query(value = "select * from orders where user_id = ?1", nativeQuery = true)
	List<OrderEntity> findOrderByUserId(Long id);
	
	@Query(value = "SELECT count(*) FROM orders", nativeQuery= true)
	int sumOrder();

}
