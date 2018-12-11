import java.io.Serializable;


/**
 * Classe serialisable permettant la transmission des messages pour les serveurs RMI.
 */
public class CommunicationMessage implements Serializable {
    // Tyoe de messagre
    private String type;
    // ID du serveur emetteur
    private int srvID;
    // Heure de 0 à inf
    private int clock;
    // Nouvelle valeur de la variable partagée
    private Integer newVal;

    public CommunicationMessage(String type, int srvID, int clock) {
        this(type, srvID, clock, null);
    }

    public CommunicationMessage(String type, int srvID, int clock, Integer newVal) {
        this.type = type;
        this.srvID = srvID;
        this.clock = clock;
        this.newVal = newVal;
    }

    /** GETTERS **/
    public String getType() {
        return type;
    }

    public int getSrvID() {
        return srvID;
    }

    public int getClock() {
        return clock;
    }

    public Integer getNewVal() {
        return newVal;
    }

    /** SETTERS **/
    public void setType(String type) {
        this.type = type;
    }

    public void setSrvID(int srvID) {
        this.srvID = srvID;
    }

    public void setClock(int clock) {
        this.clock = clock;
    }

    public void setNewVal(Integer newVal) {
        this.newVal = newVal;
    }
}
