package com.mx.mitienda.service;

import com.fasterxml.jackson.core.ObjectCodec;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteFiltroDTO;
import com.mx.mitienda.repository.ClienteRepository;
import com.mx.mitienda.util.ClienteSpecBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

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

    public Cliente updateClient(Long id, ClienteDTO clienteDTO){
        Cliente oldClient = getById(id);
        if (clienteDTO.getName() != null && !clienteDTO.getName().isBlank()) oldClient.setName(clienteDTO.getName());
        if (clienteDTO.getContact() != null && !clienteDTO.getContact().isBlank()) oldClient.setContact(clienteDTO.getContact());
        if (clienteDTO.getEmail() != null && !clienteDTO.getEmail().isBlank()) oldClient.setEmail(clienteDTO.getEmail());
        if (clienteDTO.getPhone() != null && !clienteDTO.getPhone().isBlank()) oldClient.setPhone(clienteDTO.getPhone());

        return clienteRepository.save(oldClient);
    }

    public List<Cliente> advancedSearch(ClienteFiltroDTO clienteDTO){
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
