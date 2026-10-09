package org.educa.dao;

import generated.Producto;
import generated.Productos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.educa.entity.ProductoEntity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ProductoDaoImpl implements ProductoDao{

    public List<ProductoEntity> readFile(File fileXml) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Productos.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        Productos productos = (Productos) unmarshaller.unmarshal(fileXml);

        List<ProductoEntity> lista = new ArrayList<>();

        for (Producto producto : productos.getProducto()) {
            ProductoEntity productoEntity = new ProductoEntity();
            productoEntity.setProducto(producto);
            lista.add(productoEntity);
        }

        return lista;
    }

    public Productos getProductos(File file) throws JAXBException {
        JAXBContext jaxbContext = JAXBContext.newInstance(Productos.class);
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();

        Productos productos = (Productos) unmarshaller.unmarshal(file);
        return productos;
    }
}
