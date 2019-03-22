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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class WP0302PT extends bTransaction{
	talk t=null;
	talk w=null;
	StringBuffer sa=null;   //檢查
	StringBuffer sb=null;   //查詢使用的
	StringBuffer sc=null;   //檢查後調整再異動
	StringBuffer sd=null;   //分頁的資料直接異動
	
	Vector vt=null;
	public boolean action(String value)throws Throwable{
		// 回傳值為 true 表示執行接下來的資料庫異動或查詢
		// 回傳值為 false 表示接下來不執行任何指令
		// 傳入值 value 為 "新增","查詢","修改","刪除","列印","PRINT" (列印預覽的列印按鈕),"PRINTALL" (列印預覽的全部列印按鈕) 其中之一
		t=getTalk("TradeMark");
		w=getTalk("wp");
		vt=new Vector();
				
		sa=new StringBuffer();
		sb=new StringBuffer();
		sc=new StringBuffer();
		sd=new StringBuffer();
				
		if(value.equals("查詢")){
			Query();
		}else if(value.equals("修改")){
			Save();
		}else if(value.equals("刪除")){
			Delete();
		}

		return false;
	}
	public boolean Query()throws Throwable {
	String sql="";
	//取得idkey
	String idkey=getQueryValue("idkey").trim();
	setValue("idkey",idkey.toUpperCase());
	//取得rcvid
	sql="select rcvid,meid from wptlproc where idkey='"+idkey+"'";
	String[][] retIdkey=t.queryFromPool(sql);

	String rcvid=retIdkey[0][0].trim();
	String meid=retIdkey[0][1].trim();
	setValue("rcvid",rcvid.toUpperCase());
	setValue("meid",meid.toUpperCase());


	selectTable1(rcvid);//搜尋table1表格
	//將畫面第一筆meid設定在搜尋請款套組裡。
	String[][]table1=getTableData("table1");
	String strMeid=table1[0][2].trim();
	String strRcvid=table1[0][12].trim();
	
	selectDebit(strMeid,strRcvid);//搜尋請款套組

	selectMemanid(meid);//搜尋本所代理人
	selectPamanidAndId(meid);//搜尋申請人及代表人
	selectWptlproexte(idkey);//搜尋控制按鈕
	selectAnnex(idkey);//搜尋附件
	setValue("field1",sa.toString());
	return true;
	}
	
	public boolean Save()throws Throwable {
	
		String[][] table1=getTableData("table1");
		for (int i=0 ; i<table1.length ; i++ ) {
			String meid=table1[i][2].trim();
			String exteappydate=table1[i][6].trim();//最新一次延展申請日期
			String appydate=table1[i][6].trim();//延展申請日期
			String dbbouns=table1[i][7].trim();//規費加倍
			String rcvno=table1[i][8].trim();//收據號碼
			String idkey=table1[i][9].trim();
			String rcvid=table1[i][12].trim();
			String keyno=table1[i][13].trim();
			//優先執行，需要取得keyno
			saveWptlproexte(idkey,meid,rcvid,keyno,table1[i][14].trim());//儲存wptlproexte資料表
			//其次，需要用到keyno,將舊資料備份
			
			String sql="select keyno from wptlproc where idkey='"+idkey+"'";
			String[][]keynoArray=t.queryFromPool(sql);
			if(keynoArray.length<=1) {
				backupPamanid(idkey,meid);	//備份申請人
				saveWptlappyOriApman(meid);//備份代表人
				backupMemanid(idkey);//備份本所代理人
				backupWptmapply(meid,idkey);//備份主檔,商標,描述性說明，使用json
			}
					
			//將舊資料蓋過
			savePamanid(meid);//儲存申請人，代表人
			saveMemanid(meid);//儲存代理人
			execString(table1[i][11].trim()); //儲存指示性商品、商標、描述性說明
			saveAnnex(idkey);//儲存附件
	
			saveWptlprocWrkman(idkey,meid);//儲存-案件程序經手承辦員記錄檔wptlproc_wrkman,若新承辦員則寫入檔
			saveWptmapply(meid,exteappydate);//更新主檔
			saveWptlproc(appydate,dbbouns,rcvno,idkey);//更新程序檔
		}
		saveWptlprocWptmapply();//存檔更新委託人傳回來的表格。
	
		setValue("field1",sa.toString());//記錄所有異動的sql
	
		return true;
	}
	/*
		刪除
		@戴勝台
	*/	
		public boolean Delete()throws Throwable{
				
		String[][] table1=getTableData("table1");
		if(table1.length<=0){
			message("沒有資料，不能刪除");
			return false;		
		}
		
		for (int i=0 ;i<table1.length ;i++ ){
			String meid=table1[i][2].trim();
			String idkey=table1[i][9].trim();
			String keyno=table1[i][13].trim();	
			//欄位檢核keyno
			if(keyno.equals("")){
				message("沒有儲存過，不能刪除");
				return false;
			}
			delWptlproexte(keyno); //刪除Wptlproexte
			delWptlprocWrkman(idkey,meid);//刪除-案件程序經手承辦員記錄檔
			delWptlproc(idkey);//del程序檔wptlproc			
			//還原申請人，代表人，代理人
			revertMark(idkey,meid);//還原-主檔、商標、描述性說明

		}
		setValue("field1",sa.toString());//記錄所有異動的sql	
		return true;
	}
		
	/*
		搜尋table1表格
	*/
	public void selectTable1(String rcvid)throws Throwable {
	
		//表格設定
		String sql="select distinct '','a'=case when d.idkey is null then '否' else '是' end,a.meid,"
			+"b.regid,b.markname,b.extedead,a.appydate,a.dbbouns,a.rcvno,a.idkey,'修改','',a.rcvid,d.keyno,''"
			+" from wptlproc a"
			+" left outer join wptmapply b on a.meid=b.meid"
			+" left outer join wptlapbale c on a.meid=c.meid and delmk=0"
			+" left outer join wptlproexte d on a.idkey=d.idkey"
			+" where left(a.rcvid,10)=left('"+rcvid+"',10) and a.procid='0302'";
		String[][] retWptlproc=t.queryFromPool(sql);
		sa.append("\nsql\n"+sql);
		if(retWptlproc.length==0) {
			return ;
		}
		//將日期格式化
		for(int i=0; i<retWptlproc.length; i++) {
			retWptlproc[i][5]=dateformat(retWptlproc[i][5]);
			retWptlproc[i][6]=dateformat(retWptlproc[i][6]);
		}
	
		setTableData("table1",retWptlproc);
	}
	
	/*
		搜尋請款套組欄位
	*/
	public void selectDebit(String meid,String rcvid)throws Throwable {
		//欄位設定
		String sql="SELECT debitid,debitchk,ltdate,debitdate FROM wptlproc "
			+" WHERE meid ='"+ meid +"' AND rcvid ='"+rcvid+"' AND procid = '0302'";
	
		String[][]retwptlproc=t.queryFromPool(sql);
		sa.append("\nsql\n"+sql);
	
		if(retwptlproc.length>0) {
			setValue("debitid",retwptlproc[0][0]);
			setValue("debitchk",retwptlproc[0][1]);
			setValue("ltdate",dateformat(retwptlproc[0][2]));
			setValue("debitdate",dateformat(retwptlproc[0][3]));
		}
		
	}	
	
	/*
		搜尋-套裝本所代理人(多筆)，傳入參數是本所案號	
	*/
	public void selectMemanid(String meid)throws Throwable{

		String sql="select memanid,memanname from wp..wptcmeman where memanid in(select memanid from trademark..wptlmeman where meid='"+meid+"') and tm='1'";
		String[][] ret_memanid = t.queryFromPool(sql);
		setTableData("memanidTable", ret_memanid);
	}


	/*
		搜尋-指定商品，參數:本所案號	
	*/
	public void selectGoodClass(String meid)throws Throwable{	
	
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
		old搜尋-申請人與代表人，參數:本所案號
	*/
	public void oldselectPamanidAndId(String meid)throws Throwable{	
	//申請人與代表人
	//搜尋申請人編號，lapkey在wptlapman，條件為本所案號,設定在table1
		String sql = "select '',pamanid,'','','','',meid,'','','',lapkey,'' from wptlapman  where meid='" + meid
		      + "' order by pamanid desc";
		String[][]ret = t.queryFromPool(sql);
		sa.append("\n搜尋申請人\n" + sql); // 檢查點		;
		sa.append("\n表格:本所案號，申請人id，lapkey\n" + sql);	     	// 檢查
		if (ret.length > 0) {
			for (int i = 0; i < ret.length; i++) {// 利用申請人編號搜尋申請人名稱，設定在欄位
				sql = "select b.uniChineseName,b.uniOriginName,unioriginaddr,unichineseaddr from wptmapman b WHERE pamanid='" + ret[i][1].trim()
				      + "' order by b.uniChineseName";
				String[][] ret_Name = w.queryFromPool(sql);
				sa.append("\n搜尋代表人\n" + sql); // 檢查點		
				sa.append("\n設定申請人名稱(中),設定申請人名稱(原)\n"+sql);              //檢查
				if (ret_Name.length > 0) {
					ret[i][2] = ret_Name[0][0]; // 設定申請人名稱(中)
					ret[i][3] = ret_Name[0][1]; // 設定申請人名稱(原)
					ret[i][4] = ret_Name[0][2]; // 設定申請人地址(中)
					ret[i][5] = ret_Name[0][3]; // 設定申請人地址(原)
				}
				if (ret[i][10].length() > 0) {
					sql = "select id from wptlapre where lapkey='" + ret[i][10] + "'";
					String[][] ret_id = t.queryFromPool(sql);
					sa.append("\nlapkey\n"+sql);                          //檢查
					if (ret_id.length > 3) {
						ret[i][7] = ret_id[0][0].trim();
						ret[i][8] = ret_id[1][0].trim();
						ret[i][9] = ret_id[2][0].trim();
					} else if (ret_id.length == 2) {
						ret[i][7] = ret_id[0][0].trim();
						ret[i][8] = ret_id[1][0].trim();
					} else if (ret_id.length == 1) {
						ret[i][7] = ret_id[0][0].trim();
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
		String sql="SELECT pamanid,prename1,desctext,goodname1,marktype,markmeid,markname,picchk,keyno FROM wptlproexte WHERE idkey ='"+ idkey+"'";
		String[][]retwptlproexte=t.queryFromPool(sql);
		sa.append("\n搜尋-控制按鈕\n"+sql);
		if(retwptlproexte.length>0){
		//設定欄位
		setValue("ispamanid",retwptlproexte[0][0].trim());    //ispamanid//申請人
		sa.append("\n搜尋控制按鈕\n" + sql); // 檢查點		
		
		}
	}

	/*
		搜尋-附件	
	*/
	public void selectAnnex(String idkey)throws Throwable{		
		
		String sql="select '含件'=case when l.annexid is null then '否' else '是' end ,c.annex_name,isnull(l.description,''),"
			+"c.annexid,c.desc_format,desc_default,c.sort"
			+" from wptcannex c"
			+" left join wptlannex l on l.annexid = c.annexid and l.idkey='"+idkey
			+"' where c.procid ='0701' order by sort";
		String[][]retAnnex=t.queryFromPool(sql);
		sa.append("\n搜尋附件\n" + sql); // 檢查點
		if(retAnnex.length>0){
			setTableData("tableAnnex",retAnnex);
		}
	}		
	/*
		儲存本所代理人
	*/
	public void saveMemanid(String meid)throws Throwable{
		vt=new Vector();
//		sa.append("\n-------本所代理人，儲存------:\n"); // 檢查點
		String[][] ret_table3 = getTableData("memanidTable");
		if (ret_table3.length != 0) {
			String sql = "delete from wptlmeman where meid='" + meid + "'";
			vt.add(sql);
//			sa.append("\n刪除wptlmeman\n" + sql); // 檢查點
			for (int i = 0; i < ret_table3.length; i++) {
				sql = "insert into wptlmeman (meid,memanid) values ('" + meid + "','" + ret_table3[i][0] + "')";
				vt.add(sql);
//				sa.append("\n本所代理人\n" + sql); // 檢查點
			}
		} 
		execData(vt);
	}

	/*
		儲存打勾資料wptlproexte,傳入idkey,meid,rcvid
	*/
	public void saveWptlproexte(String idkey,String meid,String rcvid,String keyno,String str)throws Throwable{		
		sc =new StringBuffer();
		sc.append(str);
		
		//取得資料
		String ispamanid=getValue("ispamanid".trim());//申請人
		if(keyno.length()==10){
			String sql = "update wptlproexte set " + "pamanid='" + ispamanid + "'" + " where keyno='" + keyno + "'";
			vt.add(sql);
		}else{
			String sql = "insert into wptlproexte (meid,pamanid,idkey,rcvid,keyno) values ('"
				  + meid + "','" + ispamanid + "','"+ idkey  + "','" + rcvid + "','aaa')";
			sc.append(sql+"@");
		}
		insertAddKeyno(sc.toString());
	}
	
	/*
		異動資料庫
	*/
	public void execData(Vector vt)throws Throwable{	
	
	String[] vtsql = (String[]) vt.toArray(new String[0]);
	for(int i=0;i<vtsql.length;i++){
		sa.append(vtsql[i]+"\n");	
	}	
	
	try {
		t.execFromPool(vtsql);
		message("異動資料庫成功");
	} catch (Exception e) {
		e.printStackTrace(System.err);
		message("異動資料庫失敗" + e);
		return ;
	}

	}
	
	/*keyno自動編號
	*回傳值是String，若多筆呼叫會自動+1
	*/
	static String keynoStr="";  //key在外是可以累加
	public String getKeyno()throws Throwable {
		String year=operation.sub(getYear(),"1911");//取得民國年
		if(keynoStr.length()==10){
			keynoStr= operation.add(keynoStr,"1");
		} else{
			String sql = "select top 1 keyno from wptlproexte where left(keyno,2)='10' and  len(keyno)='10' order by keyno desc";                //取得keyno處理
			String[][]ret = t.queryFromPool(sql);
			if(ret.length>0) {
				keynoStr=ret[0][0].substring(4);
				keynoStr= operation.add(keynoStr,"1");//字串加1。
				keynoStr=convert.add0(keynoStr,"7");
				keynoStr=year+keynoStr;
			} else {
				keynoStr="1080000001";
			}
		}
		return keynoStr;
	}
	
	
	/*取得西元年
	*匯入import java.text.DateFormat;import java.text.SimpleDateFormat;import java.util.Date;
	*/
	
	public static String getYear()throws Throwable {
		Date date = new Date();
		DateFormat dateformat= new SimpleDateFormat("yyyy");
		String dateTime=dateformat.format(date);
		return dateTime;
	}
	//todo取得日期時間
	
	
	
	
	/*
	 * 將字串轉陣列 找出insert的資料，並加入keyno 重覆的keyno，則呼叫副表式，將資料改成updata，及之前那筆資料的key值。
	 */
	public void insertAddKeyno(String str) throws Throwable{
		vt=new Vector();
		Hashtable<String, String> ht = new Hashtable<String, String>();
		String keyno = "";// keyno的值
		str = str.toLowerCase();
		String[] strArray = str.split("@");
//		insertAddKeyno(strArray);

		for (int i = 0; i < strArray.length; i++) {
			if ((strArray[i].indexOf("insert")) >= 0) {
				int pcNum = strArray[i].indexOf("pc"); // 尋找idkey
				String pcStr = strArray[i].substring(pcNum, pcNum + 12);// 取得IDKEY
//				System.out.println(pcStr);
				sc.append(pcStr+"\n");
				if (ht.get(pcStr) != null) {
					// 取出上一筆的keyno值
					keyno = (String) ht.get(pcStr);
					//若兩筆pc相同，則一筆改為update，並將keyno一致
					String sql = insertToUpdate(keyno, strArray[i]);
					vt.add(sql);
					continue;
				}
				keyno = getKeyno();// 取得keyno getKeyno();
				strArray[i] = strArray[i].replace("aaa", keyno);
				// 放入(idkey,keyno)
				ht.put(pcStr, keyno);
			}
			vt.add(strArray[i]);
		}
		execData(vt);//異動資料庫
	}
	/* 將重覆的insert轉換成update
	 * 
	 */
	public static String insertToUpdate(String keyno, String str) {
		String[] strtemp = str.split(" ");
		String tableName = strtemp[2];
		String where = " where keyno='" + keyno + "'";
		int brackets = str.indexOf("(");
		int brackets2 = str.indexOf(")");
		String str2 = str.substring(brackets + 1, brackets2);// 取前面括號內的內容
		String str3 = str.substring(brackets2 + 1); // 取那面那段
		int brackets3 = str3.indexOf("(");
		int brackets4 = str3.indexOf(")");
		String str4 = str3.substring(brackets3 + 1, brackets4);// 取後面括號內的內容

		String[] fieldname = str2.split(",");
		String[] fieldvalue = str4.split(",");

		String sql = "update " + tableName + " set ";
		StringBuffer sa = new StringBuffer();
		for (int i = 0; i < fieldname.length; i++) {
			if (fieldname[i].equals("keyno")) {
				continue;
			}
			sa.append(fieldname[i] + "=" + fieldvalue[i] + ",");
		}
		sa.setLength(sa.length() - 1);
		sql = sql + sa.toString() + where;

		return sql;
	}
	/*
	附件，儲存

*/

	public void saveAnnex(String idkey) throws Throwable{
	vt=new Vector();
	String[][]tableAnnex=getTableData("tableAnnex");
	String sql="";
	
	//取得keyno值
	sql="select keyno from wptlproexte where idkey='"+idkey+"'";
	String[][]keynoArray=t.queryFromPool(sql);
	String keyno=keynoArray[0][0];
	
	
	boolean isTrue=true;
	for(int i=0; i<tableAnnex.length; i++) {
		if("1".equals(tableAnnex[i][0])) {
			if(isTrue){
			//刪除附件
				sql="delete from wptlannex where keyno='"+keyno+"'";
				vt.add(sql);
				isTrue=false;
			}
			//取得資料
			String sort=tableAnnex[i][6];//排序
			String annexid=tableAnnex[i][3];//附件代碼
			String description=tableAnnex[i][2];//輸入值
	
			sql="insert into wptlannex(keyno,idkey,sort,annexid,description)"
				+" values "
				+"('"+keyno+"','"+idkey+"','"+sort+"','"+annexid+"','"+description+"')";
			vt.add(sql);
			}
	
		}
		execData(vt);//異動資料庫

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
	/*
		儲存-案件程序經手承辦員記錄檔wptlproc_wrkman,若新承辦員則寫入檔
	*/
	public void saveWptlprocWrkman(String idkey,String meid) throws Throwable {
		vt=new Vector();
		talk t = getTalk("TradeMark");
		String mUser = getUser();
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String moddatetime=mdate+" "+mtime;
		
//		String[][]table1=getTableData("table1");
//		for(int i=0; i<table1.length; i++) {
//			String idkey=table1[i][9].trim();
//			String meid=table1[i][2].trim();
			String sql = "select wrkman from wptlproc where idkey='" + idkey + "'"; // 取得承辦員
			String[][] str_wrkman = t.queryFromPool(sql);
			String wrkman = str_wrkman[0][0];
			String jobmark = "";
			if (wrkman.equals(mUser)) { // 由職務代理時
				jobmark = "";
			} else {
				jobmark = "*";
			}			
		
			if(jobmark.length()>0) {
				sql="insert into wptlproc_wrkman(GCkey,idkey,meid,procWrkman,modDateTime,state)"
					+" VALUES ('"+getGcKey()+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(moddatetime)+",'MOD')";
				vt.add(sql);
			}
//		}
		execData(vt);//異動資料庫
	
	}
	
	
	/*存檔時使用，處理日期null的問題
	  在存檔的地方，日期不加''，因為null值旁加''變成'null'，資料庫無法接受。
	*/
	public static String noDateToNull(String date) throws Throwable {
		if(date.length()< 8 || date.indexOf("1900")==0) {
			return null;
		} else {
			date="'"+date+"'";
			return date;
		}
	}
	
	
	/*取得gckey
	*回傳值是String，若多筆呼叫會自動+1
	*/
	String gckey=null;  //key在外是可以累加
	public String getGcKey()throws Throwable {
		talk t=getTalk("TradeMark");
		if(gckey==null) {
			String sql = "select max(gckey) from wptlproc_wrkman";                //取得gckey處理
			String[][]ret = t.queryFromPool(sql);
			gckey= operation.add(ret[0][0].trim(),"1");//字串加1。
		} else {
			gckey= operation.add(gckey,"1");
		}
		if(gckey.length()<12) {
			gckey=convert.add0(gckey,"12");
		}
		return gckey;
	}
	


	/*
		更新程序檔
	*/
	public void saveWptlproc(String appydate,String dbbouns,String rcvno,String idkey)throws Throwable {
		vt=new Vector();
		//取得日期
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String wrkdate = mdate+ " " + mtime; 			   //取得Wrkdate

		String sql="select keyno from wptlproexte where idkey='"+idkey+"'";
		String[][]keynoArray=t.queryFromPool(sql);
		String keyno=keynoArray[0][0].trim();
	
		//更新資料
		sql="UPDATE wptlproc SET "
			+"appydate="+noDateToNull(appydate)
			+",dbbouns='"+dbbouns
			+"',rcvno='"+rcvno
			+"',keyno='"+keyno
			+"',wrkdate='"+wrkdate
			+"' WHERE idkey ='"+idkey+"'";
		vt.add(sql);
		execData(vt);//異動資料庫
	
	}


/*
	更新主檔
	@戴勝台
*/

	public void saveWptmapply(String meid,String exteappydate)throws Throwable{
		vt=new Vector();

		String sql="UPDATE wptmapply SET "
		+"exteappydate='"+exteappydate
		+"'  WHERE meid ='"+meid+"'";
		vt.add(sql);
		execData(vt);//異動資料庫

	}
	


	/*
		0302委託人傳回來的資料，回存到程序檔及主檔。
		@戴勝台
	*/
	
	public void saveWptlprocWptmapply()throws Throwable {
		vt =new Vector();
		String[][]trustorTable=getTableData("trustorTable");
		for(int i=0; i<trustorTable.length; i++) {
	
			//存檔時更新wptlproc的資料
			String sql="UPDATE wptlproc SET "
				+"plsmanid ='"+trustorTable[i][2].trim()+"',"
				+"conname ='"+trustorTable[i][3].trim()+"',"
				+"conEmail ='"+trustorTable[i][4].trim()+"',"
				+"othid ='"+trustorTable[i][5].trim()+"',"
				+"specialtitle ='"+trustorTable[i][6].trim()+"',"
				+"janloc ='"+trustorTable[i][7].trim()+"',"
				+"janid ='"+trustorTable[i][8].trim()+"'"
				+" WHERE IDKey ='"+trustorTable[i][1].trim()+"'";
	
			vt.add(sql);
	
			//存檔時更新wptmapply的資料，當條件是1時。
			if(trustorTable[i][9].equals("1")) {
				sql="UPDATE wptmapply SET "
					+"plsmanid ='"+trustorTable[i][2].trim()+"',"
					+"conname ='"+trustorTable[i][3].trim()+"',"
					+"othid ='"+trustorTable[i][5].trim()+"',"
					+"specialtitle ='"+trustorTable[i][6].trim()+"',"
					+"janloc ='"+trustorTable[i][7].trim()+"',"
					+"janid ='"+trustorTable[i][8].trim()+"'"
					+" WHERE meid ='"+trustorTable[i][0].trim()+"'";
				vt.add(sql);
			}
	
		}
		execData(vt);//異動資料庫
	}
	
	/*
	儲存申請人，代表人,申請人一定要先異動，才能取得lapkey的值
	*/
	public void savePamanid(String meid)throws Throwable {
		vt= new Vector(); // 儲存申請人專用的	
		String ispamanid=getValue("ispamanid".trim());//申請人
		String[][] retPamanidTable = getTableData("pamanidTable");
		String sql="";
		if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// 判斷儲存申請人
			//檢核
			for (int i = 0; i < retPamanidTable.length; i++) {
				// 檢核:判斷申請人編號為空。
				if (retPamanidTable[i][1].length() == 0) {
					message("申請人編號為空");
					return ;
				}
			}
	
			StringBuffer sbPamanid=new StringBuffer();
			for(int i=0; i<retPamanidTable.length; i++) {
				sbPamanid.append("'"+retPamanidTable[i][0]+"',");
			}
			sbPamanid.setLength(sbPamanid.length()-1);
	
			sql="select pamanid from wptlapman where meid='"+meid+"' and pamanid not in("+sbPamanid.toString()+")";
			String[][]ontInPamanid=t.queryFromPool(sql);
			//刪除wptmapman的資料
			if(ontInPamanid.length>0){
			for(int i=0; i<ontInPamanid.length; i++) {
				sql="delete from wptlapman where meid='"+meid+"' and pamanid='"+ontInPamanid[i][0]+"'";
				vt.add(sql);
			}
			}
			//新增wptmapman的資料
			sql="select pamanid from wptlapman where meid='"+meid+"'";
			String[][] newWptlapmanPamanid=t.queryFromPool(sql);
			boolean isTrue=false;			
			if(retPamanidTable.length>0){
				for( int i=0; i<retPamanidTable.length; i++) {
					isTrue=false;
					if(newWptlapmanPamanid.length>0){
						for(int j=0; j<newWptlapmanPamanid.length; j++) {
							if(retPamanidTable[i][0].equals(newWptlapmanPamanid[j][0])) {
								isTrue=true;
								break;
							}
						}
					}
					if(!isTrue){
						sql = "insert into wptlapman (meid,pamanid) values ('" + meid + "','" + retPamanidTable[i][0] + "')";
						vt.add(sql);
					}
				}
			}
		}
		execData(vt);//異動資料庫
	
		//儲存代表人
		vt=new Vector();
		if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// 儲存申請人判斷

			for (int i = 0; i < retPamanidTable.length; i++) {
				sql = "select lapkey from wptlapman where meid='" + meid + "' and pamanid ='" + retPamanidTable[i][0] + "'";
				String[][] ret_lapkey = t.queryFromPool(sql);
					if(ret_lapkey.length>0){
						sql="delete wptlapre where lapkey='"+ret_lapkey[0][0]+"'";
						vt.add(sql);
					    String[][]idTable=(String[][])get(retPamanidTable[i][0],new String[0][0]);
						for(int j=0;j<idTable.length;j++){
						sql = "insert into wptlapre (lapkey,id) values ('"+ret_lapkey[0][0]+"','" + idTable[j][0] + "')";
						vt.add(sql);
					}
					}
					// 儲存代表人，新增wptlapre的lapkey,id的資料
			} // 結束i迴圈
		} // 結束儲存代表人判斷。
	
		execData(vt);//異動資料庫
	}


	/*	備份申請人
		@戴勝台
	*/
	
	public void backupPamanid(String idkey,String meid)throws Throwable {
		vt=new Vector();
		String sql="";
	
		//刪除資料
		sql="DELETE FROM wptlappyOriApman  WHERE idkey ='"+idkey+"'";
		vt.add(sql);	
		
		sql = "select a.pamanid,b.uniChineseName,b.uniOriginName,b.unioriginaddr,b.unichineseaddr,a.lapkey "
					 +" from trademark..wptlapman a left join wp..wptmapman b on a.pamanid=b.pamanid "
					 +" where a.meid='"+meid+"' order by a.pamanid desc";				 
		
		String[][]pamanidTable=t.queryFromPool(sql);
		if(pamanidTable.length>0) {
			for(int i=0;i<pamanidTable.length;i++){
				//取得資料
				String pamanid=pamanidTable[i][0].trim();//讓與人編號
				String cmanname1=pamanidTable[i][1].trim();//讓與人名(中)
				String manname1=pamanidTable[i][2].trim();//讓與人名(原)
				String addr1=pamanidTable[i][3].trim();//讓與人地址(原)
				addr1=addr1.replace("/n","");
				String addr5=pamanidTable[i][4].trim();//讓與人地址(中)
				addr1=addr1.replace("/n","");
				String lapkey =pamanidTable[i][5].trim();//Key連結代表人
				//取得keyno值
				sql="select keyno from wptlproexte where idkey='"+idkey+"'";
				String[][]keynoArray=t.queryFromPool(sql);
				String keyno=keynoArray[0][0];
		

				//新增資料
				sql="INSERT INTO wptlappyOriApman (pamanidCHK,pamanid,cmanname1,manname1,addr1,addr5,lapkey,idkey,keyno)"
					+" values ('1','"+pamanid+"','"+cmanname1+"','"+manname1+"','"+addr1+"','"+addr5+"','"+lapkey+"','"+idkey+"','"+keyno+"')";
				vt.add(sql);
			}
		}
	
		execData(vt);//異動資料庫
	}
	/*
		搜尋-申請人與代表人，參數:本所案號
	*/
	public void selectPamanidAndId(String meid)throws Throwable {
	
		String sql = "select a.pamanid,b.uniChineseName,b.uniOriginName,b.unioriginaddr,b.unichineseaddr,a.meid,a.lapkey "
					 +" from trademark..wptlapman a left join wp..wptmapman b on a.pamanid=b.pamanid "
					 +" where a.meid='"+meid+"' order by a.pamanid desc";
	
		String[][]pamanidTable = t.queryFromPool(sql);
		setTableData("pamanidTable", pamanidTable);
		for(int i=0; i<pamanidTable.length; i++) {
			String lapkey=pamanidTable[i][6].trim();
	
			sql="select b.id,c.uniOriginPrename,c.uniChinesePrename,a.lapkey"
				+" from trademark..wptlapman a "
				+" left join trademark..wptlapre b on a.lapkey = b.lapkey "
				+" left join wp..wptmapre c on c.id=b.id "
				+" where a.lapkey = '"+lapkey+"'"
				+" order by a.pamanid";
				
			setValue("field1",sql);
			String[][]idTable=t.queryFromPool(sql);
			if(idTable.length>0){
				put(pamanidTable[i][0],idTable);
			}else{
				put(pamanidTable[i][0],null);
			}
	}
	
	}
	/*
	備份wptlappyoriapre代表人
	@戴勝台
	*/
	
	public void saveWptlappyOriApman(String meid)throws Throwable {
		vt=new Vector();
		//搜尋代表人資料
		String sql="select a.id,b.unichineseprename,b.unioriginprename,a.lapkey "
			+" from trademark..wptlapre a left join wp..wptmapre b on a.id=b.id "
			+" where lapkey in "
			+" (select lapkey from trademark..wptlapman where  meid='"+meid+"')";
		//刪除舊有的lapkey
		String[][]retLapkey=t.queryFromPool(sql);
		sql="delete from wptlappyoriapre where lapkey in "
			+" (select lapkey from trademark..wptlapman where  meid='"+meid+"')";
		vt.add(sql);
		//新增新的lapkey
		if(retLapkey.length>0) {
			for(int i=0;i<retLapkey.length;i++){
			String id=retLapkey[i][0].trim();
			String unichineseprename=retLapkey[i][1].trim();
			String unioriginprename=retLapkey[i][2].trim();
			String lapkey=retLapkey[i][3].trim();
	
			//新增資料
			sql="INSERT INTO wptlappyoriapre (idchk,id,oname,cname,lapkey) "
				+" values ('1','"+id+"','"+unichineseprename+"','"+unioriginprename+"','"+lapkey+"')";
			
			vt.add(sql);
			}
		}
		execData(vt);//異動資料庫
	}
	
	/*
		備份本所代理人
	*/
	public void backupMemanid(String idkey)throws Throwable{
	
	//刪除資料
	String sql="DELETE FROM wptlappyOriMeman WHERE idkey ='"+ idkey+"'";
	vt.add(sql);
	//取得keyno值
	sql="select keyno from wptlproexte where idkey='"+idkey+"'";
	String[][]keynoArray=t.queryFromPool(sql);
	String keyno=keynoArray[0][0];	
	
	String[][]memanidTable=getTableData("memanidTable");
	for(int i=0;i<memanidTable.length;i++){
		//取得資料
		String memanid=memanidTable[i][0].trim();//本所代理人編號
		String memanname=memanidTable[i][1].trim();//本所代理人名稱
		//新增資料
		sql="INSERT INTO wptlappyOriMeman (idkey,keyno,memanid,memanname,memanidCHK) "
			+" values ('"+idkey+"','"+keyno+"','"+memanid+"','"+memanname+"','1')";
		vt.add(sql);
	}
	execData(vt);
	
	}

	/*
		文本@文本，執行異動
	*/
	public void execString(String str)throws Throwable{
	vt=new Vector();
	String[] strArray = str.split("@");
	for (int i = 0; i < strArray.length; i++) {
		vt.add(strArray[i].trim());
	}
	execData(vt);
	}
	/*
		del程序檔wptlproc
	*/
	public void delWptlproc(String idkey)throws Throwable {
		vt=new Vector();
		//更新資料
		String sql="UPDATE wptlproc SET appydate=null,dbbouns='0',rcvno='',wrkdate=null,keyno='0' WHERE idkey ='"+idkey+"'";
		vt.add(sql);
		execData(vt);//異動資料庫
	}	
	/*
	del 更新主檔
	@戴勝台
	*/

	public void delWptmapply(String meid)throws Throwable{
		vt=new Vector();
		String sql="UPDATE wptmapply SET exteappydate=null WHERE meid ='"+meid+"'";
		vt.add(sql);
		execData(vt);//異動資料庫

	}
	/*
		刪除-案件程序經手承辦員記錄檔
	*/
	public void delWptlprocWrkman(String idkey,String meid) throws Throwable {
		vt=new Vector();
		talk t = getTalk("TradeMark");
		String mUser = getUser();
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String moddatetime=mdate+" "+mtime;
		
		String sql = "select wrkman from wptlproc where idkey='" + idkey + "'"; // 取得承辦員
		String[][] str_wrkman = t.queryFromPool(sql);
		String wrkman = str_wrkman[0][0];
		String jobmark = "";
		if (wrkman.equals(mUser)) { // 由職務代理時
			jobmark = "";
		} else {
			jobmark = "*";
		}			
		if(jobmark.length()>0) {
			sql="insert into wptlproc_wrkman(GCkey,idkey,meid,procWrkman,modDateTime,state)"
				+" VALUES ('"+getGcKey()+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(moddatetime)+",'DEL')";
			vt.add(sql);
		}
		execData(vt);//異動資料庫
	}
	/*
		刪除wptlproexte
	*/
	public void delWptlproexte(String keyno)throws Throwable{		
		vt= new Vector();
		String sql = "delete from wptlproexte where keyno='" + keyno + "'";
		vt.add(sql);
		execData(vt);//異動資料庫
	}
	/*
		備份主檔,商標,描述性說明，使用json
	*/
	public void backupWptmapply(String meid,String idkey)throws Throwable {
		//搜尋資料	
		String sql="SELECT markname,picchk,marktype,markmeid,markappyid,markregid,relchk,exteappydate,cdesctext,desctext "
					+" FROM wptmapply"
					+" WHERE meid ='"+meid+"'";
					
		String[][]retwptmapply=t.queryFromPool(sql);
		String sqlToArray=sqlToString(sql,retwptmapply);   //方法:製作成JSON的檔案格式
	//將JSON的資料儲存起來
		sql="UPDATE wptlproc set bk='"+sqlToArray.toString()+"' where idkey='"+idkey+"'";
		vt.add(sql);
		execData(vt);//異動資料庫			

	}
	
	/*
		傳入sql轉出JSON格式的字串,傳入sql及ret檔。
	*/
	public String sqlToString(String sql,String[][] ret) {
		sql=sql.toLowerCase();
		sql =sql.replace("'","''");  //新增一個'個，這樣才能存入sql
		int numSelect=sql.indexOf("select");
		int numFrom=sql.indexOf("from");
		int numWhere=sql.indexOf("where");
		setValue("field3",ret.length+sql);
		String select=sql.substring(numSelect+6,numFrom).trim();
		String from=sql.substring(numFrom+4,numWhere).trim();
		String where=sql.substring(numWhere+5).trim();
		String[] selectToArray=select.split(",");
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("table",from);
		jsonObject.put("key",where);
	
		JSONArray jsonArray=new JSONArray();
	
		for(int j=0; j<ret.length; j++) {
			JSONObject jsonObjToSelect=new JSONObject();
			for(int i=0; i<selectToArray.length; i++) {
				jsonObjToSelect.put(selectToArray[i],ret[j][i]);
			}
			jsonArray.add(jsonObjToSelect);
		}
	
		jsonObject.put("field",jsonArray);
		return jsonObject.toString();
	}	
	/*
	還原-主檔、商標、描述性說明
	*/
	
	public void revertMark(String idkey,String meid)throws Throwable {
		vt=new Vector();
		String sql="select bk from wptlproc where idkey='"+idkey+"'";
		String[][]bkArray=t.queryFromPool(sql);
		if(bkArray.length>0){
			String bk=bkArray[0][0].trim();
			bk =bk.replace("'","''");  //新增一個'個，這樣才能存入sql
			JSONObject object=JSONObject.fromObject(bk.trim());
			JSONArray field=object.getJSONArray("field");
			JSONObject data=field.getJSONObject(0);
		
			String markname=(String)data.get("markname");//商標名稱
			String picchk=(String)data.get("picchk");//圖樣中不主張專用權
			String marktype=(String)data.get("marktype");//商標種類
			String markmeid=(String)data.get("markmeid");//(正商標)本所案號
			String markappyid=(String)data.get("markappyid");//申請號碼
			String markregid=(String)data.get("markregid");//註冊號
			String relchk=(String)data.get("relchk");
			String exteappydate=(String)data.get("exteappydate");//最後一次延展申請日
			String cdesctext=(String)data.get("cdesctext");//描述性說明(中)
			String desctext=(String)data.get("desctext");//描述性說明
		
			//儲存資料
			sql="UPDATE wptmapply SET markname='"+markname.trim()
				+"',picchk='"+picchk.trim()
				+"',marktype='"+marktype.trim()
				+"',markmeid='"+markmeid.trim()
				+"',markappyid='"+markappyid.trim()
				+"',markregid='"+markregid.trim()
				+"',relchk='"+relchk.trim()
				+"',exteappydate='"+exteappydate.trim()
				+"',cdesctext='"+cdesctext.trim()
				+"',desctext='"+desctext.trim()		   
				+"' WHERE meid ='"+meid.trim()+"'";
			vt.add(sql);
			execData(vt);	
		}

	}
	
}
