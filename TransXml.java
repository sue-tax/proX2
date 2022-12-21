/**
 *
 */

/* 最終的にはパス１で解析結果を
 * TransItemのリストにして、
 * その後のパスは、TransItemを元に処理する
 *
 * 当面はpythonとの比較をかねて、従来方式とする
 */


package data;

import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import cmn.D;

// TODO 各セルに端数処理を入れたいが、
//       |d3| の縦棒がコンフリクトする
//       全角｜ならセーフ？
// TODO 循環定義が未対応と思う
// TODO セルに計算式を入れて、最後が=ならば、答を出力
//       ItemCalc [...] と同じようなイメージ
// TODO `&sum('.'(@)(@0))` @のみで２～直前までを示す
// TODO '表名'(0)(2)  (0)は最後の行や列を示す


/**
 * XMLの処理（キャプション、変数の管理）
 *
 */
public class TransXml {
	/** XMLに登録するエレメントの種類を示す属性名 */
	public static final String ATTRIB_TYPE = "type";
	/** XMLに登録するエレメントの種類を示す属性値 */
	public static final String INDICATOR_CAPTION = "proX_CAPTION";
	public static final String INDICATOR_VARIABLE = "proX_VARIABLE";
	public static final String INDICATOR_TABLE = "proX_TABLE";
	public static final String INDICATOR_CELL = "proX_CELL";

	/** 変数定義の表示用の文字列を示す属性名 */
	private static final String ATTRIB_STROUT = "strout";

	/** 変数定義の計算式の文字列（式表示のため？ではない？）を示す属性名 */
	private static final String ATTRIB_EXPR = "expr";

	/** 変数定義の端数処理の文字列を示す属性名 */
	private static final String ATTRIB_HASU = "hasu";

	/** 表変数定義の行を示す属性名 */
	public static final String ATTRIB_ROW = "row";
	/** 表変数定義の列を示す属性名 */
	public static final String ATTRIB_COLUMN = "column";

	/** キャプションのルートを示す文字列 */
	private static final String ROOT_CAPTION_ID = "root";

	/** キャプションの見出し指定がない場合のダミーの文字列 */
	private static final String DUMMY_CAPTION_ID = "_";

	/** 表変数定義のセルを示すノード名 */
	public static final String CELL_NODE_NAME = "cell";
	// 通常変数や表は、変数名や表名がノード名になる */

	private Document doc;
    public Element rootCaption;	// できれば、privateに

    /** 処理中のキャプションの深さ */
    private int currentDepth;

    /** 処理中のキャプションを示すエレメント */
    public Element currentCaption;		// できれば、privateに


    public static final String ERROR_VARIABLE_NOTFOUND
    	= "!変数が見つからない!";
    public static final String ERROR_VARIABLE_DUPLICATE
    	= "!変数が複数見つかった!";
    public static final String ERROR_VARIABLE_CIRCULATED
    	= "!変数の定義が循環している!";
//    private static final String ERROR_VARIABLE_NOCALC =
//    		"!変数がまだ計算されていない!";
    public static final String ERROR_TABLE_NOTFOUND
		= "!表が見つからない!";
	public static final String ERROR_TABLE_DUPLICATE
		= "!表が複数見つかった!";
	public static final String ERROR_NOT_FOUND_CELL
	    	= "!表のセルが見つからない!";
	public static final String ERROR_INVALID_INDEX
			= "!表のインデックスが間違っています!";

    private static final String ERROR_XML_TREEXPATH
    	= "!XMLエラー(treepath)";	// 最後の!は不要
    private static final String ERROR_XML_ROOTXPATH
		= "!XMLエラー(rootpath)";	// 最後の!は不要
    private static final String ERROR_XML_NODEXPATH =
    		"!XMLエラー(nodepath)";	// 最後の!は不要


	TransXml() {
		try {
			DocumentBuilderFactory docFactory
					= DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder
					= docFactory.newDocumentBuilder();
		    doc = docBuilder.newDocument();
		    rootCaption = doc.createElement(
		    		ROOT_CAPTION_ID);
		    doc.appendChild(rootCaption);
		    Attr attr = doc.createAttribute(
		    		ATTRIB_TYPE);
		    attr.setValue(INDICATOR_CAPTION);
		    rootCaption.setAttributeNode(attr);
		} catch (ParserConfigurationException pce) {
	          pce.printStackTrace();
	    }
	}

	/**
	 * translateの各パスの開始時に呼ぶ
	 */
	public void reset() {
	    this.currentDepth = 0;
	    this.currentCaption = this.rootCaption;
	}

