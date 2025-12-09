package com.cudeca.service;

import com.cudeca.exception.AsientoNoDisponibleException;
import com.cudeca.model.enums.EstadoAsiento;
import com.cudeca.model.evento.Asiento;
import com.cudeca.model.evento.Evento;
import com.cudeca.model.evento.ZonaRecinto;
import com.cudeca.model.evento.TipoEntrada;
import com.cudeca.repository.AsientoRepository;
import com.cudeca.repository.EventoRepository;
import com.cudeca.repository.TipoEntradaRepository;
import com.cudeca.repository.ZonaRecintoRepository;
import com.cudeca.testutil.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

/**
 * Test de concurrencia para verificar el bloqueo PESSIMISTIC_WRITE de asientos.
 * Simula múltiples usuarios intentando reservar el mismo asiento simultáneamente.
 *
 * CRÍTICO: Este test verifica que NO haya sobreventa de asientos.
 */
@IntegrationTest
@DisplayName("AsientoConcurrency - Test de Bloqueo Transaccional")
class AsientoConcurrencyTest {

    @Autowired
    private AsientoRepository asientoRepository;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private ZonaRecintoRepository zonaRecintoRepository;

    @Autowired
    private TipoEntradaRepository tipoEntradaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Asiento asientoCompartido;
    private ZonaRecinto zona;
    private Evento evento;
    private TipoEntrada tipoEntrada;

    @BeforeEach
    void setUp() {
        // Limpiar datos previos
        asientoRepository.deleteAll();
        zonaRecintoRepository.deleteAll();
        eventoRepository.deleteAll();

        // Crear evento de prueba
        evento = Evento.builder()
                .nombre("Concierto de Prueba Concurrencia")
                .descripcion("Evento para probar bloqueos")
                .fechaInicio(java.time.Instant.now().plus(30, java.time.temporal.ChronoUnit.DAYS))
                .build();
        evento = eventoRepository.save(evento);

        // Crear tipo de entrada
        tipoEntrada = TipoEntrada.builder()
                .nombre("Entrada General")
                .costeBase(java.math.BigDecimal.valueOf(25.00))
                .cantidadTotal(100)
                .evento(evento)
                .build();
        tipoEntrada = tipoEntradaRepository.save(tipoEntrada);

        // Crear zona
        zona = ZonaRecinto.builder()
                .nombre("Zona VIP")
                .aforoTotal(50)
                .evento(evento)
                .asientos(new ArrayList<>())
                .build();
        zona = zonaRecintoRepository.save(zona);

        // Crear asiento compartido (el que intentarán reservar múltiples threads)
        asientoCompartido = Asiento.builder()
                .zona(zona)
                .tipoEntrada(tipoEntrada)
                .codigoEtiqueta("A1")
                .estado(EstadoAsiento.LIBRE)
                .fila(1)
                .columna(1)
                .build();
        asientoCompartido = asientoRepository.save(asientoCompartido);
    }

    @Test
    @DisplayName("Solo un thread debe poder bloquear el mismo asiento (test de concurrencia)")
    void testBloqueoOptimista_SoloUnThreadGana() throws InterruptedException {
        // Arrange
        int numThreads = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1); // Para sincronizar el inicio
        AtomicInteger bloqueoExitoso = new AtomicInteger(0);
        AtomicInteger bloqueoFallido = new AtomicInteger(0);

        List<Future<Boolean>> futures = new ArrayList<>();

