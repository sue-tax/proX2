/**
 *
 */
package data;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

import cmn.D;
import cmn.StringBufferNull;
import data.TransXml.CellValue;
import data.TransXml.ElementValue;

/**
 * @author sue-t
 *
 */
public class ItemRefTable extends ItemRef {

//	private String strIndexParm;

	private String strPre1;
	private String strIndex1;
	private String strPost1;
	private String strPre2;
	private String strIndex2;
	private String strPost2;

	public static final Pattern PATTERN_TABLE_INDEX =
			Pattern.compile(
			"^(?<rep>(\\(|（)((([^(（)）]+)|(?'rep'))+)(\\)|）))"
			);
//    tableIndexMatch = r'^(?<rep>(\()((([^()]+)|(?&rep))+)(\)))'
//	0「(2)」
//	1「(2)」
//	2「(」
//	3「2」
//	4「2」
//	5「2」
//	6「)」

	public static final char INDICATOR_INDEX_OFFSET0 = '@';
	public static final char INDICATOR_INDEX_OFFSET1 = '＠';

	public static final String ERROR_NOT_PERIOD_WITHOUT_TABLE =
			"!表外で「.」は使えません!";
	public static final String ERROR_INVALID_RC =
			"!表の行指定・列指定が間違っています!";
	public static final String ERROR_NOT_OFFSET_WITHOUT_TABLE =
			"!表外でオフセット指定は使えません!";
	public static final String ERROR_OUT_OB_INDEX =
			"!表の行・列の範囲を外れています!";

	/**
	 */
	public ItemRefTable( Matcher mRef ) {
   		super(mRef.start(0), mRef.end(0),
   				mRef.group(0), null);
		D.dprint_method_start();
   		Matcher m = PATTERN_REF_SYNTAX.
				matcher(mRef.group(0));
		if (! m.find()) {
			this.strError = ERROR_INVALID_REFER;
			this.strOutput = mRef.group(0)
					+ this.strError;
			return;
		}
		try {
			ItemRefMain(mRef, m);
		} catch(Exception e) {
			StringBufferNull sbOutput = new
					StringBufferNull(this.strPre);
			sbOutput.append(this.strPathPre);
			sbOutput.append(this.strInputPath);
			sbOutput.append(this.strPathPost);
			sbOutput.append(this.strVariable);
			sbOutput.append(this.strRefPathPre);
			sbOutput.append(this.strRefInputPath);
			sbOutput.append(this.strSeparator);
			sbOutput.append(this.strInputValue);
			sbOutput.append(this.strAccumPre);
			sbOutput.append(this.strInputAccum);
			sbOutput.append(this.strPost);
			sbOutput.append(this.strPre1);
			sbOutput.append(this.strIndex1);
			sbOutput.append(this.strPost1);
			sbOutput.append(this.strPre2);
			sbOutput.append(this.strIndex2);
			sbOutput.append(this.strPost2);
			this.strOutput = sbOutput.toString();
			D.dprint_method_end();
			return;
		}
		D.dprint_method_end();
		return;
	}


	private void ItemRefMain( Matcher mRef, Matcher m )
			throws Exception {
		D.dprint_method_start();
		boolean bSuccess = true;

		this.strPre = m.group(1);
		this.strPost = m.group(16);

		// 「#変数パス指定:」の処理
		this.strPathPre = m.group(3);
		this.strInputPath = m.group(4);
		this.strPathPost = m.group(5);
		if (m.group(4) != null) {
			this.strPath = Item.convertPath(m.group(4));
			if (this.strPath == null) {
				this.strError = ERROR_INVALID_PATH;
				this.strInputPath += this.strError;
				bSuccess = false;
			}
		}

		if (! m.group(6).equals(".")) {
			this.strVariable = m.group(6);
			this.strVariableID = Item.convertID(
					this.strVariable);
			D.dprint("+++"+this.strVariableID+"+++");
			if (this.strVariableID.equals("")) {
				this.strError = ERROR_INVALID_VARIABLE;
				this.strVariable += this.strError;
				bSuccess = false;
			}
			// 変数が存在するかのチェックは、ここでしない
		} else {
			// '.' 自分自身を指すので、テーブルがらみの処理
			if (Item.currentTable == null) {
				this.strError = ERROR_NOT_PERIOD_WITHOUT_TABLE;
				this.strVariable += this.strError;
				bSuccess = false;
			}
			this.strVariable = m.group(6);
			this.strVariableID = Item.transXml.
					getVariableID(Item.currentTable);
			D.dprint(this.strVariable);
			D.dprint(this.strVariableID);
		}

		// 「?変数参照パス表示」の処理
		this.strRefPathPre = m.group(8);	// ? or ？
		this.strRefInputPath = m.group(9);

		this.strSeparator = m.group(11);
		this.strInputValue = m.group(12);
		this.strAccumPre = m.group(14);
		this.strInputAccum = m.group(15);

		// m_ref.group(4)は表のインデックス指定
		itemRefTable(mRef.group(4));

		if (! bSuccess) {
			D.dprint(bSuccess);
			throw new Exception();
		}
		this.strError = null;
		D.dprint_method_end();
		return;
	}


