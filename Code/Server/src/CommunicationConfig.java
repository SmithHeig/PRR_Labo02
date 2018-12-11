/**
 * Cette classe contient les constantes du programme.
 * Le nombre de servveur doit être entré ici, avant le lancement du programme. Les id des
 * serveurs vont de 0 à N-1.
 */
public class CommunicationConfig {
    // Valeurs des messages de commmunucations
    static final String REQUEST_MESSAGE = "REQUEST";    // requête
    static final String FREE_MESSAGE = "FREE";          // libération
    static final String RECEIPT_MESSAGE = "RECEIPT";    // acquitement
    // nombre de site totaux
    static final int NB_SITE = 3;
    // port du registre
    static final int PORT = 1099;
    // nom de base des serveurs et nom du registre
    static final String SERVERS_NAME = "localhost";

    // Affiche les messages de débug
    static final boolean isDebug = true;
}
