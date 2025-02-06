	package com.example.service;

	import java.util.ArrayList;
	import java.util.Date;
	import java.util.List;
	import java.util.Optional;
	import java.util.stream.Collectors;

	import com.example.model.*;
	import jakarta.transaction.Transactional;
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
			Address shippAddress;
				// get address delivery
			 shippAddress = order.getDeliveryAddress();


			// save address to db
			Address savedAddress = addressRepository.save(shippAddress);
			user.getAddresses().add(savedAddress);

			// check if address exists
			if (!user.getAddresses().contains(savedAddress)) {
				user.getAddresses().add(savedAddress); // add new address to user
			}

			// save user with new address
			userRepository.save(user);

			// get restaurant by id
			Optional<Restaurant> restaurant = restaurantRepository.findById(order.getRestaurantId());
			if (restaurant.isEmpty()) {
				throw new RestaurantException("Restaurant not found with id " + order.getRestaurantId());
			}

			// create new order
			Order createdOrder = new Order();
			createdOrder.setCustomer(user);
			createdOrder.setDeliveryAddress(savedAddress);
			createdOrder.setCreatedAt(new Date());
			createdOrder.setOrderStatus("PENDING");
			createdOrder.setRestaurant(restaurant.get());


			//get users cart
			Cart cart = cartService.findCartByUserId(user.getId());


			List<OrderItem> orderItems = new ArrayList<>();


			for (CartItem cartItem : cart.getItems()) {
				OrderItem orderItem = new OrderItem();
				orderItem.setFood(cartItem.getFood());
				orderItem.setIngredients(cartItem.getIngredients());
				orderItem.setQuantity(cartItem.getQuantity());
				orderItem.setTotalPrice(cartItem.getFood().getPrice() * cartItem.getQuantity());

				// save each item
				orderItems.add(orderItemRepository.save(orderItem));
			}

			// price count
			Long totalPrice = orderItems.stream()
					.mapToLong(OrderItem::getTotalPrice)
					.sum();

			createdOrder.setTotalAmount(totalPrice);
			createdOrder.setItems(orderItems);


			Order savedOrder = orderRepository.save(createdOrder);

			// add order to restaurant
			restaurant.get().getOrders().add(savedOrder);
			restaurantRepository.save(restaurant.get());

			// create payment
			Payment payment = new Payment();
			payment.setOrder(savedOrder);
			payment.setPaymentMethod("CART");
			payment.setPaymentStatus("PENDING");
			payment.setTotalAmount(savedOrder.getTotalAmount());
			payment.setCreatedAt(new Date());


			paymentSerive.savePayment(payment);

			//connect order with payment
			savedOrder.setPayment(payment);
			orderRepository.save(savedOrder);

			// link generator for payment
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
			return orderRepository.findAllUserOrders(userId);
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


		@Transactional
		public void handleDeletedFood(Long foodId) {
			// find all pending orders with this food
			List<Order> ordersToDelete = orderRepository.findByFoodAndStatus(foodId, "PENDING");

			// delete them
			if (!ordersToDelete.isEmpty()) {
				orderRepository.deleteAll(ordersToDelete);
			}
		}
	}