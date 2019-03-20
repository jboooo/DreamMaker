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

public class WP0143PT extends bTransaction{
	StringBuffer sa=null;
	talk t=null;
	talk w=null;
	public boolean action(String value)throws Throwable{
		// 回傳值為 true 表示執行接下來的資料庫異動或查詢
		// 回傳值為 false 表示接下來不執行任何指令
		// 傳入值 value 為 "新增","查詢","修改","刪除","列印","PRINT" (列印預覽的列印按鈕),"PRINTALL" (列印預覽的全部列印按鈕) 其中之一
		sa=new StringBuffer();
		t=getTalk("TradeMark");	
		w=getTalk("wp");
		
		if("查詢".equals(value)){
			Query();
		}else if("修改".equals(value)){
			Update();		
		}else if("刪除".equals(value)){
			DELETE();
		}
		return false;
	}
	public boolean	Query()throws Throwable{
		
		String meid="";
		String idkey=getQueryValue("idkey");
		String findate="";
		String sql="select debitchk,debitid,ltdate,debitdate,meid,rcvid,findate,appydate,Idkey from wptlproc where idkey='"+idkey+"'";
		String[][]retWptlproc=t.queryFromPool(sql);
		sa.append("\ndebitchk,debitid,ltdate,debitdate,meid,rcvid,findate,appydate,idkey\n"+sql);//檢查
		if(retWptlproc.length>0){
			setValue("debitchk",retWptlproc[0][0].trim());
			setValue("debitid",retWptlproc[0][1].trim());
			setValue("ltdate",dateformat(retWptlproc[0][2].trim()));
			setValue("debitdate",dateformat(retWptlproc[0][3].trim()));
			setValue("meid",retWptlproc[0][4].trim());
			setValue("rcvid",retWptlproc[0][5].trim());
			setValue("findate",dateformat(retWptlproc[0][6].trim()));
			setValue("appydate",dateformat(retWptlproc[0][7].trim()));
			setValue("idkey",retWptlproc[0][8].trim());
			meid=retWptlproc[0][4].trim();
			idkey=retWptlproc[0][8].trim();
			findate=dateformat(retWptlproc[0][6].trim());
		}
		
		sql="select endcause,giveupchk,AgreeRegNo,AgreeRegistrant,chkdoc,Ownedate,Ownsdate,Regid,Appydate,Regdate,Extesay,Extecanceldate,Saydate from wptmapply where meid='"+meid+"'";
		String[][]retWptmapply=t.queryFromPool(sql);
		sa.append("\nendcause,giveupchk,AgreeRegNo,AgreeRegistrant,chkdoc,Ownedate,Ownsdate,Regid,Appydate,Regdate,Extesay,Extecanceldate\n"+sql);//檢查
		if(retWptmapply.length>0){
			setValue("endcause",retWptmapply[0][0].trim());
			setValue("giveupchk",retWptmapply[0][1].trim());
			setValue("AgreeRegNo",retWptmapply[0][2].trim());
			setValue("AgreeRegistrant",retWptmapply[0][3].trim());
			setValue("chkdoc",retWptmapply[0][4].trim());
			setValue("Ownedate",dateformat(retWptmapply[0][5].trim()));
			setValue("Ownsdate",dateformat(retWptmapply[0][6].trim()));
			setValue("Regid",retWptmapply[0][7].trim());
			setValue("Appydate",dateformat(retWptmapply[0][8].trim()));
			setValue("Regdate",dateformat(retWptmapply[0][9].trim()));
			setValue("Extesay",retWptmapply[0][10].trim());
			setValue("Extecanceldate",dateformat(retWptmapply[0][11].trim()));
			setValue("Saydate",dateformat(retWptmapply[0][12].trim()));			
		}

		//memo
		sql="select p.rcvid,p.memo,p.procid+'--'+c.procname,p.idkey,'0' as xx" 
		   +" from wptlproc p"
		   +" left join wptcproc c on p.procid = c.procid"
		   +" where meid='"+meid+"' and idkey = '"+idkey+"'"
		   +" union "
		   +" select p.rcvid,p.memo,p.procid+'--'+c.procname,p.idkey,'1' as xx" 
		   +" from wptlproc p "
		   +" left join wptcproc c on p.procid = c.procid "
		   +" where meid='"+meid+"' and idkey <> '"+idkey+"'"
		   +" and len(replace(memo,' ',''))>0 "
		   +" order by xx" ;
		String[][]ret_memo=t.queryFromPool(sql);
		sa.append("\nmemo:\n"+sql);                           //檢查
		setTableData("memoTable",ret_memo);	
		
		//申請人與代表人
		//搜尋申請人編號，lapkey在wptlapman，
		sql = "select '',meid,pamanid,'','','','','',lapkey from wptlapman  where meid='" + meid
		      + "' order by pamanid desc";
		String[][] ret = t.queryFromPool(sql);
		sa.append("\n表格:本所案號，申請人id，lapkey\n" + sql);	     	// 檢查
		if (ret.length > 0) {
			for (int i = 0; i < ret.length; i++) {// 利用申請人編號搜尋申請人名稱，設定在欄位
				sql = "select b.uniChineseName,b.uniOriginName from wptmapman b WHERE pamanid='" + ret[i][2].trim()
				      + "' order by b.uniChineseName";
				String[][] ret_Name = w.queryFromPool(sql);
				sa.append("\n設定申請人名稱(中),設定申請人名稱(原)\n"+sql);              //檢查
				if (ret_Name.length > 0) {
					ret[i][3] = ret_Name[0][0]; // 設定申請人名稱(中)
					ret[i][4] = ret_Name[0][1]; // 設定申請人名稱(原)
				}
				if (ret[i][8].length() > 0) {
					sql = "select id from wptlapre where lapkey='" + ret[i][8] + "'";
					String[][] ret_id = t.queryFromPool(sql);
					sa.append("\nlapkey\n"+sql);                          //檢查
					if (ret_id.length == 3) {
						ret[i][5] = ret_id[0][0].trim();
						ret[i][6] = ret_id[1][0].trim();
						ret[i][7] = ret_id[2][0].trim();
					} else if (ret_id.length == 2) {
						ret[i][5] = ret_id[0][0].trim();
						ret[i][6] = ret_id[1][0].trim();
					} else if (ret_id.length == 1) {
						ret[i][5] = ret_id[0][0].trim();
					}
				}

			}
			setTableData("pamanidTable", ret);
		}
		setVisible("idTable", false);

		
		//搜尋本所代理人(多筆)，來自wptlmeman,條件是本所案號
		sql = "select memanid from wptlmeman where meid='" + meid + "'";
		String[][] ret_memanid = t.queryFromPool(sql);
		sa.append("\n代理人id\n" + sql);	                 // 檢查	
		StringBuffer sb = new StringBuffer();
		if (ret_memanid.length > 0) {
			for (int i = 0; i < ret_memanid.length; i++) {
				sb.append("'" + ret_memanid[i][0] + "',");
			}
			sb.setLength(sb.length() - 1);
			sql = "select memanid,memanname from wptcmeman where memanid in(" + sb.toString() + ") and tm='1'";
			sa.append("\n代理人id及姓名\n" + sql);	                 // 檢查	
			String[][] ret_memanname = w.queryFromPool(sql);
			if (ret_memanname.length > 0) {
				setTableData("memanidTable", ret_memanname);
			}
		} // 搜尋代理人if結束
		
		//商標
		sql = "SELECT Cdesctext,desctext,markname,markpic,marktype,picchk,agreeregistrant,agreeregno FROM wptmapply where meid='"
		      + meid + "'";
		ret = t.queryFromPool(sql);
			sa.append("\n中文描述性說明,描述性說明,商標名稱,商標圖案path,商標種類,圖樣中不主張專利權,同意書商標權人,同意書註冊號數\n" + sql);	  // 檢查			
		if (ret.length > 0) {
			setValue("markname", ret[0][2]); // 商標名稱
			setValue("markpic", ret[0][3]); // 商標圖案path
			setValue("marktype", ret[0][4]); // 商標種類
			setValue("picchk", ret[0][5]); // 圖樣中不主張專利權
			setValue("agreeregistrant", ret[0][6]); // 同意書商標權人
			setValue("agreeregno", ret[0][7]); // 同意書註冊號數
		//描述性說明
			setValue("Cdesctext", ret[0][0]); // 中文描述性說明
			setValue("desctext", ret[0][1]); // 描述性說明
		}		
		
		//指定商品
		sql = "Select Goodclass,uniNOrigdname,uniNChgdname,delmk,'','',Goodclass,uniNOrigdname,uniNChgdname from wptlapbale where meid='"
		      + meid + "' and delmk<>1";
		String[][] ret_goodclass = t.queryFromPool(sql);
		// 檢查
		sa.append("\n搜尋wptlapbale,指定商品\n" + sql);
		if (ret_goodclass.length > 0) {
			setTableData("goodclassTable", ret_goodclass);
			// 設定第一筆資料到畫面上
			setValue("checkclass", ret_goodclass[0][0]); // 用來記錄類別代號，存檔的時後使用的。
			setValue("goodclass_field", ret_goodclass[0][0]);
			setValue("uniNOrigdname", ret_goodclass[0][1]);
			setValue("uniNChgdname", ret_goodclass[0][2]);
		}
	
		// 搜尋查核欄位
		sql = "select pamanid,desctext,goodname1,goodname2,markname from wptladjchgaft where idkey='" + idkey+ "'";
		String[][] ret_check = t.queryFromPool(sql);
		sa.append("\n搜尋查核欄位\n" + sql);                       //檢查
		if (ret_check.length != 0) {
			setValue("ispamanid", ret_check[0][0]); // 申請人
			setValue("isdesctext", ret_check[0][1]); //描述性說明
			setValue("goodname1", ret_check[0][2]); //指定商品1
			setValue("goodname2", ret_check[0][3]); //指定商品2
			setValue("ismarkname", ret_check[0][4]); //商標
		}
		
//		//還原bk內的資料
		String[][]retWptlprocProcid=null;
//		sql="select bk from wptlproc where idkey='"+idkey+"'";
//		String[][] retBk=t.queryFromPool(sql);
//		StringBuffer sbIdkey=new StringBuffer();
//		if(retBk.length>0) {
//			sa.append("\nBK\n"+retBk[0][0]);
//			JSONArray array=JSONArray.fromObject(retBk[0][0].trim());
//			JSONObject jsonObject=array.getJSONObject(0);
//			JSONArray field=jsonObject.getJSONArray("field");
//			for(int i=0; i<field.size(); i++) {
//				JSONObject data=field.getJSONObject(i);
//				String jsIdkey=(String)data.get("idkey");
//				sbIdkey.append("'"+jsIdkey+"',");
//			}
//			sbIdkey.setLength(sbIdkey.length()-1);
//			sql="select '',idkey,findate,wrkdate,wrkman,meid,lawdate,procname,empname"
//				+" from wptlproc PL left join wptcproc PC on PL.procid=PC.procid left join wp..wptmemp E on PL.wrkman=E.empid "
//				+" where idkey in ("+sbIdkey.toString()+")";
//			retWptlprocProcid=t.queryFromPool(sql);
//			sa.append("\n處理中程序還原BK，sql\n"+sql);
//			for(int i=0; i<retWptlprocProcid.length; i++) {  //鎖住內容，不能修改。
//				retWptlprocProcid[i][0]="1";
//				setEditable("procidTable",i,0,false);
//			}
//		} else { 
			//wptlproc程序檔選擇完成。
			sql="usp_relparent '"+meid+"'";
			String[][]meidIn=t.queryFromPool(sql);
		
			sql="select '',idkey,findate,wrkdate,wrkman,meid,lawdate,procname,empname"
				+" from wptlproc PL left join wptcproc PC on PL.procid=PC.procid left join wp..wptmemp E on PL.wrkman=E.empid "
				+" where meid in ('"+meidIn[0][0]+"') and findate is null and len(replace(meid,' ',''))>0";
		
			retWptlprocProcid=t.queryFromPool(sql);
			sa.append("\n處理中程序sql\n"+sql);
			if(retWptlprocProcid.length>0) {
				for(int i=0; i<retWptlprocProcid.length; i++) {
					if(retWptlprocProcid[i][1].equals(idkey)) {
						retWptlprocProcid[i][0]="1";
						setEditable("procidTable",i,0,false);  //TODO
					}
				}
			}
		
//		}
		
		setTableData("procidTable",retWptlprocProcid);

	
		//設定要鎖的欄位
		if(!findateToOpen(findate)) {
			setEditableField();	//鎖全部
		} else {
			setEditableField();	//鎖全部
			//不鎖時會打開的
			setEditable("memoTable",true);
			setEditable("chkdoc",true);
			setEditable("appydate",true);
			setEditable("agree",true);
		
			// memo是否可以寫入
			if (ret_memo.length > 0) {
				for (int i = 0; i < ret_memo.length; i++) {
					if (!ret_memo[i][3].equals(idkey)) {
						setEditable("memoTable", i, 1, false);
					}
				}
			}
		
			// 搜尋查核欄位
			if (ret_check.length > 0) {
				setEditable("memanidTable", true);          //代理人
				if (ret_check[0][0].equals("1")) {                              // 申請人
					setEditable("ispamanid", false);
					setEditable("pamanidTable", "pamanid", true);
					addScript("EMC['pamanidTable'].getButton(0).setEditable(true);");// 新增,開啟
					addScript("EMC['pamanidTable'].getButton(2).setEditable(true);");// 刪除,開啟
					setEditable("pamanidTable", "btn", true);
				}else{
					setEditable("ispamanid", true);				
				}

				if (ret_check[0][1].equals("1")) {                             //描述性說明
					setEditable("Cdesctext", true);
					setEditable("desctext", true);
				}else{
					setEditable("isdesctext", true);
				}
				if (ret_check[0][2].equals("1")) {                                 //指定商品1
					setEditable("goodclass_field", true);
				}else{
					setEditable("goodname1", true);
				}
				if (ret_check[0][3].equals("1")) {                                 //指定商品2

					setEditable("uniNOrigdname", true);
					setEditable("uniNChgdname", true);
				}else{
					setEditable("goodname2", true);
				}
				if (ret_check[0][4].equals("1")) {                               //商標
					setEditable("agreeregistrant", true);// 併入商標權人
					setEditable("agreeregno", true);// 併在註冊號
				}else{
					setEditable("ismarkname", true);// 併在註冊號
				
				}
			}else{
				setEditable("memanidTable", true);          //代理人
				setEditable("ispamanid", true);				
				setEditable("isdesctext", true);
				setEditable("goodclass_field", true);
				setEditable("goodname1", true);
				setEditable("goodname2", true);
				setEditable("ismarkname", true);// 併在註冊號
				setVisible("idTable", true);

				}			

			}
		
		setValue("field1",sa.toString());	
		return true;
		
	}
	
