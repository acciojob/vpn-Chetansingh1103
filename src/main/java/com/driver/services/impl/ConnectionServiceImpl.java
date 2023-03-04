package com.driver.services.impl;

import com.driver.model.*;
import com.driver.repository.ConnectionRepository;
import com.driver.repository.ServiceProviderRepository;
import com.driver.repository.UserRepository;
import com.driver.services.ConnectionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionServiceImpl implements ConnectionService {
    @Autowired
    UserRepository userRepository2;
    @Autowired
    ServiceProviderRepository serviceProviderRepository2;
    @Autowired
    ConnectionRepository connectionRepository2;

    @Override
    public User connect(int userId, String countryName) throws Exception{

        User user = userRepository2.findById(userId).get();

        if(user.getConnected()){
            throw new Exception("Already connected");
        }

        if(user.getOriginalCountry().getCountryName().name().equalsIgnoreCase(countryName)){
            return user;
        }


        for(ServiceProvider serviceProvider : user.getServiceProviderList()){

            for(Country country : serviceProvider.getCountryList()){
                if(country.getCountryName().name().equalsIgnoreCase(countryName)){

                    user.setMaskedIp(country.getCountryName().toCode() + "." + serviceProvider.getId() + "." + userId);
                    user.setConnected(Boolean.TRUE);

                    Connection connection = new Connection();
                    connection.setUser(user);
                    connection.setServiceProvider(serviceProvider);

                    user.getConnectionList().add(connection);
                    serviceProvider.getConnectionList().add(connection);

                    userRepository2.save(user);
                    serviceProviderRepository2.save(serviceProvider);

                    return user;

                }
            }

        }

        throw new Exception("Unable to connect");
    }
    @Override
    public User disconnect(int userId) throws Exception {

        User user = userRepository2.findById(userId).get();

        if(!user.getConnected()){
            throw new Exception("Already disconnected");
        }

        user.setConnected(Boolean.FALSE);
        user.setMaskedIp(null);


        userRepository2.save(user);

        return user;

    }
    @Override
    public User communicate(int senderId, int receiverId) throws Exception {
        User sender = userRepository2.findById(senderId).orElse(null);
        User receiver = userRepository2.findById(receiverId).orElse(null);

        if(sender == null || receiver == null){
            throw new NullPointerException();
        }

        if(receiver.getConnected()){

            if (receiver.getMaskedIp() == null) {
                throw new Exception("Receiver IP is null");
            }

            String maskedIp = receiver.getMaskedIp();
            String countryCode = maskedIp.substring(0,3);

            if(!sender.getOriginalCountry().getCountryName().toCode().equals(countryCode)){
                String countryName = receiver.getOriginalCountry().getCountryName().name();
                try {
                    sender = connect(senderId,countryName);
                } catch (Exception e) {
                    throw new CannotEstablishCommunicationException();
                }
            }
        }
        else {
            if(!sender.getOriginalCountry().equals(receiver.getOriginalCountry())){
                try {
                    sender = connect(senderId,receiver.getOriginalCountry().getCountryName().name());
                } catch (Exception e) {
                    throw new CannotEstablishCommunicationException();
                }
            }
        }
        return sender;
    }
    public static class CannotEstablishCommunicationException extends Exception {
        public CannotEstablishCommunicationException() {
            super("Cannot establish communication");
        }
    }
}
