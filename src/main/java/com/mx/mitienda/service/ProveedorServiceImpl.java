package com.mx.mitienda.service;

import com.mx.mitienda.exception.DuplicateProveedorException;
import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.mapper.ProveedorMapper;
import com.mx.mitienda.model.Proveedor;
import com.mx.mitienda.model.dto.ProveedorDTO;
import com.mx.mitienda.model.dto.ProveedorResponseDTO;
import com.mx.mitienda.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ProveedorServiceImpl implements IProveedorService{


    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;

   @Override
    public List<ProveedorResponseDTO> getAll(){
        Stream<Proveedor> stream = proveedorRepository.findByActiveTrue(Sort.by(Sort.Direction.ASC,"id")).stream();
        return stream.map(proveedorMapper::toResponse).collect(Collectors.toList());
    }

    @Override
    public ProveedorResponseDTO getById(Long id){
        Proveedor proveedor= proveedorRepository.findByIdAndActiveTrue(id).orElseThrow(()-> new NotFoundException("Proveedor con id :::" +id + " no encontrado"));
        return proveedorMapper.toResponse(proveedor);
    }

    @Override
    public ProveedorResponseDTO save(ProveedorDTO proveedorDTO){
        proveedorRepository.findByEmailAndNameAndActiveTrue(proveedorDTO.getEmail(), proveedorDTO.getName())
                .ifPresent(p-> {throw new DuplicateProveedorException("Proveedor ya existe con ese correo y nombre, intenta con otro");
        });
        Proveedor proveedor =  proveedorMapper.toEntity(proveedorDTO);
        Proveedor saved = proveedorRepository.save(proveedor);
        return proveedorMapper.toResponse(saved);
    }

    @Override
    public ProveedorResponseDTO update(Long id, ProveedorDTO proveedorDTO) {

        Proveedor existing = proveedorRepository.findByIdAndActiveTrue(id).orElseThrow(()-> new NotFoundException("Proveedor con Id no encontrado"));

        if (proveedorDTO.getName() != null) {
            existing.setName(proveedorDTO.getName());
        }
        if (proveedorDTO.getContact() != null) {
            existing.setContact(proveedorDTO.getContact());
        }
        if(proveedorDTO.getEmail()!=null){
            existing.setEmail(proveedorDTO.getEmail());
        }
        Proveedor saved = proveedorRepository.save(existing);
        return proveedorMapper.toResponse(saved);
    }

    @Override
    public void disable(Long id) {
        Proveedor proveedor = proveedorRepository.findByIdAndActiveTrue(id).orElseThrow(()-> new NotFoundException("Proveedor con Id no encontrado"));
        proveedor.setActive(false);
        proveedorRepository.save(proveedor);
    }

}
