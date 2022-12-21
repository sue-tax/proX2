package data;

import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

import cmn.D;
import data.TransXml.ElementValue;
import value.BigValue;
import value.Expression;
import value.UnitValue;

/**
 * アイテムの基底クラス
 * @author sue-tax
 * @version 0.80
 */
abstract class Item {
    private static final String CHANGE_ID_SRC
    		= "㈱㈲㈹①②③④⑤⑥⑦⑧⑨⓵⓶⓷⓸⓹⓺⓻⓼⓽❶❷❸❹❺❻❼❽❾⑴⑵⑶⑷⑸⑹⑺⑻⑼";

    private static final String CHANGE_ID_DST
			= "株有代123456789123456789123456789123456789";

    private static final Pattern PATTERN_CODE_BLOCK
    		= Pattern.compile("^```");

    private static final Pattern PATTERN_INVALID_ID
			= Pattern.compile("( |　|\t|\n)");

    // *.#,&は、除く
    private static final Pattern PATTERN_DELETE_ID_SYMBOL
			= Pattern.compile("["
			+ "`:?\\[\\]!\";'$%()=\\-^~|{}+<>/,・＾！”：；＃＄％＆’"
			+ "（）＝ー～｜｛｝［］＋＊＜＞？。、【】「」"
			+ "]");
    // r'[`\:\?\[\]!"\;\'W\"\$\%\(\)\=\-\^\~\|\{\}\+\<\>\/,\\・＾！”：；＃＄％＆’（）＝ー～｜｛｝［］＋＊＜＞？。、【】]'
    // python版では、「」を除いていない

	/** 端数処理指示　定数定義、計算指定で使用 */
	public static final Pattern PATTERN_HASU =
			Pattern.compile(
			"(|)(([udr])([0-9]+)|千円未満切捨|百円未満切捨)(|)");
//    hasuMatch = r'(|)(([udr])([0-9]+)|千円未満切捨|百円未満切捨)(|)'

	protected static String strSrc;
	protected static int index;
	protected static int lenInput;
	protected static TransString transString;
	protected static TransXml transXml;
	protected static int mode;

    protected static boolean bHankaku;
    protected static boolean bLeap;
    protected static char chLeapInit;
    protected static char chLeapTerm;
    protected static boolean bComma;
    protected static boolean bTani;

	/** 元テキストの全体での開始位置 */
	protected int start;

	/** 元テキストの全体での終了位置＋１ */
	protected int end;

	/** 元テキスト */
	protected String strInput;

	/** 変換テキスト */
	protected String strOutput;

//	/** エラーの有無 */
//	protected boolean flagError;
	/** エラーならエラーメッセージ、成功ならnull */
	protected String strError;

	/** 処理中の表のタグ */
    protected static Element currentTable;

    /** 処理中の表のセルの行 */
    protected static int currentRow;

    /** 処理中の表のセルの行 */
    protected static int currentColumn;


   	public static final String ERROR_INVALID_HASU
		= "!端数が正しくない!";
   	public static final String ERROR_CAPTION_IN_CALC
   		= "!計算式中に見出しが入っている!";
    public static final String ERROR_INVALID_PATH
		= "!パス指定が間違っている!";
    public static final String ERROR_INVALID_VARIABLE
		= "!変数名が間違っている!";

   	public static final String UNCALC
   		= "!計算不能!";

   	private static final UnitValue value1000 =
   			new UnitValue(1000);
   	private static final UnitValue value100 =
   			new UnitValue(100);

   	Item( int start, int end,
   			String strInput, String strOutput ) {
		this.start = start;
		this.end = end;
		this.strInput = strInput;
		this.strOutput = strOutput;
		this.strError = null;
   	}


   	/**
   	 * Item処理のための初期化
   	 * （TransString.translateでパス毎に呼び出すことを想定）
   	 * @param strSrc
   	 * @param transString
   	 * @param transXml
   	 * @param mode
   	 */
   	public static void reset( String strSrc,
   			TransString transString,
   			TransXml transXml, int mode ) {
   		// 本来は staticでない方がよいと思うが
   		// parseの際に便利そうなので、
   		// 取り敢えず、staticにしている。
   		Item.strSrc = strSrc;
   		Item.index = 0;
   		Item.lenInput = strSrc.length();
   		Item.transString = transString;
   		Item.transXml = transXml;
   		Item.mode = mode;

   		Item.bHankaku = false;
   		Item.bLeap = true;
   		Item.chLeapInit = '"';
   		Item.chLeapTerm = '"';
   		Item.bComma = false;
   		Item.bTani = true;	// 未使用かも？
   		value.Expression.setSeido(4, 4);
   		return;
   	}


