/**
 * アイテムであるsum関数のクラス
 * @author sue-t
 *
 */
package data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.florianingerl.util.regex.Matcher;
import com.florianingerl.util.regex.Pattern;

import cmn.D;
import cmn.StringBufferNull;
import value.BigValue;
import value.Expression;
import value.UnitValue;


/**
 * @author sue-t
 *&sum(
 *'【#変数パス指定:】指定変数名【?【変数参照パス表示】】
 *【:【変数値】【?【変数演算式】】】'
 *【, ...】)
 *【;【計算額】【？【演算式】】】`
 */
public class ItemSum extends ItemFunction {

	public static Pattern PATTERN_FUNCTION
			= Pattern.compile(
			"(&sum)"						// 1
			+ "(\\(|（)( *)"				// 2,3
			+ "(('|’)([^'’]+)('|’)( *))"	// 4 5,6,7,8
			+ "(((,|、)( *)('|’)([^'’]+)('|’)( *))*)"
											// 9 10 11,12,13,14,15,16
			+ "(\\)|）)");					// 17
//0「&sum('abc', 'def', 'ghi'):123?aadaa」
//1「&sum」
//2「(」
//3「」
//4「'abc'」
//5「'」
//6「abc」
//7「'」
//8「」
//9「, 'def', 'ghi'」
//10「, 'ghi'」
//11「,」
//12「 」
//13「'」
//14「ghi」
//15「'」
//16「」
//17「)」

	// ItemFunction
//	public static Pattern PATTERN_PARM
//			= Pattern.compile(
//			"(,|、)( *)('|’)([^'’]+)('|’)( *)");
//0「, 'ghi'」
//1「,」
//2「 」
//3「'」
//4「ghi」
//5「'」
//6「」

	// ItemFunction
//	public static final Pattern PATTERN_EACH =
//			Pattern.compile(
//			"((#|＃)([^:：]+)(:|：))?" +	// 1 2,3,4
//			"([^?？:：'’]+)" +				// 5
//			"((\\?|？)([^:：'’?？]*))?" +	// 6 7,8
//			"(" +							// 9 (10-14)
//			"(:|：)([^'’?]*)" +			// 10,11
//			"((\\?|？)([^'’]*))?" +		// 12 13,14
//			")?" );							// (9)
//0「#abc/def:var?/abc,/def:3?1+2」
//1「#abc/def:」
//2「#」
//3「abc/def」
//4「:」
//5「var」
//6「?/abc,/def」
//7「?」
//8「/abc,/def」
//9「:3?1+2」
//10「:」
//11「3」
//12「?1+2」
//13「?」
//14「1+2」

	// ItemFunction
//   	/** 関数の開始文字（` or ‘） */
//	protected String strPre;
//	/** 関数の終了文字（` or ‘） */
//	protected String strPost;
//	/** 関数名 */
//	protected String strFunction;
//	/** 関数の区切文字（; or ；） */
//	protected String strSeparator;
//	/** 関数値の入力文字列 */
//	protected String strInputValue;
//	private String strSeparator;	// ;
//	private String strInputValue;	// ;の後に、入力されている数字
//	private String strValue;
//	private String strAccumPre;		// ?
//	private String strInputAccum;	// ?の後に、入力されている演算式
//	private String strAccum;

	private String strParmPre;		// (
	private String strParmPost;		// )

	// ItemFunction
//   	public static class ParmVariable {
//	}

   	private ArrayList<ParmVariable> listPV;

   	// Item
//    public static final String ERROR_INVALID_PATH
//		= "!パス指定が間違っている!";
//    public static final String ERROR_INVALID_VARIABLE
//		= "!変数名が間違っている!";
   	// ItemFunction
//	public static final String ERROR_INVALID_FUNCTION
//		= "!関数名、形式が間違っています!";
//  	public static final String ERROR_INVALID_PARM
//		= "!パラメーターの指定が間違っている!";


