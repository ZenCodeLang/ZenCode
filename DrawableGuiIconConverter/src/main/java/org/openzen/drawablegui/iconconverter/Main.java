/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.drawablegui.iconconverter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Hoofdgebruiker
 */
public class Main {
	public static void main(String[] args) throws Exception {
		String filename = "baseline-build-24px.svg"; //args[0];
		//String filename = "baseline-dashboard-24px.svg";
		String className = "BuildIcon";
		File file = new File(filename);
		if (!file.exists()) {
			System.out.println("No such file: " + filename);
			return;
		}
		
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(file);
		doc.getDocumentElement().normalize();
		
		List<String> pathNames = new ArrayList<>();
		
		Element rootElement = doc.getDocumentElement();
		String width = rootElement.getAttribute("width");
		String height = rootElement.getAttribute("height");
		StringBuilder output = new StringBuilder();
		output.append("import org.openzen.drawablegui.DCanvas;\n");
		output.append("import org.openzen.drawablegui.DPath;\n");
		output.append("import org.openzen.drawablegui.DTransform2D;\n");
		output.append("import org.openzen.drawablegui.DColorableIcon;\n");
		output.append("import org.openzen.drawablegui.draw.DDrawTarget;\n");
		output.append("\n");
		output.append("public class ").append(className).append(" implements DColorableIcon {\n");
		output.append("\tpublic static final ").append(className).append(" INSTANCE = new ").append(className).append("();\n");
		output.append("\t\n");
		output.append("\tprivate ").append(className).append("() {}\n");
		output.append("\t\n");
		
		NodeList childNodes = rootElement.getChildNodes();
		for (int i = 0; i < childNodes.getLength(); i++) {
			Node node = childNodes.item(i);
			if (node.getNodeName().equals("path")) {
				Element pathElement = (Element)node;
				String pathName = "PATH_" + pathNames.size();
				output.append("\tprivate static final DPath ").append(pathName).append(" = tracer -> {\n");
				convertPath(pathElement.getAttribute("d"), output, "\t\t");
				output.append("\t};\n");
				pathNames.add(pathName);
			} else if (node.getNodeName().equals("#text")) {
				// skip
			} else {
				System.out.println("Warning: " + node.getNodeName() + " not supported");
			}
		}
		
		output.append("\t\n");
		output.append("\t@Override\n");
		output.append("\tpublic void draw(DDrawTarget target, int z, DTransform2D transform, int color) {\n");
		for (String pathName : pathNames) {
			output.append("\t\target.fillPath(z, ").append(pathName).append(", transform, color);\n");
		}
		output.append("\t}\n");
		output.append("\n");
		output.append("\t@Override\n");
		output.append("\tpublic float getNominalWidth() {\n");
		output.append("\t\treturn ").append(width).append(";\n");
		output.append("\t}\n");
		output.append("\n");
		output.append("\t@Override\n");
		output.append("\tpublic float getNominalHeight() {\n");
		output.append("\t\treturn ").append(height).append(";\n");
		output.append("\t}\n");
		output.append("}\n");
		System.out.println(output.toString());
	}
	
