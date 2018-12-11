/**
 * @authors Jeremie Chatillon et James Smith
 * @file Serveur.java
 * Classe permettant la synchronisation d'une variable grace à l'algorithme de lamport.
 * Nous nous somme basé sur le pseudocode donné au cour.
 *
 * Nous avons fait en sorte que toutes les instructions sont en synchronised mis à part
 *  l'envoit de message pour ne fais faire d'interbloquage.
 * wait: https://stackoverflow.com/questions/10395509/java-notify-gets-called-before-wait
 * notifiy: https://community.oracle.com/thread/1179274
 */

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;


public class Serveur extends UnicastRemoteObject implements IServer{

    // Variable partagée
    private int var;
    // Nombre de site total
    private int NB_SITE;
    // Id unique du site
    private int ID;
    // temps local du site
    private int clock;

    // Indique si le site peut accéder en section critique
    private boolean scAccorde;
    // Indique si le site est en attente d'entrée en section critique
    private boolean isBlocked;

    // Nous avons décidé de faire 2 tableaux pour "t_file". Nous aurions aussi pu faire
    //  un tableau de pair ou une classe interne
    private String[] msgType;
    private int[] estampilles;

    // Registre RMI
    private Registry reg;

    // permet d'afficher les messages de débug
    private boolean isDebug;

    /**
     * Constructeur 
     * @param NB_SITE   Nombre de site total
     * @param ID        Id unique du site
     * @param isDebug   permet d'afficher les messages de débug
     * @throws RemoteException
     */
    public Serveur(int NB_SITE, int ID, boolean isDebug) throws RemoteException {
        super();
        var = 0;

        this.NB_SITE = NB_SITE;
        this.ID = ID;
        clock = 0;
        scAccorde = false;

        msgType = new String[NB_SITE];
        estampilles = new int[NB_SITE];     // init à la valeur 0

        Arrays.fill(msgType, CommunicationConfig.FREE_MESSAGE);

        // Récupération du registre
        reg = LocateRegistry.getRegistry(CommunicationConfig.PORT);

        this.isDebug = isDebug;

    }

    /**
     * Getter vat (RMI)
     * @return var
     * @throws RemoteException
     */
    @Override
    public int getVar() throws RemoteException {
        synchronized (this) {
            return var;
        }
    }

    /**
     * Setter "var" (RMI)
     * @param newVar - nouvelle valeur à donner à var
     * @throws RemoteException
     */
    @Override
    public void setVar(int newVar) throws RemoteException {
        debug("Set var started: " + newVar);
        demande();

        attente();

        // Section critique
        var = newVar;

        fin(newVar);
    }

    /**
     * Incrémente var (RMI)
     * @throws RemoteException
     */
    @Override
    public void increment() throws RemoteException {
        debug("Increment var started");
        demande();
        attente();
        // Section critique
        ++var;
        fin(var);
    }

    /**
     * Méthde de demande en section critique. envoit le message à tous les autres serveurs
     */
    private void demande(){
        int tmpclock;
        CommunicationMessage cm;
        synchronized (this) {
            ++clock;
            msgType[ID] = CommunicationConfig.REQUEST_MESSAGE;
            estampilles[ID] = clock;
            tmpclock = clock;
            cm = new CommunicationMessage(CommunicationConfig.REQUEST_MESSAGE, ID, tmpclock);
            debug("Message sent: " + CommunicationConfig.REQUEST_MESSAGE+ " " + ID+ " " + tmpclock);
        }

        for(int i = 0; i < NB_SITE; ++i){
            if(i != ID)
                envoit(cm, i);
        }
    }

