package com.mx.mitienda.service;

import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteFiltroDTO;
import com.mx.mitienda.model.dto.ClienteResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IClienteService {
    List<ClienteResponseDTO> getAll();
    Cliente getClienteEntityById(Long id);
    ClienteResponseDTO getById(Long id);
    ClienteResponseDTO save(ClienteDTO cliente);
    void disableClient(Long id);
    ClienteResponseDTO updateClient(Long id, ClienteDTO clienteDTO);
    List<ClienteResponseDTO> advancedSearch(ClienteFiltroDTO clienteDTO);
    Page<ClienteResponseDTO> advancedSearchPage(
            ClienteFiltroDTO filtro,
            Pageable pageable
    );
}