	private void itemRefTable( String strIndex ) {
		D.dprint_method_start();
		D.dprint(strIndex);
		Matcher mIndex1 = PATTERN_TABLE_INDEX.
				matcher(strIndex);
		if (! mIndex1.find()) {
			this.strError = ERROR_INVALID_RC;
			this.strPre1 = strIndex + this.strError;
			D.dprint(this.strError);
			D.dprint_method_end();
			return;
		}
		D.dprint(mIndex1.group(0));
		this.strPre1 = mIndex1.group(2);
		this.strIndex1 = mIndex1.group(3);	//4,5の可能性あり
		this.strPost1 = mIndex1.group(6);
		Matcher mIndex2 = PATTERN_TABLE_INDEX.
				matcher(strIndex.substring(mIndex1.end(0)));
		if (! mIndex2.find()) {
			this.strError = ERROR_INVALID_RC;
			this.strPre2 = strIndex.substring(mIndex1.end(0))
					+ this.strError;
			D.dprint(this.strError);
			D.dprint_method_end();
			return;
		}
		D.dprint(mIndex2.group(0));
		this.strPre2 = mIndex2.group(2);
		this.strIndex2 = mIndex2.group(3);	//4,5の可能性あり
		this.strPost2 = mIndex2.group(6);
		D.dprint_method_end();
		return;
	}


	public TransValue translateSecond() {
		D.dprint_method_start();
		TransValue tv = new TransValue();
		if (this.strError != null) {
			tv.strOutput = this.strOutput;
			tv.strValue = this.strError;
			tv.strError = this.strError;
			D.dprint(tv);
			D.dprint_method_end();
			return tv;
		}
		try {
			tv = translateSecondMain();
		} catch(Exception e) {

		}
		D.dprint(tv);
		D.dprint_method_end();
		return tv;
	}


