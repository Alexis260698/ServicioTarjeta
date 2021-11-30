package com.example.Proyecto.controller;

import com.example.Proyecto.dto.TarjetaConsulta;
import com.example.Proyecto.dto.TarjetaDto;
import com.example.Proyecto.entity.Cliente;
import com.example.Proyecto.entity.Cuenta;
import com.example.Proyecto.entity.TarjetaDebito;
import com.example.Proyecto.repository.TarjetaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

@RestController
public class TarjetaController {

    @Autowired
    TarjetaRepository tarjetaRepository;

    @Autowired
    RestTemplate restTemplate;

    @PostMapping("/crearTarjeta/{usuario}/{cbu}")
    public ResponseEntity<TarjetaDto> crearTarjeta(@PathVariable("usuario") String usuario,
                                                   @PathVariable("cbu") String cbu,
                                                   @RequestBody TarjetaDebito tarjetaDebito) {

        if (!islogged(usuario)) {
            return new ResponseEntity("Primero debes Iniciar sesion", HttpStatus.BAD_REQUEST);
        }
        Cliente cliente = tarjetaRepository.consumirCliente(usuario);

        if (cliente.getUsuario() != null) {
            if (tarjetaRepository.clienteEsMayor(cliente.getEdad())) {
                Optional<Cuenta> cuenta = tarjetaRepository.traerCuenta(cliente, cbu);
                if (cuenta.isPresent()) {
                    return ResponseEntity.ok(tarjetaRepository.crearTarjeta(cliente, cuenta.get(), tarjetaDebito));
                } else {
                    return new ResponseEntity("No se encontro esa cuenta", HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity("El cliente debe ser mayor", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity("No Se encontro el cliente", HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/listarTarjetas/{usuario}")
    public ResponseEntity<List<TarjetaConsulta>> listarTarjetas(@PathVariable("usuario") String usuario) {
        if (!islogged(usuario)) {
            return new ResponseEntity("Primero debes Iniciar sesion", HttpStatus.BAD_REQUEST);
        }

        Cliente cliente = tarjetaRepository.consumirCliente(usuario);
        if (cliente.getUsuario() != null) {
            return ResponseEntity.ok(tarjetaRepository.listarTarjetas(cliente));
        } else {
            return new ResponseEntity("No Se encontro el cliente", HttpStatus.NOT_FOUND);
        }

    }

    @PutMapping("/ActivarTarjeta/{usuario}/{tarjeta}")
    public ResponseEntity<String> activarTarjeta(@PathVariable("usuario") String usuario, @PathVariable("tarjeta") String nTarjeta) {
        if (!islogged(usuario)) {
            return new ResponseEntity("Primero debes Iniciar sesion", HttpStatus.BAD_REQUEST);
        }

        Cliente cliente = tarjetaRepository.consumirCliente(usuario);
        if (cliente.getUsuario() != null) {
            if (tarjetaRepository.clienteCumpleParametros(cliente, nTarjeta)) {
                TarjetaDebito tarjeta = tarjetaRepository.buscarTarjeta(cliente, nTarjeta);
                if (tarjeta != null) {
                    if (tarjeta.getMarca().equals("marca")) {
                        return new ResponseEntity("La tarjeta ya esta Activada", HttpStatus.BAD_REQUEST);
                    } else {
                        return new ResponseEntity("Peticion Aprobada", HttpStatus.OK);
                    }
                } else {

                    return new ResponseEntity("Peticion Fallida", HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity("Peticion Fallida", HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity("No Se encontro el cliente", HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/gestionarLimite/{usuario}/{ntarjeta}/{limite}")
    public ResponseEntity<String> gestionarLimite(@PathVariable("usuario") String usuario,
                                                  @PathVariable("ntarjeta") String nTarjeta,
                                                  @PathVariable("limite") double limite) {

        if (!islogged(usuario)) {
            return new ResponseEntity("Primero debes Iniciar sesion", HttpStatus.BAD_REQUEST);
        }

        Cliente cliente = tarjetaRepository.consumirCliente(usuario);
        if (cliente.getUsuario() != null) {
            if(tarjetaRepository.actualizarLimite(cliente, nTarjeta, limite)){
                return new ResponseEntity("Peticion Finalizada", HttpStatus.OK);
            }
        }
        return new ResponseEntity("Peticion Fallida", HttpStatus.BAD_REQUEST);
    }

    @PutMapping("/bloquearTarjeta/{usuario}/{tarjeta}")
    public ResponseEntity<TarjetaDebito> bloquearTarjeta(@PathVariable("usuario") String usuario,
                                                         @PathVariable("tarjeta") String tarjeta){
        if (!islogged(usuario)) {
            return new ResponseEntity("Primero debes Iniciar sesion", HttpStatus.BAD_REQUEST);
        }

        Cliente cliente = tarjetaRepository.consumirCliente(usuario);
        if (cliente.getUsuario() != null) {
            if(tarjetaRepository.bloquearTarjeta(cliente,tarjeta)){
                return new ResponseEntity("Peticion exitosa", HttpStatus.OK);
            }
            return new ResponseEntity("Peticion Fallida", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("Cliente no encontrado", HttpStatus.NOT_FOUND);
    }

    @PutMapping("/vincularTarjeta/{usuario}/{tarjeta}")
    public ResponseEntity<String> vincularTarjeta(@PathVariable("usuario")String usuario,
                                  @PathVariable("tarjeta")String tarjeta){
        if (!islogged(usuario)) {
            return new ResponseEntity("Primero debes Iniciar sesion", HttpStatus.BAD_REQUEST);
        }

        Cliente cliente = tarjetaRepository.consumirCliente(usuario);
        if (cliente.getUsuario() != null) {
            if(tarjetaRepository.vincularTarjeta(cliente,tarjeta)){
                return new ResponseEntity("Peticion exitosa", HttpStatus.OK);
            }
            return new ResponseEntity("Peticion Fallida", HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity("Cliente no encontrado", HttpStatus.NOT_FOUND);

    }


    public boolean islogged(String usuario) {
        Boolean isLogged = restTemplate.getForObject("http://localhost:8080/islogged/" + usuario, Boolean.class);
        System.out.println(isLogged.booleanValue());
        return isLogged;
    }

}
