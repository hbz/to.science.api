package archive.fedora;

/**
 * This class is used in ApplicationProfile
 * 
 * @author Jan Schnasse
 *
 */
public class MapEntry {
    /**
     * a label
     */
    public String label = null;
    /**
     * a icon
     */
    public String icon = null;

    /**
     * The name is a short-form for the uri used in JSON-LD
     */
    public String name = null;

    /**
     * the full id as uri
     */
    public String uri = null;
}