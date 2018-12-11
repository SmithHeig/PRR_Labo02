/**
 * @authors Jeremie Chatillon et James Smith
 * @file ApplicationText.java
 * Classe de testes.
 * Permet de lancer 3 simultanément applicatins qui vont incrémenter une variable
 * partagée. Il faut que 3 serveurs soient lancé au préalable.
 */

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class ApplicationTest {

    public static void main(String[] args) throws InterruptedException {
        // Initalisation des tâches applicatives //
        Thread app1 = new Thread() {
            public void run() {
                test(0, 3);
            }
        };
        Thread app2 = new Thread() {
            public void run() {
                test(1, 3);
            }
        };
        Thread app3 = new Thread() {
            public void run() {
                test(2, 3);
            }
        };

        // Remise à 0 de la variable partagée (pour facicliter la leture des résultats
        init();
        TimeUnit.SECONDS.sleep(2);
        System.out.println("");

        // Start des tests
        app1.start();
        app2.start();
        app3.start();

    }

    /**
     * Affiche le menu utilisateur
     */
    protected static void init(){
        String srvName = CommunicationConfig.SERVERS_NAME + "-" + 0;
        try {
            Registry registry = null;
            if (CommunicationConfig.isDebug) {
                System.out.println("Try to connect to: " + srvName);
            }
            registry = LocateRegistry.getRegistry(CommunicationConfig.SERVERS_NAME);
            IServer serveur = (IServer)registry.lookup(srvName);

            if (CommunicationConfig.isDebug) {
                System.out.println("Connected to connect to: " + srvName);
            }

            serveur.setVar(0);
            System.out.println(srvName + " val is: "+ serveur.getVar());


        } catch (RemoteException |  NotBoundException e) {
            // Erreur de connexion
            System.out.println("Server error, connexion fail");
            if(CommunicationConfig.isDebug){
                e.printStackTrace();
            }
        }
    }

    /**
     * Incémente la variable d'un site n fois
     * @param id
     */
    public static void test(int id, int n){
        String srvName = CommunicationConfig.SERVERS_NAME + "-" + id;
        try {
            Registry registry = null;
            if (CommunicationConfig.isDebug) {
                System.out.println("Try to connect to: " + srvName);
            }
            registry = LocateRegistry.getRegistry(CommunicationConfig.SERVERS_NAME);
            IServer serveur = (IServer)registry.lookup(srvName);

            if (CommunicationConfig.isDebug) {
                System.out.println("Connected to connect to: " + srvName);
            }
            //System.out.println(srvName + " val is: "+ serveur.getVar());

            for(int i = 0; i < n; ++ i){
                serveur.increment();
            }
            System.out.println(srvName + " val is: "+ serveur.getVar());


            // Boucle pour changer/obtenir la variable.
        } catch (RemoteException |  NotBoundException e) {
            // Erreur de connexion
            System.out.println("Server error, connexion fail");
            if(CommunicationConfig.isDebug){
                e.printStackTrace();
            }
        }
    }

}