        // Act - Lanzar múltiples threads que intenten bloquear el mismo asiento
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            Future<Boolean> future = executor.submit(() -> {
                try {
                    // Esperar a que todos los threads estén listos
                    latch.await();

                    // Intentar bloquear el asiento
                    return intentarBloquearAsiento(asientoCompartido.getId(), threadId);
                } catch (Exception e) {
                    System.err.println("Thread " + threadId + " falló: " + e.getMessage());
                    return false;
                }
            });
            futures.add(future);
        }

        // Dar señal de inicio a todos los threads simultáneamente
        Thread.sleep(100); // Pequeña pausa para que todos estén esperando
        latch.countDown();

        // Esperar a que todos terminen
        for (Future<Boolean> future : futures) {
            try {
                Boolean resultado = future.get(5, TimeUnit.SECONDS);
                if (resultado) {
                    bloqueoExitoso.incrementAndGet();
                } else {
                    bloqueoFallido.incrementAndGet();
                }
            } catch (TimeoutException | ExecutionException e) {
                bloqueoFallido.incrementAndGet();
                System.err.println("Thread timeout o error: " + e.getMessage());
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Assert - Solo UN thread debería haber tenido éxito
        System.out.println("Bloqueos exitosos: " + bloqueoExitoso.get());
        System.out.println("Bloqueos fallidos: " + bloqueoFallido.get());

        assertThat(bloqueoExitoso.get()).isEqualTo(1);
        assertThat(bloqueoFallido.get()).isEqualTo(numThreads - 1);

        // Verificar que el asiento quedó BLOQUEADO
        Asiento asientoFinal = asientoRepository.findById(asientoCompartido.getId()).orElseThrow();
        assertThat(asientoFinal.getEstado()).isEqualTo(EstadoAsiento.BLOQUEADO);
    }

    @Test
    @DisplayName("Múltiples threads pueden bloquear asientos DIFERENTES simultáneamente")
    void testBloqueoMultiplesAsientos_TodosExitosos() throws InterruptedException {
        // Arrange - Crear varios asientos
        List<Asiento> asientos = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            Asiento asiento = Asiento.builder()
                    .zona(zona)
                    .tipoEntrada(tipoEntrada)
                    .codigoEtiqueta("B" + i)
                    .estado(EstadoAsiento.LIBRE)
                    .fila(2)
                    .columna(i)
                    .build();
            asientos.add(asientoRepository.save(asiento));
        }

        int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger exitosos = new AtomicInteger(0);

        List<Future<Boolean>> futures = new ArrayList<>();

        // Act - Cada thread bloquea un asiento diferente
        for (int i = 0; i < numThreads; i++) {
            final int index = i;
            Future<Boolean> future = executor.submit(() -> {
                try {
                    latch.await();
                    return intentarBloquearAsiento(asientos.get(index).getId(), index);
                } catch (Exception e) {
                    return false;
                }
            });
            futures.add(future);
        }

        latch.countDown();

        for (Future<Boolean> future : futures) {
            try {
                if (future.get(5, TimeUnit.SECONDS)) {
                    exitosos.incrementAndGet();
                }
            } catch (Exception e) {
                // Falló
            }
        }

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        // Assert - TODOS deberían haber tenido éxito (asientos diferentes)
        assertThat(exitosos.get()).isEqualTo(numThreads);

        // Verificar que todos los asientos están BLOQUEADOS
        for (Asiento asiento : asientos) {
            Asiento actualizado = asientoRepository.findById(asiento.getId()).orElseThrow();
            assertThat(actualizado.getEstado()).isEqualTo(EstadoAsiento.BLOQUEADO);
        }
    }

    @Test
    @DisplayName("No se puede bloquear un asiento ya BLOQUEADO")
    void testBloquearAsientoYaBloqueado_DebeRechazar() {
        // Arrange - Bloquear el asiento primero
        asientoCompartido.bloquear();
        asientoRepository.save(asientoCompartido);

        // Act & Assert
        assertThatThrownBy(() -> intentarBloquearAsientoConExcepcion(asientoCompartido.getId(), 999))
                .isInstanceOf(AsientoNoDisponibleException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    @DisplayName("No se puede bloquear un asiento VENDIDO")
    void testBloquearAsientoOcupado_DebeRechazar() {
        // Arrange
        asientoCompartido.vender();
        asientoRepository.save(asientoCompartido);

        // Act & Assert
        assertThatThrownBy(() -> intentarBloquearAsientoConExcepcion(asientoCompartido.getId(), 999))
                .isInstanceOf(AsientoNoDisponibleException.class)
                .hasMessageContaining("no está disponible");
    }

    @Test
    @DisplayName("Test de estrés: 100 threads intentando reservar 10 asientos")
    void testEstres_100Threads_10Asientos() throws InterruptedException {
        // Arrange - Crear 10 asientos
        List<Asiento> asientos = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Asiento asiento = Asiento.builder()
                    .zona(zona)
                    .tipoEntrada(tipoEntrada)
                    .codigoEtiqueta("C" + i)
                    .estado(EstadoAsiento.LIBRE)
                    .fila(3)
                    .columna(i)
                    .build();
            asientos.add(asientoRepository.save(asiento));
        }

        int numThreads = 100;
        ExecutorService executor = Executors.newFixedThreadPool(20); // Pool de 20
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger exitosos = new AtomicInteger(0);
        AtomicInteger fallidos = new AtomicInteger(0);

        List<Future<Boolean>> futures = new ArrayList<>();

        // Act - 100 threads compiten por 10 asientos
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            // Cada thread intenta reservar un asiento aleatorio
            final int asientoIndex = i % asientos.size();

            Future<Boolean> future = executor.submit(() -> {
                try {
                    latch.await();
                    return intentarBloquearAsiento(asientos.get(asientoIndex).getId(), threadId);
                } catch (Exception e) {
                    return false;
                }
            });
            futures.add(future);
        }

        latch.countDown();

        for (Future<Boolean> future : futures) {
            try {
                if (future.get(10, TimeUnit.SECONDS)) {
                    exitosos.incrementAndGet();
                } else {
                    fallidos.incrementAndGet();
                }
            } catch (Exception e) {
                fallidos.incrementAndGet();
            }
        }

        executor.shutdown();
        executor.awaitTermination(15, TimeUnit.SECONDS);

        // Assert - Exactamente 10 deberían tener éxito (uno por asiento)
        System.out.println("Exitosos: " + exitosos.get() + ", Fallidos: " + fallidos.get());
        assertThat(exitosos.get()).isEqualTo(10);
        assertThat(fallidos.get()).isEqualTo(90);

        // Verificar que los 10 asientos están bloqueados
        long asientosBloqueados = asientoRepository.findAll().stream()
                .filter(a -> a.getEstado() == EstadoAsiento.BLOQUEADO)
                .count();
        assertThat(asientosBloqueados).isEqualTo(10);
    }

    @Test
    @DisplayName("Verificar que findAllByIdWithLock realmente bloquea")
    void testFindAllByIdWithLock_BloqueoReal() throws Exception {
        // Arrange
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch thread1Iniciado = new CountDownLatch(1);
        CountDownLatch thread2PuedeEmpezar = new CountDownLatch(1);
        AtomicInteger ordenDeEjecucion = new AtomicInteger(0);
        AtomicInteger thread2Bloqueado = new AtomicInteger(0);

        // Thread 1: Bloquea el asiento con lock y espera DENTRO DE LA TRANSACCIÓN
        Future<Integer> future1 = executor.submit(() -> {
            return transactionTemplate.execute(status -> {
                try {
                    List<Asiento> asientos = asientoRepository.findAllByIdWithLock(
                        List.of(asientoCompartido.getId())
                    );

                    thread1Iniciado.countDown(); // Avisa que tiene el lock
                    thread2PuedeEmpezar.await(3, TimeUnit.SECONDS); // Espera señal (con timeout)

                    // Modifica el asiento
                    asientos.get(0).bloquear();
                    asientoRepository.saveAll(asientos);

                    return ordenDeEjecucion.incrementAndGet(); // Debería ser 1
                } catch (InterruptedException e) {
                    status.setRollbackOnly();
                    throw new RuntimeException(e);
                }
            });
        });

        // Esperar a que thread1 obtenga el lock
        thread1Iniciado.await(2, TimeUnit.SECONDS);
        Thread.sleep(100); // Asegurar que thread1 tiene el lock

        // Thread 2: Intenta obtener el mismo asiento (debería esperar)
        Future<Integer> future2 = executor.submit(() -> {
            return transactionTemplate.execute(status -> {
                thread2Bloqueado.set(1); // Marca que está intentando
                List<Asiento> asientos = asientoRepository.findAllByIdWithLock(
                    List.of(asientoCompartido.getId())
                );

                // Este código NO debería ejecutarse hasta que thread1 libere el lock
                return ordenDeEjecucion.incrementAndGet(); // Debería ser 2
            });
        });

        Thread.sleep(300); // Dar tiempo a thread2 para intentar acceder y quedar bloqueado

        // Verificar que thread2 está intentando pero no ha incrementado el contador
        assertThat(thread2Bloqueado.get()).isEqualTo(1);
        assertThat(ordenDeEjecucion.get()).isEqualTo(0); // Ninguno ha terminado aún

        // Liberar thread1
        thread2PuedeEmpezar.countDown();

        // Assert
        Integer orden1 = future1.get(5, TimeUnit.SECONDS);
        Integer orden2 = future2.get(5, TimeUnit.SECONDS);

        assertThat(orden1).isEqualTo(1); // Thread1 termina primero
        assertThat(orden2).isEqualTo(2); // Thread2 espera y termina segundo

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }

    /**
     * Método auxiliar que simula el bloqueo transaccional de un asiento.
     * Este método debería estar en CheckoutService en producción.
     * Usa TransactionTemplate para funcionar correctamente en threads del ExecutorService.
     */
    boolean intentarBloquearAsiento(Long asientoId, int threadId) {
        try {
            return transactionTemplate.execute(status -> {
                try {
                    // 1. Obtener asiento con bloqueo PESSIMISTIC_WRITE
                    List<Asiento> asientos = asientoRepository.findAllByIdWithLock(List.of(asientoId));

                    if (asientos.isEmpty()) {
                        throw new IllegalArgumentException("Asiento no encontrado");
                    }

                    Asiento asiento = asientos.get(0);

                    // 2. Verificar que está LIBRE
                    if (asiento.getEstado() != EstadoAsiento.LIBRE) {
                        throw new AsientoNoDisponibleException(
                            "El asiento " + asiento.getCodigoEtiqueta() + " no está disponible"
                        );
                    }

                    // 3. Bloquear asiento
                    asiento.bloquear();
                    asientoRepository.save(asiento);

                    System.out.println("Thread " + threadId + " bloqueó exitosamente el asiento " +
                                     asiento.getCodigoEtiqueta());
                    return true;

                } catch (AsientoNoDisponibleException e) {
                    System.out.println("Thread " + threadId + " falló: " + e.getMessage());
                    status.setRollbackOnly();
                    return false;
                }
            });
        } catch (Exception e) {
            System.out.println("Thread " + threadId + " error inesperado: " + e.getMessage());
            return false;
        }
    }

    /**
     * Método auxiliar que lanza excepciones para tests que esperan AsientoNoDisponibleException.
     */
    void intentarBloquearAsientoConExcepcion(Long asientoId, int threadId) {
        transactionTemplate.execute(status -> {
            // 1. Obtener asiento con bloqueo PESSIMISTIC_WRITE
            List<Asiento> asientos = asientoRepository.findAllByIdWithLock(List.of(asientoId));

            if (asientos.isEmpty()) {
                throw new IllegalArgumentException("Asiento no encontrado");
            }

            Asiento asiento = asientos.get(0);

            // 2. Verificar que está LIBRE
            if (asiento.getEstado() != EstadoAsiento.LIBRE) {
                throw new AsientoNoDisponibleException(
                    "El asiento " + asiento.getCodigoEtiqueta() + " no está disponible"
                );
            }

            // 3. Bloquear asiento
            asiento.bloquear();
            asientoRepository.save(asiento);

            System.out.println("Thread " + threadId + " bloqueó exitosamente el asiento " +
                             asiento.getCodigoEtiqueta());
            return true;
        });
    }
}

