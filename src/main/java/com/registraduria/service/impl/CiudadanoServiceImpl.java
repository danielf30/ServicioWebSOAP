package com.registraduria.service.impl;

import com.registraduria.entity.Ciudadano;
import com.registraduria.service.CiudadanoService;
import com.registraduria.util.XmlUtil;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.Marshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.charset.StandardCharsets;

@WebService(endpointInterface = "com.registraduria.service.CiudadanoService")
public class CiudadanoServiceImpl implements CiudadanoService {

    private static final String CONNECTION_STRING = "DefaultEndpointsProtocol=https;AccountName=sendmenssagestrigger;AccountKey=9jdd+tniN9/lGlg7jCACKWbL15AyxXNGbo1GRyaDIOhg8YfIaLPtAdvhfGApvLWkLmM8VqZMH3Aa+ASt2x/1Hw==;EndpointSuffix=core.windows.net";
    private static final String CONTAINER_NAME = "output-service-soap";

    private BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(CONNECTION_STRING).buildClient();
    private BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(CONTAINER_NAME);

    @Override
    public String registrarCiudadano(String ciudadanoXml) {
        try {
            // Deserializar el XML en un objeto Ciudadano
            JAXBContext jaxbContext = JAXBContext.newInstance(Ciudadano.class);
            Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
            Ciudadano ciudadano = (Ciudadano) unmarshaller.unmarshal(new StringReader(ciudadanoXml));

            // Usar los setters para establecer los valores del objeto Ciudadano
            ciudadano.setIdCiudadano(ciudadano.getIdCiudadano());
            ciudadano.setTipoDocumentoIdentidad(ciudadano.getTipoDocumentoIdentidad());
            ciudadano.setNumDocumentoIdentidad(ciudadano.getNumDocumentoIdentidad());
            ciudadano.setNombre(ciudadano.getNombre());
            ciudadano.setApellido(ciudadano.getApellido());
            ciudadano.setCorreoElectronico(ciudadano.getCorreoElectronico());
            ciudadano.setOperador(ciudadano.getOperador());

            // Convertir el objeto Ciudadano a XML
            StringWriter writer = new StringWriter();
            JAXBContext context = JAXBContext.newInstance(Ciudadano.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.marshal(ciudadano, writer);
            String ciudadanoAsXml = writer.toString();

            // Escribir el XML en Azure Blob Storage
            BlobClient blobClient = blobContainerClient.getBlobClient(ciudadano.getNumDocumentoIdentidad() + ".xml");
            blobClient.upload(new ByteArrayInputStream(ciudadanoAsXml.getBytes(StandardCharsets.UTF_8)), ciudadanoAsXml.length());

            return "Ciudadano registrado con éxito en Azure Blob Storage";
        } catch (Exception e) {
            e.printStackTrace();
            return "Error registrando ciudadano en Azure Blob Storage";
        }
    }

    @Override
    public String actualizarInformacion(String client_identifier, String ciudadanoXml) {
        BlobClient blobClient = blobContainerClient.getBlobClient(client_identifier + ".xml");

        if (blobClient.exists()) {
            try {
                blobClient.upload(new ByteArrayInputStream(ciudadanoXml.getBytes(StandardCharsets.UTF_8)), ciudadanoXml.length(), true);

                return "Información actualizada con éxito en Azure Blob Storage";
            } catch (Exception e) {
                e.printStackTrace();
                return "Error actualizando información en Azure Blob Storage";
            }
        } else {
            return "Ciudadano no encontrado en Azure Blob Storage";
        }
    }
}
