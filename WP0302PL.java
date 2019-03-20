package dai;
import jcx.jform.bTransaction;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import jcx.lib.pMethod;

public class WP0302PL extends bTransaction{
	talk t=null;
	talk w=null;
	StringBuffer sa=null;
	StringBuffer sb=null;
	StringBuffer sc=null;
	Vector vt=null;
	public boolean action(String value)throws Throwable{
		// 回傳值為 true 表示執行接下來的資料庫異動或查詢
		// 回傳值為 false 表示接下來不執行任何指令
		// 傳入值 value 為 "新增","查詢","修改","刪除","列印","PRINT" (列印預覽的列印按鈕),"PRINTALL" (列印預覽的全部列印按鈕) 其中之一
		t=getTalk("TradeMark");
		w=getTalk("wp");
		sa=new StringBuffer();      //檢查用
		sb=new StringBuffer();      //回傳sql
		sc=new StringBuffer();		//回傳keyno的sql
		vt=new Vector();
		
		SAVE();

		return false;
	}
	
	public boolean SAVE()throws Throwable{
		String meid=getValue("Meid");
		String idkey=getValue("Idkey");
		String row=getValue("Row");
		String keyno=getValue("keyno");
		
		//取得rcvid
		String sql="select rcvid from wptlproc where idkey='"+idkey+"'";
		String[][] retIdkey=t.queryFromPool(sql);
		String rcvid="";	
		if(retIdkey.length>0){
			rcvid=retIdkey[0][0].trim();
		}
		
		saveDesctext(meid);//儲存描述性說明
		saveMarkname(meid);//儲存商標
		saveGoodclass(meid);//儲存指定商品類別
		saveWptlproexte(idkey,meid,rcvid,keyno);//儲存wptlproexte資料表
		put("row",row);
		put("sql",sb.toString());
		put("sql2",sc.toString());
		
//		setValue(".field1",sb.toString());
//		setValueAt(".table1",sb.toString(),1,"idkey");
		hideDialog();
		//this.addScript("window.parent.GLOBAL.get('showdialog').setVisible(false);");//關閟本頁
		this.addScript("window.parent.document.getElementById('button3').click();");//取得button3並執行
		
		
	
		return true;	
	}
/*
		儲存描述性說明
	*/
	public void saveDesctext(String meid)throws Throwable {
		//描述性說明
		sa.append("\n-------描述性說明，儲存------:\n"); // 檢查點
		String isdesctext = getValue("isdesctext").trim();
		String cdesctext = getValue("cdesctext").trim();
		String desctext = getValue("desctext").trim();
		if ("1".equals(isdesctext)) {
			String sql = "update wptmapply set cdesctext='" + cdesctext + "',desctext='" + desctext + "' where meid='" + meid
			             + "'";
			vt.add(sql);
			sa.append("\n描述性說明\n" + sql); // 檢查點
		}
		execDate(vt);
	}
	/*
		儲存商標資料
	*/
	public void saveMarkname(String meid)throws Throwable {

		//取得資料
		String ismarkname=getValue("ismarkname".trim());//商標名稱
		String ismarktype=getValue("ismarktype".trim());//商標種類
		String ismarkmeid=getValue("ismarkmeid".trim());//正商標資料
		String ispicchk=getValue("ispicchk".trim());//圖樣中不主張專用權

		//取得資料
		String markname=getValue("markname".trim());//商標名稱
		if(ismarkname.equals("1")) {
			String sql="UPDATE wptmapply SET "
			           +" markname='"+markname
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n商標名稱:\n"+sql);
		}

		String picchk=getValue("picchk".trim());//圖樣中不主張專用權
		if(ispicchk.equals("1")) {
			String sql="UPDATE wptmapply SET "
			           +" picchk='"+picchk
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n商標名稱:\n"+sql);
		}

		String marktype=getValue("marktype".trim());//商標種類
		if(ismarktype.equals("1")) {
			String sql="UPDATE wptmapply SET "
			           +" marktype='"+marktype
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n商標名稱:\n"+sql);
		}
		String markmeid=getValue("markmeid".trim());//(正商標)本所案號
		String markappyid=getValue("markappyid".trim());//申請號碼
		String markregid=getValue("markregid".trim());//註冊號
		if(ismarkmeid.equals("1")) {
			String sql="UPDATE wptmapply SET "
			           +" markmeid='"+markmeid
			           +"',markappyid='"+markappyid
			           +"',markregid='"+markregid
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n商標名稱:\n"+sql);
		}
		String relchk=getValue("relchk".trim());//有無關連商標
		if(markmeid.length()>0 || markappyid.length()>0 || markregid.length()>0) {
			String sql="UPDATE wptmapply SET "
			           +" relchk='"+relchk
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n商標名稱:\n"+sql);
		}
		sa.append("\n商標名稱:hello\n");
		execDate(vt);

	}

