package com.cudeca.model.negocio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MonederoTest {

    private Monedero monedero;

    @BeforeEach
    void setUp() {
        monedero = new Monedero();
    }

    @Test
    @DisplayName("Consultar saldo inicial es cero")
    void testConsultarSaldoInicial() {
        assertEquals(BigDecimal.ZERO, monedero.consultarSaldo());
    }

    @Test
    @DisplayName("Ingresar importe positivo aumenta el saldo")
    void testIngresarImportePositivo() {
        monedero.ingresar(new BigDecimal("100.50"));
        assertEquals(new BigDecimal("100.50"), monedero.consultarSaldo());
    }

    @Test
    @DisplayName("Ingresar múltiples importes se acumula correctamente")
    void testIngresarMultiplesImportes() {
        monedero.ingresar(new BigDecimal("50"));
        monedero.ingresar(new BigDecimal("25.25"));
        assertEquals(new BigDecimal("75.25"), monedero.consultarSaldo());
    }

    @Test
    @DisplayName("Retirar importe con saldo suficiente disminuye el saldo")
    void testRetirarConSaldoSuficiente() {
        monedero.ingresar(new BigDecimal("200"));
        monedero.retirar(new BigDecimal("75"));
        assertEquals(new BigDecimal("125"), monedero.consultarSaldo());
    }

    @Test
    @DisplayName("Retirar importe exacto deja el saldo en cero")
    void testRetirarImporteExacto() {
        monedero.ingresar(new BigDecimal("100"));
        monedero.retirar(new BigDecimal("100"));
        assertEquals(BigDecimal.ZERO, monedero.consultarSaldo());
    }

    @Test
    @DisplayName("Retirar importe mayor que el saldo lanza IllegalStateException")
    void testRetirarConSaldoInsuficiente() {
        monedero.ingresar(new BigDecimal("50"));
        assertThrows(IllegalStateException.class, () -> {
            monedero.retirar(new BigDecimal("100"));
        });
    }

    @Test
    @DisplayName("Ingresar importe nulo lanza IllegalArgumentException")
    void testIngresarImporteNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            monedero.ingresar(null);
        });
    }

    @Test
    @DisplayName("Ingresar importe cero lanza IllegalArgumentException")
    void testIngresarImporteCero() {
        assertThrows(IllegalArgumentException.class, () -> {
            monedero.ingresar(BigDecimal.ZERO);
        });
    }

    @Test
    @DisplayName("Ingresar importe negativo lanza IllegalArgumentException")
    void testIngresarImporteNegativo() {
        assertThrows(IllegalArgumentException.class, () -> {
            monedero.ingresar(new BigDecimal("-50"));
        });
    }

    @Test
    @DisplayName("Retirar importe nulo lanza IllegalArgumentException")
    void testRetirarImporteNulo() {
        assertThrows(IllegalArgumentException.class, () -> {
            monedero.retirar(null);
        });
    }

    @Test
    @DisplayName("Retirar importe cero lanza IllegalArgumentException")
    void testRetirarImporteCero() {
        assertThrows(IllegalArgumentException.class, () -> {
            monedero.retirar(BigDecimal.ZERO);
        });
    }

    @Test
    @DisplayName("Retirar importe negativo lanza IllegalArgumentException")
    void testRetirarImporteNegativo() {
        assertThrows(IllegalArgumentException.class, () -> {
            monedero.retirar(new BigDecimal("-50"));
        });
    }
}
