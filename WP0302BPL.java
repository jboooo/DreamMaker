package dai;
import jcx.jform.hproc;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WP0302BPL extends hproc{
   
	talk t=null;
	talk w=null;
	StringBuffer sa=null;
	Vector vt=null;
	public String action(String value)throws Throwable {
		// 回傳值為 true 表示執行接下來的資料庫異動或查詢
		// 回傳值為 false 表示接下來不執行任何指令
		// 傳入值 value 為 "新增","查詢","修改","刪除","列印","PRINT" (列印預覽的列印按鈕),"PRINTALL" (列印預覽的全部列印按鈕) 其中之一
		getButton(3).setEnabled(true);
		t=getTalk("TradeMark");
		w=getTalk("wp");
		sa=new StringBuffer();
		vt=new Vector();
		
	    Query();

		return value;	
	}
	
	public boolean Query()throws Throwable {	
		String sql="";
		
		String idkey=getValue("idkey").trim();
		String meid=getValue("meid").trim();
		String row=getValue("row").trim();
		String keyno=getValue("keyno").trim();
		
		setValue("Idkey",idkey);
		setValue("Meid",meid);
		setValue("Row",row);
		setValue("keyno",keyno);
		
		//取得idkey

		//下半部模組
		selectMarkname(meid);//搜尋商標及描述性說明
		selectGoodClass(meid);//搜尋指定商品類別
		selectWptlproexte(idkey);//搜尋控制按鈕

		setValue("field1",sa.toString());
		return true;
	}


	/*
		搜尋-商標及描述性說明，參數:本所案號
	*/
	public void selectMarkname(String meid)throws Throwable {

		//搜尋資料
		String sql="SELECT markname,picchk,marktype,markpic,markmeid,markappyid,markregid,cdesctext,desctext,relchk FROM wptmapply WHERE meid ='"+ meid+"'";
		String[][]retwptmapply=t.queryFromPool(sql);
		sa.append("\n商標"+sql+"\n");
		sa.append("\n商標及描述性說明\n" + sql); // 檢查點
		String markmeid="";
		//設定欄位
		setValue("markname",retwptmapply[0][0].trim());    //商標名稱
		setValue("picchk",retwptmapply[0][1].trim());    //圖樣中不主張專用權
		setValue("marktype",retwptmapply[0][2].trim());    //商標種類
		setValue("markpic",retwptmapply[0][3].trim());    //商標圖樣
		setValue("markmeid",retwptmapply[0][4].trim());    //(正商標)本所案號
		markmeid=retwptmapply[0][4].trim();
		setValue("markappyid",retwptmapply[0][5].trim());    //申請號碼
		setValue("markregid",retwptmapply[0][6].trim());    //註冊號
		setValue("cdesctext",retwptmapply[0][7].trim());    //中文描述性說明
		setValue("desctext",retwptmapply[0][8].trim());    //描述性說明
		setValue("relchk",retwptmapply[0][9].trim());    //有無關連商標
		if(markmeid.trim().length()>0) {
			//搜尋資料
			sql="SELECT markName,owneDate,ownsDate FROM wptmapply WHERE markmeid = '"+markmeid+"'";
			retwptmapply=t.queryFromPool(sql);
			sa.append("\n正商標\n"+sql+"\n");

			//設定欄位
			setValue("markName",retwptmapply[0][0].trim());    //商標名稱
			setValue("owneDate",dateformat(retwptmapply[0][1].trim()));    //專用滿期日
			setValue("ownsDate",dateformat(retwptmapply[0][2].trim()));    //專用起始日
		}
		setValue("field5",sa.toString());
	}
	/*
		搜尋-指定商品，參數:本所案號
	*/
	public void selectGoodClass(String meid)throws Throwable {

		//指定商品
		String sql = "Select Goodclass,uniNOrigdname,uniNChgdname,delmk,'','',Goodclass,uniNOrigdname,uniNChgdname from wptlapbale where meid='"
		             + meid + "' and delmk<>1";
		String[][] ret_goodclass = t.queryFromPool(sql);
		sa.append("\n搜尋指定商品\n" + sql); // 檢查點
		// 檢查
		sa.append("\n搜尋wptlapbale,指定商品\n" + sql);
		if (ret_goodclass.length > 0) {
			setTableData("goodclassTable", ret_goodclass);
			setEditable("goodclassTable", false);
			// 設定第一筆資料到畫面上
			setValue("checkclass", ret_goodclass[0][0]); // 用來記錄類別代號，存檔的時後使用的。
			setValue("goodclass_field", ret_goodclass[0][0]);
			setValue("uniNOrigdname", ret_goodclass[0][1]);
			setValue("uniNChgdname", ret_goodclass[0][2]);
		}
	}
	
	/*
		搜尋-控制按鈕
	*/
	public void selectWptlproexte(String idkey)throws Throwable {

		//搜尋資料
		String sql="SELECT pamanid,prename1,desctext,goodname1,marktype,markmeid,markname,picchk,keyno FROM wptlproexte WHERE idkey ='"+ idkey+"'";
		String[][]retwptlproexte=t.queryFromPool(sql);
		sa.append("\n搜尋-控制按鈕\n"+sql);
		if(retwptlproexte.length>0) {
			//設定欄位
			setValue("ispamanid",retwptlproexte[0][0].trim());    //ispamanid//申請人
			setValue("isprename1",retwptlproexte[0][1].trim());    //代表人一(原)
			setValue("isdesctext",retwptlproexte[0][2].trim());    //isdesctext//描述性說明
			setValue("isgoodname1",retwptlproexte[0][3].trim());    //goodname1//指定商品1
			setValue("ismarktype",retwptlproexte[0][4].trim());    //商標種類
			setValue("ismarkmeid",retwptlproexte[0][5].trim());    //正商標資料
			setValue("ismarkname",retwptlproexte[0][6].trim());    //有無關聯商標
			setValue("ispicchk",retwptlproexte[0][7].trim());    //有無關聯商標
			setValue("keyno",retwptlproexte[0][8].trim()); //流水號
			sa.append("\n搜尋空制按鈕\n" + sql); // 檢查點

		}
	}
	/*方法:日期格式化轉化成2013/01/01
	  讀檔時使用:若日期資料讀到"1900"開頭，回傳"";
	*/
	public String dateformat(String date){             
		if(date.length()==0 || date.indexOf("1900")==0){
			return"";
		} else if(date.length()>0){
		String []tmp  = date.split(" ");
		String data = tmp[0].trim();
		data=convert.replace(data,"-","");
		date=convert.FormatedDate(data,"/");
		}		
	return date;
	}
}