   	/**
   	 * テキストから、Itemを１つ取り出す
   	 * @return
   	 */
   	public static Item parse() {
   		D.dprint_method_start();
//   		D.dprint(Item.index);
		String strParse = Item.strSrc.substring(
				Item.index);
		Item item;
   		Matcher m = ItemSkip.PATTERN_SKIP.
				matcher(strParse);
		if (! m.find()) {
			item = new ItemSkip(Item.lenInput);
			Item.index = Item.lenInput;
			D.dprint_method_end();
			return item;
		} else if (m.start(0) != 0) {
			item = new ItemSkip(
					Item.index + m.start(0));	// + 1);
			Item.index += m.start(0);
			D.dprint_method_end();
			return item;
		}
		if ((Item.index == 0)
				|| (Item.strSrc.charAt(Item.index-1) == '\n')) {
			// 行の先頭の場合
			Matcher mCaption = ItemCaption.PATTERN_CAPTION.
					matcher(strParse);
			if (mCaption.find()) {
				D.dprint(mCaption);
				item = new ItemCaption(mCaption);
    			Item.index += mCaption.end(0);
    			D.dprint_method_end();
    			return item;
			}
			// TODO
		}
        Matcher mDef = ItemDef.PATTERN_DEF.
        		matcher(strParse);
        if (mDef.find()) {
        	item = ItemDef.parse(mDef);
			Item.index += mDef.end(0);
			D.dprint_method_end();
			return item;
        }
        Matcher mRef = ItemRefNormal.PATTERN_REF.
        		matcher(strParse);
        if (mRef.find()) {
        	item = ItemRef.parse(mRef);
			Item.index += mRef.end(0);
			D.dprint_method_end();
			return item;
        }
        Matcher mCalc = ItemCalc.PATTERN_CALC.
        		matcher(strParse);
        if (mCalc.find()) {
            if (! ((mCalc.group(2).equals(" "))
            		|| (mCalc.group(2).equals("x")))) {
            	// Markdown のチェックボックスの可能性なし
            	item = new ItemCalc(mCalc);
				Item.index += mCalc.end(0);
				D.dprint_method_end();
				return item;
            }
        }
		Matcher mCodeBlock = Item.PATTERN_CODE_BLOCK.
				matcher(strParse);
		if (mCodeBlock.find()) {
			D.dprint(mCodeBlock);
			item = new ItemCodeBlock(
					Item.index,
					Item.index + mCodeBlock.end(0),
					mCodeBlock.group(0),
					mCodeBlock.group(0));
			Item.index += mCodeBlock.end(0);
			D.dprint_method_end();
			return item;
		}
		Matcher mFunc = ItemFunction.PATTERN_FUNCTION.
				matcher(strParse);
		if (mFunc.find()) {
			D.dprint(mFunc);
			item = ItemFunction.parse(mFunc);
			Item.index += mFunc.end(0);
			D.dprint_method_end();
			return item;
		}
		String str = strParse.substring(0, 1);
		item = new ItemUnmatch(Item.index,
				Item.index + 1,
				str, str);
		Item.index++;
		D.dprint_method_end();
		return item;
   	}

   	/**
   	 * パス１の処理（変数定義以外は処理不要）
   	 * @param passN
   	 */
//   	public void translateFirst( int passN ) {
//   		return;
//   	}


   	/**
   	 * ２パス時のItemの処理結果
   	 */
   	public static class TransValue {
   		/** 変換後の文字列 */
   		public String strOutput;

   		/** 計算額を示す文字列 */
   		public String strValue;

   		/** エラーメッセージを示す文字列　nullなら正常 */
   		public String strError;
   	}


   	/**
   	 * 表変数用の初期化
   	 * transString ２パス（translateSecond）の最初に呼ぶ
   	 * １パスでも呼んでいるが、不要かも？
   	 */
   	public static void translateInit() {
        Item.currentTable = null;	// 処理中の表のタグ
        Item.currentRow = 0;		// 処理中の表のセルの行
        Item.currentColumn = 0 ;	// 処理中の表のセルの列
        return;
   	}


