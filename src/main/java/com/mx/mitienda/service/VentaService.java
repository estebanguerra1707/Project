package com.mx.mitienda.service;

import com.mx.mitienda.exception.NotFoundException;
import com.mx.mitienda.model.Venta;
import com.mx.mitienda.model.dto.VentaFiltroDTO;
import com.mx.mitienda.repository.VentaRepositorty;
import com.mx.mitienda.specification.VentasSpecification;
import com.mx.mitienda.util.SpecificationBuilder;
import com.mx.mitienda.util.VentaSpecBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class VentaService {

    @Autowired
    public VentaRepositorty ventaRepositorty;


    public List<Venta> getAll(){
        return ventaRepositorty.findAll();
    }

    public Venta getById(Long id){
        return ventaRepositorty.findByIdAndActivoTrue(id).orElseThrow(()->(new NotFoundException("la venta no ha sido encontrada con el id:::"+ id)));
    }

    public Venta save(Venta venta){
            venta.setActivo(true);
        return ventaRepositorty.save(venta);
    }

    public void inactive(Long id) {
        Venta venta = getById(id);
        venta.setActivo(false);
        ventaRepositorty.save(venta);
    }

    public Venta updateSell(Long id, Venta sellUpdated){
        Venta existSell = getById(id);
        existSell.setCliente(sellUpdated.getCliente());
        existSell.setFechaVenta(sellUpdated.getFechaVenta());
        existSell.setTotalVenta(sellUpdated.getTotalVenta());
        return ventaRepositorty.save(existSell);
    }

    public List<Venta> advancedSearch(VentaFiltroDTO ventaFiltroDTO) {

        Specification<Venta> spec = new VentaSpecBuilder()
                .active(ventaFiltroDTO.getActive())
                .client(ventaFiltroDTO.getClient())
                .dateBetween(ventaFiltroDTO.getStart(), ventaFiltroDTO.getEnd())
                .sellPerDay(ventaFiltroDTO.getDay())
                .sellPerMonth(ventaFiltroDTO.getMonth())
                .sellPerYear(ventaFiltroDTO.getYear())
                .totalMajorTo(ventaFiltroDTO.getMin())
                .totalMinorTo(ventaFiltroDTO.getMax())
                .exactTotal(ventaFiltroDTO.getTotal())
                .withId(ventaFiltroDTO.getId())
                .build();
        return ventaRepositorty.findAll(spec);
    }

}
