package com.example.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.model.*;
import com.example.repository.CartItemRepository;
import com.example.repository.OrderRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Exception.FoodException;
import com.example.Exception.RestaurantException;
import com.example.repository.IngredientsCategoryRepository;
import com.example.repository.foodRepository;
import com.example.request.CreateFoodRequest;


@Service
public class FoodServiceImplementation implements FoodService {
	@Autowired
	private foodRepository foodRepository;

	@Autowired
	private CartItemRepository cartItemRepository;

	@Autowired
	private OrderRepository orderRepository;
	

	
//	@Autowired
//	private RestaurantRepository restaurantRepository;


	
	@Autowired
	private IngredientsService ingredientService;
	
	@Autowired
	private IngredientsCategoryRepository ingredientCategoryRepo;

	@Override
	public Food createFood(CreateFoodRequest  req,
						   Category category,
						   Restaurant restaurant)
			throws FoodException,
	RestaurantException {

		// Check if the main request object exists
		if (req == null) {
			throw new FoodException("Request cannot be null");
		}

		// Validate required fields
		if (req.getName() == null || req.getName().trim().isEmpty()) {
			throw new FoodException("Food name cannot be empty");
		}

		if (req.getDescription() == null || req.getDescription().trim().isEmpty()) {
			throw new FoodException("Food description cannot be empty");
		}

		// Validate price
		if (req.getPrice() <= 0) {
			throw new FoodException("Price must be greater than 0");
		}

		// Validate images list
		if (req.getImages() == null || req.getImages().isEmpty()) {
			throw new FoodException("At least one image is required");
		}

		// Check required relationships
		if (category == null) {
			throw new FoodException("Food category cannot be null");
		}

		if (restaurant == null) {
			throw new RestaurantException("Restaurant cannot be null");
		}

		// Validate ingredients if required
		if (req.getIngredients() == null || req.getIngredients().isEmpty()) {
			throw new FoodException("Food must have at least one ingredient");
		}

			Food food=new Food();
			food.setFoodCategory(category);
			food.setCreationDate(new Date());
			food.setDescription(req.getDescription());
			food.setImages(req.getImages());
			food.setName(req.getName());
			food.setPrice((long) req.getPrice());
			food.setSeasonal(req.isSeasonal());		
			food.setVegetarian(req.isVegetarian());
			food.setIngredients(req.getIngredients());
		food.setRestaurant(restaurant);
			food = foodRepository.save(food);

			restaurant.getFoods().add(food);
			return food;
		
	}


	@Override
	@Transactional
	public void deleteFood(Long foodId) throws FoodException {
		Food food = findFoodById(foodId);
		if (food == null) {
			throw new FoodException("Food not found with id: " + foodId);
		}

		try {
			// First find all orders containing this food
			List<Order> affectedOrders = orderRepository.findByItemsFoodId(foodId);

			for (Order order : affectedOrders) {
				// Remove related order_items
				order.getItems().removeIf(item -> item.getFood().getId().equals(foodId));

				if (order.getItems().isEmpty()) {
					// If order has no items left, delete the entire order
					orderRepository.delete(order);
				} else {
					// Otherwise recalculate order totals
					order.setTotalItem(order.getItems().size());
					order.setTotalPrice((int) order.getItems().stream()
							.mapToDouble(OrderItem::getTotalPrice)
							.sum());
					orderRepository.save(order);
				}
			}

			// Remove cart items
			cartItemRepository.deleteByFoodId(foodId);

			// Remove restaurant association
			Restaurant restaurant = food.getRestaurant();
			if (restaurant != null) {
				restaurant.getFoods().remove(food);
				food.setRestaurant(null);
			}

			// Finally delete the food itself
			foodRepository.delete(food);

		} catch (Exception e) {
			throw new FoodException("Error deleting food: " + e.getMessage());
		}
	}
	@Override
	public List<Food> getRestaurantsFood(
			Long restaurantId, 
			boolean isVegetarian, 
			boolean isNonveg,
			boolean isSeasonal,
			String foodCategory) throws FoodException {
		List<Food> foods = foodRepository.findByRestaurantId(restaurantId);
		
		
		
	    if (isVegetarian) {
	        foods = filterByVegetarian(foods, isVegetarian);
	    }
	    if (isNonveg) {
	        foods = filterByNonveg(foods, isNonveg);
	    }

	    if (isSeasonal) {
	        foods = filterBySeasonal(foods, isSeasonal);
	    }
	    if(foodCategory!=null && !foodCategory.equals("")) {
	    	foods = filterByFoodCategory(foods, foodCategory);
	    }
		
		return foods;
		
	}
	
	private List<Food> filterByVegetarian(List<Food> foods, boolean isVegetarian) {
	    return foods.stream()
	            .filter(food -> food.isVegetarian() == isVegetarian)
	            .collect(Collectors.toList());
	}
	private List<Food> filterByNonveg(List<Food> foods, boolean isNonveg) {
	    return foods.stream()
	            .filter(food -> !food.isVegetarian())
	            .collect(Collectors.toList());
	}
	private List<Food> filterBySeasonal(List<Food> foods, boolean isSeasonal) {
	    return foods.stream()
	            .filter(food -> food.isSeasonal() == isSeasonal)
	            .collect(Collectors.toList());
	}
	private List<Food> filterByFoodCategory(List<Food> foods, String foodCategory) {
	    
		return foods.stream()
			    .filter(food -> {
			        if (food.getFoodCategory() != null) {
			            return food.getFoodCategory().getName().equals(foodCategory);
			        }
			        return false; // Return true if food category is null
			    })
			    .collect(Collectors.toList());
	}

	@Override
	public List<Food> searchFood(String keyword) {
		List<Food> items=new ArrayList<>();

		if(keyword != null && !keyword.trim().isEmpty()) {
			System.out.println("keyword -- "+keyword);
			items=foodRepository.searchByNameOrCategory(keyword);
		}
		
		return items;
	}

	@Override
	public Food updateAvailibilityStatus(Long id) throws FoodException {
		Food food = findFoodById(id);
		
		food.setAvailable(!food.isAvailable());
		foodRepository.save(food);
		return food;
	}

	@Override
	public Food findFoodById(Long foodId) throws FoodException {
		Optional<Food> food = foodRepository.findById(foodId);
		if (food.isPresent()) {
			return food.get();
		}
		throw new FoodException("food with id" + foodId + "not found");
	}

}