	public TransValue translateSecondMain()
			throws Exception {
		D.dprint_method_start();
		TransValue tv = new TransValue();
		StringBufferNull sbOutput = new StringBufferNull();
		ElementValue elementValue = new ElementValue();
		if (this.strPath == null) {
			elementValue = Item.transXml.getTableSingleNoPath(
					this.strVariableID);
		} else {
			elementValue = Item.transXml.getTableSingleWithPath(
					this.strVariableID,
					this.strPath);
		}
		D.dprint(elementValue);
		if (elementValue.strError != null) {
			this.strVariable += elementValue.strError;
			tv.strError = elementValue.strError;
			throw new Exception();
		}
		CellValue cv = translateRefTable();
		if (cv.strError != null) {
			sbOutput.append(this.strPre);
			sbOutput.append(this.strPathPre);
			sbOutput.append(this.strInputPath);
			sbOutput.append(this.strPathPost);
			sbOutput.append(this.strVariable);
			sbOutput.append(cv.strError);
			if (this.strRefPathPre != null) {
				sbOutput.append(this.strRefPathPre);
				sbOutput.append(this.strPath);
			}
			sbOutput.append(this.strSeparator);
			sbOutput.append(this.strInputValue);
			sbOutput.append(this.strAccumPre);
			sbOutput.append(this.strInputAccum);
			sbOutput.append(this.strPost);
			tv.strOutput = sbOutput.toString();
			tv.strValue = cv.strValue;
			tv.strError = cv.strError;
			D.dprint(tv);
			D.dprint_method_end();
			return tv;
		}
		if (Item.mode == TransString.MODE_REPLACE) {
			sbOutput.append(this.strPre);
			sbOutput.append(this.strPathPre);
			sbOutput.append(this.strInputPath);
			sbOutput.append(this.strPathPost);
			sbOutput.append(this.strVariable);
			if (this.strRefPathPre != null) {
				sbOutput.append(this.strRefPathPre);
				sbOutput.append(cv.strPath);
			}
			if (this.strSeparator != null) {
				sbOutput.append(this.strSeparator);
			} else {
				sbOutput.append(DEFAULT_SEPARATOR);
			}
			sbOutput.append(cv.strValue);
			if (this.strAccumPre != null) {
				sbOutput.append(this.strAccumPre);
				sbOutput.append(elementValue.strOutput);
			}
			sbOutput.append(this.strPost);
			sbOutput.append(this.strPre1);
			sbOutput.append(this.strIndex1);
			sbOutput.append(this.strPost1);
			sbOutput.append(this.strPre2);
			sbOutput.append(this.strIndex2);
			sbOutput.append(this.strPost2);
		} else {
			sbOutput.append(" ");	//this.strPre);
//			sbOutput.append(this.strPathPre);
//			sbOutput.append(this.strInputPath);
//			sbOutput.append(this.strPathPost);
			sbOutput.append(this.strVariable);
//			if (this.strRefPathPre != null) {
//				sbOutput.append(this.strRefPathPre);
//				sbOutput.append(elementValue.strPath);
//			}
			sbOutput.append(" ");
//			if (this.strSeparator != null) {
//				sbOutput.append(this.strSeparator);
//			} else {
//				sbOutput.append(DEFAULT_SEPARATOR);
//			}
			sbOutput.append(cv.strValue);
//			if (this.strAccumPre != null) {
//				sbOutput.append(this.strAccumPre);
//				sbOutput.append(elementValue.strOutput);
//			}
			sbOutput.append(" ");	//this.strPost);
			sbOutput.append(this.strPre1);
			sbOutput.append(this.strIndex1);
			sbOutput.append(this.strPost1);
			sbOutput.append(this.strPre2);
			sbOutput.append(this.strIndex2);
			sbOutput.append(this.strPost2);
		}
		tv.strOutput = sbOutput.toString();
		tv.strValue = cv.strValue;
		tv.strError = null;
		D.dprint(tv);
		D.dprint_method_end();
		return tv;
	}