	/**
	 * キャプションをXMLに登録する
	 * @param strCaptionID キャプション名
	 * @param depth キャプションの深さ
	 * @return キャプションのXMLのエレメント（エラーの場合はnull）
	 */
	public Element registCaption( String strCaptionID,
			int depth
			) {
		D.dprint_method_start();
		D.dprint(strCaptionID);
//		D.dprint(depth);
		Element elementCaption = null;
		try {
			elementCaption = doc.createElement(
					strCaptionID);
		} catch (DOMException e) {
			return null;
		}
	    Attr attr = doc.createAttribute(
	    		ATTRIB_TYPE);
	    attr.setValue(INDICATOR_CAPTION);
	    elementCaption.setAttributeNode(attr);
//	    D.dprint(elementCaption);
//	    D.dprint(this.currentDepth);
	    if (depth == this.currentDepth + 1) {
			// 直前のキャプションの子キャプション
//			D.dprint(this.currentCaption);
			this.currentCaption.appendChild(elementCaption);
			this.currentCaption = elementCaption;
//			D.dprint(this.currentCaption);
		} else if (depth > this.currentDepth + 1) {
			// 直前のキャプションの孫以下の子孫キャプション
			for (int _d = this.currentDepth + 1;
					_d < depth; _d++ ) {
				Element dummyCaption = doc.createElement(
						DUMMY_CAPTION_ID);
				this.currentCaption.appendChild(dummyCaption);
				this.currentCaption = dummyCaption;
			}
			this.currentCaption.appendChild(elementCaption);
			this.currentCaption = elementCaption;
		} else if (depth == this.currentDepth) {
			// 直前のキャプションの弟キャプション
			Element parentCaption = (Element)this.currentCaption.
					getParentNode();
		    parentCaption.appendChild(elementCaption);
		    this.currentCaption = elementCaption;
		} else {
			// 直前のキャプションの祖先（親含む）の弟キャプション
			Element parentCaption = this.currentCaption;
			for (int _d = depth - 1;
					_d < this.currentDepth; _d++) {
				Element childCaption = parentCaption;
				parentCaption = (Element)childCaption.
						getParentNode();
			}
		    parentCaption.appendChild(elementCaption);
		    this.currentCaption = elementCaption;
		}
	    this.currentDepth = depth;
		D.dprint_xml(this);
		D.dprint_method_end();
		return elementCaption;
	}


	/**
	 * 変数定義のアトランダム処理をする際の
	 * カレント・キャプションを元に戻す、
	 * または、CaptionItemの処理で
	 * カレント・キャプションを設定する
	 * @param elementCaption 切替えるキャプション
	 */
	public void setCurrentCaption( Element elementCaption ) {
		this.currentCaption = elementCaption;
		return;
	}

	/**
	 * 指定された変数を示すエレメントからキャプションを設定する
	 * @param elementVariable
	 */
	public Element setCaptionVariable( Element elementVariable ) {
		D.dprint_method_start();
		D.dprint(elementVariable);
		Element oldCaption = this.currentCaption;
		this.currentCaption = (Element)elementVariable.
				getParentNode();
		D.dprint(oldCaption);
		D.dprint_method_end();
		return oldCaption;
	}


	/**
	 * 通常変数をXMLに登録する
	 * @param strVariableID
	 * @return
	 */
	public Element registVariableNormal(
			String strVariableID, String strCalc ) {
		D.dprint_method_start();
		D.dprint(strVariableID);
		Element elementVariable = doc.createElement(
				strVariableID);
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_TYPE,
	    		TransXml.INDICATOR_VARIABLE);
	    D.dprint(elementVariable);
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_EXPR,
	    		strCalc);	// 暫定的に登録する
		this.currentCaption.appendChild(elementVariable);
		D.dprint_xml(this);
		D.dprint(elementVariable);
		D.dprint_method_end();
		return elementVariable;
	}

	/**
	 * 表変数をXMLに登録する
	 * @param strVariableID
	 * @return
	 */
	public Element registVariableTable(
			String strVariableID, String strTable ) {
		D.dprint_method_start();
		D.dprint(strVariableID);
		Element elementVariable = doc.createElement(
				strVariableID);
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_TYPE,
	    		TransXml.INDICATOR_TABLE);
	    D.dprint(elementVariable);
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_EXPR,
	    		strTable);	// 暫定的に登録する
		this.currentCaption.appendChild(elementVariable);
		D.dprint_xml(this);
		D.dprint(elementVariable);
		D.dprint_method_end();
		return elementVariable;
	}


	public void registVariableTableRC(
			Element elementTable,
			int row_num, int column_num ) {
		D.dprint_method_start();
		elementTable.setAttribute(
	    		TransXml.ATTRIB_ROW,
	    		String.valueOf(row_num));
		elementTable.setAttribute(
	    		TransXml.ATTRIB_COLUMN,
	    		String.valueOf(column_num));
		D.dprint_method_end();
		return;
	}


	/**
	 * 表内のセルをXMLに登録する
	 * @param strVariableID
	 * @return
	 */
	public Element registVariableCell(
			Element tableElement,
			int row_num, int column_num,
			String strValue) {
		D.dprint_method_start();
		D.dprint(tableElement);
		D.dprint(row_num);
		D.dprint(column_num);
		D.dprint(strValue);
		D.dprint(strValue);
		Element elementVariable = doc.createElement(
				CELL_NODE_NAME);
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_TYPE,
	    		TransXml.INDICATOR_CELL);
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_ROW,
	    		String.valueOf(row_num));
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_COLUMN,
	    		String.valueOf(column_num));
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_EXPR,
	    		strValue);	// 暫定的に登録する
