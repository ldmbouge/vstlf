/************************************************************************
 MIT License

 Copyright (c) 2010 University of Connecticut

 Permission is hereby granted, free of charge, to any person obtaining
 a copy of this software and associated documentation files (the
 "Software"), to deal in the Software without restriction, including
 without limitation the rights to use, copy, modify, merge, publish,
 distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to
 the following conditions:

 The above copyright notice and this permission notice shall be
 included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
***********************************************************************/

package edu.uconn.vstlf.database.xml;

/*
* Copyright 2002 Sun Microsystems, Inc. All rights reserved.
*
* Redistribution and use in source and binary forms, with or without
* modification, are permitted provided that the following conditions
* are met:
*
* - Redistributions of source code must retain the above copyright
*   notice, this list of conditions and the following disclaimer.
*
* - Redistribution in binary form must reproduce the above copyright
*   notice, this list of conditions and the following disclaimer in
*   the documentation and/or other materials provided with the
*   distribution.
*
* Neither the name of Sun Microsystems, Inc. or the names of
* contributors may be used to endorse or promote products derived
* from this software without specific prior written permission.
*
* This software is provided "AS IS," without a warranty of any
* kind. ALL EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND
* WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY,
* FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT, ARE HEREBY
* EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES
* SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
* DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN
* OR ITS LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR
* FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR
* PUNITIVE DAMAGES, HOWEVER CAUSED AND REGARDLESS OF THE THEORY OF
* LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE SOFTWARE,
* EVEN IF SUN HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
*
* You acknowledge that Software is not designed, licensed or intended
* for use in the design, construction, operation or maintenance of
* any nuclear facility.
 */

import java.io.File;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Sample Utility class to work with DOM document
 */
public class DOMUtil {

  /** Prints the specified node, then prints all of its children. */
  public static void printDOM(Node node) {
    int type = node.getNodeType();
    switch (type) {
      // print the document element
      case Node.DOCUMENT_NODE: {
        System.out.println("<?xml version=\"1.0\" ?>");
        printDOM(((Document)node).getDocumentElement());
        break;
      }

      // print element with attributes
    case Node.ELEMENT_NODE: {
      System.out.print("<");
      System.out.print(node.getNodeName());
      NamedNodeMap attrs = node.getAttributes();
      for (int i = 0; i < attrs.getLength(); i++) {
        Node attr = attrs.item(i);
        System.out.print(" " + attr.getNodeName().trim() +
                         "=\"" + attr.getNodeValue().trim() +
                         "\"");
      }
      System.out.println(">");

      NodeList children = node.getChildNodes();
      if (children != null) {
        int len = children.getLength();
        for (int i = 0; i < len; i++)
          printDOM(children.item(i));
      }

      break;
    }

    // handle entity reference nodes
  case Node.ENTITY_REFERENCE_NODE: {
    System.out.print("&");
    System.out.print(node.getNodeName().trim());
    System.out.print(";");
    break;
  }

  // print cdata sections
case Node.CDATA_SECTION_NODE: {
  System.out.print("<![CDATA[");
  System.out.print(node.getNodeValue().trim());
  System.out.print("]]>");
  break;
}

// print text
case Node.TEXT_NODE: {
  System.out.print(node.getNodeValue().trim());
  break;
}

// print processing instruction
case Node.PROCESSING_INSTRUCTION_NODE: {
  System.out.print("<?");
  System.out.print(node.getNodeName().trim());
  String data = node.getNodeValue().trim(); {
    System.out.print(" ");
    System.out.print(data);
  }
  System.out.print("?>");
  break;
}
    }

    if (type == Node.ELEMENT_NODE) {
      System.out.println();
      System.out.print("</");
      System.out.print(node.getNodeName().trim());
      System.out.print('>');
    }
  }

  /**
   * Parse the XML file and create Document
   * @param fileName
   * @return Document
   */
  public static Document parse(String fileName) throws Exception{
    Document document = null;
    // Initiate DocumentBuilderFactory
    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

    // To get a validating parser
    factory.setValidating(false);
    // To get one that understands namespaces
    factory.setNamespaceAware(true);

    //try {
      // Get DocumentBuilder
      DocumentBuilder builder = factory.newDocumentBuilder();
      // Parse and load into memory the Document
      document = builder.parse( new File(fileName));
      return document;
      /*
    } catch (SAXParseException spe) {
      // Error generated by the parser
      System.out.println("\n** Parsing error , line " + spe.getLineNumber()
                         + ", uri " + spe.getSystemId());
      System.out.println(" " + spe.getMessage() );
      // Use the contained exception, if any
      Exception x = spe;
      if (spe.getException() != null)
        x = spe.getException();
      x.printStackTrace();
    } catch (SAXException sxe) {
      // Error generated during parsing
      Exception x = sxe;
      if (sxe.getException() != null)
        x = sxe.getException();
      x.printStackTrace();
    } catch (ParserConfigurationException pce) {
      // Parser with specified options can't be built
      pce.printStackTrace();
    } catch (IOException ioe) {
      // I/O error
      ioe.printStackTrace();
    }

    return null; //*/
  }

  /**
   * This method writes a DOM document to a file
   * @param filename
   * @param document
   */
  public static void writeXmlToFile(String filename, Document document) {
    try {
      // Prepare the DOM document for writing
      Source source = new DOMSource(document);

      // Prepare the output file
      File file = new File(filename);
      Result result = new StreamResult(file);

      // Write the DOM document to the file
      // Get Transformer
      Transformer xformer = TransformerFactory.newInstance().newTransformer();
      // Write to a file
      xformer.transform(source, result);
    } catch (TransformerConfigurationException e) {
      System.out.println("TransformerConfigurationException: " + e);
    } catch (TransformerException e) {
      System.out.println("TransformerException: " + e);
    }
  }

  /**
   * Count Elements in Document by Tag Name
   * @param tag
   * @param document
   * @return number elements by Tag Name
   */
  public static int countByTagName(String tag, Document document){
    NodeList list = document.getElementsByTagName(tag);
    return list.getLength();
  }

}