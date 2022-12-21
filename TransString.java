/**
 *
 */
package data;

//https://web-dev.hatenablog.com/entry/eclipse/efxclipse/hello-world

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.w3c.dom.Element;

import cmn.D;
import data.Item.TransValue;
import value.Expression;

/**
 * proXで処理する文字データ
 * @author sue-t
 *
 */
public class TransString {
	/** バージョンを示す文字列 */
	public static final String VERSION = "0.81";

	/** 最初の１パスを示すパス回数 */
	public static final int PASS_FIRST = 1;

	/** ２パスを示すパス回数 */
	public static final int PASS_2 = -1;

	/** 置換（計算）モード */
	public static final int MODE_REPLACE = 0;
	/** 清書モード */
	public static final int MODE_REWRITE = 1;

	/** 変換モード */
	private int mode;

	/** 変換前の文字列 */
	private String strInput;

	/** 変換後の文字列 */
	private String strOutput;

//	/** 変換後の置換文字列 */
//	private String strOutputReplace;
//
//	/** 変換後の清書文字列 */
//	private String strOutputRewrite;

	/** 変数定義の最大パス数 */
    private int passDefMax;

	/** 変換時のエラー回数 */
	private int countError;

	/** 処理中の行番号 */
	private int numLine;

	/** 発生エラーのリスト */
	private ArrayList<String> listError;

	{
		Expression.resetOption();
//	    def init_expression(self):
//        value.Expression.set_number_zenkaku(true);
//            # ０１２３４…９
//        Expression.set_number_kanji(True)
//            # 〇一二三四…九
//        Expression.set_number_kyu(True)
//            # 〇壱弐参四…九
//        Expression.set_number_tanni(True)
//            # 十百千万億兆京
//        Expression.set_number_tanni_kyu(True)   # 拾萬
//        Expression.set_number_tanni_hoka(False) # 什佰陌仟
//        Expression.set_period_zenkaku(True) # 全角ピリオド
//        Expression.set_period_maru(True)    # 全角丸（。）
//        Expression.set_comma_zenkaku(True)
//            # 全角カンマ（，）
//        Expression.set_comma_ten(True)      # 全角点（、）
//        Expression.set_comma_underscore(True)
//        Expression.set_comma_underscore_zenkaku(True)
//        Expression.set_plus_zenkaku(True)
//        Expression.set_minus_zenkaku(True)
//        Expression.set_minus_dash(True)
//        Expression.set_minus_chouon(True)
//        Expression.set_minus_hyphen(True)
//        Expression.set_minus_sankaku_white(True)
//        Expression.set_minus_sankaku_black(True)
//        Expression.set_mult_zenkaku(True)
//        Expression.set_mult_kakeru(True)
//        Expression.set_mult_upper_x(True)
//        Expression.set_mult_lower_x(True)
//        Expression.set_mult_upper_x_zenkaku(True)
//        Expression.set_mult_lower_x_zenkaku(True)
//        Expression.set_mult_zenkaku_batsu(True)
//        Expression.set_divide_zenkaku(True)
//        Expression.set_divide_waru(True)
//        Expression.set_pow_zenkaku(True)

		// [],{},［］ は使わない
        value.Expression.setFalseKakkoHanChu();
        value.Expression.setFalseKakkoHanDai();
        value.Expression.setFalseKakkoZenChu();
//        value.Expression.setFalseKakkoZenDai();

//        Expression.set_kakko_zen_shou(True)
//        Expression.set_kakko_zen_chu(True)
//        Expression.set_kakko_zen_dai(True)
//        Expression.set_kakko_zen_sumi(True)
//        Expression.set_kakko_kikkou(True)
//        Expression.set_percent_hankaku(True)
//        Expression.set_percent_zenkaku(True)
//        Expression.set_percent_hanmoji(True)
//        Expression.set_percent_zenmoji(True)
//        Expression.set_unit(True)
	}


	public TransString( String strSrc, int mode ) {
		this.strInput = strSrc;
		this.strOutput = null;
//		this.strOutputReplace = null;
//		this.strOutputRewrite = null;
		this.mode = mode;
	}


	private void reset() {
		return;
	}

	/**
	 * 文字列を変換する
	 */
	public String translate() {
		D.dprint_method_start();
		TransXml transXml = new TransXml();
		this.countError = 0;

//        # Alt+Rで処理すると、「\r」が入ることがあるようだ
//        self.str_input = str_input.replace('\r', '')

        this.passDefMax = 1;    // 変数定義の最大パス数

    	List<Item> itemList = new ArrayList<Item>();

    	D.dprint(this.strInput);
    	// パス１　（≠１回目のパス）
    	this.reset();
    	transXml.reset();
    	Item.reset(this.strInput, this, transXml,
    			this.mode);
    	Item item;
    	D.dprint("==================================================");
    	D.dprint("==================パス１==========================");
    	D.dprint("==================================================");
    	Item.translateInit();	// パス１では不要かも？
    	while( Item.index < Item.lenInput) {
    		item = Item.parse();
    		D.dprint(item);
    		if (item != null) {
    			itemList.add(item);
    		}
    	}
    	D.dprint_xml(transXml);

	    this.reset();
	    transXml.reset();
	    StringBuffer sb = new StringBuffer();
    	D.dprint("==================================================");
    	D.dprint("==================パス２==========================");
    	D.dprint("==================================================");
    	this.numLine = 1;
    	this.listError = new ArrayList<String>();
    	Item.initDefineList();
    	Item.translateInit();
    	for (Item itemAtom : itemList) {
       		TransValue tv = itemAtom.translateSecond();
       		if (tv.strError != null) {
       			this.countError ++;
       			String strError = String.format(
       					"%d:%s",
       					this.numLine, tv.strOutput);
       			this.listError.add(strError);
       		}
       		D.dprint(tv.strOutput);
       		sb.append(tv.strOutput);
       		int i = 0;
       		D.dprint(itemAtom.strInput);
       		while (i < itemAtom.strInput.length()) {
       			int j = itemAtom.strInput.indexOf(
       					'\n', i);
       			if (j == -1) {
       				break;
       			}
       			this.numLine ++;
       			i = j + 1;
       		}
    	}
    	this.strOutput = sb.toString();
    	D.dprint_xml(transXml);

    	Iterator<Element> iter = Item.listDefVariable.iterator();
    	while (iter.hasNext()) {
    		Element element = iter.next();
    		D.dprint(element.toString());
    	}
        D.dprint_method_end();
		return this.strOutput;
	}


	public int getPassDefMax() {
		return passDefMax;
	}

	public void resetPassDefMax(int setPassDef) {
		if (setPassDef > this.passDefMax) {
			this.passDefMax = setPassDef;
		}
	}

	public void setPassDefMax(int passDefMax) {
		this.passDefMax = passDefMax;
	}

	public String getCountError() {
		String str = Integer.toString(this.countError);
		return str;
	}

	public ArrayList<String> getListError() {
		return this.listError;
	}

	public String getStrOutput() {
		return this.strOutput;
	}

}
