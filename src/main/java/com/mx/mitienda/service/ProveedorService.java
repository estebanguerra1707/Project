package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.repository.ProveedorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProveedorService {

    @Autowired
    private ProveedorRepository proveedorRepository;

    public List<Proveedor> getAll(){
        return proveedorRepository.findByActiveTrue();
    }

    public Proveedor getById(Long id){
        return proveedorRepository.findByIdAndActiveTrue(id).orElseThrow(()-> new NotFoundException("Proveedor con id :::" +id + " no encontrado"));
    }

    public Proveedor save(Proveedor proveedor){

        if(proveedor.getActive()==null){
            proveedor.setActive(true);
        }
        return proveedorRepository.save(proveedor);
    }



}
