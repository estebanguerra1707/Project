package com.mx.mitienda.specification;

import com.mx.mitienda.model.Cliente;
import org.springframework.data.jpa.domain.Specification;

public class ClienteSpecification {
    public static Specification<Cliente> active(Boolean active){
        if(active==null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("activo"),active);
    }

    public static Specification<Cliente> name(String name){
        if(name==null || name.isBlank()) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")),"%" + name.toLowerCase() +"%");
    }

    public static Specification<Cliente> email(String email){
        if(email==null || email.isBlank()) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("correo")),"%" + email.toLowerCase() + "%");
    }

    public static Specification<Cliente> phone(String number){
        if(number == null || number.isBlank()) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.like(criteriaBuilder.lower(root.get("correo")),"%" + number + "%");
    }

    public static Specification<Cliente> id(Long id){
        if(id==null) return null;
        return (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("id"), id);
    }
}
