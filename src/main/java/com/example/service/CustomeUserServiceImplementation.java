package com.example.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.example.domain.USER_ROLE;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.model.User;
import com.example.repository.UserRepository;

@Service
public class CustomeUserServiceImplementation implements UserDetailsService {
	
	private UserRepository userRepository;
	
	public CustomeUserServiceImplementation(UserRepository userRepository) {
		this.userRepository=userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		
		Optional<User> user = userRepository.findByEmail(username);
		
		if(user.isEmpty()) {

			throw new UsernameNotFoundException("user not found with email  - "+username);
		}
		
		USER_ROLE role=user.get().getRole();
		
		if(role==null) role=USER_ROLE.ROLE_CUSTOMER;
		
		System.out.println("role  ---- "+role);
		
		List<GrantedAuthority> authorities=new ArrayList<>();
		
		authorities.add(new SimpleGrantedAuthority(role.toString()));
		
		return new org.springframework.security.core.userdetails.User(
				user.get().getEmail(),user.get().getPassword(),authorities);
	}

}
