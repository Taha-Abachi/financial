package com.pars.financial.mapper;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.pars.financial.dto.CustomerDto;
import com.pars.financial.entity.Customer;

@Component
public class CustomerMapper {
    public CustomerDto getFrom(Customer cd) {
        CustomerDto c = new CustomerDto();
        c.name = cd.getName();
        c.surName = cd.getSurname();
        c.phoneNumber = cd.getPrimaryPhoneNumber();
        return c;
    }
    public List<CustomerDto> getFrom(List<Customer> customers) {
        if(customers == null) return null;
        List<CustomerDto> dtos = new ArrayList<>();
        for(Customer cst : customers) {
            dtos.add(getFrom(cst));
        }
        return dtos;
    }
}
