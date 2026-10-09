package org.educa.service;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.educa.dao.ProductoDao;
import org.educa.dao.ProductoDaoImpl;
import org.educa.entity.ProductoEntity;
import org.educa.entity.SummaryEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.List;

public class ProductoService {

    public final ProductoDao productoDao = new ProductoDaoImpl();

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
        List<ProductoEntity> lista = productoDao.readFile(new File(fileXml));
        for (ProductoEntity productoEntity : lista) {
            // Precio Final
            BigDecimal precio = productoEntity.getProducto().getPrecio();
            BigDecimal descuento = productoEntity.getProducto().getDescuento();
            BigDecimal importeDescuento = precio
                    .multiply(descuento)
                    .divide(BigDecimal.valueOf(100));
            BigDecimal precioFinal = precio.subtract(importeDescuento).setScale(2, RoundingMode.CEILING);
            productoEntity.setPrecioFinal(precioFinal);

            // Costes
            BigDecimal costesAlmacenaje = productoEntity.getProducto().getCostes().getCostesAlmacenaje();
            BigDecimal costesEnvio = productoEntity.getProducto().getCostes().getCostesEnvio();
            BigDecimal costes = costesAlmacenaje.add(costesEnvio);
            productoEntity.setCost(costes);

            // Beneficio
            BigDecimal beneficios = precioFinal.subtract(costes);
            productoEntity.setProfit(beneficios);

        }
        return lista;
    }

    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        File file = new File(fileXml);
        Productos productos = productoDao.getProductos(file);

        // Nimero productos
        int numeroProductos = productos.getProducto().size();

        // Beneficios totales
        BigDecimal beneficioTotal = BigDecimal.ZERO;
        for (Producto producto : productos.getProducto()) {
            // Precio Final
            BigDecimal precio = producto.getPrecio();
            BigDecimal descuento = producto.getDescuento();
            BigDecimal importeDescuento = precio
                    .multiply(descuento)
                    .divide(BigDecimal.valueOf(100));
            BigDecimal precioFinal = precio
                    .subtract(importeDescuento)
                    .setScale(2, RoundingMode.CEILING);
            // Costes
            BigDecimal costesAlmacenaje = producto.getCostes().getCostesAlmacenaje();
            BigDecimal costesEnvio = producto.getCostes().getCostesEnvio();
            BigDecimal costes = costesAlmacenaje.add(costesEnvio);

            // Beneficio total
            BigDecimal beneficio = precioFinal.subtract(costes);
            beneficioTotal = beneficioTotal.add(beneficio);
        }
        // Fecha par el nombre
        String nombreFichero = file.getName().replace(".xml", "");
        String fecha = nombreFichero.replace("inventario_", "");

        // Creas la entidad con los datos obtenodos
        SummaryEntity summaryEntity = new SummaryEntity(fecha,
                numeroProductos,
                beneficioTotal,
                file.getAbsolutePath(),
                nombreFichero,
                file.length()
        );
        // Creas la rura de la carpeta de objeto path y la creas
        Path directorio = Paths.get(path);
        Files.createDirectories(directorio);
        // Creas la ruta de el fichero de objeto path
        Path fichero = directorio.resolve("result_" + fecha + ".txt");
        // Creas el archivo
        Files.writeString(fichero, summaryEntity.toPrint());

    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
    // TODO: Implementar
    }
}