   	public TransValue translateSecond() {
   		D.dprint_method_start();
   		TransValue tv = new TransValue();
   		tv.strOutput = this.strOutput;
   		tv.strValue = "";
   		tv.strError = null;
   		D.dprint_method_end();
   		return tv;
   	}



   	public static class AccValue {
   		/** 変換後の文字列 */
   		public String strOutput;

   		/** 変数参照などを含まない演算式を示す文字列 */
   		public String strExpr;

   		/** 計算額を示す文字列 */
   		public String strValue;
   	}


   	/**
   	 * 変数参照などを含んだ計算式を計算する。
   	 * 現状では、計算式の中に、変数定義はない
   	 * @param strShiki
   	 * @param strHasu
   	 * @return
   	 */
   	public static AccValue accumulate(
   			String strShiki, String strHasu ) {
   		D.dprint_method_start();
   		D.dprint(strShiki);
   		D.dprint(strHasu);
   		AccValue accValue = new AccValue();
   		StringBuffer sbOutput = new StringBuffer("");
   		StringBuffer sbExpr = new StringBuffer("");
   		int index = 0;
   		int length = strShiki.length();
   		boolean bError = false;
   		while (index < length) {
   			if (strShiki.charAt(index) == '\n') {
   				// 改行を読み飛ばす
   				sbOutput.append('\n');
   				index ++;
   				continue;	// 20221205
   			}
   			String strSub = strShiki.substring(index);
   			if ((index != 0) &&
   					(strShiki.charAt(index-1) == '\n')) {
   				// 改行直後は見出し行であるかを確認する
   				// pos == 0 の場合は、確認不要
   				Matcher mCaption = ItemCaption.
   						PATTERN_CAPTION.matcher(strSub);
   				if (mCaption.find()) {
   					sbOutput.append(strShiki);
   					sbOutput.append(
   							ERROR_CAPTION_IN_CALC);
   					accValue.strValue = UNCALC;
   					break;
   				}
   			}

   			// 変数定義の処理
//   			Matcher mDef = ItemDef.PATTERN_DEF.
//   					matcher(strSub);
//   			if (mDef.find()) {
//   				AccValue accDef = ItemDef.accumlate(
//   						mDef);
//   				sbOutput.append(accDef.strOutput);
//   				sbExpr.append(accDef.strExpr);
//   			}

   			// 変数参照の処理
			Matcher mRef = ItemRefNormal.PATTERN_REF.
					matcher(strSub);
			if (mRef.find()) {
	        	Item itemRef = ItemRef.parse(mRef);
				TransValue tv = itemRef.translateSecond();
				sbOutput.append(tv.strOutput);
				if (tv.strError == null) {
					sbExpr.append("("); // 不要かも
					sbExpr.append(tv.strValue);
					sbExpr.append(")"); // 不要かも
				} else {
					bError = true;
					accValue.strValue = tv.strError;
				}
				index += mRef.end(0);
				continue;
			}

   			// 関数の処理
			Matcher mFunc = ItemFunction.PATTERN_FUNCTION.
					matcher(strSub);
			if (mFunc.find()) {
				Item itemFunction = ItemFunction.parse(mFunc);
				if (itemFunction != null) {
					TransValue tv = itemFunction.translateSecond();
					sbOutput.append(tv.strOutput);
					if (tv.strError == null) {
						sbExpr.append(tv.strValue);
					} else {
						bError = true;
						accValue.strValue = tv.strError;
					}
					index += mFunc.end(0);
					continue;
				}
			}

			D.dprint("**"+strSub+"**");
   			sbOutput.append(strSub.charAt(0));
   			sbExpr.append(strSub.charAt(0));
   			index ++;
   		}

		accValue.strExpr = sbExpr.toString();
   		if (! bError) {
			D.dprint(accValue.strExpr);
			EvalValue ev = Item.evaluate(accValue.strExpr,
					strHasu);
			accValue.strValue = ev.strValue;
		}
		accValue.strOutput = sbOutput.toString();
   		D.dprint(accValue.strOutput);
   		D.dprint(accValue.strExpr);
   		D.dprint(accValue.strValue);
   		D.dprint_method_end();
   		return accValue;
   	}


