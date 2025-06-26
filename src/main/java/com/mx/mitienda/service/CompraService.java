package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Compra;
import com.mx.mitienda.util.enums.Rol;
import com.mx.mitienda.model.Usuario;
import com.mx.mitienda.model.dto.CompraFiltroDTO;
import com.mx.mitienda.repository.CompraRepository;
import com.mx.mitienda.util.CompraSpecBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CompraService {
    @Autowired
    public CompraRepository compraRepository;

    @Autowired
    public UsuarioService usuarioService;

    public List<Compra> getAll(String username, String role){
        if(role.equals(Rol.ADMIN)){
            return compraRepository.findByActiveTrue();
        }else{
            return compraRepository.findByUsuario_UsernameAndActiveTrue(username);
        }
    }

    //que traiga datos por fecha o por cliente
    //falta el delete logico de la tienda


    public Compra getById(Long id){
        return compraRepository.findByIdAndActiveTrue(id).orElseThrow(()->(new NotFoundException("La compra con el id:: " +id+"no se ha encontrado")));
    }

    public Compra save(Compra compra, String username){
        Usuario usuario  = usuarioService.getByUsername(username).orElseThrow(() ->new NotFoundException("Usuario no encontrado::"+ username));
            compra.setActive(true);
            compra.setUsuario(usuario);
        return compraRepository.save(compra);
    }

    public void inactivePurchase(Long id) {
        Compra compra = getById(id);
        compra.setActive(false);
        compraRepository.save(compra);
    }

    public Compra updatePurchase(Compra updatedBuy){
        Compra oldPurchase = getById(updatedBuy.getId());
        oldPurchase.setPurchaseDate(updatedBuy.getPurchaseDate());
        oldPurchase.setTotalAmount(updatedBuy.getTotalAmount());
        oldPurchase.setProveedor(updatedBuy.getProveedor());
        return compraRepository.save(oldPurchase);
    }

    public List<Compra> advancedSearch(CompraFiltroDTO compraDTO){
        Specification<Compra> spec = new CompraSpecBuilder()
                .active(compraDTO.getActive())
                .supplier(compraDTO.getSupplier())
                .dateBetween(compraDTO.getStart(), compraDTO.getEnd())
                .totalMajorTo(compraDTO.getMin())
                .totalMinorTo(compraDTO.getMax())
                .build();
        return compraRepository.findAll(spec);
    }
}
