package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteFiltroDTO;
import com.mx.mitienda.repository.ClienteRepository;
import com.mx.mitienda.util.ClienteSpecBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClienteService {
    @Autowired
    public ClienteRepository clienteRepository;

    public List<Cliente> getAll(){
        return clienteRepository.findByActiveTrue();
    }

    public Cliente getById(Long id){
        return clienteRepository.findByIdAndActiveTrue(id).orElseThrow(()-> new NotFoundException("Cliente con id:::" + id + " no encontrado"));
    }

    public Cliente save(Cliente cliente){
            cliente.setActive(true);
        return clienteRepository.save(cliente);
    }

    public void disableClient(Long id){
        Cliente cliente = getById(id);
        cliente.setActive(false);
        clienteRepository.save(cliente);
    }

    public Cliente updateClient(Cliente cliente){
        Cliente oldClient = getById(cliente.getId());
        oldClient.setContact(cliente.getContact());
        oldClient.setName(cliente.getName());
        oldClient.setEmail(cliente.getEmail());
        oldClient.setPhone(cliente.getPhone());
        return clienteRepository.save(oldClient);
    }
    public List<Cliente> advancedSearcch(ClienteFiltroDTO clienteDTO){
        Specification spec = new ClienteSpecBuilder()
                .active(clienteDTO.getActive())
                .name(clienteDTO.getName())
                .email(clienteDTO.getEmail())
                .phoneNumber(clienteDTO.getPhone())
                .withId(clienteDTO.getId())
                .build();
        return clienteRepository.findAll(spec);
    }

}
