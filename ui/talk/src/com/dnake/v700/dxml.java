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
	private DocumentBuilderFactory mFactory = null; 
	private DocumentBuilder mBuilder = null;
	private Document mDoc = null;

	private void doInitialize() {
		mFactory = DocumentBuilderFactory.newInstance();
		try {
			mBuilder = mFactory.newDocumentBuilder();
			mDoc = mBuilder.newDocument();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
	}

	public dxml() {
		this.doInitialize();
	}

	public dxml(String xml) {
		this.doInitialize();
		this.parse(xml);
	}

	public boolean parse(String xml) {
		if (xml == null || xml.length() < 16)
			return false;
		ByteArrayInputStream in = new ByteArrayInputStream(xml.getBytes());
		return this.parse(in);
	}

	public boolean parse(InputStream in) {
		if (in != null) {
			try {
				mDoc = mBuilder.parse(in);
				return true;
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
		if (s != null) {
			try {
				return Integer.parseInt(s);
			} catch(Exception e) {
			}
		}
		return val;
	}

	public float getFloat(String tags, float val) {
		String s = this.getText(tags);
		if (s != null) {
			try {
				return Float.parseFloat(s);
			} catch(Exception e) {
			}
		}
		return val;
	}

	public String getText(String tags, String val) {
		String s = this.getText(tags);
		if (s == null)
			return val;
		return s;
	}

	public String getText(String tags) {
		Node n = mDoc.getFirstChild();
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

	public void setFloat(String tags, float val) {
		this.setText(tags, String.valueOf(val));
	}

	public void setText(String tags, String val) {
		Node n = mDoc.getFirstChild();

		StringTokenizer tk = new StringTokenizer(tags, "/");
		String s = tk.nextToken();
		if (n == null) {
			n = mDoc.createElement(s);
			mDoc.appendChild(n);
		}

		while (tk.hasMoreTokens()) {
			s = tk.nextToken();

			Node n2 = findFirstElement(n, s);
			if (n2 == null) {
				n2 = mDoc.createElement(s);
				n.appendChild(n2);
			}
			n = n2;
		}
		if (val != null) {
			if (n.hasChildNodes()) {
				NodeList nl = n.getChildNodes();
				for(int i=0; i<nl.getLength(); i++) {
					Node n2 = nl.item(i);
					n.removeChild(n2);
				}
			}
			n.appendChild(mDoc.createTextNode(val));
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
        DOMSource source = new DOMSource(mDoc);
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
		} catch (IOException e) {
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
		} catch (IOException e) {
		}
	}
}
