

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interface du serveur pur faire du RMI
 */
public interface IServer extends Remote {

    /**
     * Obtient la valeur partagée
     * @return la valeur partagée
     * @throws RemoteException
     */
    int getVar() throws RemoteException;

    /**
     * Modifie  la valeur partagée dans tous les sites
     * @param i la valeur partagée
     * @throws RemoteException
     */
    void setVar(int i) throws RemoteException;

    /**
     * Incrémente la valeur partagée dans tous les sites
     * @throws RemoteException
     */
    void increment() throws RemoteException;

    /**
     * Permet au site de recevoir un message
     * @param message   Message reçu
     * @throws RemoteException
     */
    void recoit(CommunicationMessage message) throws RemoteException;
}
