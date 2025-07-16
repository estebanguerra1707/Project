package com.mx.mitienda.mapper;

import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {
    public Cliente toEntity(ClienteDTO clienteDTO) {
        Cliente cliente = new Cliente();
        cliente.setName(clienteDTO.getName());
        cliente.setContact(clienteDTO.getContact());
        cliente.setEmail(clienteDTO.getEmail());
        cliente.setPhone(clienteDTO.getPhone());
        cliente.setActive(true);
        return cliente;
    }

    public ClienteResponseDTO toResponse(Cliente cliente) {
        ClienteResponseDTO clienteResponseDTO = new ClienteResponseDTO();
        clienteResponseDTO.setId(cliente.getId());
        clienteResponseDTO.setName(cliente.getName());
        clienteResponseDTO.setContact(cliente.getContact());
        clienteResponseDTO.setEmail(cliente.getEmail());
        clienteResponseDTO.setPhone(cliente.getPhone());
        clienteResponseDTO.setIsActive(cliente.getActive());
        return clienteResponseDTO;
    }

    public void updateEntity(Cliente cliente, ClienteDTO clienteDTO) {
        if (clienteDTO.getName() != null && !clienteDTO.getName().isBlank()) cliente.setName(clienteDTO.getName());
        if (clienteDTO.getContact() != null && !clienteDTO.getContact().isBlank()) cliente.setContact(clienteDTO.getContact());
        if (clienteDTO.getEmail() != null && !clienteDTO.getEmail().isBlank()) cliente.setEmail(clienteDTO.getEmail());
        if (clienteDTO.getPhone() != null && !clienteDTO.getPhone().isBlank()) cliente.setPhone(clienteDTO.getPhone());
    }
}
