package com.example.ec_202201c.service;

import com.example.ec_202201c.domain.Order;
import com.example.ec_202201c.repository.CartListRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CartListService {
	@Autowired
	private CartListRepository cartListRepository;

	public Order findShoppingCartByUserId(Integer userId) {
		Order order = cartListRepository.findShoppingCartByUserId(userId);
		return order;
	}

	public void deleteOrderItemsAndOrderToppingsByOrderItemId(Integer orderItemId, Integer userId) {
		cartListRepository.deleteOrderItemsAndOrderToppingsByOrderItemId(orderItemId, userId);
	}
	
	public void deleteOrderByUserId(Integer userId) {
		cartListRepository.deleteOrderByUserId(userId);
	}

}
