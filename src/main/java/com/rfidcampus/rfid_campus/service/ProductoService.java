package com.rfidcampus.rfid_campus.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rfidcampus.rfid_campus.model.Producto;
import com.rfidcampus.rfid_campus.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    // Método principal que llama al controlador
    public List<Producto> listarProductosOrdenados(String algoritmo) {
        // Obtenemos la lista de la BD
        List<Producto> productos = new ArrayList<>(productoRepository.findAll());

        switch (algoritmo.toLowerCase()) {
            case "intercambio":
                ordenarPorIntercambio(productos);
                break;
            case "seleccion":
                ordenarPorSeleccion(productos);
                break;
            case "insercion":
                ordenarPorInsercion(productos);
                break;
            case "shell":
                ordenarPorShell(productos);
                break;
            default:
                // Por defecto Intercambio si no se especifica
                ordenarPorIntercambio(productos);
                break;
        }
        return productos;
    }

    // ============================================================
    // 1. MÉTODO DE INTERCAMBIO (Burbuja mejorada o estándar)
    // ============================================================
    private void ordenarPorIntercambio(List<Producto> lista) {
        int n = lista.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (lista.get(j).getPrecio().compareTo(lista.get(j + 1).getPrecio()) > 0) {
                    swap(lista, j, j + 1);
                }
            }
        }
    }

    // ============================================================
    // 2. MÉTODO DE SELECCIÓN
    // ============================================================
    private void ordenarPorSeleccion(List<Producto> lista) {
        int n = lista.size();
        for (int i = 0; i < n - 1; i++) {
            int indiceMinimo = i;
            for (int j = i + 1; j < n; j++) {
                if (lista.get(j).getPrecio().compareTo(lista.get(indiceMinimo).getPrecio()) < 0) {
                    indiceMinimo = j;
                }
            }
            swap(lista, i, indiceMinimo);
        }
    }

    // ============================================================
    // 3. MÉTODO DE INSERCIÓN
    // ============================================================
    private void ordenarPorInsercion(List<Producto> lista) {
        int n = lista.size();
        for (int i = 1; i < n; i++) {
            Producto key = lista.get(i);
            int j = i - 1;

            while (j >= 0 && lista.get(j).getPrecio().compareTo(key.getPrecio()) > 0) {
                lista.set(j + 1, lista.get(j));
                j = j - 1;
            }
            lista.set(j + 1, key);
        }
    }

    // ============================================================
    // 4. MÉTODO SHELL
    // ============================================================
    private void ordenarPorShell(List<Producto> lista) {
        int n = lista.size();
        for (int gap = n / 2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i += 1) {
                Producto temp = lista.get(i);
                int j;
                for (j = i; j >= gap && lista.get(j - gap).getPrecio().compareTo(temp.getPrecio()) > 0; j -= gap) {
                    lista.set(j, lista.get(j - gap));
                }
                lista.set(j, temp);
            }
        }
    }

    // ============================================================
    // ✅ BÚSQUEDA BINARIA (Requiere lista ordenada)
    // ============================================================
    public Producto buscarPorPrecioBinario(double precioBuscado) {
        // 1. Primero ordenamos (Shell es rápido)
        List<Producto> lista = listarProductosOrdenados("shell");
        
        int izquierda = 0;
        int derecha = lista.size() - 1;

        while (izquierda <= derecha) {
            int medio = izquierda + (derecha - izquierda) / 2;
            double precioMedio = lista.get(medio).getPrecio().doubleValue();

            if (precioMedio == precioBuscado) {
                return lista.get(medio);
            }

            if (precioMedio < precioBuscado) {
                izquierda = medio + 1;
            } else {
                derecha = medio - 1;
            }
        }
        return null; // No encontrado
    }

    // Auxiliar para intercambiar elementos
    private void swap(List<Producto> lista, int i, int j) {
        Producto temp = lista.get(i);
        lista.set(i, lista.get(j));
        lista.set(j, temp);
    }
    
    // Métodos CRUD básicos
    public List<Producto> listarTodos() { return productoRepository.findAll(); }
    public Producto guardar(Producto p) { return productoRepository.save(p); }
    public void eliminar(Long id) { productoRepository.deleteById(id); }
}