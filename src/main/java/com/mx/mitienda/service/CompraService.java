package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Compra;
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


    public List<Compra> getAll(){
        return compraRepository.findAll();
    }

    //que traiga datos por fecha o por cliente
    //falta el delete logico de la tienda


    public Compra getById(Long id){
        return compraRepository.findByIdAndActiveTrue(id).orElseThrow(()->(new NotFoundException("La compra con el id:: " +id+"no se ha encontrado")));
    }

    public Compra save(Compra compra){
            compra.setActive(true);
        return compraRepository.save(compra);
    }

    public void inactiveBuy(Long id) {
        Compra compra = getById(id);
        compra.setActive(false);
        compraRepository.save(compra);
    }

    public Compra updateSale(Compra updatedBuy){
        Compra oldBuy = getById(updatedBuy.getId());
        oldBuy.setPurchase_date(updatedBuy.getPurchase_date());
        oldBuy.setTotal_amount(updatedBuy.getTotal_amount());
        oldBuy.setProveedor(updatedBuy.getProveedor());
        return compraRepository.save(oldBuy);
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
