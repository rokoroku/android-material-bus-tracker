package kr.rokoroku.mbus.api.gbis.core;

import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import kr.rokoroku.mbus.api.gbis.model.GbisComMsgHeader;
import kr.rokoroku.mbus.api.gbis.model.GbisResponseHeader;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.StringWriter;
import java.lang.reflect.Type;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import retrofit.converter.ConversionException;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedString;

/**
 * Created by rok on 2015. 4. 22..
 */
public class GbisXmlConverter extends SimpleXmlConverter {
    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(body.in());

            NodeList headerNode = document.getElementsByTagName("msgHeader");
            NodeList bodyNode = document.getElementsByTagName("msgBody");

            if(headerNode.getLength() > 0) {
                GbisResponseHeader gbisResponseHeader = (GbisResponseHeader) deserialize(headerNode.item(0), GbisResponseHeader.class, false);
                int responseCode = gbisResponseHeader.getResponseCode();
                if (responseCode == GbisException.RESULT_OK && bodyNode.getLength() > 0) {
                    return deserialize(bodyNode.item(0), type, true);

                } else if(responseCode == GbisException.ERROR_NO_RESULT) {
                    return null;

                } else if(responseCode == GbisException.ERROR_REFER_COM_MSG_HEADER) {
                    headerNode = document.getElementsByTagName("comMsgHeader");
                    GbisComMsgHeader gbisComMsgHeader = (GbisComMsgHeader) deserialize(headerNode.item(0), GbisComMsgHeader.class, false);
                    throw new GbisException(gbisComMsgHeader.getResponseCode(), gbisComMsgHeader.getMessage());

                } else {
                    throw new GbisException(responseCode, gbisResponseHeader.getMessage());
                }
            } else {
                // 종종 발생.
                // throw new GbisException(-1, "Wrong Response");
                return null;
            }

        } catch (ConversionException e) {
            throw e;

        } catch (Exception e) {
            throw new ConversionException(e);
        }
    }

    private Object deserialize(Node node, Type type, boolean wrap) throws ConversionException, TransformerException {
        TypedInput typedInput;
        String nodeString = nodeToString(node);
        if(wrap) {
            typedInput = new TypedString("<node>" + nodeString + "</node>");
        } else {
            typedInput = new TypedString(nodeString);
        }
        return super.fromBody(typedInput, type);
    }

    private String nodeToString(Node node) throws TransformerException {
        StringWriter sw = new StringWriter();
        Transformer t = TransformerFactory.newInstance().newTransformer();
        t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        t.transform(new DOMSource(node), new StreamResult(sw));
        return sw.toString();
    }
}
