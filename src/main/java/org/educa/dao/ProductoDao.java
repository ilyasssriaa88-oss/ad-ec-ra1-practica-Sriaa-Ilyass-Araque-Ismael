package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBException;
import org.educa.entity.ProductoEntity;

import java.io.File;
import java.util.List;

public interface ProductoDao {
    List<ProductoEntity> readFile(File fileXml) throws JAXBException;
}
