package com.formulasearchengine.math.mathoid;

import com.formulasearchengine.mathmltools.xmlhelper.NonWhitespaceNodeList;
import com.formulasearchengine.mathmltools.xmlhelper.XMLHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;

/**
 * Transformer from Enriched Math to a well formed MathML that contains
 * pMML and cMML semantics.
 * <p>
 * It is mainly used after the MathoidConverter was called. Mathoid delivers an
 * enriched MathML format, which only contains pMML. This transformator
 * adds the cMML semantics and combines them in a new document.
 *
 * @author Vincent Stange
 */
public class EnrichedMathMLTransformer {

    private static Logger logger = Logger.getLogger(EnrichedMathMLTransformer.class);

    private static final String XSL = "com/formulasearchengine/math/mathoid/EnrichedMathML2Cmml.xsl";

    private final Document readDocument;

    /**
     * Takes enriched MathML and build a document out of it. This will be
     * further used by the transformer.
     *
     * @param eMathML The MathML the transformer will use
     */
    public EnrichedMathMLTransformer(String eMathML) {
        readDocument = XMLHelper.string2Doc(eMathML, false);
    }

    /**
     * Transformers the enriched MathML to a well formed MathML that contains
     * pMML and cMML semantics into a new document and returns it as a string.
     * <p>
     * This method still has a lot of flaws.
     *
     * @param xpath current used xpath instance
     * @return String of the new formed document or null, if transformation failed.
     * @throws Exception a lot could go wrong here: parser or transformer error
     */
    public String getFullMathML(XPath xpath) throws Exception {
        Element semanticRoot = (Element) xpath.compile("*//semantics").evaluate(readDocument, XPathConstants.NODE);
        boolean hasSemanticEle = semanticRoot != null;

        // get the first mrow element
        NonWhitespaceNodeList mrowNodes = new NonWhitespaceNodeList((NodeList) xpath.compile("*//mrow").evaluate(readDocument, XPathConstants.NODESET));
        Element mrowNode = (Element) mrowNodes.getFirstElement();

        // secure the id field
        copyIdField(mrowNode);

        try {
            // create the cmml apply node and then
            // adopt the new cmml structure onto the original enriched mathml
            Node tmpChild = XMLHelper.xslTransform(mrowNode, XSL).getFirstChild();
            Node applyNode = readDocument.adoptNode(tmpChild.cloneNode(true));

            // create a new "annotation-xml" node and append the created cmml structure
            Element cmmlSemanticNode = readDocument.createElement("annotation-xml");
            cmmlSemanticNode.setAttribute("encoding", "MathML-Content");
            cmmlSemanticNode.appendChild(applyNode);

            // add the new "annotation-xml" to the semantics root
            if (hasSemanticEle) {
                semanticRoot.appendChild(cmmlSemanticNode);
            } else {
                // switch nodes around to include a noew semantics element
                Node mathRoot = readDocument.getFirstChild();
                Node tmpMrowNode = mathRoot.removeChild(mrowNode);

                Element newSemanticRoot = readDocument.createElement("semantics");
                newSemanticRoot.appendChild(tmpMrowNode);
                newSemanticRoot.appendChild(cmmlSemanticNode);

                mathRoot.appendChild(newSemanticRoot);
            }
        } catch (Exception e) {
            logger.error("enriched mathml transformation failed", e);
            return null;
        }
        return XMLHelper.printDocument(readDocument.getFirstChild());
    }

    /**
     * Copy the "data-semantic-id" attribute to "id", if it does not exist.
     * Will recursively go over every child.
     *
     * @param readNode element to change
     */
    void copyIdField(Element readNode) {
        String newId = readNode.getAttribute("data-semantic-id");
        if (!StringUtils.isEmpty(newId)) {
            readNode.setAttribute("id", "p" + newId);
        }
        for (Node child : new NonWhitespaceNodeList(readNode.getChildNodes())) {
            copyIdField((Element) child);
        }
    }
}
