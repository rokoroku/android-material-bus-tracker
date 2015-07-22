package kr.rokoroku.mbus.api.tago.core;

import com.mobprofs.retrofit.converters.SimpleXmlConverter;

import kr.rokoroku.mbus.api.seoul.core.*;
import kr.rokoroku.mbus.api.seoul.core.SeoulBusException;
import kr.rokoroku.mbus.api.seoul.model.SeoulBusResponseHeader;
import kr.rokoroku.mbus.api.seoul.model.SeoulStationInfoList;

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

import kr.rokoroku.mbus.api.tago.model.ResponseHeader;
import retrofit.converter.ConversionException;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedString;

/**
 * Created by rok on 2015. 4. 22..
 */
public class TagoXmlConverter extends SimpleXmlConverter {
    @Override
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        try {
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(body.in());

            NodeList headerNode = document.getElementsByTagName("header");
            NodeList bodyNode = document.getElementsByTagName("body");

            if(headerNode.getLength() > 0) {
                ResponseHeader header = (ResponseHeader) deserialize(headerNode.item(0), ResponseHeader.class, false);
                if (header.resultCode == TagoException.RESULT_OK && bodyNode.getLength() > 0) {
                    return deserialize(bodyNode.item(0), type, true);

                } else if(header.resultCode == TagoException.ERROR_NO_RESULT) {
                    return null;

                } else {
                    throw new TagoException(header.resultCode, header.resultMsg);
                }
            } else {
                throw new TagoException(-1, "Wrong Response");
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