   	/**
   	 * 変数の循環定義をチェックするためのリスト
   	 */
   	public static ArrayList<Element> listDefVariable;


   	/**
   	 * 変数の循環定義チェックのための初期化処理
   	 */
   	public static void initDefineList() {
   		D.dprint_method_start();
   		Item.listDefVariable = new ArrayList<Element>();
   		D.dprint_method_end();
   		return;
   	}


   	private static boolean contains( Element element ) {
   		D.dprint_method_start();
   		D.dprint(element);
   		Iterator<Element> iter = //new Iterator<Element>.
   				Item.listDefVariable.iterator();
   		while (iter.hasNext()) {
   			Element elementAtom = iter.next();
   			if (elementAtom.isSameNode(element)) {
   				D.dprint_method_end();
   				return true;
   			}
   		}
   		D.dprint_method_end();
   		return false;
   	}


   	public static boolean checkDefineList(
   			Element elementVariable ) {
   		D.dprint_method_start();
   		D.dprint(elementVariable);
   		boolean flag;
   		D.dprint(Item.listDefVariable);
   		if (contains(elementVariable)) {
   			Iterator<Element> iterElement
   					= Item.listDefVariable.iterator();
   			while (iterElement.hasNext()) {
   				Element element = iterElement.next();
   				Item.transXml.registVariableCirculate(
   						element);
   			}
   			Item.listDefVariable.clear();
   			flag = false;
   		} else {
   			Item.listDefVariable.add(elementVariable);
   			flag = true;
   		}
   		D.dprint_method_end();
   		return flag;
   	}


   	public static void popDefineList(
   			Element elementVariable ) {
   		D.dprint_method_start();
   		Item.listDefVariable.remove(elementVariable);
   		D.dprint_method_end();
   		return;
   	}


   	/**
   	 * element で示される変数（通常変数・表変数のセル）を
   	 * 計算して計算額を登録する。
   	 * element.getTextContent が空で、
   	 * element.getAttribute(STR_EXPR) が設定されている
   	 * 前提で呼ばれる。
   	 * @param element
   	 * @return
   	 */
   	public static ElementValue defineVariable(
   			Element element) {
   		D.dprint_method_start();
   		ElementValue ev = new ElementValue();
   		boolean flag = checkDefineList(element);
   		if (! flag) {
   			// 変数定義が循環している
   			ev = Item.transXml.getVariable(element);
   			D.dprint(ev.strError);
   			D.dprint_method_end();
   			return ev;
   		}
   		// 変数を計算する。
   		ev = Item.transXml.getVariable(element);
		D.dprint(ev.strExpr);
		Element nowCaption = Item.transXml.
				setCaptionVariable(element);
   		Item.AccValue av = Item.accumulate(
				ev.strExpr, ev.strHasu);
   		Item.transXml.setCurrentCaption(nowCaption);
		D.dprint(av.strOutput);
		D.dprint(av.strExpr);
		D.dprint(av.strValue);
		popDefineList(element);

		Item.transXml.registVariableValue(
				ev.element, av.strValue,
				av.strOutput, av.strExpr, ev.strHasu);
		ev.strOutput = av.strOutput;
		ev.strValue = av.strValue;
		ev.strExpr = av.strExpr;
		if (av.strValue.charAt(0) == '!') {
			ev.strError = av.strValue;
		}
   		D.dprint_method_end();
   		return ev;
   	}


   	public static class ParmList {
   		String strError;
   		/** パラメータに合致する変数の計算額のリスト */
   		List<String> listValue;
   		/** パラメータに合致する変数のパスのリスト */
   		List<String> listPath;
   		/** パラメータに合致する変数の計算式のリスト */
   		List<String> listExpr;

   		public ParmList() {
   			strError = null;
   			listValue = new ArrayList<String>();
   			listPath = new ArrayList<String>();
   			listExpr = new ArrayList<String>();
   		}
   	}


