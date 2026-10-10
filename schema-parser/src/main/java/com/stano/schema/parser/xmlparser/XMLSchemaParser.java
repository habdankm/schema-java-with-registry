package com.stano.schema.parser.xmlparser;

import java.io.BufferedReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX-driving XML parser used internally by {@link com.stano.schema.parser.SchemaParser} to read
 * and validate schema XML documents.
 *
 * <p>Configures a JAXP {@link SAXParser} that validates incoming documents against the bundled
 * {@code /schema.xsd} classpath resource, is namespace-aware, and has secure processing features
 * enabled (disallowing {@code DOCTYPE} declarations and external general/parameter entities).
 * Parsing is driven via SAX, with content events dispatched to a caller-supplied {@link
 * SchemaContentHandler}.
 */
public class XMLSchemaParser {
  private static final Logger LOGGER = LoggerFactory.getLogger(XMLSchemaParser.class);

  /**
   * Parses the XML content from the given reader, dispatching SAX events to the given handler.
   *
   * @param reader the source of the raw XML content to parse
   * @param schemaContentHandler the SAX content handler that receives parse events and builds up a
   *     {@link com.stano.schema.model.Schema} as content is parsed
   * @throws RuntimeException if the XML reader cannot be created, or if parsing or schema
   *     validation fails; the underlying cause is wrapped
   */
  public void parse(BufferedReader reader, SchemaContentHandler schemaContentHandler) {
    try {
      XMLReader parser = createXMLReader();

      parser.setContentHandler(schemaContentHandler);
      parser.parse(new InputSource(reader));
    } catch (Exception x) {
      throw new RuntimeException(x);
    }
  }

  private XMLReader createXMLReader() {
    try {
      // The XSD is not part of this repository: com.stano:schema-xsd bundles it as a JAR resource,
      // and schema-model
      // depends on that artifact with `api`, so on the JVM every consumer of this library already
      // has it.
      //
      // An IMAGE is the case worth knowing about: GraalVM keeps only what it is told to keep, so
      // the resource has to
      // be named in the native-image arguments (-H:IncludeResources) or it is simply absent at run
      // time. Absent means
      // getResource returns null and SchemaFactory.newSchema(null) validates nothing, so an entire
      // run parses
      // unvalidated in silence. That is why the missing case says so, once, instead of passing
      // unnoticed.
      var schemaResource = getClass().getResource("/schema.xsd");
      if (schemaResource == null) {
        LOGGER.warn(
            "No /schema.xsd on the classpath, so documents are parsed WITHOUT validation. It comes"
                + " from the com.stano:schema-xsd artifact; in a native image it must also be"
                + " included explicitly through -H:IncludeResources.");
      }

      SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      Schema schema = schemaFactory.newSchema(schemaResource);

      // Allocate and configure JAXP SAX parser factory
      SAXParserFactory factory = SAXParserFactory.newInstance();
      factory.setSchema(schema);
      factory.setNamespaceAware(true);
      factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
      factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
      factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

      // Allocate parser
      SAXParser parser = factory.newSAXParser();

      // Return configured SAX XMLReader
      XMLReader reader = parser.getXMLReader();
      reader.setErrorHandler(new DefaultHandler());

      return reader;
    } catch (Exception x) {
      throw new RuntimeException(x);
    }
  }
}
