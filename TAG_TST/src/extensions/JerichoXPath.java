package extensions;

import java.util.List;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.jaxen.BaseXPath;
import org.jaxen.Context;
import org.jaxen.JaxenException;
import org.jaxen.Navigator;
import org.jaxen.dom.DocumentNavigator;
import org.jaxen.util.SingletonList;

/**
 * Cette classe étend la gestion du XPath réalisée par la librairie Jaxen pour
 * l'adapté à la librairie Jericho. Elle permet d'obtenir les XPATH associé à
 * des objets récupérés par Jericho.
 * 
 * @author Fabien Levieil
 * 
 */
public class JerichoXPath extends BaseXPath {

	/**
	 * Id de sérialisation.
	 */
	private static final long serialVersionUID = -6969112785840871593L;

	/**
	 * Construit une manipulateur de XPATH pour Jericho à partir d'info.
	 * @param xpathExpr l'expression de XPATH vers l'élément.
	 * @param navigator le navigator de document associé.
	 * @throws JaxenException en cas d'erreur.
	 */
	public JerichoXPath(String xpathExpr, Navigator navigator) throws JaxenException {
		super(xpathExpr, navigator);
	}

	/**
	 * Construit une manipulateur de XPATH pour Jericho à partir d'info.
	 * @param xpathExpr l'expression de XPATH vers l'élément.
	 * @throws JaxenException en cas d'erreur.
	 */
	public JerichoXPath(String xpathExpr) throws JaxenException {
		super(xpathExpr, DocumentNavigator.getInstance());
	}

	/**
	 * Jericho specific method to get the context associated with a node.
	 * @param node the current node being visited.
	 * @return the Context associated with the node.
	 */
	protected Context getContext(Object node) {
		if (node instanceof Context) {
			return (Context) node;
		}
		Context fullContext = new Context(getContextSupport());
		if (node instanceof Source) {
			Element rootNode = (Element) getNavigator().getDocumentNode((Source) node);
			fullContext.setNodeSet(new SingletonList(rootNode));
		} else if (node instanceof List) {
			fullContext.setNodeSet((List) node);
		} else {
			List list = new SingletonList(node);
			fullContext.setNodeSet(list);
		}
		return fullContext;
	}
}