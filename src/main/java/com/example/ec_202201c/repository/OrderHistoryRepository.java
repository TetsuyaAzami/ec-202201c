package com.example.ec_202201c.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.example.ec_202201c.domain.Item;
import com.example.ec_202201c.domain.Order;
import com.example.ec_202201c.domain.OrderItem;
import com.example.ec_202201c.domain.OrderTopping;
import com.example.ec_202201c.domain.Topping;

@Repository
public class OrderHistoryRepository {
	private final ResultSetExtractor<List<Order>> ORDER_HISTORY_ROW_MAPPER = (rs) -> {
		List<Order> orderList = new ArrayList<>();
		Order order = null;
		// orderドメインのフィールドであるorderItemListを作ってorderにセット
		List<OrderItem> orderItemList = new ArrayList<>();

		OrderItem orderItem = null;
		List<OrderTopping> orderToppingList = null;

		int preOrderId = -1;
		int preOrderItemId = -1;
		while (rs.next()) {
			int orderId = rs.getInt("o_id");
			// OrderIdが前のOrderIdと一致しているか確認する
			if (preOrderId != orderId) {
				
				order = new Order();
				order.setOrderItemList(orderItemList);
				order.setOrderDate(rs.getDate("o_order_date"));
				orderList.add(order);
				//System.out.println("\n\n\n\n\n\n\n" + orderList);
				
				//orderItemIdが前のOrderItemIdと一致しているか確認する
			}
			int orderItemId = rs.getInt("oi_id");
			if (orderItemId != preOrderItemId) {
				orderItem = new OrderItem();
				// orderItemドメインのフィールドであるorderToppingListを作ってorderItemにセット
				orderToppingList = new ArrayList<>();
				orderItem.setOrderToppingList(orderToppingList);

				// 価格、画像パスを取得するためのItemドメインを作ってorderItemにセット
				Item item = new Item();
				orderItem.setItem(item);

				// orderItem数量、サイズ
				orderItem.setQuantity(rs.getInt("oi_quantity"));
				orderItem.setSize(rs.getString("oi_size").charAt(0));

				// itemの名前、imageのパス、価格をitemオブジェクトに入れる
				item.setName(rs.getString("i_name"));
				item.setImagePath(rs.getString("i_image_path"));
				if (orderItem.getSize() == 'M') {
					item.setPriceM(rs.getInt("i_price"));
				} else if (orderItem.getSize() == 'L') {
					item.setPriceL(rs.getInt("i_price"));
				}
				
				order.setTotalPrice(rs.getInt("o_total_price"));
				orderItemList.add(orderItem);
				preOrderItemId = orderItemId;
				preOrderId = orderId;
			}

			OrderTopping orderTopping = new OrderTopping();
			// orderToppingドメインのフィールドであるToppingを作ってToppingにセット
			Topping topping = new Topping();
			orderTopping.setTopping(topping);

			// toppingの名前、価格をtoppingオブジェクトに入れる
			topping.setName(rs.getString("t_name"));
			if ('M' == orderItem.getSize()) {
				topping.setPriceM(rs.getInt("t_price"));
			} else if ('L' == orderItem.getSize()) {
				topping.setPriceL(rs.getInt("t_price"));
			}
			orderToppingList.add(orderTopping);

		}
		return orderList;
	};

	@Autowired
	private NamedParameterJdbcTemplate template;

	public List<Order> findHistoryByUserId(Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ");
		sql.append("o.id o_id, ");
		sql.append("oi.id oi_id, ");
		sql.append("o.order_date o_order_date, ");
		sql.append("i.name i_name, ");
		sql.append("i.image_path i_image_path, ");
		sql.append("oi.size oi_size, ");
		sql.append("CASE oi.size WHEN 'L' THEN i.price_l ");
		sql.append("WHEN 'M' THEN i.price_m ");
		sql.append("END i_price, ");
		sql.append("oi.quantity oi_quantity, ");
		sql.append("t.name t_name , ");
		sql.append("CASE oi.size WHEN 'L' THEN t.price_l ");
		sql.append("WHEN 'M' THEN t.price_m ");
		sql.append("END t_price, ");
		sql.append("o.total_price o_total_price ");
		sql.append("FROM orders o ");
		sql.append("INNER JOIN order_items oi ");
		sql.append("ON o.id = oi.order_id ");
		sql.append("LEFT OUTER JOIN order_toppings ot ");
		sql.append("ON oi.id = ot.order_item_id ");
		sql.append("LEFT OUTER JOIN items i ");
		sql.append("ON oi.item_id = i.id ");
		sql.append("LEFT OUTER JOIN toppings t ");
		sql.append("ON ot.topping_id = t.id ");
		sql.append("WHERE o.user_id = :userId AND o.status != 0 ");
		sql.append("ORDER BY o.order_date DESC;");

		SqlParameterSource param = new MapSqlParameterSource().addValue("userId", userId);
		List<Order> orderList = template.query(sql.toString(), param, ORDER_HISTORY_ROW_MAPPER);
		return orderList;
	}
}
