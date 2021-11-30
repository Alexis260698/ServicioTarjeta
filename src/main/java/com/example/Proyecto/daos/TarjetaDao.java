package com.example.Proyecto.daos;

import com.example.Proyecto.entity.TarjetaDebito;
import org.springframework.data.repository.CrudRepository;

public interface TarjetaDao extends CrudRepository<TarjetaDebito, String> {
}
