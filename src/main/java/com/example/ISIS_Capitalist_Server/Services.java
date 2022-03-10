/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.ISIS_Capitalist_Server;

import com.example.ISIS_Capitalist_Server.generated.World;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author agodfrin
 */
public class Services {
    
    public World readWorldFromXml(String pseudo) throws JAXBException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        InputStream input = getClass().getClassLoader().getResourceAsStream(pseudo+"-world.xml");
        if (input == null){
            input = getClass().getClassLoader().getResourceAsStream("world.xml");
        }
        World w = (World) u.unmarshal(input);
        return w;
    }
    
    public void saveWorldToXml(String pseudo, World world) throws JAXBException, FileNotFoundException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        OutputStream output = new FileOutputStream(pseudo+"-world.xml");
        m.marshal(world, output);
    }
    
    public World getWorld(String pseudo) throws JAXBException {
        return readWorldFromXml(pseudo);
    }
    
}
