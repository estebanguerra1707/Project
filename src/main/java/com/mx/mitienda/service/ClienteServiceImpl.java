package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ClienteMapper;
import com.mx.mitienda.model.Cliente;
import com.mx.mitienda.model.dto.ClienteDTO;
import com.mx.mitienda.model.dto.ClienteFiltroDTO;
import com.mx.mitienda.model.dto.ClienteResponseDTO;
import com.mx.mitienda.repository.ClienteRepository;
import com.mx.mitienda.util.ClienteSpecBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteServiceImpl implements IClienteService{

    private final ClienteRepository clienteRepository;
    private final ClienteMapper clienteMapper;
    @Override
    public List<ClienteResponseDTO> getAll() {
        return clienteRepository.findByActiveTrue()
                .stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

    @Override
    public Cliente getClienteEntityById(Long id) {
        return clienteRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new NotFoundException("Cliente con id:::" + id + " no encontrado"));
    }

    @Override
    public ClienteResponseDTO getById(Long id) {
        return clienteMapper.toResponse(getClienteEntityById(id));
    }

    @Override
    public ClienteResponseDTO save(Cliente cliente) {
        cliente.setActive(true);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Override
    public void disableClient(Long id) {
        Cliente cliente = getClienteEntityById(id);
        cliente.setActive(false);
        clienteRepository.save(cliente);
    }

    @Override
    public ClienteResponseDTO updateClient(Long id, ClienteDTO clienteDTO) {
        Cliente cliente = getClienteEntityById(id);
        clienteMapper.updateEntity(cliente, clienteDTO);
        return clienteMapper.toResponse(clienteRepository.save(cliente));
    }

    @Override
    public List<ClienteResponseDTO> advancedSearch(ClienteFiltroDTO clienteDTO) {
        Specification spec = new ClienteSpecBuilder()
                .active(clienteDTO.getActive())
                .name(clienteDTO.getName())
                .email(clienteDTO.getEmail())
                .phoneNumber(clienteDTO.getPhone())
                .withId(clienteDTO.getId())
                .build();
        return clienteRepository.findAll(spec).stream()
                .map(clienteMapper::toResponse)
                .toList();
    }

}