    /**
     * Méthde d'attente tant que le serveur n'a pas l'accord de rentrer en section
     *  critique
     */
    private void attente() {
        // ATTENTE //
        int newVal;
        boolean tmpScAccorde;
        synchronized (this) {
            scAccorde = permission();
            debug("Ask permission done, SCaccess: " + scAccorde);
            if (!scAccorde) {
                debug("Waiting permission to enter in critical section.");
                isBlocked = true;
                try {
                    this.wait();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Méthde de fin de section critique, indique aux autres serveurs la nouvelle valeur
     * de la variable
     * @param newVal nouvelle valeur de la variable
     */
    private void fin(int newVal){
        CommunicationMessage cm;
        synchronized (this) {
            int tmpClock;
            msgType[ID] = CommunicationConfig.FREE_MESSAGE;
            estampilles[ID] = clock;
            tmpClock = clock;
            cm = new CommunicationMessage(CommunicationConfig.FREE_MESSAGE, ID, tmpClock, newVal);
            debug("Message sent: " + CommunicationConfig.FREE_MESSAGE + " " + ID + " " + tmpClock + " " + newVal);
        }

        for (int i = 0; i < NB_SITE; ++i) {
            if (i != ID) {
                envoit(cm, i);
            }
        }

        synchronized (this) {
            scAccorde = false;
        }
    }

    /**
     * Calcule si le serveur obtient la permission de rentrer en section critique
     * @return s'il a la permission ou pas
     */
    private boolean permission(){
        boolean accord = true;
        for(int j = 0; j < NB_SITE; ++j){
            if(j != ID){
                accord = accord
                        && (estampilles[ID] < estampilles[j])
                        || (estampilles[ID] == estampilles[j])
                        && ID < j;
            }
        }
        return accord;
    }

    /**
     * envoit un message à un serveur à l'aide de RMI
     * @param message Message à transmettre
     * @param to      id du serveur
     */
    protected void envoit(CommunicationMessage message, int to){
        try {
            IServer srv = (IServer) reg.lookup(CommunicationConfig.SERVERS_NAME
                    + "-" + to);
            srv.recoit(message);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (NotBoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Recoit message (RMI)
     * @param message   Message reçu
     * @throws RemoteException
     */
    @Override
    public void recoit(CommunicationMessage message) throws RemoteException{
        debug("Message reciew: " + message.getType() + " " + message.getSrvID() + " " + message.getClock());
        int emetteur = message.getSrvID();
        int estempille = message.getClock();

        int myClcok;
        synchronized (this) {
            myClcok = clock = Integer.max(clock, estempille) + 1;
        }
        CommunicationMessage cm = null;
        switch (message.getType()){
            case CommunicationConfig.REQUEST_MESSAGE :

                synchronized (this) {
                    msgType[emetteur] = CommunicationConfig.REQUEST_MESSAGE;
                    estampilles[emetteur] = estempille;
                    cm = new CommunicationMessage(CommunicationConfig.RECEIPT_MESSAGE, ID, myClcok);
                }
                envoit(cm, emetteur);
                break;

            case CommunicationConfig.FREE_MESSAGE :
                synchronized (this) {
                    msgType[emetteur] = CommunicationConfig.FREE_MESSAGE;
                    estampilles[emetteur] = estempille;
                    // Mise à jour de la valeur
                    var = message.getNewVal();
                    debug("Var has been set by other server to: " + message.getNewVal());
                }
                break;

            case CommunicationConfig.RECEIPT_MESSAGE:
                synchronized (this) {
                    if (!msgType[emetteur].equals(CommunicationConfig.REQUEST_MESSAGE)){
                        msgType[emetteur] = CommunicationConfig.RECEIPT_MESSAGE;
                        estampilles[emetteur] = estempille;
                    }
                }
                break;
        }

        synchronized (this) {
            scAccorde = msgType[ID].equals(CommunicationConfig.REQUEST_MESSAGE) && permission();

            //
            if(isBlocked && scAccorde) {
                isBlocked = false;
                this.notify();
            }
        }
    }


    /**
     * Affiche un message d'infrmatin dans la consle si le mode débug est activé
     * @param s message à aficher
     */
    private void debug(String s){
        if(isDebug)
            System.out.println(s);
    }

    /**
     * Lance le serveur
     * @param args id du serveur
     */
    public static void main(String[] args) {
        if(args.length !=  1){
            System.err.println("You should pass the server ID as argument");
            System.exit(1);
        }

        int serverId = Integer.parseInt(args[0]);

        boolean serverStarted = false;
        String srvName = CommunicationConfig.SERVERS_NAME + "-" + serverId;
        try {
            // Boucle pour si jamais le serveur n'existe pas
            do {
                try {
                    // Connexion au registe
                    if (CommunicationConfig.isDebug) {
                        System.out.println("Try to connect to: " + srvName);
                    }
                    Registry registry = LocateRegistry.getRegistry(CommunicationConfig.PORT);

                    // Création du serveur
                    IServer srv = new Serveur(CommunicationConfig.NB_SITE, serverId, CommunicationConfig.isDebug);

                    registry.bind(srvName, srv);

                    serverStarted = true;

                    if (CommunicationConfig.isDebug) {
                        System.out.println("Server is ready => " + srvName);
                    }

                } catch (RemoteException e) {
                    // Création du registry s'il existe pas
                    if (CommunicationConfig.isDebug) {
                        System.out.println("Creation of the registery.");
                    }
                    Registry r = LocateRegistry.createRegistry(CommunicationConfig.PORT);
                } catch (AlreadyBoundException e) {
                    // Serveur déjà existant => quitte
                    System.err.println("Server ID already taken");
                    e.printStackTrace();
                    System.exit(1);
                }

            } while (!serverStarted);
        } catch(Exception e){
            // Server erreur => quitte
            System.err.println("Server error");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
