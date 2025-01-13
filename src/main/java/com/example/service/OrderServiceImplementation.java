package com.example.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.example.Exception.CartException;
import com.example.Exception.OrderException;
import com.example.Exception.RestaurantException;
import com.example.Exception.UserException;
import com.example.response.PaymentResponse;
import com.example.repository.AddressRepository;
import com.example.repository.OrderItemRepository;
import com.example.repository.OrderRepository;
import com.example.repository.RestaurantRepository;
import com.example.repository.UserRepository;
import com.example.request.CreateOrderRequest;
@Service
public class OrderServiceImplementation implements OrderService {
	
	@Autowired
	private AddressRepository addressRepository;
	@Autowired
	private CartSerive cartService;
	@Autowired
	private OrderItemRepository orderItemRepository;
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private RestaurantRepository restaurantRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private PaymentService paymentSerive;
	
	@Autowired
	private NotificationService notificationService;




	@Override
	public PaymentResponse createOrder(CreateOrderRequest order, User user) throws UserException, RestaurantException, CartException, StripeException {

		// Получаем адрес доставки из запроса
		Address shippAddress = order.getDeliveryAddress();

		// Сохраняем адрес в базу данных
		Address savedAddress = addressRepository.save(shippAddress);

		// Проверяем, что этот адрес не был добавлен ранее
		if (!user.getAddresses().contains(savedAddress)) {
			user.getAddresses().add(savedAddress); // Добавляем новый адрес в список пользователя
		}

		// Сохраняем пользователя с привязанным новым адресом
		userRepository.save(user);

		// Получаем ресторан по ID
		Optional<Restaurant> restaurant = restaurantRepository.findById(order.getRestaurantId());
		if (restaurant.isEmpty()) {
			throw new RestaurantException("Restaurant not found with id " + order.getRestaurantId());
		}

		// Создаем новый заказ
		Order createdOrder = new Order();
		createdOrder.setCustomer(user);
		createdOrder.setDeliveryAddress(savedAddress);
		createdOrder.setCreatedAt(new Date());
		createdOrder.setOrderStatus("PENDING");
		createdOrder.setRestaurant(restaurant.get());


		// Получаем корзину пользователя
		Cart cart = cartService.findCartByUserId(user.getId());

		// Создаем список для сохранения заказанных товаров
		List<OrderItem> orderItems = new ArrayList<>();

		// Для каждого товара в корзине создаем объект OrderItem
		for (CartItem cartItem : cart.getItems()) {
			OrderItem orderItem = new OrderItem();
			orderItem.setFood(cartItem.getFood());
			orderItem.setIngredients(cartItem.getIngredients());
			orderItem.setQuantity(cartItem.getQuantity());
			orderItem.setTotalPrice(cartItem.getFood().getPrice() * cartItem.getQuantity());

			// Сохраняем каждый OrderItem
			orderItems.add(orderItemRepository.save(orderItem));
		}

		// Рассчитываем общую стоимость заказа
		Long totalPrice = orderItems.get(0).getTotalPrice();

		createdOrder.setTotalAmount(totalPrice);
		createdOrder.setItems(orderItems);

		// Сохраняем заказ
		Order savedOrder = orderRepository.save(createdOrder);

		// Добавляем заказ в список заказов ресторана
		restaurant.get().getOrders().add(savedOrder);
		restaurantRepository.save(restaurant.get());

		// Создаем объект Payment
		Payment payment = new Payment();
		payment.setOrder(savedOrder);
		payment.setPaymentMethod("CART");
		payment.setPaymentStatus("PENDING");
		payment.setTotalAmount(savedOrder.getTotalAmount());
		payment.setCreatedAt(new Date());

		// Сохраняем платеж
		paymentSerive.savePayment(payment);

		// Связываем платеж с заказом
		savedOrder.setPayment(payment);
		orderRepository.save(savedOrder);

		// Генерируем ссылку для оплаты
		return paymentSerive.generatePaymentLink(savedOrder);
	}

	@Override
	public void cancelOrder(Long orderId) throws OrderException {
           Order order =findOrderById(orderId);
           if(order==null) {
        	   throw new OrderException("Order not found with the id "+orderId);
           }
		
		    orderRepository.deleteById(orderId);
		
	}
	
	public Order findOrderById(Long orderId) throws OrderException {
		Optional<Order> order = orderRepository.findById(orderId);
		if(order.isPresent()) return order.get();
		
		throw new OrderException("Order not found with the id "+orderId);
	}

	@Override
	public List<Order> getUserOrders(Long userId) throws OrderException {
		List<Order> orders=orderRepository.findAllUserOrders(userId);
		return orders;
	} 

	@Override
	public List<Order> getOrdersOfRestaurant(Long restaurantId,String orderStatus) throws OrderException, RestaurantException {
		
			List<Order> orders = orderRepository.findOrdersByRestaurantId(restaurantId);
			
			if(orderStatus!=null) {
				orders = orders.stream()
						.filter(order->order.getOrderStatus().equals(orderStatus))
						.collect(Collectors.toList());
			}
			
			return orders;
	}
//    private List<MenuItem> filterByVegetarian(List<MenuItem> menuItems, boolean isVegetarian) {
//    return menuItems.stream()
//            .filter(menuItem -> menuItem.isVegetarian() == isVegetarian)
//            .collect(Collectors.toList());
//}
	
	

	@Override
	public Order updateOrder(Long orderId, String orderStatus) throws OrderException {
		Order order=findOrderById(orderId);
		
		System.out.println("--------- "+orderStatus);
		
		if(orderStatus.equals("OUT_FOR_DELIVERY") || orderStatus.equals("DELIVERED") 
				|| orderStatus.equals("COMPLETED") || orderStatus.equals("PENDING")) {
			order.setOrderStatus(orderStatus);
			Notification notification=notificationService.sendOrderStatusNotification(order);
			return orderRepository.save(order);
		}
		else throw new OrderException("Please Select A Valid Order Status");
		
		
	}
	
	

}
