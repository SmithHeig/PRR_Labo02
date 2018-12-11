/**
 * @authors Jeremie Chatillon et James Smith
 * @file Application.java
 * Tâche applicative qui se connecte à un serveur par RMI. Les serveurs doivent être déjà
 * fonctionnels avant de lancer une tâche. 1 tâche applicative par serveur.
 * Les serveurs ont leurs id de 0 à N-1.
 */

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Scanner;

public class Application {

    /**
     * Tâche applicative liée à 1 serveur
     * @param args id du serveur
     */
    public static void main(String[] args) {

        // Lecture de l'id du serveur
        if(args.length !=  1){
            System.err.println("You should pass the server id as argument");
            System.exit(1);
        }
        int serverId = Integer.parseInt(args[0]);
        String srvName = CommunicationConfig.SERVERS_NAME + "-" + serverId;

        try {
            // Connexion RMI au serveur
            Registry registry = null;
            if (CommunicationConfig.isDebug) {
                System.out.println("Try to connect to: " + srvName);
            }
            registry = LocateRegistry.getRegistry(CommunicationConfig.SERVERS_NAME);
            // Récupération du serveur
            IServer serveur = (IServer)registry.lookup(srvName);

            if (CommunicationConfig.isDebug) {
                System.out.println("Connected to connect to: " + srvName);
            }

            int newVar;
            int command = 0;
            Scanner sc = new Scanner(System.in);
            // Boucle pour changer/obtenir/incréementer la variable paratgée
            do{
                displayMenu();
                command = sc.nextInt();
                switch (command){
                    case 0:
                        // Changement de variable
                        System.out.println("Choose the new value: ");
                        newVar = sc.nextInt();
                        serveur.setVar(newVar);
                        System.out.println("Set done done. The value is: "
                                + serveur.getVar());
                        break;
                    case 1:
                        // Obtention de la variable
                        System.out.println("The value is: " + serveur.getVar());
                        break;
                    case 2:
                        // Incrément de la variable
                        serveur.increment();
                        System.out.println("Increment done. The value is: "
                                + serveur.getVar());
                        break;
                    case 3:
                        // Quitte
                        break;
                    default:
                        System.out.println("Flase input.");
                }
            } while(command != 3);
        } catch (RemoteException |  NotBoundException e) {
            // Erreur de connexion
            System.out.println("Server error, connexion fail");
            if(CommunicationConfig.isDebug){
                e.printStackTrace();
            }
            System.exit(1);
        }

        System.out.println("Application terminate");
        System.exit(0);
    }

    /**
     * Affiche le menu utilisateur
     */
    protected static void displayMenu(){
        System.out.println("");
        System.out.println("Choose your action:");
        System.out.println("Set variable: 0");
        System.out.println("Get variable: 1");
        System.out.println("variable++  : 2");
        System.out.println("Exit        : 3");

    }

}
