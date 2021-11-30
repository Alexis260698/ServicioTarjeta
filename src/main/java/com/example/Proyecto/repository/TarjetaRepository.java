package com.example.Proyecto.repository;

import com.example.Proyecto.configuration.RestTemplateConfiguration;
import com.example.Proyecto.dto.TarjetaConsulta;
import com.example.Proyecto.dto.TarjetaDto;
import com.example.Proyecto.entity.Cliente;
import com.example.Proyecto.entity.Cuenta;
import com.example.Proyecto.entity.TarjetaDebito;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TarjetaRepository {


    @Autowired
    RestTemplate restTemplate;

    @Autowired
    ModelMapper modelMapper;

    public Cliente consumirCliente(String usuario) {
        try {
            Cliente cliente = restTemplate.getForObject("http://localhost:8080/buscarCliente/" + usuario, Cliente.class);
            return cliente;
        } catch (Exception e) {
            return new Cliente();
        }
    }

    public boolean clienteEsMayor(Integer edad) {
        return edad >= 18 ? true : false;
    }

    public TarjetaDto crearTarjeta(Cliente cliente, Cuenta cuenta, TarjetaDebito tarjetaDebito) {
        tarjetaDebito.setTipo("Nueva");
        tarjetaDebito.setBloqueo(false);
        tarjetaDebito.setEstado("Pendiente");
        tarjetaDebito.setMetodoPago(false);


        Cuenta cuentaConTarjeta = agregarTarjetaCuenta(cuenta, tarjetaDebito);

        Cliente clienteConTarjeta = agregarTarjetaCliente(cliente, cuentaConTarjeta);

        enviarDatosCliente(clienteConTarjeta);

        return convertirTarjeta(tarjetaDebito);
    }

    public TarjetaDto convertirTarjeta(TarjetaDebito tarjeta) {
        return modelMapper.map(tarjeta, TarjetaDto.class);
    }

    private Cuenta agregarTarjetaCuenta(Cuenta cuenta, TarjetaDebito tarjetaDebito) {
        cuenta.setTarjetaDebito(tarjetaDebito);
        return cuenta;
    }

    public void enviarDatosCliente(Cliente cliente) {
        restTemplate.put("http://localhost:8080/actualizarCliente", cliente);
    }

    public Cliente agregarTarjetaCliente(Cliente cliente, Cuenta cuenta) {
        for (Cuenta c : cliente.getCuentas()) {
            if (c.getCbu().equals(cuenta.getCbu())) {
                c.setTarjetaDebito(cuenta.getTarjetaDebito());
            }
        }
        return cliente;
    }


    public Optional<Cuenta> traerCuenta(Cliente cliente, String cbu) {
        return cliente.getCuentas().stream().filter(c -> c.getCbu().equals(cbu)).findFirst();
    }

    public List<TarjetaConsulta> listarTarjetas(Cliente cliente) {
        List<TarjetaConsulta> tarjetas = new ArrayList<>();
        for (Cuenta c : cliente.getCuentas()) {
            if (c.getTarjetaDebito() != null) {
                TarjetaConsulta tarjeta = modelMapper.map(c.getTarjetaDebito(), TarjetaConsulta.class);
                tarjetas.add(tarjeta);
            }
        }
        return tarjetas;

    }

    public TarjetaDebito buscarTarjeta(Cliente cliente, String nTarjeta) {
        for (Cuenta c : cliente.getCuentas()) {
            if(c.getTarjetaDebito()!=null){
                if (c.getTarjetaDebito().getNumeroTarjeta().equals(nTarjeta)) {
                    if (c.getTarjetaDebito().getEstado().equalsIgnoreCase("Pendiente")) {
                        c.getTarjetaDebito().setEstado("Activa");
                        enviarDatosCliente(cliente);
                        return c.getTarjetaDebito();
                    } else {
                        return new TarjetaDebito("marca");
                    }
                }
            }
        }
        return null;
    }

    public boolean actualizarLimite(Cliente cliente, String tarjeta, double limite){
        for(Cuenta c: cliente.getCuentas()){
            if(c.getTarjetaDebito()!=null){
                if(c.getTarjetaDebito().getNumeroTarjeta().equals(tarjeta)){
                    c.getTarjetaDebito().setLimiteDeExtraccion((float)limite);
                    enviarDatosCliente(cliente);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean bloquearTarjeta(Cliente cliente, String tarjeta){
        for(Cuenta c: cliente.getCuentas()){
            if(c.getTarjetaDebito()!=null){
                if(c.getTarjetaDebito().getNumeroTarjeta().equals(tarjeta)){
                    c.getTarjetaDebito().setBloqueo(true);
                    enviarDatosCliente(cliente);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean vincularTarjeta(Cliente cliente, String tarjeta){
        for(Cuenta c: cliente.getCuentas()){
            if(c.getTarjetaDebito()!=null){
                if(c.getTarjetaDebito().getNumeroTarjeta().equals(tarjeta)){
                    c.getTarjetaDebito().setMetodoPago(true);
                    enviarDatosCliente(cliente);
                    return true;
                }
            }
        }
        return false;
    }


    public boolean clienteCumpleParametros(Cliente cliente, String tarjeta) {
        Cuenta cuenta = buscarCuenta(cliente, tarjeta);

        if (cuenta.getTipoDeCuenta() != null) {
            if (cliente.getCelular() != null && cliente.getEmail() != null) {
                if (cuenta.getSaldo() > 10000) {
                    return true;
                }
            }
        }
        return false;
    }

    public Cuenta buscarCuenta(Cliente cliente, String tarjeta) {
        for (Cuenta c : cliente.getCuentas()) {
            if(c.getTarjetaDebito()!=null){
                if (c.getTarjetaDebito().getNumeroTarjeta().equals(tarjeta)) {
                    return c;
                }
            }
        }
        return new Cuenta();
    }
}
