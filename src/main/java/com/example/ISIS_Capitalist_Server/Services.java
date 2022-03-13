/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.ISIS_Capitalist_Server;

import com.example.ISIS_Capitalist_Server.generated.PallierType;
import com.example.ISIS_Capitalist_Server.generated.ProductType;
import com.example.ISIS_Capitalist_Server.generated.ProductsType;
import com.example.ISIS_Capitalist_Server.generated.TyperatioType;
import com.example.ISIS_Capitalist_Server.generated.World;
import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author agodfrin
 */
public class Services {

    public World readWorldFromXml(String username) throws JAXBException, FileNotFoundException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Unmarshaller u = cont.createUnmarshaller();
        World world;

        /*InputStream input = getClass().getClassLoader().getResourceAsStream(pseudo + "-world.xml");
        if (input == null) {
            input = getClass().getClassLoader().getResourceAsStream("world.xml");
        }
        world = (World) u.unmarshal(input);
        updateScore(world);
        return world;*/
        if (new File(username + "_world.xml").exists()) {
            System.out.println("find !");
            world = (World) u.unmarshal(new File(username + "_world.xml"));
        } else {
            InputStream input = getClass().getClassLoader().getResourceAsStream("world.xml");
            System.out.println("world : ??" + input);
            world = (World) u.unmarshal(input);
        }
        updateScore(world);
        saveWorldToXml(world, username);
        return world;

    }

    public void saveWorldToXml(World world, String username) throws JAXBException, FileNotFoundException {
        JAXBContext cont = JAXBContext.newInstance(World.class);
        Marshaller m = cont.createMarshaller();
        OutputStream output = new FileOutputStream(username + "-world.xml");
        m.marshal(world, output);
    }

    public World getWorld(String username) throws JAXBException, FileNotFoundException {
        World w = this.readWorldFromXml(username);
        this.updateScore(w);
        w.setLastupdate(System.currentTimeMillis());
        this.saveWorldToXml(w, username);
        return w;
    }

    public ProductType findProductById(World world, int id) {
        for (ProductType p : world.getProducts().getProduct()) {
            if (id == p.getId()) {
                return p;
            }
        }
        return null;
    }

    public PallierType findManagerByName(World world, String name) {
        for (PallierType m : world.getManagers().getPallier()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }

    // prend en paramètre le pseudo du joueur et le produit
    // sur lequel une action a eu lieu (lancement manuel de production ou
    // achat d’une certaine quantité de produit)
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateProduct(String username, ProductType newproduct) throws FileNotFoundException, JAXBException {
        // aller chercher le monde qui correspond au joueur
        World world = getWorld(username);
        try {
            world = getWorld(username);
            // trouver dans ce monde, le produit équivalent à celui passé
            // en paramètre
            ProductType product = findProductById(world, newproduct.getId());
            if (product == null) {
                return false;
            }
            // calculer la variation de quantité. Si elle est positive c'est
            // que le joueur a acheté une certaine quantité de ce produit
            // sinon c’est qu’il s’agit d’un lancement de production.
            int qtchange = newproduct.getQuantite() - product.getQuantite();
            if (qtchange > 0) {
                // soustraire de l'argent du joueur le cout de la quantité
                // achetée et mettre à jour la quantité de product
                // world.setMoney(world.getMoney() - (product.getCout() * qtchange));
                world.setMoney(world.getMoney() - (getRealPrice(product) * ((1 - Math.pow(product.getCroissance(), qtchange)) / 1 - product.getCroissance())));
                product.setQuantite(product.getQuantite() + qtchange);
            } else {
                // initialiser product.timeleft à product.vitesse
                // pour lancer la production
                product.setTimeleft(newproduct.getVitesse());
            }
            // sauvegarder les changements du monde
            saveWorldToXml(world, username);
            updateScore(world);
            return true;
        } catch (JAXBException ex) {
            Logger.getLogger(Services.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    private double getRealPrice(ProductType p) {
        Double d;
        d = p.getCout() * Math.pow(p.getCroissance(), p.getQuantite());
        return d;
    }

    // prend en paramètre le pseudo du joueur et le manager acheté.
    // renvoie false si l’action n’a pas pu être traitée
    public Boolean updateManager(String username, PallierType newmanager) throws FileNotFoundException {
        // aller chercher le monde qui correspond au joueur
        World world;
        try {
            world = getWorld(username);
            // trouver dans ce monde, le manager équivalent à celui passé
            // en paramètre
            PallierType manager_found = findManagerByName(world, newmanager.getName());
            if(manager_found == null || manager_found.isUnlocked()){
                return false;
            }
            ProductType product = findProductById(world, manager_found.getIdcible());
            if(product == null){
                return false;
            }
            
            //Vérifier si joueur a assez d'argent
            if(world.getMoney() < manager_found.getSeuil()){
            return false;
        }
      
            //Unlock manager 
            manager_found.setUnlocked(true);
            
            // Marqué produit comme managé 
            product.setManagerUnlocked(true);
            saveWorldToXml(world, username);
            
            //application bonus 
            world.setMoney(world.getMoney()-manager_found.getSeuil());
            applyBonus(world, manager_found);
            
        } catch (JAXBException ex) {
            Logger.getLogger(Services.class.getName()).log(Level.SEVERE, null, ex);
        }
        return true;
    }

    @SuppressWarnings("empty-statement")
    public Boolean updateScore(World world) throws FileNotFoundException {
        /*long temps_ecoule = System.currentTimeMillis() - world.getLastupdate();
        for (ProductType p : world.getProducts().getProduct()){
            if (!p.isManagerUnlocked()) {
                if (p.getTimeleft() < temps_ecoule){
                    world.setMoney(world.getMoney() + p.getRevenu());
                }else{
                    p.setTimeleft(p.getTimeleft() - temps_ecoule);
                }
            }else{
                world.setMoney(world.getMoney() + p.getRevenu()*(temps_ecoule/(p.getVitesse())-p.getTimeleft()));
                p.setTimeleft(temps_ecoule%p.getVitesse());
            }
        }
        world.setLastupdate(System.currentTimeMillis());
        return false;*/
        ProductsType products = world.getProducts();
        List<ProductType> listProducts = products.getProduct();
        for (ProductType p : listProducts) {
            if (p.isManagerUnlocked()) {
                long temps_ecoule = System.currentTimeMillis() - world.getLastupdate();
                long money = (int) temps_ecoule / p.getVitesse();
                world.setMoney(world.getMoney() + (p.getRevenu() * money));
                
            } else {
                if (p.getQuantite() > 0) {
                    if (p.getTimeleft() != 0 && p.getTimeleft() < System.currentTimeMillis() - world.getLastupdate()) {

                        System.out.println("money" + world.getMoney() + "reven" + p.getRevenu());
                        world.setMoney(world.getMoney() + p.getRevenu());
                    } else {
                        p.setTimeleft(0);
                    }

                }
            }
        }
        world.setLastupdate(System.currentTimeMillis());
        return false;
    }

        /*
	 Applique bonus des upgrades, unlocks et managers	
	 */
	public void applyBonus(World world, PallierType pallier) {
		
		int bonusVitesse = 1;
		int bonusGain = 1;
		int bonusAnge = 0;
		
		int idCible = pallier.getIdcible();
		
		if( pallier.getTyperatio() == TyperatioType.ANGE ) {
			bonusAnge = (int) pallier.getRatio();
			world.setAngelbonus(bonusAnge);
		} else {
			if( pallier.getTyperatio() == TyperatioType.VITESSE ) {
				bonusVitesse = (int) pallier.getRatio();
			}
			if( pallier.getTyperatio() == TyperatioType.GAIN ) {
				bonusGain = (int) pallier.getRatio();
			}
			
			
			List<ProductType> worldProducts = world.getProducts().getProduct();

			if(idCible==0) {
				for(int p=0; p<worldProducts.size(); p++) {
					worldProducts.get(p).setVitesse( worldProducts.get(p).getVitesse()/bonusVitesse );
					worldProducts.get(p).setRevenu( worldProducts.get(p).getRevenu()*bonusGain );
					System.out.println("\tP: " + worldProducts.get(p).getName() + " " + pallier.getRatio() + " " +pallier.getTyperatio());
				}
			} else {
				for(int p=0; p<worldProducts.size(); p++) {
					if( worldProducts.get(p).getId() == idCible) {
						worldProducts.get(p).setVitesse( worldProducts.get(p).getVitesse()/bonusVitesse );
						worldProducts.get(p).setRevenu( worldProducts.get(p).getRevenu()*bonusGain );
						System.out.println("\tP: " + worldProducts.get(p).getName() + " " + pallier.getRatio() + " " +pallier.getTyperatio());
					}
				}
			}
			
			
		}
		
		
		
		
		
		
		
		
	}
}
