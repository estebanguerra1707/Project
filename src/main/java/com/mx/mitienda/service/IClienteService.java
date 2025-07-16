package com.mx.mitienda.service;

import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteFiltroDTO;
import com.mx.mitienda.model.dto.ClienteResponseDTO;

import java.util.List;

public interface IClienteService {
    List<ClienteResponseDTO> getAll();
    Cliente getClienteEntityById(Long id);
    ClienteResponseDTO getById(Long id);
    ClienteResponseDTO save(Cliente cliente);
    void disableClient(Long id);
    ClienteResponseDTO updateClient(Long id, ClienteDTO clienteDTO);
    List<ClienteResponseDTO> advancedSearch(ClienteFiltroDTO clienteDTO);
}
