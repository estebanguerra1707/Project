package com.mx.mitienda.mapper;

import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteResponseDTO;
import org.springframework.stereotype.Component;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteDTO dto) {
        Cliente cliente = new Cliente();
        cliente.setName(dto.getName());
        cliente.setContact(dto.getContact());
        cliente.setEmail(dto.getEmail());
        cliente.setPhone(dto.getPhone());
        cliente.setActive(true);
        return cliente;
    }

    public ClienteResponseDTO toResponse(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.setId(cliente.getId());
        dto.setName(cliente.getName());
        dto.setContact(cliente.getContact());
        dto.setEmail(cliente.getEmail());
        dto.setPhone(cliente.getPhone());
        dto.setIsActive(cliente.getActive());
        return dto;
    }

    public void updateEntity(Cliente cliente, ClienteDTO dto) {
        if (dto.getName() != null && !dto.getName().isBlank()) {
            cliente.setName(dto.getName());
        }
        if (dto.getContact() != null && !dto.getContact().isBlank()) {
            cliente.setContact(dto.getContact());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            cliente.setEmail(dto.getEmail());
        }
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            cliente.setPhone(dto.getPhone());
        }
    }
}