	private CellValue translateRefTable()
			throws Exception {
		D.dprint_method_start();
		CellValue cv = transXml.new CellValue();
		D.dprint(this.strIndex1);
		D.dprint(this.strIndex2);
		// 行指定の処理
		int index1;
		String strXpathIndex1;
		D.dprint(this.strIndex1);
		if ((this.strIndex1.charAt(0) == INDICATOR_INDEX_OFFSET0)
				|| (this.strIndex1.charAt(0) == INDICATOR_INDEX_OFFSET1)) {
			// オフセット指定
			if (Item.currentRow == 0) {
				cv.strError = ERROR_NOT_OFFSET_WITHOUT_TABLE;
				this.strIndex1 += cv.strError;
				throw new Exception();
			}
			index1 = Integer.valueOf(
					this.strIndex1.substring(1))
					+ Item.currentRow;
			D.dprint(index1);
			if (index1 <= 0) {
				cv.strError = ERROR_OUT_OB_INDEX;
				this.strIndex1 += cv.strError;
				throw new Exception();
			}
			strXpathIndex1 = "";
		} else {
			D.dprint(this.strIndex1);
			AccValue av = Item.accumulate(
					this.strIndex1, "");
			D.dprint(av.strValue);
			try {
				index1 = Integer.valueOf(av.strValue);
				strXpathIndex1 = "";
				// 数指定
			} catch(Exception e) {
				// 値指定
				strXpathIndex1 = String.format(
						"./%s[(@%s=\"1\") and (text()=\"%s\")]/@%s",
						TransXml.CELL_NODE_NAME,
						TransXml.ATTRIB_COLUMN,
						this.strIndex1,
						TransXml.ATTRIB_ROW);
				D.dprint(strXpathIndex1);
				index1 = 0;
			}
		}
		// 列指定の処理
		int index2;
		String strXpathIndex2;
		if ((this.strIndex2.charAt(0) == INDICATOR_INDEX_OFFSET0)
				|| (this.strIndex2.charAt(0) == INDICATOR_INDEX_OFFSET1)) {
			// オフセット指定
			if (Item.currentColumn == 0) {
				cv.strError = ERROR_NOT_OFFSET_WITHOUT_TABLE;
				this.strIndex2 += cv.strError;
				throw new Exception();
			}
			index2 = Integer.valueOf(
					this.strIndex2.substring(1))
					+ Item.currentColumn;
			D.dprint(index2);
			if (index2 <= 0) {
				cv.strError = ERROR_OUT_OB_INDEX;
				this.strIndex2 += cv.strError;
				throw new Exception();
			}
			strXpathIndex2 = "";
		} else {
			AccValue av = Item.accumulate(
					this.strIndex2, "");
			try {
				index2 = Integer.valueOf(av.strValue);
				strXpathIndex2 = "";
				// 数指定
			} catch(Exception e) {
				// 値指定
				strXpathIndex2 = String.format(
						"./%s[(@%s=\"1\") and (text()=\"%s\")]/@%s",
						TransXml.CELL_NODE_NAME,
						TransXml.ATTRIB_ROW,
						this.strIndex2,
						TransXml.ATTRIB_COLUMN);
				D.dprint(strXpathIndex2);
				index2 = 0;
			}
		}
		D.dprint("AAAA");
		D.dprint(index1);
		D.dprint(strXpathIndex1);
		D.dprint(index2);
		D.dprint(strXpathIndex2);
//		CellValue cv = null;	// = new CellValue();
//		String strFullPath;
		if (this.strPath == null) {
			// パス指定なし
	        // 現在のキャプション内で変数を探す

			// TransXmlのメソッドにすべき？
			String strFind = String.format(
					"%s[@%s=\"%s\"]",
					this.strVariableID,
					TransXml.ATTRIB_TYPE,
					TransXml.INDICATOR_TABLE
					);
			D.dprint(strFind);
			XPath xpath = XPathFactory.newInstance().
					newXPath();
			XPathExpression expr;
			NodeList nodeList;
			try {
				expr = xpath.compile(strFind);
				nodeList = (NodeList) expr.evaluate(
						transXml.currentCaption,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				// TODO
				cv.strError = "!error" + e.toString() + "!";
				D.dprint(e);
				D.dprint_method_end();
				return cv;
			}
			if (nodeList.getLength() != 0) {
				// 変数が見つかった
				cv = Item.transXml.
						getCellValue(
						(Element)nodeList.item(0),
						index1, strXpathIndex1,
						index2, strXpathIndex2);
			} else {
				// 全体から変数を探す
				// TODO
				strFind = String.format(
						"//{}[@{}=\"{}\"]",
						this.strVariableID,
						TransXml.ATTRIB_TYPE,
						TransXml.INDICATOR_TABLE
						);
				D.dprint(strFind);
				xpath = XPathFactory.newInstance().
						newXPath();
				try {
					expr = xpath.compile(strFind);
					nodeList = (NodeList) expr.evaluate(
							transXml.rootCaption,
							XPathConstants.NODESET);
				} catch (XPathExpressionException e) {
					// TODO
					cv.strError = "!error" + e.toString() + "!";
					D.dprint(e);
					D.dprint_method_end();
					return cv;
				}
				if (nodeList.getLength() != 0) {
					// 変数が見つかった
					// もしかしたら、複数対応が必要かも？
					cv = Item.transXml.
							getCellValue(
							(Element)nodeList.item(0),
							index1, strXpathIndex1,
							index2, strXpathIndex2);
				} else {
					cv.strError = TransXml.
							ERROR_NOT_FOUND_CELL;
//					cv.strIndex1 = null;
//					cv.strIndex2 = null;
//					cv.strPath = null;
					D.dprint_method_end();
					return cv;
				}
			}
		} else {
			// パス指定
			String strFind = String.format(
					"%s/%s[@%s=\"%s\"]",
					this.strPath,
					this.strVariableID,
					TransXml.ATTRIB_TYPE,
					TransXml.INDICATOR_TABLE);
			D.dprint(strFind);
			XPath xpath = XPathFactory.newInstance().
					newXPath();
			XPathExpression expr;
			NodeList nodeList;
			try {
				expr = xpath.compile(strFind);
				nodeList = (NodeList) expr.evaluate(
						(strFind.charAt(0)=='/')?
								transXml.rootCaption:
								transXml.currentCaption,
						XPathConstants.NODESET);
			} catch (XPathExpressionException e) {
				// TODO
				cv.strError = "!error" + e.toString() + "!";

				D.dprint(e);
				D.dprint_method_end();
				return cv;
			}
			if (nodeList.getLength() != 0) {
				// 変数が見つかった
				// もしかしたら、複数対応が必要かも？
				cv = Item.transXml.
						getCellValue(
						(Element)nodeList.item(0),
						index1, strXpathIndex1,
						index2, strXpathIndex2);
			} else {
				cv.strValue = TransXml.
						ERROR_NOT_FOUND_CELL;
//				cv.strIndex1 = null;
//				cv.strIndex2 = null;
//				cv.strPath = null;
				D.dprint_method_end();
				return cv;
			}
		}
		if (cv.strValue == "") {
			ElementValue ev = Item.defineVariable(
					cv.element);
			cv.strValue = ev.strValue;
		}
		D.dprint_method_end();
		return cv;
	}
//      else:
//          # 全体から変数を探す
//          xpath_full ="//{}[@{}=\"{}\"]".format(
//                  variable_id, self.typeAttrib,
//                  self.tableIndicator)
//          d.dprint(xpath_full)
//          try:
//              tagTables = self.rootCaption. \
//                      xpath(xpath_full)
//          except Exception as err:
//              str_value = self.errMsgXMLrootxpath \
//                      + str(err)+'!'
//              self.error_count += 1
//          else:
//              if len(tagTables) == 0:
//                  str_fullpath = \
//                      self.treeCaption. \
//                      getpath(self.currentCaption)
//                  str_value = self.errMsgNotFound
//                  str_out_index1 = '0'
//                  str_out_index2 = '0'
//              elif len(tagTables) != 1:
//                  list_fullpath = []
//                  for tag in tagTables:
//                      list_fullpath.add(
//                              self.treeCaption. \
//                              getpath(tag))
//                  str_fullpath = \
//                          '.'.join(list_fullpath)
//                  str_value = self.errMsgDuplicated
//                  str_out_index1 = '0'
//                  str_out_index2 = '0'
//                  self.error_count += 1
//              else:
//                  (str_value, str_out_index1,
//                      str_out_index2) = \
//                          self.get_cell_data(
//                          tagTables[0],
//                          int_index1, xpath_index1,
//                          int_index2, xpath_index2)
//                  if str_value == None:
//                      str_value = "!Error1!"
//                  str_fullpath = self.treeCaption. \
//                          getpath(tagTables[0])
//                  self.error_count += 1
//  else:
//      # パス指定
//      str_find = "{}/{}[@{}=\"{}\"]".format(
//              str_xpath, variable_id,
//              self.typeAttrib, self.tableIndicator)
//      d.dprint_name("***str_find", str_find)
//      try:
//          if str_find[0] == '/':
//              msg = self.errMsgXMLtreexpath
//              d.dprint("treeCaption")
//              tagTables = self.treeCaption. \
//                  findall(str_find)
//          else:
//              msg = self.errMsgXMLnodexpath
//              d.dprint("currentCaption")
//              tagTables = self.currentCaption. \
//                      findall(str_find)
//      except Exception as err:
//          str_fullpath = ''
//          str_value = msg
//          str_out_index1 = '0'
//          str_out_index2 = '0'
//      else:
//          d.dprint(tagTables)
//          if len(tagTables) == 0:
//              str_fullpath = str_xpath
//              str_value = self.errMsgNotFound
//              self.error_count += 1
//          elif len(tagTables) != 1:
//              list_fullpath = []
//              for tag in tagTables:
//                  list_fullpath.add(
//                          self.treeCaption. \
//                          getpath(tag))
//              str_fullpath = '.'.join(list_fullpath)
//              str_value = self.errMsgDuplicated
//              self.error_count += 1
//              str_out_index1 = '0'
//              str_out_index2 = '0'
//          else:
//              (str_value, str_out_index1, str_out_index2) = \
//                      self.get_cell_data(
//                      tagTables[0],
//                      int_index1, xpath_index1,
//                      int_index2, xpath_index2)
//              if str_value == None:
//                  str_value = "!Error1!"
//              str_fullpath = self.treeCaption. \
//                      getpath(tagTables[0])
//              self.error_count += 1
//  d.dprint(str_value)
//  d.dprint(str_fullpath)
//  d.dprint(str_out_index1)
//  d.dprint(str_out_index2)
//  d.dprint_method_end()
//  return (str_value, str_fullpath,
//          str_out_index1, str_out_index2)


}


