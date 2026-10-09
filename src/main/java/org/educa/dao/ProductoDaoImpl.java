package org.educa.dao;

import generated.Productos;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;
import org.educa.entity.ProductoEntity;

import java.io.File;
import java.util.List;

public class ProductoDaoImpl implements ProductoDao {
    @Override
    public List<ProductoEntity> readFile(File fileXml) throws JAXBException {
        return null;
    }
}
