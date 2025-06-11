package com.mx.mitienda.util;

import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.specification.ClienteSpecification;
import org.springframework.data.jpa.domain.Specification;

public class ClienteSpecBuilder {
    private final SpecificationBuilder<Cliente> builder = new SpecificationBuilder<>();


    public ClienteSpecBuilder active(Boolean active){
        builder.and(ClienteSpecification.active(active));
        return this;
    }

    public ClienteSpecBuilder name(String name){
        builder.and(ClienteSpecification.name(name));
        return this;
    }
    public ClienteSpecBuilder email(String email){
        builder.and(ClienteSpecification.email(email));
        return this;
    }
     public ClienteSpecBuilder phoneNumber(String phone){
        builder.and(ClienteSpecification.phone(phone));
        return this;
     }
     public ClienteSpecBuilder withId(Long id){
        builder.and(ClienteSpecification.id(id));
        return this;
     }

     public Specification<Cliente> build(){
        return builder.build();
     }

}
