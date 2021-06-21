package space.gavinklfong.demo.streamapi;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.asm.Advice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import space.gavinklfong.demo.streamapi.models.Order;
import space.gavinklfong.demo.streamapi.models.Product;
import space.gavinklfong.demo.streamapi.repos.CustomerRepo;
import space.gavinklfong.demo.streamapi.repos.OrderRepo;
import space.gavinklfong.demo.streamapi.repos.ProductRepo;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@DataJpaTest
public class StreamApiVicTest {

    @Autowired
    private CustomerRepo customerRepo;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Test
    void ejercicio1() {
        log.info("-----Obtener la lista de productos que pertenecen a la categoria Books con price>100-----");
        List<Product> resultado=productRepo.findAll()
                .stream()
                .filter(producto->producto.getCategory().equalsIgnoreCase("Books"))
                .filter(producto -> producto.getPrice()>100)
                .collect(Collectors.toList());

        log.info(resultado.toString());
    }

    @Test
    void ejercicio2() {
        log.info("-----Otener una lista de ordenes con productos que pertenecen a la categoria 'Baby' -----");
        List<Order> resultado=orderRepo.findAll()
                .stream()
                .filter(orden->
                        orden.getProducts()
                        .stream()
                        .anyMatch(producto->producto.getCategory().equalsIgnoreCase("Baby")))
                .collect(Collectors.toList());
        log.info(String.valueOf(resultado.size()));
        log.info(resultado.toString());
    }

    @Test
    void ejercicio3() {
        log.info("-----Obtener una lista de productos con cateria 'TOYS' y luego aplique un descuento del 10%  -----");
        List<Product> resultado=productRepo.findAll()
                .stream()
                .filter(producto->producto.getCategory().equalsIgnoreCase("Toys"))
                .map(producto-> producto.withPrice(producto.getPrice()*0.9))//with es una anotacion de lombok que modifica el atributo y crea una clon de la colecccion
                .collect(Collectors.toList());
        log.info(String.valueOf(resultado.size()));
        log.info(resultado.toString());
    }


    @Test
    void ejercicio4() {
        log.info("-----Obtener un lista de productos ordenados por CLIENTE de nivel 2 entre el 01/02/2021 y 01/04/2021-----");
        List<Product> resutlado=orderRepo.findAll()
                .stream()
                .filter(orden->orden.getCustomer().getTier()==2)//Filtrar los cliente de nivel 2
                .filter(orden->orden.getOrderDate().compareTo(LocalDate.of(2021, 2, 1))>=0)//filtrar las ordenes mayor al 2021.02.01
                .filter(orden->orden.getOrderDate().compareTo(LocalDate.of(2021, 4, 1))<=0)//filtrar las ordenes menores al 2021.04.01
                .flatMap(orden->orden.getProducts().stream())//Aplanar en un stream de productos. Cada orden contiene una lista de productos.
                .distinct()//De los productos obtener solo los distintos
                .collect(Collectors.toList());//Obtener una collection de productos

        log.info(String.valueOf(resutlado.size()));
        log.info(resutlado.toString());
    }

    @Test
    void ejercicio5() {
        log.info("-----Obtener los productos mas baratos de la categoria 'Libros'-----");
        Optional<Product> resultado=productRepo.findAll()
                .stream()//Convierte a strem la lista de productos
                .filter(producto->producto.getCategory().equalsIgnoreCase("Books"))//Filtra todos los producto de la cateroia Books
                .sorted(Comparator.comparing(Product::getPrice))//Ordena los productos ascendentemente
                .findFirst();//Obtiene el primer producto

        log.info(resultado.get().toString());
    }

    @Test
    void ejecicio5_v2() {
        Optional<Product> resultado=productRepo.findAll()
                .stream()
                .filter(producto->producto.getCategory().equalsIgnoreCase("Books"))
                .min(Comparator.comparing(Product::getPrice));
        log.info(resultado.get().toString());
    }

    @Test
    void ejercicio6() {
        log.info("----- Obtener las 3 ordenes recientes -----");
        List<Order> result=orderRepo.findAll()
                .stream()
                .sorted(Comparator.comparing(Order::getOrderDate).reversed())
                .limit(3)
                .collect(Collectors.toList());
    }

    @Test
    void ejercicio7() {
        log.info("----- Obtener una lista de ordenes que se piedron el 2021.03.15, mostrar las ordenes en cosoloa y luego devuelva la lista de ordenes -----");
        List<Product> resultado=orderRepo.findAll()
                .stream()
                .filter(orden->orden.getOrderDate().isEqual(LocalDate.of(2021,3,15)))
                .peek(orden-> System.out.println(orden.toString()))//Depuracion, ver los elementos a medida que fluye
                .flatMap(orden->orden.getProducts().stream())
                .distinct()
                .collect(Collectors.toList());

        log.info(String.valueOf(resultado.size()));
        log.info(resultado.toString());
    }

    @Test
    void ejercicio8() {
        log.info("----- Calcular la suma globla de todas las ordenes realizados en Febrero del 2021  -----");
        Double resultado=orderRepo.findAll()
                .stream()
                .filter(orden->orden.getOrderDate().compareTo(LocalDate.of(2021, 2, 1))>=0)
                .filter(orden->orden.getOrderDate().compareTo(LocalDate.of(2021,3,1))<0)
                .flatMap(orden->orden.getProducts().stream())
                .mapToDouble(producto->producto.getPrice())
                .sum();
        log.info(resultado.toString());

    }

    @Test
    void ejercicio9() {
        log.info("----- Calcular el pago promedio de los pedidos realizados el 2021.03.14 -----");
        Double resultado=orderRepo.findAll()
                .stream()
                .filter(orden->orden.getOrderDate().isEqual(LocalDate.of(2021, 03, 15)))
                .flatMap(orden->orden.getProducts().stream())
                .mapToDouble(producto->producto.getPrice())
                .average().getAsDouble();
        log.info(resultado.toString());
    }

    @Test
    void ejercicio10() {
        log.info("----- Obtener una coleccion de estadisticas (suma, promedio, maximo, minimo) de todos los productos por categoria 'Books'  -----");
        DoubleSummaryStatistics estadisticas= productRepo.findAll()
                .stream()
                .filter(producto->producto.getCategory().equalsIgnoreCase("Books"))
                .mapToDouble(producto->producto.getPrice())
                .summaryStatistics();

        log.info(estadisticas.toString());
    }
}
