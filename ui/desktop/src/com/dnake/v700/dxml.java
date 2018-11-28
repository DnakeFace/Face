package com.dnake.v700;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class dxml {
	DocumentBuilderFactory docFactory = null; 
	DocumentBuilder docBuilder = null;
	Document doc = null;

	public dxml() {
		docFactory = DocumentBuilderFactory.newInstance();
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		doc = docBuilder.newDocument();
	}

	public boolean parse(String xml) {
		if (xml == null || xml.length() < 16)
			return false;
		ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
		return this.parse(in);
	}

	public boolean parse(InputStream in) {
		if (in == null)
			return false;

		try {
			doc = docBuilder.parse(in);
			return true;
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Node findFirstElement(Node parent, String tag) {
		if (parent == null || parent.hasChildNodes() == false)
			return null;

		Node n = parent.getFirstChild();
		while (n != null) {
			if (n.getNodeName().equals(tag))
				return n;
			n = n.getNextSibling();
		}
		return null;
	}

	public int getInt(String tags, int val) {
		String s = this.getText(tags);
		if (s != null && s.length() < 11)
			return Integer.parseInt(s);
		return val;
	}

	public String getText(String tags, String val) {
		String s = this.getText(tags);
		if (s == null)
			return val;
		return s;
	}

	public String getText(String tags) {
		Node n = doc.getFirstChild();
		if (n == null)
			return null;

		StringTokenizer tk = new StringTokenizer(tags, "/");
		String s = tk.nextToken();
		if (n.getNodeName().equals(s) == false)
			return null;
		while (n != null && tk.hasMoreTokens()) {
			n = findFirstElement(n, tk.nextToken());
		}
		if (n != null && n.hasChildNodes())
			return n.getFirstChild().getNodeValue();
		return null;
	}

	public void setInt(String tags, int val) {
		this.setText(tags, String.valueOf(val));
	}

	public void setText(String tags, String val) {
		Node n = doc.getFirstChild();
		Node n2;
		String s;

		StringTokenizer tk = new StringTokenizer(tags, "/");
		s = tk.nextToken();
		if (n == null) {
			n = doc.createElement(s);
			doc.appendChild(n);
		}

		while (tk.hasMoreTokens()) {
			s = tk.nextToken();

			n2 = findFirstElement(n, s);
			if (n2 == null) {
				n2 = doc.createElement(s);
				n.appendChild(n2);
			}
			n = n2;
		}
		if (val != null) {
			if (n.hasChildNodes()) {
				NodeList nl = n.getChildNodes();
				for(int i=0; i<nl.getLength(); i++) {
					n2 = nl.item(i);
					n.removeChild(n2);
				}
			}
			n.appendChild(doc.createTextNode(val));
		}
	}

	public String toString() {
		TransformerFactory factory = TransformerFactory.newInstance();
        Transformer former = null;
		try {
			former = factory.newTransformer();
		} catch (TransformerConfigurationException e1) {
			e1.printStackTrace();
		}
		StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        DOMSource source = new DOMSource(doc);
        try {
        	former.transform(source, result);
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return writer.toString();
	}

	public Boolean load(String url) {
		Boolean result = false;
		try {
			FileInputStream in = new FileInputStream(url);
			if (this.parse(in))
				result = true;
			in.close();
		} catch (FileNotFoundException e) {
			//e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public void save(String url) {
		try {
			FileOutputStream out = new FileOutputStream(url);
			out.write(this.toString().getBytes());
			out.flush();
			out.getFD().sync();
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
