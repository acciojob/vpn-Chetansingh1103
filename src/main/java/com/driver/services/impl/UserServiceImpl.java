package com.driver.services.impl;

import com.driver.model.Country;
import com.driver.model.CountryName;
import com.driver.model.ServiceProvider;
import com.driver.model.User;
import com.driver.repository.CountryRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository3;
    @Autowired
    ServiceProviderRepository serviceProviderRepository3;
    @Autowired
    CountryRepository countryRepository3;

    @Override
    public User register(String username, String password, String countryName) throws Exception{
        for(CountryName countryName1 : CountryName.values()){

            if(countryName1.name().equalsIgnoreCase(countryName)){

                User user = new User();

                user.setUsername(username);
                user.setPassword(password);
                user.setConnected(Boolean.FALSE);
                user.setMaskedIp(null);

                Country country = new Country();
                country.setCountryName(countryName1);
                country.setCode(countryName1.toCode());
                country.setUser(user);

                user.setOriginalCountry(country);
                user.setServiceProviderList(null);
                user = userRepository3.save(user);

                user.setOriginalIp(countryName1.toCode() + "" + user.getId());

                userRepository3.save(user);

            }
        }
        throw  new Exception();
    }

    @Override
    public User subscribe(Integer userId, Integer serviceProviderId) {

        User user = userRepository3.findById(userId).get();

        ServiceProvider serviceProvider = serviceProviderRepository3.findById(serviceProviderId).get();

        // creating the list of users for serviceProvider
        List<User> userList = new ArrayList<>();
        userList.add(user);
        serviceProvider.setUsers(userList);

        // creating the list of serviceProviders for user
        List<ServiceProvider> serviceProviderList = new ArrayList<>();
        serviceProviderList.add(serviceProvider);
        user.setServiceProviderList(serviceProviderList);

        userRepository3.save(user);

        return user;
    }
}
