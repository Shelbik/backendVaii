package com.example.service;


import java.util.*;

import com.example.model.*;
import com.example.repository.*;
import com.example.request.UpdateProfileRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.Exception.UserException;
import com.example.config.JwtProvider;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImplementation implements UserService {


	@Autowired
	private NotificationRepository notificationRepository;

	private UserRepository usersRepository;
	private JwtProvider jwtProvider;
	private PasswordEncoder passwordEncoder;
	private PasswordResetTokenRepository passwordResetTokenRepository;
	private JavaMailSender javaMailSender;

	@Autowired
	private OrderRepository orderRepository;

	@Autowired
	private CartRepository cartRepository;

	@Autowired
	private AddressRepository addressRepository;

	@Autowired
	private RestaurantRepository restaurantRepository;
	
	public UserServiceImplementation(
			UserRepository usersRepository,
			JwtProvider jwtProvider,
			PasswordEncoder passwordEncoder,
			PasswordResetTokenRepository passwordResetTokenRepository,
			JavaMailSender javaMailSender) {
		
		this.usersRepository = usersRepository;
		this.jwtProvider=jwtProvider;
		this.passwordEncoder=passwordEncoder;
		this.passwordResetTokenRepository=passwordResetTokenRepository;
		this.javaMailSender=javaMailSender;
		
	}

	@Override
	public User findUserProfileByJwt(String jwt) throws UserException {
		String email=jwtProvider.getEmailFromJwtToken(jwt);
		
		
		Optional<User> user = usersRepository.findByEmail(email);
		
		if(user.isEmpty()) {
			throw new UserException("user not exist with email "+email);
		}
		return user.get();
	}

	@Override
	public List<User> findAllUsers() {
		// TODO Auto-generated method stub
		return usersRepository.findAll();
	}

	@Override
	public List<User> getPenddingRestaurantOwner() {
		
		return usersRepository.getPenddingRestaurantOwners();
	}
	
	@Override
    public void updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        usersRepository.save(user);
    }

	@Override
	public void sendPasswordResetEmail(User user) {
		
		// Generate a random token (you might want to use a library for this)
        String resetToken = generateRandomToken();
        
        // Calculate expiry date
        Date expiryDate = calculateExpiryDate();

        // Save the token in the database
        PasswordResetToken passwordResetToken = new PasswordResetToken(resetToken,user,expiryDate);
        passwordResetTokenRepository.save(passwordResetToken);

        // Send an email containing the reset link
        sendEmail(user.getEmail(), "Click the following link to reset your password: http://localhost:3000/account/reset-password?token=" + resetToken);
	}
	private void sendEmail(String to, String message) {
	    SimpleMailMessage mailMessage = new SimpleMailMessage();

	    mailMessage.setTo(to);
	    mailMessage.setSubject("Password Reset");
	    mailMessage.setText(message);

	    javaMailSender.send(mailMessage);
	}
	private String generateRandomToken() {
	    return UUID.randomUUID().toString();
	}
	private Date calculateExpiryDate() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, 10);
        return cal.getTime();
    }
	
	@Override
	public User findUserByEmail(String username) throws UserException {
		
		Optional<User> user= usersRepository.findByEmail(username);
		
		if(user.isPresent()) {
			
			return user.get();
		}
		
		throw new UserException("user not exist with username "+username);
	}


	@Override
	public User findUserById(long id) throws Exception {
		return usersRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found for email: " + id));
	}

	@Override
	public User updateUserProfile(String jwt, UpdateProfileRequest updateRequest) throws Exception {
		User user = findUserProfileByJwt(jwt);

		// Обновляем только указанные поля
		if (updateRequest.getFullname() != null) {
			user.setFullName(updateRequest.getFullname());
		}
		if (updateRequest.getEmail() != null) {
			user.setEmail(updateRequest.getEmail());
		}

		// Сохраняем изменения
		return usersRepository.save(user);
	}

	@Transactional
	@Override
	public void deleteUserByJwtToken(String jwt) throws Exception {
		// Извлекаем email
		String email = extractEmailFromToken(jwt);



		// Находим пользователя
		User user = usersRepository.findByEmail(email.toLowerCase())
				.orElseThrow(() -> new EntityNotFoundException("User not found for email: " + email));


		// Логирование начала удаления
		System.out.println("Начинается удаление пользователя с email: " + email);

		// Удаляем связанные данные
		deleteRelatedData(user);

		// Удаляем пользователя
		usersRepository.delete(user);
		System.out.println("Пользователь с email " + email + " успешно удалён.");
	}

	private void deleteRelatedData(User user) {


		// Удаление заказов
		List<Order> orders = orderRepository.findAllUserOrders(user.getId());
		if (!orders.isEmpty()) {
			System.out.println("Удаляем заказы пользователя: " + orders.size());
			orderRepository.deleteAll(orders);
		}


		// Удаление адресов
		List<Address> addresses = addressRepository.findByUsers_Id(user.getId());
		if (!addresses.isEmpty()) {
			System.out.println("Удаляем адреса пользователя: " + addresses.size());
			addressRepository.deleteAll(addresses);
		}

		// Удаление корзины
		Optional<Cart> cart = cartRepository.findByCustomer_Id(user.getId());
		if (cart.isPresent()) {
			System.out.println("Удаляем корзину пользователя");
			cartRepository.delete(cart.get());
		}

		// Удаление ресторана
		Restaurant restaurant = restaurantRepository.findByOwnerId(user.getId());
		if (restaurant != null) {
			System.out.println("Удаляем ресторан пользователя");
			restaurantRepository.delete(restaurant);
		}
	}

	@Transactional
	@Override
	public void deleteUserById(Long id) throws Exception {



		User user = usersRepository.findById(id)
				.orElseThrow(() -> new EntityNotFoundException("User not found for id: " + id));

		deleteRelatedData(user);

		// Удаляем пользователя
		usersRepository.delete(user);
		System.out.println("User with " + id + " is deleted.");
	}

	private String extractEmailFromToken(String jwt) {
		return jwtProvider.getEmailFromJwtToken(jwt);
	}






}