//		this.currentCaption.appendChild(elementVariable);
	    tableElement.appendChild(elementVariable);
		D.dprint_xml(this);
		D.dprint(elementVariable);
		D.dprint_method_end();
		return elementVariable;
	}


	public class CellValue {
		public String strValue;	// = null;
		public String strIndex1;	// = null;
		public String strIndex2;	// = null;
		public String strPath;	// = null;
		public String strError;	// = null;
		public Element element;	// = null;

		public CellValue() {
			strValue = null;
			strIndex1 = null;
			strIndex2 = null;
			strPath = null;
			strError = null;
			element = null;
		}
	}


	public CellValue getCellValue( Element tagTable,
			int index1, String strXpathIndex1,
			int index2, String strXpathIndex2 ) {
		D.dprint_method_start();
		D.dprint(tagTable);
		D.dprint(index1);
		D.dprint(strXpathIndex1);
		D.dprint(index2);
		D.dprint(strXpathIndex2);
		CellValue cv = new CellValue();
		if (index1 != 0) {
			cv.strIndex1 = String.valueOf(index1);
		} else {
			XPath xpath = XPathFactory.newInstance().
					newXPath();
			XPathExpression expr;
			NodeList nodeList;
			try {
				expr = xpath.compile(strXpathIndex1);
				nodeList = (NodeList) expr.evaluate(
						tagTable,
						XPathConstants.NODESET);
				D.dprint(nodeList);
				D.dprint(nodeList.getLength());
				if (nodeList.getLength() == 1) {
					cv.strIndex1 = nodeList.item(0).
							getTextContent();
					D.dprint(cv.strIndex1);
				} else {
					cv.strError = ERROR_INVALID_INDEX;
				}
			} catch (XPathExpressionException e) {
				D.dprint(e);
				cv.strError = "!error" + e.toString() +"!";
			}
		}
		if (index2 != 0) {
			cv.strIndex2 = String.valueOf(index2);
		} else {
			XPath xpath = XPathFactory.newInstance().
					newXPath();
			XPathExpression expr;
			NodeList nodeList;
			try {
				expr = xpath.compile(strXpathIndex2);
				nodeList = (NodeList) expr.evaluate(
						tagTable,
						XPathConstants.NODESET);
				D.dprint(nodeList);
				if (nodeList.getLength() == 1) {
					cv.strIndex2 = nodeList.item(0).
							getTextContent();
					D.dprint(cv.strIndex2);
				} else {
					cv.strError = ERROR_INVALID_INDEX;
				}
			} catch (XPathExpressionException e) {
				D.dprint(e);
				cv.strError = "!error" + e.toString() +"!";
			}
		}
		if (cv.strError != null) {
			D.dprint(cv.strError);
			D.dprint_method_end();
			return cv;
		}
		String strXpath = String.format(
				"./%s[@%s=\"%s\"][@%s=\"%s\"]"
				+ "[@%s=\"%s\"]",
				CELL_NODE_NAME,
				ATTRIB_TYPE, INDICATOR_CELL,
				ATTRIB_ROW, cv.strIndex1,
				ATTRIB_COLUMN, cv.strIndex2);
		D.dprint(strXpath);
		XPath xpath = XPathFactory.newInstance().
				newXPath();
		XPathExpression expr;
		NodeList nodeList;
		try {
			expr = xpath.compile(strXpath);
			nodeList = (NodeList) expr.evaluate(
					tagTable,
					XPathConstants.NODESET);
			D.dprint(nodeList);
			if (nodeList.getLength() == 1) {
				cv.strValue = nodeList.item(0).getTextContent();
				D.dprint(cv.strValue);
				cv.element = (Element) nodeList.item(0);
			} else {
				cv.strError = ERROR_NOT_FOUND_CELL;
			}
		} catch (XPathExpressionException e) {
			D.dprint(e);
			cv.strError = "!error" + e.toString() +"!";
		}
		cv.strPath = getNodePath(tagTable);
		D.dprint(cv.strValue);
		D.dprint(cv.strIndex1);
		D.dprint(cv.strIndex2);
		D.dprint(cv.strPath);
		D.dprint_method_end();
		return cv;
	}


	public ArrayList<ArrayList<Element>>
			getCellElement( Element tableElement ) {
		D.dprint_method_start();
		D.dprint_xml(this);
		ArrayList<ArrayList<Element>> llCell =
				new ArrayList<ArrayList<Element>>();
		int row_num = Integer.valueOf(tableElement.
				getAttribute(ATTRIB_ROW));
		D.dprint(row_num);
		int column_num = Integer.valueOf(tableElement.
				getAttribute(ATTRIB_COLUMN));
		D.dprint(column_num);
		Node cellElement = tableElement.getFirstChild(); // 子ノードを取得
		for (int row=1;row<=row_num;row++) {
			ArrayList<Element> lCell =
					new ArrayList<Element>();
			for (int column=1;column<=column_num;column++) {
				D.dprint(cellElement.getAttributes().
						getNamedItem(ATTRIB_ROW));
				D.dprint(cellElement.getAttributes().
						getNamedItem(ATTRIB_COLUMN));
				lCell.add((Element) cellElement);
				cellElement = cellElement.getNextSibling();
			}
			llCell.add(lCell);
		}
		D.dprint(llCell);
		D.dprint_method_end();
		return llCell;
	}


	public void registVariableStrout(
			Element elementVariable, String strOut ) {
		D.dprint_method_start();
		D.dprint(strOut);
		elementVariable.setAttribute(
	    		TransXml.ATTRIB_STROUT,
	    		strOut);
		D.dprint_method_end();
		return;
	}


	/**
	 * 変数に計算額などをセットする
	 * @param elementVariable 変数のXMLエレメント
	 * @param strValue 計算額
	 * @param strOut 表示用の計算式
	 * @param strExpr 式表示の文字列
	 * @param strHasu 端数処理指示の文字列
	 * @return
	 */
	public boolean registVariableValue(
			Element elementVariable,
			String strValue, String strOut,
			String strExpr, String strHasu ) {
		D.dprint_method_start();
		D.dprint("++++++++++++++++++++++++++++++++++++++");
		D.dprint(strValue);
		Node elementText = elementVariable.getFirstChild();
		if (elementText != null) {
			D.dprint("AAAA");
			elementText.setNodeValue(strValue);
		} else {
			D.dprint("BBBB");
			elementVariable.appendChild(doc.
					createTextNode(strValue));
		}
		elementVariable.setAttribute(
	    		TransXml.ATTRIB_STROUT,
	    		strOut);
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_EXPR,
	    		strExpr);
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_HASU,
	    		strHasu);
		D.dprint_method_end();
		return true;
	}


	public void setVariableDuplicateError(
			NodeList listNode ) {
		D.dprint_method_start();
		D.dprint(listNode);
		D.dprint_xml(this);
		for (int i=0; i < listNode.getLength(); i++) {
			Element element = (Element)listNode.item(i);
			element.appendChild(doc.createTextNode(
					ERROR_VARIABLE_DUPLICATE));
		}
		D.dprint_xml(this);
		D.dprint_method_end();
		return;
	}


	public boolean getVariableDuplicateError(
			Element elementVariable ) {
		D.dprint_method_start();
		D.dprint(elementVariable);
		String strValue = elementVariable.getTextContent();
		boolean flag = strValue.equals(ERROR_VARIABLE_DUPLICATE);
		D.dprint(flag);
		D.dprint_method_end();
		return flag;
	}


	/**
	 * キャプション内の同一変数の有無の確認
	 * @param strVariableID
	 * @return nodeList
	 */
	public NodeList searchVariable(
			String strVariableID ) {
		D.dprint_method_start();
		D.dprint(strVariableID);
		String strFind = String.format(
				"%s[@%s=\"%s\"]",
				strVariableID,
				TransXml.ATTRIB_TYPE,
				TransXml.INDICATOR_VARIABLE);
		D.dprint(strFind);
		XPath xpath = XPathFactory.newInstance().newXPath();
		XPathExpression expr;
		NodeList nodeList;
		try {
			expr = xpath.compile(strFind);
			nodeList = (NodeList) expr.evaluate(
					this.currentCaption,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			D.dprint(e);
			D.dprint_method_end();
			return null;
		}
		D.dprint(nodeList);
		D.dprint_method_end();
		return nodeList;
	}


	public void registVariableCirculate(
			Element elementVariable ) {
		D.dprint_method_start();
		String strError = ERROR_VARIABLE_CIRCULATED;
		// TODO テキストノードの削除
		// 変数（表のセルも）の子ノードは
		// テキストノードだけのはず
		D.dprint("*******************************************");
		Node elementText = elementVariable.getFirstChild();
		if (elementText != null) {
			D.dprint("AAAA");
			elementText.setNodeValue(strError);
		} else {
			D.dprint("BBBB");
			elementVariable.appendChild(doc.createTextNode(
				strError));
		}
		String strOut = elementVariable.
				getAttribute(ATTRIB_STROUT);
		elementVariable.setAttribute(
	    		TransXml.ATTRIB_STROUT,
	    		strOut + strError);
	    elementVariable.setAttribute(
	    		TransXml.ATTRIB_EXPR,
	    		strError);
	    D.dprint(elementVariable.getNodeName());
		D.dprint(elementVariable.
				getAttribute(ATTRIB_STROUT));
	    D.dprint(elementVariable.getTextContent());
		D.dprint_method_end();
		return;
	}


	public String getVariableID( Element element ) {
		D.dprint_method_start();
		D.dprint(element);
		String strVarialbeID = element.getNodeName();
		D.dprint(strVarialbeID);
		D.dprint_method_end();
		return strVarialbeID;
	}


	public static class ElementValue {
		/** 変数定義の際の実際計算式を表す文字列 */
		public String strOutput;
		/** 変数値を表す文字列 */
		public String strValue;
		/** 式表示用の文字列 */
		public String strExpr;
		/** 変数の参照パスを表す文字列 */
		public String strPath;
		/** エラーならエラーメッセージ、成功ならnull */
		public String strError;
		public Element element;
		public String strHasu;
	}


	/**
	 * 変数値を取り出すために変数定義から呼び出す
	 * @param elementVariable
	 * @return
	 */
	public ElementValue getVariable(
			Element elementVariable ) {
		D.dprint_method_start();
		D.dprint(elementVariable);
		ElementValue ev = new ElementValue();
		ev.strOutput = elementVariable.getAttribute(ATTRIB_STROUT);
		ev.strValue = elementVariable.getTextContent();
		ev.strExpr = elementVariable.getAttribute(ATTRIB_EXPR);
		ev.strPath = getNodePath((Node)elementVariable);
		if ((ev.strValue != "")
				&& (ev.strValue.charAt(0) == '!')) {
			ev.strError = ev.strValue;
		} else {
			ev.strError = null;
		}
		ev.element = elementVariable;
		ev.strHasu = elementVariable.getAttribute(ATTRIB_HASU);
		D.dprint(ev.strOutput);
		D.dprint(ev.strValue);
		D.dprint(ev.strExpr);
		D.dprint(ev.strPath);
		D.dprint(ev.strError);
		D.dprint_method_end();
		return ev;
	}


	/**
	 * XMLに登録してある変数のノードを探す。
	 * 現在のキャプションを基準に探す。
	 * もしなければ、全体から探す。
	 * @param strVariableID 変数名
	 * @return ElementValue
	 */
	public ElementValue getVariableSingleNoPath(
			String strVariableID ) {
		D.dprint_method_start();
		D.dprint(strVariableID);
		D.dprint_xml(this);
		ElementValue ev = new ElementValue();
		// 現在のキャプション内で変数を探す
		String strFind = String.format(
				"%s[@%s=\"%s\"]",
				strVariableID,
				TransXml.ATTRIB_TYPE,
				TransXml.INDICATOR_VARIABLE);
		XPath xpath = XPathFactory.newInstance().
				newXPath();
		XPathExpression expr;
		NodeList nodeList;
		D.dprint(this.currentCaption);
		D.dprint(this.currentCaption.getNodeName());
		try {
			expr = xpath.compile(strFind);
			nodeList = (NodeList) expr.evaluate(
					this.currentCaption,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			ev.strOutput = "";
			ev.strPath = "";
			ev.strValue = TransXml.ERROR_XML_TREEXPATH
					+ e.toString() + "!";
			ev.strError = ev.strValue;
			ev.element = null;
			D.dprint(e);
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		D.dprint(nodeList);
		D.dprint(nodeList.getLength());
		if (nodeList.getLength() != 0) {
			// 現在のキャプション内で変数が見つかった。
			assert(nodeList.getLength() == 1);
			Element element = (Element)nodeList.item(0);
			D.dprint(element.getTextContent());
			ev.strOutput = element.getAttribute(ATTRIB_STROUT);
			ev.strValue = element.getTextContent();
			ev.strPath = getNodePath((Node)element);
			ev.strExpr = element.getAttribute(ATTRIB_EXPR);
//			ev.strError = (ev.strValue != "")?null:
//				ERROR_VARIABLE_NOTFOUND;
			ev.strError = null;
			ev.element = element;
			D.dprint(ev.strOutput);
			D.dprint(ev.strValue);
			D.dprint(ev.strError);
			D.dprint_method_end();
			return ev;
		}
		// 全体から変数を探す
		strFind = String.format(
				"//%s[@%s=\"%s\"]",
				strVariableID,
				TransXml.ATTRIB_TYPE,
				TransXml.INDICATOR_VARIABLE);
		xpath = XPathFactory.newInstance().
				newXPath();
		try {
			expr = xpath.compile(strFind);
			nodeList = (NodeList) expr.evaluate(
					this.rootCaption,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			ev.strOutput = "";
			ev.strPath = "";
			ev.strValue = TransXml.ERROR_XML_ROOTXPATH
					+ e.toString() + "!";
			ev.strError = ev.strValue;
			ev.element = null;
			D.dprint(e);
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		if (nodeList.getLength() == 0) {
			ev.strOutput = "";
			ev.strPath = "";
			ev.strValue = TransXml.ERROR_VARIABLE_NOTFOUND;
			ev.strError = ev.strValue;
			ev.element = null;
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		if (nodeList.getLength() > 1) {
			ev.strOutput = "";
			ev.strPath = "";
			ev.strValue = TransXml.ERROR_VARIABLE_DUPLICATE;
			ev.strError = ev.strValue;
			ev.element = null;
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		Element element = (Element)nodeList.item(0);
		ev.strOutput = element.getAttribute(ATTRIB_STROUT);
		ev.strValue = element.getTextContent();
		ev.strPath = getNodePath(element);
		ev.strExpr = element.getAttribute(ATTRIB_EXPR);
		ev.strError = null;
		ev.element = element;
		D.dprint_method_end();
		return ev;
	}


	/**
	 * XMLに登録してある変数のノードを、指定されたパスから探す。
	 * パスが/から始まればルートから探し、
	 * それ以外は現在のキャプションを基準に探す。
	 * @param strVariableID 変数名
	 * @param strPath 指定パス
	 * @return ElementValue
	 */
	public ElementValue getVariableSingleWithPath(
			String strVariableID, String strPath ) {
		D.dprint_method_start();
		D.dprint(strVariableID);
		D.dprint(strPath);
		D.dprint_xml(this);
		ElementValue ev = new ElementValue();
		String strFind = String.format(
				"%s/%s[@%s=\"%s\"]",
				strPath,
				strVariableID,
				ATTRIB_TYPE,
				INDICATOR_VARIABLE);
		XPath xpath = XPathFactory.newInstance().
				newXPath();
		XPathExpression expr;
		NodeList nodeList;
		String strErrMsg;
		Element elementSearch;
		if (strFind.charAt(0) == '/') {
			strErrMsg = ERROR_XML_TREEXPATH;
			elementSearch = this.rootCaption;
			strFind = "/" + ROOT_CAPTION_ID + strFind;
		} else {
			strErrMsg = ERROR_XML_NODEXPATH;
			elementSearch = this.currentCaption;
		}
		D.dprint(strFind);
		try {
			expr = xpath.compile(strFind);
			nodeList = (NodeList) expr.evaluate(
					elementSearch,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			ev.strOutput = "";
			ev.strPath = "";
			ev.strValue = strErrMsg
					+ e.toString() + "!";
			ev.strError = ev.strValue;
			ev.element = null;
			D.dprint(e);
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		if (nodeList.getLength() == 0) {
			ev.strOutput = "";
			ev.strPath = "";
			ev.strValue = TransXml.ERROR_VARIABLE_NOTFOUND;
			ev.strError = ev.strValue;
			ev.element = null;
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		} else if (nodeList.getLength() != 1) {
			ev.strOutput = "";
			ev.strPath = "";
			ev.strValue = TransXml.ERROR_VARIABLE_DUPLICATE;
			ev.strError = ev.strValue;
			ev.element = null;
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		Element elementFound = (Element)nodeList.item(0);
		ev.strOutput = elementFound.getAttribute(ATTRIB_STROUT);
		ev.strValue = elementFound.getTextContent();
		ev.strPath = getNodePath(elementFound);
		ev.strExpr = elementFound.getAttribute(ATTRIB_EXPR);
		ev.strError = null;
		ev.element = elementFound;
		D.dprint_method_end();
		return ev;
	}


	/**
	 * XMLに登録してある表のノードを探す。
	 * 現在のキャプションを基準に探す。
	 * もしなければ、全体から探す。
	 * @param strTable 表名
	 * @return ElementValue elment,strErrorのみ使用
	 */
	public ElementValue getTableSingleNoPath(
			String strTable ) {
		D.dprint_method_start();
		D.dprint(strTable);
		D.dprint_xml(this);
		ElementValue ev = new ElementValue();
		// 現在のキャプション内で変数を探す
		String strFind = String.format(
				"%s[@%s=\"%s\"]",
				strTable,
				TransXml.ATTRIB_TYPE,
				TransXml.INDICATOR_TABLE);
		XPath xpath = XPathFactory.newInstance().
				newXPath();
		XPathExpression expr;
		NodeList nodeList;
		D.dprint(this.currentCaption);
		D.dprint(this.currentCaption.getNodeName());
		try {
			expr = xpath.compile(strFind);
			nodeList = (NodeList) expr.evaluate(
					this.currentCaption,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			ev.strError = TransXml.ERROR_XML_TREEXPATH
					+ e.toString() + "!";
			ev.element = null;
			D.dprint(e);
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		D.dprint(nodeList);
		D.dprint(nodeList.getLength());
		if (nodeList.getLength() != 0) {
			// 現在のキャプション内で変数が見つかった。
			assert(nodeList.getLength() == 1);
			Element element = (Element)nodeList.item(0);
			D.dprint(element.getTextContent());
			ev.strError = null;
			ev.element = element;
			D.dprint_method_end();
			return ev;
		}
		// 全体から変数を探す
		strFind = String.format(
				"//%s[@%s=\"%s\"]",
				strTable,
				TransXml.ATTRIB_TYPE,
				TransXml.INDICATOR_TABLE);
		xpath = XPathFactory.newInstance().
				newXPath();
		try {
			expr = xpath.compile(strFind);
			nodeList = (NodeList) expr.evaluate(
					this.rootCaption,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			ev.strError = TransXml.ERROR_XML_ROOTXPATH
					+ e.toString() + "!";
			ev.element = null;
			D.dprint(e);
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		if (nodeList.getLength() == 0) {
			ev.strError = TransXml.ERROR_TABLE_NOTFOUND;
			ev.element = null;
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		if (nodeList.getLength() > 1) {
			ev.strError = TransXml.ERROR_TABLE_DUPLICATE;
			ev.element = null;
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		Element element = (Element)nodeList.item(0);
		ev.strError = null;
		ev.element = element;
		D.dprint_method_end();
		return ev;
	}


	/**
	 * XMLに登録してある表のノードを、指定されたパスから探す。
	 * パスが/から始まればルートから探し、
	 * それ以外は現在のキャプションを基準に探す。
	 * @param strTable 表名
	 * @param strPath 指定パス
	 * @return ElementValue
	 */
	public ElementValue getTableSingleWithPath(
			String strTable, String strPath ) {
		D.dprint_method_start();
		D.dprint(strTable);
		D.dprint(strPath);
		D.dprint_xml(this);
		ElementValue ev = new ElementValue();
		String strFind = String.format(
				"%s/%s[@%s=\"%s\"]",
				strPath,
				strTable,
				ATTRIB_TYPE,
				INDICATOR_TABLE);
		XPath xpath = XPathFactory.newInstance().
				newXPath();
		XPathExpression expr;
		NodeList nodeList;
		String strErrMsg;
		Element elementSearch;
		if (strFind.charAt(0) == '/') {
			strErrMsg = ERROR_XML_TREEXPATH;
			elementSearch = this.rootCaption;
			strFind = "/" + ROOT_CAPTION_ID + strFind;
		} else {
			strErrMsg = ERROR_XML_NODEXPATH;
			elementSearch = this.currentCaption;
		}
		D.dprint(strFind);
		try {
			expr = xpath.compile(strFind);
			nodeList = (NodeList) expr.evaluate(
					elementSearch,
					XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			ev.strError = strErrMsg
					+ e.toString() + "!";
			ev.element = null;
			D.dprint(e);
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		if (nodeList.getLength() == 0) {
			ev.strError = TransXml.ERROR_TABLE_NOTFOUND;
			ev.element = null;
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		} else if (nodeList.getLength() != 1) {
			ev.strError = TransXml.ERROR_TABLE_DUPLICATE;
			ev.element = null;
			D.dprint(ev);
			D.dprint_method_end();
			return ev;
		}
		Element elementFound = (Element)nodeList.item(0);
		ev.strError = null;
		ev.element = elementFound;
		D.dprint_method_end();
		return ev;
	}


	public ArrayList<ElementValue> getVariableMulti(
			String strVariableID, String strPath ) {
		D.dprint_method_start();
		D.dprint(strVariableID);
		D.dprint(strPath);
		ArrayList<ElementValue> listEv = new ArrayList<ElementValue>();
		NodeList nodeList;
		String strFind;
		XPath xpath;
		XPathExpression expr;
		if (strPath.equals("")) {
			// 現在のキャプション内で変数を探す
			strFind = String.format(
					"%s[@%s=\"%s\"]",
					strVariableID,
					TransXml.ATTRIB_TYPE,
					TransXml.INDICATOR_VARIABLE);
			xpath = XPathFactory.newInstance().
					newXPath();
			D.dprint(this.currentCaption);
			D.dprint(this.currentCaption.getNodeName());
			try {
				expr = xpath.compile(strFind);
				nodeList = (NodeList) expr.evaluate(
						this.currentCaption,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				ElementValue ev = new ElementValue();
				ev.strOutput = "";
				ev.strPath = "";
				ev.strValue = ERROR_XML_NODEXPATH
						+ e.toString() + "!";
				ev.strError = ev.strValue;
				listEv.add(ev);
				D.dprint(e);
				D.dprint(listEv);
				D.dprint_method_end();
				return listEv;
			}
			D.dprint(nodeList);
			D.dprint(nodeList.getLength());
			if (nodeList.getLength() == 0) {
				// 全体から変数を探す
				strFind = String.format(
						"//%s[@%s=\"%s\"]",
						strVariableID,
						TransXml.ATTRIB_TYPE,
						TransXml.INDICATOR_VARIABLE);
				xpath = XPathFactory.newInstance().
						newXPath();
				try {
					expr = xpath.compile(strFind);
					nodeList = (NodeList) expr.evaluate(
							this.rootCaption,
							XPathConstants.NODESET);
				} catch (XPathExpressionException e) {
					ElementValue ev = new ElementValue();
					ev.strOutput = "";
					ev.strPath = "";
					ev.strValue = ERROR_XML_ROOTXPATH
							+ e.toString() + "!";
					ev.strError = ev.strValue;
					listEv.add(ev);
					D.dprint(e);
					D.dprint(listEv);
					D.dprint_method_end();
					return listEv;
				}
			}
		} else {
			strFind = String.format(
					"%s/%s[@%s=\"%s\"]",
					strPath,
					strVariableID,
					ATTRIB_TYPE,
					INDICATOR_VARIABLE);
			xpath = XPathFactory.newInstance().
					newXPath();
			String strErrMsg;
			Element elementSearch;
			if (strFind.charAt(0) == '/') {
				strErrMsg = ERROR_XML_TREEXPATH;
				elementSearch = this.rootCaption;
				strFind = "/" + ROOT_CAPTION_ID + strFind;
			} else {
				strErrMsg = ERROR_XML_NODEXPATH;
				elementSearch = this.currentCaption;
			}
			D.dprint(strFind);
			try {
				expr = xpath.compile(strFind);
				nodeList = (NodeList) expr.evaluate(
						elementSearch,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				ElementValue ev = new ElementValue();
				ev.strOutput = "";
				ev.strPath = "";
				ev.strValue = strErrMsg
						+ e.toString() + "!";
				ev.strError = ev.strValue;
				listEv.add(ev);
				D.dprint(e);
				D.dprint(listEv);
				D.dprint_method_end();
				return listEv;
			}
		}
		if (nodeList.getLength() == 0) {
			D.dprint("length 0");
			D.dprint(listEv);
			D.dprint_method_end();
			return listEv;
		}
		for (int i=0; i<nodeList.getLength(); i++) {
			Element element = (Element)nodeList.item(i);
			ElementValue ev = new ElementValue();
			ev.strOutput = element.getAttribute(ATTRIB_STROUT);
			ev.strValue = element.getTextContent();
			if ((ev.strValue != "")
					&& (ev.strValue.charAt(0) == '!')) {
				ev.strError = ev.strValue;
			} else {
				ev.strError = null;
			}
			ev.strPath = getNodePath(element);
			ev.strExpr = element.getAttribute(ATTRIB_EXPR);
//			ev.strError = null;
			ev.element = element;
			ev.strHasu = element.getAttribute(ATTRIB_HASU);
			listEv.add(ev);
		}
		D.dprint(listEv);
		D.dprint_method_end();
		return listEv;
	}


	/**
	 * D.dprint用
	 * @return
	 */
	public Document getDoc() {
		return doc;
	}


	// https://stackoverflow.com/questions/36179764/get-path-to-all-xmls-nodes
	// Jason Smiley
	/**
	 * Builds the Path to the Node in the XML Structure.
	 *
	 * @param node Child {@link Node}
	 * @return {@link String} representation of Path to XML Node.
	 */
	public String getNodePath(Node node) {
	    if(node == null) {
	        throw new IllegalArgumentException("Node cannot be null");
	    }
//	    StringBuilder pathBuilder = new StringBuilder("/");
//	    pathBuilder.append(node.getNodeName());
	    StringBuilder pathBuilder = new StringBuilder("");

	    Node currentNode = node;

	    if(currentNode.getNodeType() != Node.DOCUMENT_NODE) {
	        while (currentNode.getParentNode() != null) {
	            currentNode = currentNode.getParentNode();

	            if(currentNode.getNodeType() == Node.DOCUMENT_NODE) {
	                break;
	            } else if(getIndexOfArrayNode(currentNode) != null) {
	                pathBuilder.insert(0, "/" + currentNode.getNodeName() + "[" + getIndexOfArrayNode(currentNode) + "]");
	            } else {
	                pathBuilder.insert(0, "/" + currentNode.getNodeName());
	            }
	        }
	    }

	    return pathBuilder.toString();
	}

	/**
	 * TODO - doesn't handle Formatted XML - treats formatting as Text Nodes and needs to skip these.
	 *
	 * Light node test to see if Node is part of an Array of Elements.
	 *
	 * @param node {@link Node}
	 * @return True if part of an array. Otherwise false.
	 */
	private boolean isArrayNode(Node node) {
	    if (node.getNextSibling() == null && node.getPreviousSibling() == null) {
	        // Node has no siblings
	        return false;
	    } else {
	        // Check if node siblings are of the same name. If so, then we are inside an array.
	        return (node.getNextSibling() != null && node.getNextSibling().getNodeName().equalsIgnoreCase(node.getNodeName()))
	                || (node.getPreviousSibling() != null && node.getPreviousSibling().getNodeName().equalsIgnoreCase(node.getNodeName()));
	    }
	}

	/**
	 *  TODO - doesn't handle Formatted XML - treats formatting as Text Nodes and needs to skip these.
	 *  Figures out the Index of the Array Node.
	 *
	 *  @param node {@link Node}
	 *  @return Index of element in array. Returns null if not inside an array.
	 */
	private Integer getIndexOfArrayNode(Node node) {
	    if(isArrayNode(node)) {
	        int leftCount = 0;

	        Node currentNode = node.getPreviousSibling();

	        while(currentNode != null) {
	            leftCount++;
	            currentNode = currentNode.getPreviousSibling();
	        }
	        return leftCount;
	    } else {
	        return null;
	    }
	}
}