	ItemSum( Matcher mFunc, Matcher mSum ) {
		super(mFunc.start(0), mFunc.end(0), mFunc.group(0), null);
		D.dprint_method_start();
		try {
			D.dprint("try");
			ItemSumMain(mFunc, mSum);
		} catch(Exception e) {
			D.dprint("catch");
			StringBufferNull sbOutput = new StringBufferNull(
					this.strPre);
			sbOutput.append(this.strFunction);
			sbOutput.append(this.strParmPre);
			Iterator<ParmVariable> iter = this.listPV.iterator();
			while (iter.hasNext()) {
				ParmVariable pv = iter.next();
				sbOutput.append(pv.strComma);
				sbOutput.append(pv.strPreSpace);
				if (pv.strPre != null) {
					sbOutput.append(pv.strPre);
					sbOutput.append(pv.strPathPre);
					sbOutput.append(pv.strPathOut);
					sbOutput.append(pv.strPathPost);
					if (pv.strPath == null) {
						this.strError = ERROR_INVALID_PATH;
						sbOutput.append(this.strError);
					}
					sbOutput.append(pv.strVariable);
					if (pv.strVariableID.equals("")) {	//null) {
						this.strError = ERROR_INVALID_VARIABLE;
						sbOutput.append(this.strError);
					}
					sbOutput.append(pv.strRefPathPre);
					sbOutput.append(pv.strRefInputPath);
					sbOutput.append(pv.strSeparator);
					sbOutput.append(pv.strInputValue);
					sbOutput.append(pv.strAccumPre);
					sbOutput.append(pv.strInputAccum);
					sbOutput.append(pv.strPost);
				} else {
					sbOutput.append(pv.strPost);
					this.strError = ERROR_INVALID_PARM;
					sbOutput.append(this.strError);
				}
				sbOutput.append(pv.strPostSpace);
			}
			sbOutput.append(this.strParmPost);
			sbOutput.append(this.strSeparator);
			sbOutput.append(this.strInputValue);
			sbOutput.append(this.strAccumPre);
			sbOutput.append(this.strInputAccum);
			sbOutput.append(this.strPost);
			this.strOutput = sbOutput.toString();
			D.dprint(this.strOutput);
			D.dprint(this.strError);
			D.dprint_method_end();
			return;
		}
		D.dprint(this.strOutput);
		D.dprint_method_end();
		return;
	}


	private void ItemSumMain( Matcher mFunc, Matcher mSum )
			throws Exception {
		D.dprint("ItemSumMain");
		D.dprint_method_start();
		D.dprint_name("mFunc", mFunc.group(0));
		analyze(mFunc);
		D.dprint_name("mSum", mSum.group(0));
		this.strFunction = mSum.group(1);
		D.dprint(this.strFunction);
		this.strParmPre = mSum.group(2);
		this.strParmPost = mSum.group(17);
		boolean bSuccess = true;
		this.listPV = new ArrayList<ParmVariable>();
		ParmVariable pv;
		Matcher mEach = PATTERN_EACH.matcher(mSum.group(4));
		if (! mEach.find()) {
			bSuccess = false;
			pv = new ParmVariable();
			pv.strPre = null;	//ERROR_INVALID_PARMを示す
			pv.strPost = mSum.group(4);
		} else {
			D.dprint_name("mParm", mEach.group(0));
			pv = createParmVariable(mEach);
			pv.strComma = null;
			pv.strPre = mSum.group(5);
			pv.strPost = mSum.group(7);
			pv.strPreSpace = mSum.group(3);
			pv.strPostSpace = mSum.group(8);
			if (pv.strPath == null) {
				bSuccess = false;
			}
			if (pv.strVariableID.equals("")) {	// == null) {
				bSuccess = false;
			}
		}
		this.listPV.add(pv);
		Matcher mParm = PATTERN_PARM.matcher(mSum.group(9));
//		int indexParm = 0;
		while (mParm.find()) {
//			indexParm ++;
			D.dprint(mParm.group(0));
			mEach = PATTERN_EACH.matcher(mParm.group(4));
			if (! mEach.find()) {
				bSuccess = false;
				pv = new ParmVariable();
				pv.strPre = null;	//ERROR_INVALID_PARMを示す
				pv.strPost = mParm.group(4);
			}
			D.dprint(mEach.group(0));
			ParmVariable pv2 = createParmVariable(mEach);
			pv.strPreSpace = mParm.group(2);
			pv.strPostSpace = mParm.group(6);
			pv2.strComma = mParm.group(1);
			pv2.strPre = mParm.group(3);
			pv2.strPost = mParm.group(5);
			if (pv2.strPath == null) {
				bSuccess = false;
			}
			if (pv2.strVariableID.equals("")) {	// == null) {
				bSuccess = false;
			}
			this.listPV.add(pv2);
		}
		if (! bSuccess) {
			throw new Exception();
		}
		D.dprint_method_end();
		return;
	}


	// translateFirstは定義不要