	private static void convertPath(String path, StringBuilder output, String indent) {
		CharStream stream = new CharStream(path);
		float x = 0;
		float y = 0;
		float guideX = 0;
		float guideY = 0;
		char instruction = stream.next();
		while (stream.hasMore()) {
			if (!stream.nextIsNumber())
				instruction = stream.next();
			
			switch (instruction) {
				case 'M': {
					String sx = stream.nextFloat();
					String sy = stream.nextFloat();
					output.append(indent).append("tracer.moveTo(")
							.append(sx).append("f, ")
							.append(sy).append("f);\n");
					x = Float.parseFloat(sx);
					y = Float.parseFloat(sy);
					break;
				}
				case 'm': {
					x += stream.parseFloat();
					y += stream.parseFloat();
					output.append(indent)
							.append("tracer.moveTo(")
							.append(x).append("f, ")
							.append(y).append("f);\n");
					break;
				}
				case 'L': {
					String sx = stream.nextFloat();
					String sy = stream.nextFloat();
					output.append(indent).append("tracer.lineTo(")
							.append(sx).append("f, ")
							.append(sy).append("f);\n");
					x = Float.parseFloat(sx);
					y = Float.parseFloat(sy);
					break;
				}
				case 'l': {
					x += stream.parseFloat();
					y += stream.parseFloat();
					output.append(indent)
							.append("tracer.lineTo(")
							.append(x).append("f, ")
							.append(y).append("f);\n");
					break;
				}
				case 'H': {
					x = stream.parseFloat();
					output.append(indent)
							.append("tracer.lineTo(")
							.append(x).append("f, ")
							.append(y).append("f);\n");
					break;
				}
				case 'h': {
					x += stream.parseFloat();
					output.append(indent)
							.append("tracer.lineTo(")
							.append(x).append("f, ")
							.append(y).append("f);\n");
					break;
				}
				case 'V': {
					y = stream.parseFloat();
					output.append(indent)
							.append("tracer.lineTo(")
							.append(x).append("f, ")
							.append(y).append("f);\n");
					break;
				}
				case 'v': {
					y += stream.parseFloat();
					output.append(indent)
							.append("tracer.lineTo(")
							.append(x).append("f, ")
							.append(y).append("f);\n");
					break;
				}
				case 'C': {
					float x1 = stream.parseFloat();
					float y1 = stream.parseFloat();
					float x2 = stream.parseFloat();
					float y2 = stream.parseFloat();
					float x3 = stream.parseFloat();
					float y3 = stream.parseFloat();
					output.append(indent)
							.append("tracer.bezierCubic(")
							.append(x1).append("f, ")
							.append(y1).append("f, ")
							.append(x2).append("f, ")
							.append(y2).append("f, ")
							.append(x3).append("f, ")
							.append(y3).append("f);\n");
					x = x3;
					y = y3;
					guideX = x2;
					guideY = y2;
					break;
				}
				case 'c': {
					float x1 = x + stream.parseFloat();
					float y1 = y + stream.parseFloat();
					float x2 = x + stream.parseFloat();
					float y2 = y + stream.parseFloat();
					float x3 = x + stream.parseFloat();
					float y3 = y + stream.parseFloat();
					x = x3;
					y = y3;
					guideX = x2;
					guideY = y2;
					output.append(indent)
							.append("tracer.bezierCubic(")
							.append(x1).append("f, ")
							.append(y1).append("f, ")
							.append(x2).append("f, ")
							.append(y2).append("f, ")
							.append(x3).append("f, ")
							.append(y3).append("f);\n");
					break;
				}
				case 'S': {
					float x1 = x + (x - guideX);
					float y1 = y + (y - guideY);
					float x2 = stream.parseFloat();
					float y2 = stream.parseFloat();
					float x3 = stream.parseFloat();
					float y3 = stream.parseFloat();
					output.append(indent)
							.append("tracer.bezierCubic(")
							.append(x1).append("f, ")
							.append(y1).append("f, ")
							.append(x2).append("f, ")
							.append(y2).append("f, ")
							.append(x3).append("f, ")
							.append(y3).append("f);\n");
					x = x3;
					y = y3;
					guideX = x2;
					guideY = y2;
					break;
				}
				case 's': {
					float x1 = x + (x - guideX);
					float y1 = y + (y - guideY);
					float x2 = x + stream.parseFloat();
					float y2 = y + stream.parseFloat();
					float x3 = x + stream.parseFloat();
					float y3 = y + stream.parseFloat();
					x = x3;
					y = y3;
					guideX = x2;
					guideY = y2;
					output.append(indent)
							.append("tracer.bezierCubic(")
							.append(x1).append("f, ")
							.append(y1).append("f, ")
							.append(x2).append("f, ")
							.append(y2).append("f, ")
							.append(x3).append("f, ")
							.append(y3).append("f);\n");
					break;
				}
				case 'Z':
				case 'z':
					output.append(indent).append("tracer.close();\n");
					break;
				default:
					System.out.println("Error: path instruction " + instruction + " not supported");
					return;
			}
		}
	}
	
	private static class CharStream {
		private int index;
		private final String value;
		
		public CharStream(String value) {
			this.index = 0;
			this.value = value;
		}
		
		public char next() {
			skipWhitespace();
			return value.charAt(index++);
		}
		
		public boolean hasMore() {
			return index < value.length();
		}
		
		public boolean nextIsNumber() {
			skipWhitespace();
			char c = value.charAt(index);
			return c == '-' || (c >= '0' && c <= '9');
		}
		
		public String nextFloat() {
			skipWhitespace();
			int from = index;
			if (value.charAt(index) == '-')
				index++;
			
			char next = value.charAt(index);
			while (next >= '0' && next <= '9')
				next = value.charAt(++index);
			if (value.charAt(index) == '.') {
				index++;
				next = value.charAt(index);
				while (next >= '0' && next <= '9')
					next = value.charAt(++index);
			}
			
			return value.substring(from, index);
		}
		
		public float parseFloat() {
			return Float.parseFloat(nextFloat());
		}
		
		public void skipWhitespace() {
			char next = value.charAt(index);
			while (next == ' ' || next == '\t' || next == '\r' || next == '\n' || next == ',')
				next = value.charAt(++index);
		}
	}
}
