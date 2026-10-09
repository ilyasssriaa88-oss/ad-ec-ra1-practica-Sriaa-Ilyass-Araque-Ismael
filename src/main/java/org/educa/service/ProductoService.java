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

    /**
     * Lee un archivo XML de inventario, procesa sus productos y calcula los importes financieros
     *
     * @param fileXml Ruta absoluta o relativa del archivo XML que contiene los datos de los productos
     * @return Una lista de {@link ProductoEntity} con todos los cálculos financieros ya asignados
     * @throws JAXBException Si ocurre un error al deserializar o leer el archivo XML
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
     * Genera un informe resumido en formato de texto a partir de un archivo XML
     * Calcula el número total de productos, el beneficio global acumulado y guarda el archivo en la ruta
     *
     * @param path    Ruta del directorio donde se creará el archivo
     * @param fileXml Ruta del archivo XML de inventario que se va a leer
     * @throws JAXBException Si ocurre un error al procesar el archivo XML
     * @throws IOException   Si ocurre un error al crear el directorio o al escribir el archivo de texto
     */
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
        SummaryEntity summaryEntity = new SummaryEntity(fecha, numeroProductos, beneficioTotal, file.getAbsolutePath(), nombreFichero, file.length());

        // Creas la rura de la carpeta de objeto path y la creas
        Path directorio = Paths.get(path);
        Files.createDirectories(directorio);

        // Creas la ruta de el fichero de objeto path
        Path fichero = directorio.resolve("result_" + fecha + ".txt");

        // Creas el archivo
        Files.writeString(fichero, summaryEntity.toPrint());

    }

    /**
     * Exporta los datos detallados de los productos del inventario XML a un archivo Excel
     *
     * @param path    Ruta del directorio donde se guardará el archivo Excel
     * @param fileXml Ruta del archivo XML de inventario de origen
     * @throws JAXBException  Si ocurre un error al procesar el archivo XML
     * @throws IOException    Si ocurre un error al crear los directorios o escribir el archivo Excel
     * @throws ParseException Si ocurre un error al procesar fechas
     */
    public void exportExcel(String path, String fileXml) throws JAXBException, IOException, ParseException {
        File file = new File(fileXml);
        Productos productos = productoDao.getProductos(file);
        // Creo el Workbook dentro de un try-with-resources porque implementa Closeable.
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Productos");
            Row cabecera = sheet.createRow(0);

            cabecera.createCell(0).setCellValue("Codigo");
            cabecera.createCell(1).setCellValue("Número de Serie");
            cabecera.createCell(2).setCellValue("Precio");
            cabecera.createCell(3).setCellValue("Descuento");
            cabecera.createCell(4).setCellValue("Precio Final");
            cabecera.createCell(5).setCellValue("Costes Envío");
            cabecera.createCell(6).setCellValue("Costes Almacenaje");
            cabecera.createCell(7).setCellValue("Beneficio");
            // Creo los estilos
            CellStyle estiloCabecera = workbook.createCellStyle();

            Font Negrita = workbook.createFont();
            Negrita.setBold(true);

            estiloCabecera.setFont(Negrita);

            for (Cell cell : cabecera) {
                cell.setCellStyle(estiloCabecera);
            }

            DataFormat formato = workbook.createDataFormat();

            CellStyle EuroVerde = workbook.createCellStyle();
            EuroVerde.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            EuroVerde.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            EuroVerde.setDataFormat(formato.getFormat("#,##0.00 €"));

            CellStyle EuroBlaco = workbook.createCellStyle();
            EuroBlaco.setDataFormat(formato.getFormat("#,##0.00 €"));

            CellStyle PorcentajeVerde = workbook.createCellStyle();
            PorcentajeVerde.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            PorcentajeVerde.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            PorcentajeVerde.setDataFormat(formato.getFormat("0.00%"));

            CellStyle PorcentajeBlanco = workbook.createCellStyle();
            PorcentajeBlanco.setDataFormat(formato.getFormat("0.00%"));

            CellStyle LetraNegritaVerde = workbook.createCellStyle();
            LetraNegritaVerde.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            LetraNegritaVerde.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            LetraNegritaVerde.setFont(Negrita);

            CellStyle LetraNegritaBanco = workbook.createCellStyle();
            LetraNegritaBanco.setFont(Negrita);

            CellStyle Verde = workbook.createCellStyle();
            Verde.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            Verde.setFillPattern(FillPatternType.SOLID_FOREGROUND);


            int fila = 1;

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

                // Beneficio
                BigDecimal beneficio = precioFinal.subtract(costes);
                // Creo la fila con la variable gloval que luego la voy aumentando
                Row row = sheet.createRow(fila);

                row.createCell(0).setCellValue(producto.getCodigo());
                row.createCell(1).setCellValue(producto.getNumeroSerie());
                row.createCell(2).setCellValue(precio.doubleValue());
                row.createCell(3).setCellValue(descuento.doubleValue() / 100);
                row.createCell(4).setCellValue(precioFinal.doubleValue());
                row.createCell(5).setCellValue(costesEnvio.doubleValue());
                row.createCell(6).setCellValue(costesAlmacenaje.doubleValue());
                row.createCell(7).setCellValue(beneficio.doubleValue());

                if (fila % 2 == 1) {
                    row.getCell(0).setCellStyle(LetraNegritaVerde);
                    row.getCell(1).setCellStyle(Verde);
                    row.getCell(2).setCellStyle(EuroVerde);
                    row.getCell(3).setCellStyle(PorcentajeVerde);
                    row.getCell(4).setCellStyle(EuroVerde);
                    row.getCell(5).setCellStyle(EuroVerde);
                    row.getCell(6).setCellStyle(EuroVerde);
                    row.getCell(7).setCellStyle(EuroVerde);

                } else {
                    row.getCell(0).setCellStyle(LetraNegritaBanco);
                    row.getCell(2).setCellStyle(EuroBlaco);
                    row.getCell(3).setCellStyle(PorcentajeBlanco);
                    row.getCell(4).setCellStyle(EuroBlaco);
                    row.getCell(5).setCellStyle(EuroBlaco);
                    row.getCell(6).setCellStyle(EuroBlaco);
                    row.getCell(7).setCellStyle(EuroBlaco);

                }
                fila++;

                Path directorio = Paths.get(path);
                Files.createDirectories(directorio);

                Path fichero = directorio.resolve("export_junio2026.xlsx");

                try (FileOutputStream outputStream = new FileOutputStream(fichero.toFile())) {
                    workbook.write(outputStream);
                }

            }
        }
    }
}
