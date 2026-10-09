package org.educa.service;

import jakarta.xml.bind.JAXBException;
import org.educa.dao.ProductoDao;
import org.educa.dao.ProductoDaoImpl;
import org.educa.entity.ProductoEntity;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;

public class ProductoService {

    public final ProductoDao productoDao = new ProductoDaoImpl();

    public List<ProductoEntity> readFile(String fileXml) throws JAXBException {
        List<ProductoEntity> lista = productoDao.readFile(new File(fileXml));

        return lista;
    }

    public void exportSummary(String path, String fileXml) throws JAXBException, IOException {
        // TODO: Implementar
    }

    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        // TODO: Implementar
    }
}
