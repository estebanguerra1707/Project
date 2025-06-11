package com.mx.mitienda.util;

import org.springframework.data.jpa.domain.Specification;

public class SpecificationBuilder<T> {
    //construye objetos specificartion de forma dinamica y segura, sin preocuparse por si uno de los filtros es null
    //funciona para cualquier entidad

    private Specification<T> spec = Specification.where(null);


    //metodo para agregar una nueva condicion al specification actual
    //retorna this para permitir encadenamiento, builder.and(...).and(...).build()).
    public SpecificationBuilder<T> and(Specification<T> newSpec){
        if(newSpec!=null){
            spec =  spec.and(newSpec);
        }
        return this;
    }

    public SpecificationBuilder<T> or(Specification<T>... specs) {
        Specification<T> orSpec = Specification.where(null);
        for (Specification<T> s : specs) {
            if (s != null) {
                orSpec = orSpec == null ? s : orSpec.or(s);
            }
        }
        return this;
    }


    //se llama al build y se rettoprna el specification completo, con todos los filtros agregados

    public Specification<T> build(){
        return spec;
    }
}