	public TransValue translateSecond() {
		D.dprint_method_start();
		TransValue tv = new TransValue();
		if (this.strOutput != null) {
			tv.strOutput = this.strOutput;
			tv.strValue = "!計算不能!";
			tv.strError = this.strError;
			D.dprint(tv.strError);
			D.dprint_method_end();
			return tv;
		}
		if (this.strError != null) {
			tv.strOutput = this.strOutput;
			tv.strValue = this.strError;	//"!計算不能!";
			tv.strError = this.strError;
			D.dprint(tv.strError);
			D.dprint_method_end();
			return tv;
		}

		List<String> listValue = new ArrayList<String>();
		Iterator<ParmVariable> iter = this.listPV.iterator();
		boolean bError = false;
		String strError = null;
		while (iter.hasNext()) {
			ParmVariable pv = iter.next();
			ParmList pm = Item.defineVariableMulti(
					pv.strVariableID, pv.strPath);
			if (pm.strError != null) {
				bError = true;
				strError = pm.strError;
			}
			listValue.addAll(pm.listValue);
			pv.strValue = String.join(":",  pm.listValue);
			pv.strRefPath = String.join("?", pm.listPath);
			pv.strExpr = String.join(":", pm.listExpr);
		}
		D.dprint(listValue);
		D.dprint(bError);
		D.dprint(strError);
		if (! bError) {
			UnitValue value;
			this.strAccum = String.join("+", listValue);
			D.dprint(this.strAccum);
			Expression expr = new Expression(strAccum,
					Item.bLeap,
					Item.chLeapInit, Item.chLeapTerm);
			value = expr.value();
	   		D.dprint(value);
	   		if (value != null) {
	   			this.strValue = value.toString(
						5, BigValue.ROUND_DOWN,
						Item.bHankaku,
						Item.bComma);
	   		} else {
	   			this.strValue = "!計算不能!";
	   		}
		} else {
			this.strValue = strError;
			D.dprint(this.strValue);
		}

		if (Item.mode == TransString.MODE_REPLACE) {
			StringBufferNull sbOutput = new StringBufferNull(
					this.strPre);
			sbOutput.append(this.strFunction);
			sbOutput.append(this.strParmPre);
			Iterator<ParmVariable> iterPv = this.listPV.iterator();
			while (iterPv.hasNext()) {
				ParmVariable pv = iterPv.next();
				sbOutput.append(pv.strComma);
				sbOutput.append(pv.strPreSpace);
				sbOutput.append(pv.strPre);
				sbOutput.append(pv.strPathPre);
				sbOutput.append(pv.strPathOut);
				sbOutput.append(pv.strPathPost);
				sbOutput.append(pv.strVariable);
				if (pv.strRefPathPre != null) {
					sbOutput.append(pv.strRefPathPre);
					sbOutput.append(pv.strRefPath);
				}
				if (pv.strSeparator != null) {
					sbOutput.append(pv.strSeparator);
				} else {
					sbOutput.append(ItemRefNormal.DEFAULT_SEPARATOR);
				}
				sbOutput.append(pv.strValue);
				if (pv.strAccumPre != null) {
					sbOutput.append(pv.strAccumPre);
					sbOutput.append(pv.strExpr);
				}
				sbOutput.append(pv.strPost);
			}
			sbOutput.append(this.strParmPost);
			if (this.strSeparator != null) {
				sbOutput.append(this.strSeparator);
				sbOutput.append(this.strValue);
			}
			if (this.strAccumPre != null) {
				sbOutput.append(this.strAccumPre);
				sbOutput.append(this.strAccum);
			}
			sbOutput.append(this.strPost);
			this.strOutput = sbOutput.toString();
		} else {
			StringBufferNull sbOutput = new StringBufferNull(
					" ");	//this.strPre);
			sbOutput.append(this.strFunction);
			sbOutput.append(this.strParmPre);
			Iterator<ParmVariable> iterPv = this.listPV.iterator();
			while (iterPv.hasNext()) {
				ParmVariable pv = iterPv.next();
				sbOutput.append(pv.strPreSpace);
				sbOutput.append(pv.strPre);
//				sbOutput.append(pv.strPathPre);
//				sbOutput.append(pv.strPathOut);
//				sbOutput.append(pv.strPathPost);
				sbOutput.append(pv.strVariable);
//				if (pv.strRefPathPre != null) {
//					sbOutput.append(pv.strRefPathPre);
//					sbOutput.append(pv.strRefPath);
//				}
				sbOutput.append(" ");	//pv.strSeparator);
				sbOutput.append(pv.strValue);
//				if (pv.strAccumPre != null) {
//					sbOutput.append(pv.strAccumPre);
//					sbOutput.append(pv.strExpr);
//				}
				sbOutput.append(pv.strPost);
			}
			sbOutput.append(this.strParmPost);
			if (this.strSeparator != null) {
				sbOutput.append(" ");	//this.strSeparator);
				sbOutput.append(this.strValue);
			}
//			if (this.strAccumPre != null) {
//				sbOutput.append(this.strAccumPre);
//				sbOutput.append(this.strAccum);
//			}
			sbOutput.append(" ");	//this.strPost);
			this.strOutput = sbOutput.toString();
		}
		tv.strValue = this.strValue;
		tv.strOutput = this.strOutput;
		tv.strError = null;
		D.dprint(this.strOutput);
		D.dprint(this.strError);
		D.dprint_method_end();
		return tv;
	}

}