	public boolean Update() throws Throwable{
		Vector vt=new Vector();
		String sql="";		
		String idkey=getValue("idkey").trim();
		String rcvid=getValue("rcvid").trim();
		
		Date date = new Date();  //取得工作日期
        DateFormat dateformat= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String wrkdate=dateformat.format(date);
		String mUser=getUser();		//取得操作者		
		
		sql="select wrkman from wptlproc where idkey='"+idkey+"'";//取得承辦員
		String[][]str_wrkman=t.queryFromPool(sql);
		String wrkman=((str_wrkman.length>0)?str_wrkman[0][0]:"");

		String jobmark=((wrkman.equals(mUser))?"":"*");		//由職務代理時
		String appydate=getValue("appydate").trim();//更正提出日

		//商標主檔修改
		String chkok=getValue("chkok").trim();
		String Auditid=getValue("Regid").trim();//存註冊號碼
		String Auditdate=getValue("Regdate").trim(); //審定公告日，存註冊公告日
		String Regdate=getValue("Regdate").trim();//註冊公告日
		String Regid=getValue("Regid").trim(); //註冊號碼
		String Ownsdate=getValue("Ownsdate").trim();//專用起始日
		String Ownedate=getValue("Ownedate").trim();//專用滿期日
		String Regstat= (appydate.equals("")?"V":(chkok.equals("1")?"V":"A"));//證書狀態
		String Chkdoc=getValue("Chkdoc").trim();//指示要證書但不延展
		String AgreeRegNo=getValue("AgreeRegNo").trim();//註冊號數
		String meid=getValue("meid").trim();//本所案號
		String Extesay="2";   //不通知延展
		String Saydate=getValue("Saydate").trim();//指示日期
		
		if(Chkdoc.equals("1") && Saydate.equals("")){
			message("指示要證書但不延展有勾選，指示日期必須要有值");
			return false;
		}
		
		
		sql="update wptmapply set"
		+" Auditid='"+Auditid
		+"',Auditdate="+noDateToNull(Auditdate)
		+",Regdate="+noDateToNull(Regdate)
		+",Regid='"+Regid
		+"',Ownsdate="+noDateToNull(Ownsdate)
		+",Ownedate="+noDateToNull(Ownedate)
		+",Regstat='"+Regstat
		+"',Saydate="+noDateToNull(Saydate)
		+",Chkdoc='"+Chkdoc
		+"',AgreeRegNo='"+AgreeRegNo
		+"',Extesay='"+Extesay     
		+"' where meid='"+meid+"'";		
				
		vt.add(sql);
		sa.append("\nwptmapply:\n"+sql);
		
		//修改案件程序記錄檔
		String Findate=("".equals(appydate)?wrkdate:"");//本程序完成日
		String[][]ret_memo=getTableData("memoTable");//備註
		String memo="";
		for(int i=0;i<ret_memo.length;i++){
			if(idkey.equals(ret_memo[i][3]))	{
				memo=ret_memo[i][1].trim();
				break;
			}	
		}
		setValue("field1",Findate);
		sql="update Wptlproc set"
			+" appydate="+noDateToNull(appydate)
			+",jobmark='"+jobmark
			+"',Findate="+noDateToNull(Findate)
			+",wrkdate="+noDateToNull(wrkdate)
			+",memo='"+memo
			+"' where idkey='"+idkey+"'";		
	
		sa.append(sql);		
		
		//更新委託人表格。
		String[][]retTrustortable=getTableData("trustorTable");
		if(retTrustortable.length>0){
			for(int i=0; i<retTrustortable.length; i++) {
				//更新wptlproc委託人的資料
				sql="UPDATE wptlproc SET "
					+"plsmanid ='"+retTrustortable[i][2].trim()+"',"
					+"conname ='"+retTrustortable[i][3].trim()+"',"
					+"conEmail ='"+retTrustortable[i][4].trim()+"',"
					+"othid ='"+retTrustortable[i][5].trim()+"',"
					+"specialtitle ='"+retTrustortable[i][6].trim()+"',"
					+"janloc ='"+retTrustortable[i][7].trim()+"',"
					+"janid ='"+retTrustortable[i][8].trim()+"'"
					+" WHERE IDKey ='"+retTrustortable[i][1].trim()+"'";
		
				vt.add(sql);
				sa.append("\n----委託人資料---\n存檔時更新wptlproc的資料:\n"+sql);	        //檢查
				
				//更新wptmapply委託人的資料，當條件是1時。
				if(retTrustortable[i][9].equals("1")) {
					sql="UPDATE wptmapply SET "
						+"plsmanid ='"+retTrustortable[i][2].trim()+"',"
						+"conname ='"+retTrustortable[i][3].trim()+"',"
						+"othid ='"+retTrustortable[i][5].trim()+"',"
						+"specialtitle ='"+retTrustortable[i][6].trim()+"',"
						+"janloc ='"+retTrustortable[i][7].trim()+"',"
						+"janid ='"+retTrustortable[i][8].trim()+"'"
						+" WHERE meid ='"+retTrustortable[i][0].trim()+"'";
					vt.add(sql);
					sa.append("\n更新wptmapply委託人的資料，當條件是1時:\n"+sql+"\n");	        //檢查
				}
			}
		}
		//修改申請中代碼為010G或是010F，將申請中的程序改為已註冊。
		sql="Update wptlproc set"
			+" procid='"+("A".equals(Regstat)?"010F":"010G")//TODO
			+"' where idkey=(Select ing_idKey from Wptlproposal where meid='"+meid+"')";
		sa.append("\n修改申請中代碼為010G或是010F，將申請中的程序改為已註冊。\n"+sql);
		
	//儲存打勾資料wptladjchgaft
		sa.append("\n-------儲存打勾資料wptladjchgaft，儲存------:\n"); // 檢查點
		String ispamanid = getValue("ispamanid").trim(); // 是申請人代表人
		String isdesctext = getValue("isdesctext").trim();
		String goodname1 = getValue("goodname1").trim();
		String goodname2 = getValue("goodname2").trim();
		String ismarkname = getValue("ismarkname").trim();
	
		sql = "select * from wptladjchgaft where idkey='" + idkey + "'";
		String[][] ret_wptladjchgaft = t.queryFromPool(sql);
		sa.append("\nwptladjchgaft裡是否有資料?\n" + (ret_wptladjchgaft.length != 0)); // 檢查點
		if (ret_wptladjchgaft.length != 0) {
			sql = "update wptladjchgaft set " + "pamanid='" + ispamanid + "',"
				  + "desctext='" + isdesctext + "'," + "goodname1='" + goodname1 + "'," + "goodname2='" + goodname2
				  + "'," + "markname='" + ismarkname + "'" + " where rcvid='" + rcvid + "'";
			vt.add(sql);
			sa.append("\n更新wptladjchgaft:\n" + sql); // 檢查點
		} else {
			sql = "insert into wptladjchgaft (meid,pamanid,idkey,desctext,goodname1,goodname2,markname,rcvid) values ('"
				  + meid + "','" + ispamanid + "','" +idkey + "','" + isdesctext + "','"
				  + goodname1 + "','" + goodname2 + "','" + ismarkname + "','" + rcvid + "')";
			vt.add(sql);
			sa.append("\n新增wptladjchgaft:\n" + sql); // 檢查點
		}
	
	//案件程序經手承辦員紀錄檔:
		//案件程序經手承辦員記錄檔,若新承辦員則寫入檔
		if(jobmark.length()>0) {
			sql="insert into wptlproc_wrkman(GCkey,IDkey,meid,procWrkman,modDateTime,state)"
				+" VALUES ('"+getGcKey()+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(wrkdate)+",'MOD')";
			sa.append("\n案件程序經手承辦員記錄檔,若新承辦員則寫入檔\n"+sql);
			vt.add(sql);
		}

	//修改處理中程序:
	Vector vtBK=new Vector();
	String[][]retTable3=getTableData("procidTable");
	for(int i=0; i<retTable3.length; i++) {
		if("1".equals(retTable3[i][0].trim())) {
			sql="update wptlproc set"
				+" findate = "+noDateToNull(wrkdate)
				+", wrkdate ="+noDateToNull(wrkdate)
				+", wrkman='"+mUser.trim()
				+"' where idkey='"+retTable3[i][1].trim() +"'";
			vt.add(sql);
			vtBK.add(new String[] {retTable3[i][1],retTable3[i][2],retTable3[i][3],retTable3[i][4].trim()});
			sa.append("\n修改處理中程序:sql:\n"+sql);
		}
	}
	String[][]retBK=(String[][])vtBK.toArray(new String[0][0]);
	String sqlToArray=sqlToString("select idkey,findate,wrkdate,wrkman from wptlproc where ",retBK);   //方法:製作成JSON的檔案格式
	sa.append("\n用JSON轉成文字檔:\n"+sqlToArray);                        //檢查
	
	//將JSON的資料儲存起來
	sql="UPDATE wptlproc set bk='"+sqlToArray+"' where idkey='"+idkey+"'";
	vt.add(sql);
	sa.append("\n將文字檔儲存至wptlproc，key為idkey:\n"+sql);                        //檢查	
		
	setValue("field1",sa.toString());	
	
	String[] vtSql=(String[])vt.toArray(new String[0]);
	try{
		t.execFromPool(vtSql);
		message("更新成功");
	}catch(Exception e){
		message("更新失敗"+e);
	}

		return true;
	}
	