	/*
		儲存指定商品,參數本所案號
	*/
	public void saveGoodclass(String meid)throws Throwable {
		sa.append("\n-------指定商品，儲存。說明:isgoodname1打勾就會儲存:\n"); // 檢查點
		String isgoodname1=getValue("isgoodname1").trim();
		String[][] retGoodclassTable = getTableData("goodclassTable");   //表格GoodclassTable
		sa.append("isgoodname1:"+isgoodname1);
		sa.append("retGoodclassTable"+retGoodclassTable.length+"");
		setValue("field1",sa.toString());
		if ("1".equals(isgoodname1)) {
			for (int i = 0; i < retGoodclassTable.length; i++) {
				String sql2 = "update wptlapbale set uninorigdname='" + retGoodclassTable[i][1].trim() + "',uninchgdname='"
				              + retGoodclassTable[i][2].trim() + "',goodclass='" + retGoodclassTable[i][0].trim() + "',delmk='" + retGoodclassTable[i][3].trim()
				              + "' where meid='" + meid + "' and goodclass='" + retGoodclassTable[i][6].trim() + "'";
				vt.add(sql2);
				sa.append("\n儲存指定性商品\n" + sql2); // 檢查點
				execDate(vt);
			}
		}

	}


	/*
		儲存打勾資料wptlproexte,傳入idkey,meid,rcvid
	*/
	public void saveWptlproexte(String idkey,String meid,String rcvid,String keyno)throws Throwable {
//		sa.append("\n-------儲存打勾資料wptlproexte，儲存------:\n"); // 檢查點

		//取得資料
		String isdesctext=getValue("isdesctext".trim());//描述性說明
		String isgoodname1=getValue("isgoodname1".trim());//指定商品
		String ismarkname=getValue("ismarkname".trim());//商標名稱
		String ismarktype=getValue("ismarktype".trim());//商標種類
		String ismarkmeid=getValue("ismarkmeid".trim());//正商標資料
		String ispicchk=getValue("ispicchk".trim());//正商標資料

		if (keyno.length() > 0) {
			String sql = "update wptlproexte set " + "desctext='" + isdesctext
			      + "'," + "goodname1='" + isgoodname1 + "'," + "markname='" + ismarkname
			      + "'," + "marktype='" + ismarktype + "'," + "markmeid='" + ismarkmeid+ "'," + "picchk='" + ispicchk
			      + "'" + " where keyno='" + keyno + "'";
//			vt.add(sql);
			sc.append(sql+"@");
		} else {
//			keyno=getKeyno();
			String sql = "insert into wptlproexte (meid,desctext,goodname1,markname,marktype,markmeid,idkey,rcvid,keyno) values ('"
			      + meid + "','" + isdesctext+ "','" + isgoodname1 + "','"
			      + ismarkname + "','" + ismarktype + "','" + ismarkmeid   + "','" + idkey  + "','" + rcvid + "','aaa')";
//			vt.add(sql);
			sc.append(sql+"@");
		}
//		execDate(vt);

	}

	/*
		異動資料庫
	*/

	public void execDate(Vector vt)throws Throwable {
		sb=new StringBuffer();
		String[] vtsql = (String[]) vt.toArray(new String[0]);
		for(int i=0; i<vtsql.length; i++) {
			sb.append(vtsql[i]+"@");
		}
	}

	/*keyno自動編號
	*回傳值是String，若多筆呼叫會自動+1
	*/
//	String keyno="";  //key在外是可以累加
//	public String getKeyno()throws Throwable {
//		talk t=getTalk("TradeMark");
//		String year=operation.sub(getYear(),"1911");//取得民國年
//		if(keyno.length()==10){
//			keyno= operation.add(keyno,"1");
//		} else{
//			String sql = "select top 1 keyno from wptlproexte where left(keyno,2)='10' and  len(keyno)='10' order by keyno desc";                //取得keyno處理
//			String[][]ret = t.queryFromPool(sql);
//			if(ret.length>0) {
//				keyno=ret[0][0].substring(4);
//				keyno= operation.add(keyno,"1");//字串加1。
//				keyno=convert.add0(keyno,"7");
//				keyno=year+keyno;
//			} else {
//				keyno="1080000001";
//			}
//		}
//		return keyno;
//	}

	/*取得西元年
	*匯入import java.text.DateFormat;import java.text.SimpleDateFormat;import java.util.Date;
	*/
//
//	public static String getYear() {
//		Date date = new Date();
//		DateFormat dateformat= new SimpleDateFormat("yyyy");
//		String dateTime=dateformat.format(date);
//		return dateTime;
//	}
}