   	/**
   	 * 複数の変数定義の処理（&sumなどの関数から呼ばれる）
   	 * @param strVariable
   	 * @param strPath
   	 * @return
   	 */
   	public static ParmList defineVariableMulti(
   			String strVariable, String strPath ) {
   		D.dprint_method_start();
   		D.dprint(strVariable);
   		D.dprint(strPath);
   		ParmList pm = new ParmList();
		ArrayList<ElementValue> listEv = Item.transXml.
				getVariableMulti(
				strVariable,
				strPath);
		Iterator<ElementValue> iterEv = listEv.iterator();
		while (iterEv.hasNext()) {
			ElementValue ev = iterEv.next();
			D.dprint(ev.strError);
			D.dprint(ev.strValue);
			D.dprint(ev.strOutput);
			if (ev.strError == null) {
				if (ev.strValue == "") {
					ev = Item.defineVariable(
							ev.element);
				}
			}
			if (ev.strError != null) {
				pm.strError = ev.strError;
			}
			pm.listValue.add(ev.strValue);
			pm.listPath.add(ev.strPath);
			pm.listExpr.add(ev.strExpr);
		}
   		D.dprint(pm);
   		D.dprint_method_end();
   		return pm;
   	}


   	public static class EvalValue {
   		/** 計算額を示す文字列 */
   		public String strValue;

   		/** 端数処理指定 */
   		public HasuValue hasuValue;

   		/** 端数処理を示す文字列 */
   		public String strHasu;
   	}


   	/**
   	 * 変数参照などの処理済みの計算式を計算する
   	 * @param strExpr
   	 * @param strHasu
   	 * @return
   	 */
  	public static EvalValue evaluate( String strExpr,
   			String strHasu ) {
   		D.dprint_method_start();
   		D.dprint(strExpr);
   		D.dprint(strHasu);
   		EvalValue ev = new EvalValue();
   		HasuValue hasuValue = Item.procHasu(strHasu);
   		long place;
   		int rounding;
   		if (hasuValue.modeHasu == HasuValue.MODE_NONE) {
   			place = 5;	// 小数点以下５位
   			rounding = BigValue.ROUND_DOWN;
   		} else {
			if (hasuValue.modeHasu == HasuValue.MODE_KIRIAGE) {
				rounding = BigValue.ROUND_UP;
			} else if (hasuValue.modeHasu == HasuValue.MODE_KIRISUTE) {
				rounding = BigValue.ROUND_DOWN;
			} else {
				rounding = BigValue.ROUND_SHISHAGONYU;
			}
			place = hasuValue.ketaHasu;
		}
   		ev.hasuValue = hasuValue;
   		ev.strHasu = hasuValue.strHasu;
   		D.dprint(Item.bLeap);
   		D.dprint(Item.chLeapInit);
   		D.dprint(Item.chLeapTerm);
   		Expression expr = new Expression(
   				strExpr,
   				Item.bLeap,
   				Item.chLeapInit, Item.chLeapTerm);
   		D.dprint(expr);
   		UnitValue value = expr.value();
   		D.dprint(value);
   		if (value != null) {
   			if (place >= 0) {
   				ev.strValue = value.toString(
   						place, rounding,
   						Item.bHankaku,
   						Item.bComma);
   			} else if (place == -4) {
   				UnitValue value2 = value.divide(value1000);
   				UnitValue value3 = value2.roundDown();
   				UnitValue valueKirisute = value3.multiply(value1000);
   				ev.strValue = valueKirisute.toString(
   						0, rounding,
   						Item.bHankaku,
   						Item.bComma);
   			} else {
   				UnitValue value2 = value.divide(value100);
   				UnitValue value3 = value2.roundDown();
   				UnitValue valueKirisute = value3.multiply(value100);
   				ev.strValue = valueKirisute.toString(
   						0, rounding,
   						Item.bHankaku,
   						Item.bComma);
   			}
   		} else {
   			ev.strValue = Item.UNCALC;
//   			ev.strValue = strExpr;
   			// python版proXは、strExpr
   		}
   		D.dprint(ev.strValue);
   		D.dprint(ev.strHasu);
   		D.dprint(ev);
   		D.dprint_method_end();
   		return ev;
   	}


   	static class HasuValue {
   		public static final int MODE_NONE = 0;
   		public static final int MODE_KIRIAGE = 1;
   		public static final int MODE_KIRISUTE = 2;
   		public static final int MODE_ROUND = 3;

   		public int modeHasu;
   		public long ketaHasu;
   		public String strHasu;

   		HasuValue() {
   	   		this.modeHasu = HasuValue.MODE_NONE;
   	   		this.ketaHasu = 0;
   	   		this.strHasu = "";
   		}
   	}

