package extensions;
import java.awt.color.ColorSpace;

/**
 * Extension de l'espace de couleur CIE standard de Java pour permettre la transposition de couleur RGC et CIEXYZ en couleur CIELab.
 * CIELab est l'espace de couleur exprimé sur les composante L a et b faisant entrer en compte la notion de luminance plutôt qu'uniquement une répartition spaciale.
 * Il s'agit d'un espace de couleur adapté pour des couleurs de surface elle tient compte de l'écart de couleurs perçu par la vision humaine.
 * Cette conversion permet notament d'effectuer des calculs de distances entre les couleurs.
 * @author levieilfa
 *
 */
public class CIELab extends ColorSpace {

    private static final long serialVersionUID = 5027741380892134289L;

    /**
     * L'espace de couleur de référence.
     */
    private static final ColorSpace CIEXYZ = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

    /**
     * Constante de référence pour les fonction f et finv.
     */
    private static final double N = 4.0 / 29.0;
	
    /**
     * Renvoie l'instance.
     * @return l'instance de l'espace de couleur.
     */
    public static CIELab getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public float[] fromCIEXYZ(float[] colorvalue) {
        double l = f(colorvalue[1]);
        double L = 116.0 * l - 16.0;
        double a = 500.0 * (f(colorvalue[0]) - l);
        double b = 200.0 * (l - f(colorvalue[2]));
        return new float[] {(float) L, (float) a, (float) b};
    }

    @Override
    public float[] fromRGB(float[] rgbvalue) {
        float[] xyz = CIEXYZ.fromRGB(rgbvalue);
        return fromCIEXYZ(xyz);
    }

    @Override
    public float getMaxValue(int component) {
        return 128f;
    }

    @Override
    public float getMinValue(int component) {
        return (component == 0)? 0f: -128f;
    }    

    @Override
    public String getName(int idx) {
        return String.valueOf("Lab".charAt(idx));
    }

    @Override
    public float[] toCIEXYZ(float[] colorvalue) {
        double i = (colorvalue[0] + 16.0) * (1.0 / 116.0);
        double X = fInv(i + colorvalue[1] * (1.0 / 500.0));
        double Y = fInv(i);
        double Z = fInv(i - colorvalue[2] * (1.0 / 200.0));
        return new float[] {(float) X, (float) Y, (float) Z};
    }

    @Override
    public float[] toRGB(float[] colorvalue) {
        float[] xyz = toCIEXYZ(colorvalue);
        return CIEXYZ.toRGB(xyz);
    }

    /**
     * Constructeur par défaut du CIELab à partir du l'espace de couleur de référence.
     */
    CIELab() {
        super(ColorSpace.TYPE_Lab, 3);
    }

    /**
     * Fonction permettant la conversion des composantes depuis un espace CIEXYZ vers un espace CIELab.
     * @param x la composante à convertir
     * @return la composante convertie
     */
    private static double f(double x) {
        if (x > 216.0 / 24389.0) {
            return Math.cbrt(x);
        } else {
            return (841.0 / 108.0) * x + N;
        }
    }

    /**
     * Fonction inverse de la fonction f permettant la conversion des composantes depuis un espace CIELab vers un espace CIEXYZ.
     * @param x la composante à convertir
     * @return la composante convertie
     */
    private static double fInv(double x) {
        if (x > 6.0 / 29.0) {
            return x*x*x;
        } else {
            return (108.0 / 841.0) * (x - N);
        }
    }

    private Object readResolve() {
        return getInstance();
    }

    /**
     * Classe valorisant l'instance à partir du constructeur par défaut.
     * @author levieilfa
     *
     */
    private static class Holder {
        static final CIELab INSTANCE = new CIELab();
    }

}