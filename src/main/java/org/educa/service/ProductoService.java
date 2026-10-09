package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDao;
import org.educa.dao.ProductoDaoImpl;
import org.educa.entity.ProductoEntity;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.ParseException;
import java.util.List;

public class ProductoService {
    public final ProductoDao productoDao = new ProductoDaoImpl();

    /**
     * @param fileXml
     * @return
     * @throws JAXBException
     */
    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
        //TODO: Implementar
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

    /**
     * @param fileXml
     * @return
     * @throws JAXBException
     */
    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        //TODO: Implementar

    }

    /**
     * @param fileXml
     * @return
     * @throws JAXBException
     */
    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        //TODO: Implementar
    }
}