   	public static HasuValue procHasu( String strHasu ) {
   		D.dprint_method_start();
   		D.dprint(strHasu);
   		Item.HasuValue hasuValue = new Item.HasuValue();
   		if (strHasu != null) {
			Matcher mHasu = Item.PATTERN_HASU.matcher(strHasu);
			if (mHasu.find()) {
				if (mHasu.group(0).length() == 2) {
					// u2 など
					if (mHasu.group(3).equals("u")) {
						hasuValue.modeHasu = HasuValue.MODE_KIRIAGE;
					} else if (mHasu.group(3).equals("d")) {
						hasuValue.modeHasu = HasuValue.MODE_KIRISUTE;
					} else {
						hasuValue.modeHasu = HasuValue.MODE_ROUND;
					}
					hasuValue.ketaHasu = Long.parseLong(
							mHasu.group(4));
				} else {
					if (mHasu.group(2).equals("千円未満切捨")) {
						hasuValue.ketaHasu = -4;
					} else {
						hasuValue.ketaHasu = -2;
					}
					hasuValue.modeHasu = HasuValue.MODE_KIRISUTE;
				}
				hasuValue.strHasu = mHasu.group(0);
			}
		}
		D.dprint(hasuValue);
   		D.dprint_method_end();
   		return hasuValue;
   	}


   	/**
   	 *
   	 * @param strSrc
   	 * @return
   	 */
	public static String convertPath( String strSrc) {
		D.dprint_method_start();
		D.dprint(strSrc);
		String[] listSplit = strSrc.split("/");
		if (listSplit.length == 0) {
			D.dprint(null);
			D.dprint_method_end();
			return null;
		}
		StringBuffer sbPath = new StringBuffer();
		for (String strPath : listSplit) {
			if ((strPath.equals(".")) ||
					(strPath.equals(".."))) {
				sbPath.append(strPath);
			} else {
				String strPathID = Item.convertID(strPath);
				sbPath.append(strPathID);
			}
			sbPath.append("/");
		}
		String strDst = sbPath.substring(0,
				sbPath.length() - 1 );
		D.dprint(strDst);
		D.dprint_method_end();
		return strDst;
	}


	/**
     * 文字列を変数名・ノード名として
     * 有効な文字列に変換する
     * @param strSrc 文字列
     * @return 変換した有効な文字列
     *   strSrc内の全角英数字を半角英数字に、
     *   半角カナを全角カナに変換し、
     *   半角・全角記号（_,*を除く）を取除き、
     *   先頭が半角数字ならば先頭に_を追加する。
     *   全部が記号、空白・タブ・改行が入るなどは、
     *   ""空文字列
	 */
	public static String convertID( String strSrc ) {
		D.dprint_method_start();
		D.dprint(strSrc);
//		String strDst = strSrc;
		// 記号を削除
		Matcher m_del =
				Item.PATTERN_DELETE_ID_SYMBOL.
				matcher(strSrc);
		String strDst = m_del.replaceAll("");
		// 置換漢字（㈱など）を置換
		for (int index = 0;
				index < Item.CHANGE_ID_SRC.length();
				index ++) {
			strDst = strDst.replace(
					Item.CHANGE_ID_SRC.
							substring(index, index+1),
					Item.CHANGE_ID_DST.
							substring(index,index+1));
//			D.dprint(strDst);
		}
		// 半角カナを全角カナに（他にも変わるのがある）
		// 全角英数字を半角英数字に
		strDst = Normalizer.normalize(
				strDst, Form.NFKC);
		if (strDst.length() == 0) {
			D.dprint("*"+strDst+"*");
			D.dprint_method_end();
			return strDst;
		}
		Matcher m_invalid =
				Item.PATTERN_INVALID_ID.
				matcher(strDst);
		if (m_invalid.find()) {
			// 空白・タブ・改行が入っている
			strDst = "";
			D.dprint(strDst);
			D.dprint_method_end();
			return strDst;
			}
		if ((strDst.charAt(0) >= '0') &&
				(strDst.charAt(0) <= '9')) {
			// 数字で始まる場合は、先頭に_を追加
			strDst = "_" + strDst;
		}
		D.dprint(strDst);
		D.dprint_method_end();
		return strDst;
	}

}