	public void DELETE()throws Throwable {                          //刪除
	setValue("field1","hello");
	Vector vt=new Vector();

	//取得日期及使用者
	String mdate = datetime.getToday("YYYY/mm/dd");
	String mtime = datetime.getTime("h:m:s");
	String wrkdate = mdate+ " " + mtime; 			   //取得Wrkdate
	String mUser=getUser();  //取得系統操作者
	String idkey=getValue("idkey");
	String meid=getValue("meid");
	String gckey=getGcKey();

	String sql="insert into wptlproc_wrkman(gckey,idkey,meid,procWrkman,modDateTime,state)"
	           +" VALUES ('"+gckey+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(wrkdate)+",'DEL')";
	vt.add(sql);
	sa.append("\n 新增DEL,wptlproc_wrkman :\n"+sql);                        //檢查點
	setValue("field1",sa.toString());                       //檢查
	//還原bk內的資料
	sql="select bk from wptlproc where idkey='"+idkey+"'";
	sa.append("\nbk:\n"+sql);
	setValue("field1",sa.toString());                       //檢查
	String[][] retBk=t.queryFromPool(sql);
	if(retBk.length>0) {
		sa.append("\nBK\n"+retBk[0][0]);
		setValue("field1",sa.toString());                       //檢查
//		JSONArray array=JSONArray.fromObject(retBk[0][0].trim());
//		sa.append("\narray:"+array.toString());
//		setValue("field1",sa.toString());                       //檢查		
		JSONObject jsonObject=JSONObject.fromObject(retBk[0][0].trim());
		sa.append("\njsonObject:"+jsonObject.toString());
		setValue("field1",sa.toString());                       //檢查		
		String jsTable=(String)jsonObject.get("table");
		sa.append("\ntable:"+jsTable);
		setValue("field1",sa.toString());                       //檢查
		JSONArray field=jsonObject.getJSONArray("field");
		sa.append("\nfield:"+field.toString());
		setValue("field1",sa.toString());                       //檢查
		for(int i=0; i<field.size(); i++) {
			JSONObject data=field.getJSONObject(i);
			String jsIdkey=(String)data.get("idkey");
			String jsFndate=(String)data.get("findate");
			String jsWrkdate=(String)data.get("wrkdate");
			String JsWrkman=(String)data.get("wrkman");
			sql= "update "+jsTable+" set findate="+noDateToNull(jsFndate)+",wrkdate="+noDateToNull(jsWrkdate)+",wrkman='"+JsWrkman+"' where idkey='"+jsIdkey+"'";
			vt.add(sql);
			sa.append("\n回存wptlproc:"+sql);
			setValue("field1",sa.toString());                       //檢查
		}
	}
	setValue("field1",sa.toString());                       //檢查
	sql="update wptlproc set jobmark='',findate=null,wrkdate="+noDateToNull(mdate)+" where idkey='"+idkey+"'";
	vt.add(sql);
	sa.append("\n更新wptlproc:"+sql);

	sql="update wptmapply set Auditid='',Regdate=null,Regid='',Ownsdate=null"
	    +",Ownedate=null,Regstat='',Chkdoc='',AgreeRegNo='',Extesay='' where meid='"+meid+"'";
	vt.add(sql);
	sa.append("\n刪除wptmapply資料:\n"+sql);
	
	setValue("field1",sa.toString());                       //檢查
	String[] vtSql=(String[])vt.toArray(new String[0]);


	try {
		t.execFromPool(vtSql);
		message("更新成功");
	} catch(Exception e) {
		message("更新失敗"+e);
	}
	return ;
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
	/*從資料庫中取得所有欄位的資料，並進行禁改。
	
	*/
	public void setEditableField()throws Throwable{
	
		String sql="select CompoName from wptmFormComp where FormName='"+getFunctionName().trim()+"'";
		String[][]abc=t.queryFromPool(sql);
		
		for(int i=0;i<abc.length;i++){
			setEditable(abc[i][0],false);
		}

		return ;
	}
	
	/*findateToOpen findate開放修改欄的條件
	  import java.text.DateFormat;  import java.text.SimpleDateFormat;  import java.util.Date;
	  日期長度小於8，true
	  日期等於1900-01-01 00:00:00.0, true
	  日期大於今天，true
	*/
	public boolean findateToOpen(String findate) throws Throwable {

		if (findate.length() < 8 || findate.equals("1900-01-01 00:00:00.0")) {
			return true;
		}

		//取得今天日期
		Date date = new Date();
		DateFormat dateformat= new SimpleDateFormat("yyyy/MM/dd");
		String today=dateformat.format(date);
		Date dateToday=dateformat.parse(today);
		//將findate格式化
		Date finDate=dateformat.parse(findate);
		//比較日期，看誰在後面。

		return finDate.after(dateToday);
	
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
	/*存檔時使用，處理日期null的問題
	  在存檔的地方，日期不加''，因為null值旁加''變成'null'，資料庫無法接受。
	  回傳前還會去除前後空白
	*/
	public static String noDateToNull(String date) throws Throwable{
		if(date.length()<8 || date.indexOf("1900")==0){
			return null;
		}else {
		date="'"+date.trim()+"'";
		return date;
		}
	}	
	
	
	//傳入sql轉出JSON格式的字串,傳入sql及ret檔。
	public static String sqlToString(String sql,String[][] ret) {
		sql =sql.replace("'","");  //去除'，因為要儲存在sql時，sql無法接受。
		int numSelect=sql.indexOf("select");
		int numFrom=sql.indexOf("from");
		int numWhere=sql.indexOf("where");
//		System.out.println(numSelect);
//		System.out.println(numFrom);
//		System.out.println(numWhere);
		String select=sql.substring(numSelect+6,numFrom).trim();
		System.out.println(select);
		String from=sql.substring(numFrom+4,numWhere).trim();
		System.out.println(from);		
		String where=sql.substring(numWhere+5).trim();
		String[] selectToArray=select.split(",");
		JSONObject jsonObject=new JSONObject();
		jsonObject.put("table",from);
		jsonObject.put("key",where);		
		
		JSONArray jsonArray=new JSONArray();

		for(int j=0;j<ret.length;j++) {
			JSONObject jsonObjToSelect=new JSONObject();
			for(int i=0;i<selectToArray.length;i++) {
				jsonObjToSelect.put(selectToArray[i],ret[j][i]);
			}
			jsonArray.add(jsonObjToSelect);	
		}

		jsonObject.put("field",jsonArray);
		return jsonObject.toString(); 
	}
	
	
	
	
}
