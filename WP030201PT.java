package dai;
import jcx.jform.bTransaction;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;

public class WP030201PT extends bTransaction{
	talk t=null;
	talk w=null;
	StringBuffer sa=null;
	public boolean action(String value)throws Throwable{
		// 回傳值為 true 表示執行接下來的資料庫異動或查詢
		// 回傳值為 false 表示接下來不執行任何指令
		// 傳入值 value 為 "新增","查詢","修改","刪除","列印","PRINT" (列印預覽的列印按鈕),"PRINTALL" (列印預覽的全部列印按鈕) 其中之一
		t=getTalk("TradeMark");
		w=getTalk("wp");
		sa=new StringBuffer();		
		String idkey=getQueryValue("idkey");
		String sql="select meid from wptlproc where idkey='"+idkey+"'";
		String[][]retMeid=t.queryFromPool(sql);
		String meid=retMeid[0][0].trim();
		
		selectMemanid(meid);//搜尋本所代理人
		selectMarkname(meid);//搜尋商標及描述性說明
		selectGoodClass(meid);//搜尋指定商品類別
		selectPamanidAndId(meid);//申請人及代表人
		selectWptlproexte(idkey);//控制按鈕
	return false;
	}


	/*
		搜尋-套裝本所代理人(多筆)，傳入參數是本所案號	
	*/
	public void selectMemanid(String meid)throws Throwable{

		String sql = "select memanid from wptlmeman where meid='" + meid + "'";
		String[][] ret_memanid = t.queryFromPool(sql);
		sa.append("\n代理人id\n" + sql+"\n");	                 // 檢查
		StringBuffer sb = new StringBuffer();
		if (ret_memanid.length > 0) {
			for (int i = 0; i < ret_memanid.length; i++) {
				sb.append("'" + ret_memanid[i][0] + "',");
			}
			sb.setLength(sb.length() - 1);
			sql = "select memanid,memanname from wptcmeman where memanid in(" + sb.toString() + ") and tm='1'";
			sa.append("\n代理人id及姓名\n" + sql+"\n");	                 // 檢查
			String[][] ret_memanname = w.queryFromPool(sql);
			if (ret_memanname.length > 0) {
				setTableData("memanidTable", ret_memanname);
			}
		} // 搜尋代理人if結束
	}

	/*
		搜尋-商標及描述性說明，參數:本所案號	
	*/
	public void selectMarkname(String meid)throws Throwable{

		//搜尋資料
		String sql="SELECT markname,picchk,marktype,markpic,markmeid,markappyid,markregid,cdesctext,desctext FROM wptmapply WHERE meid ='"+ meid+"'";
		String[][]retwptmapply=t.queryFromPool(sql);
		sa.append("\n商標"+sql+"\n");

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
		if(markmeid.trim().length()>0){
			//搜尋資料
			sql="SELECT markName,owneDate,ownsDate FROM wptmapply WHERE markmeid = '"+markmeid+"'";
			retwptmapply=t.queryFromPool(sql);
			sa.append("\n正商標\n"+sql+"\n");

			//設定欄位
			setValue("markName",retwptmapply[0][0].trim());    //商標名稱
			setValue("owneDate",retwptmapply[0][1].trim());    //專用滿期日
			setValue("ownsDate",retwptmapply[0][2].trim());    //專用起始日
		}
		setValue("field5",sa.toString());
	}
	/*
		搜尋-指定商品，參數:本所案號	
	*/
	public void selectGoodClass(String meid)throws Throwable{	
	
	//指定商品
	String sql = "Select Goodclass,uniNOrigdname,uniNChgdname,delmk,'','',Goodclass,uniNOrigdname,uniNChgdname from wptlapbale where meid='"
		  + meid + "' and delmk<>1";
	String[][] ret_goodclass = t.queryFromPool(sql);
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
		搜尋-申請人與代表人，參數:本所案號	
	*/
	public void selectPamanidAndId(String meid)throws Throwable{	
	//申請人與代表人
	//搜尋申請人編號，lapkey在wptlapman，條件為本所案號,設定在table1
		String sql = "select '',pamanid,'','','','',meid,lapkey,'','','' from wptlapman  where meid='" + meid
		      + "' order by pamanid desc";
		String[][]ret = t.queryFromPool(sql);
		sa.append("\n表格:本所案號，申請人id，lapkey\n" + sql);	     	// 檢查
		if (ret.length > 0) {
			for (int i = 0; i < ret.length; i++) {// 利用申請人編號搜尋申請人名稱，設定在欄位
				sql = "select b.uniChineseName,b.uniOriginName,unioriginaddr,unichineseaddr from wptmapman b WHERE pamanid='" + ret[i][1].trim()
				      + "' order by b.uniChineseName";
				String[][] ret_Name = w.queryFromPool(sql);
				sa.append("\n設定申請人名稱(中),設定申請人名稱(原)\n"+sql);              //檢查
				if (ret_Name.length > 0) {
					ret[i][2] = ret_Name[0][0]; // 設定申請人名稱(中)
					ret[i][3] = ret_Name[0][1]; // 設定申請人名稱(原)
					ret[i][4] = ret_Name[0][2]; // 設定申請人地址(中)
					ret[i][5] = ret_Name[0][3]; // 設定申請人地址(原)
				}
				if (ret[i][7].length() > 0) {
					sql = "select id from wptlapre where lapkey='" + ret[i][7] + "'";
					String[][] ret_id = t.queryFromPool(sql);
					sa.append("\nlapkey\n"+sql);                          //檢查
					if (ret_id.length == 3) {
						ret[i][8] = ret_id[0][0].trim();
						ret[i][9] = ret_id[1][0].trim();
						ret[i][10] = ret_id[2][0].trim();
					} else if (ret_id.length == 2) {
						ret[i][8] = ret_id[0][0].trim();
						ret[i][9] = ret_id[1][0].trim();
					} else if (ret_id.length == 1) {
						ret[i][10] = ret_id[0][0].trim();
					}
				}

			}
			setTableData("pamanidTable", ret);
		}
		setVisible("idTable", false);
	}
	/*
		搜尋-控制按鈕	
	*/
	public void selectWptlproexte(String idkey)throws Throwable{	

		//搜尋資料
		String sql="SELECT pamanid,prename1,desctext,goodname1,marktype,markmeid,relchk,markname,picchk FROM wptlproexte WHERE idkey ='"+ idkey+"'";
		String[][]retwptlproexte=t.queryFromPool(sql);
		if(retwptlproexte.length>0){
		//設定欄位
		setValue("ispamanid",retwptlproexte[0][0].trim());    //ispamanid//申請人
		setValue("isprename1",retwptlproexte[0][1].trim());    //代表人一(原)
		setValue("isdesctext",retwptlproexte[0][2].trim());    //isdesctext//描述性說明
		setValue("isgoodname1",retwptlproexte[0][3].trim());    //goodname1//指定商品1
		setValue("ismarktype",retwptlproexte[0][4].trim());    //商標種類
		setValue("ismarkmeid",retwptlproexte[0][5].trim());    //正商標資料
		setValue("isrelchk",retwptlproexte[0][6].trim());    //有無關聯商標
		setValue("ismarkname",retwptlproexte[0][7].trim());    //有無關聯商標
		setValue("ispicchk",retwptlproexte[0][8].trim());    //有無關聯商標
		
		
		}
	}	
	
	
	
	
}