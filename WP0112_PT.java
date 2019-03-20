package dai;
import jcx.jform.bTransaction;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;

public class WP0112_PT extends bTransaction{
	StringBuffer sa=null;
	talk t=null;
	public boolean action(String value)throws Throwable{
		// 回傳值為 true 表示執行接下來的資料庫異動或查詢
		// 回傳值為 false 表示接下來不執行任何指令
		// 傳入值 value 為 "新增","查詢","修改","刪除","列印","PRINT" (列印預覽的列印按鈕),"PRINTALL" (列印預覽的全部列印按鈕) 其中之一
		sa=new StringBuffer();
		t=getTalk("TradeMark");
		 if("查詢".equals(value)){
		    QUERY();
		 } 
		 else if ("修改".equals(value)){
		    UPDATE();
		 }
		 else if("刪除".equals(value)){
		 DELETE();
		 }
		return false;
	}
	
	
	public void QUERY()throws Throwable{           //查詢

	String idkey=getQueryValue("idkey");
	//檢核，沒有資料就跳出。
	if(idkey.length()==0){message("沒有資料");return ;}	
	
	String sql="Select rcvid,meid,lawdate,dbbouns,appydate,debitchk,ltdate,debitdate,debitid,idkey from wptlproc where idkey='"+idkey+"'";
	String[][]ret_wptlproc=t.queryFromPool(sql);
	sa.append("\nsql:\n"+sql);                            //檢查
	if(ret_wptlproc.length==0){ 
		message("此筆idkey找不到資料，請換筆idkey輸入");	
	}
	
	String rcvid=ret_wptlproc[0][0].trim();//	收文號
	String meid=ret_wptlproc[0][1].trim();//		本所案號
	String lawdate=ret_wptlproc[0][2].trim();//		繳費期限
	String dbbouns=ret_wptlproc[0][3].trim();//		寬限期內繳費
	String appydate=ret_wptlproc[0][4].trim();//		繳費日期
	String debitchk=ret_wptlproc[0][5].trim();//		是否請款
	String ltdate=ret_wptlproc[0][6].trim();//		書信日期
	String debitdate=ret_wptlproc[0][7].trim();//		請款日期
	String debitid=ret_wptlproc[0][8].trim();//		請款單號
	idkey=ret_wptlproc[0][9].trim();//	idkey	

	setValue("rcvid",rcvid);
	setValue("meid",meid);
	setValue("lawdate",dateformat(lawdate));
	setValue("dbbouns",dbbouns);
	setValue("appydate",dateformat(appydate));
	setValue("debitchk",debitchk);
	setValue("ltdate",dateformat(ltdate));
	setValue("debitdate",dateformat(debitdate));
	setValue("debitid",debitid);
	setValue("idkey",idkey);
	setEditable("rcvid",false);
	setEditable("meid",false);
	setEditable("lawdate",false);
	setEditable("dbbouns",true);
	setEditable("appydate",true);
	setEditable("debitchk",false);
	setEditable("ltdate",false);
	setEditable("debitdate",false);
	setEditable("debitid",false);
	setEditable("idkey",false);
	

	if(meid==null || meid.length()==0){ 
		message("此筆meid是空的或是不存在");	
	}

	sql="Select giveupdate,giveupchk,cneextegiveup,endcause,enddate,appyid from wptmapply where meid='"+meid+"'";
	String[][]ret_wptmapply=t.queryFromPool(sql);
	sa.append("\nsql:\n"+sql);                            //檢查
	if(ret_wptmapply.length>0){
	String giveupdate=ret_wptmapply[0][0].trim(); //	指示放置日期
	String giveupchk=ret_wptmapply[0][1].trim(); //		新案放置
	String cneextegiveup=ret_wptmapply[0][2].trim(); //		取消指示放置日
	String endcause=ret_wptmapply[0][3].trim(); //		結案原因
	String enddate=ret_wptmapply[0][4].trim(); //		結案日期
	String appyid=ret_wptmapply[0][5].trim(); //		申請號碼

	setValue("giveupdate",dateformat(giveupdate));
	setValue("giveupchk",giveupchk);
	setValue("cneextegiveup",dateformat(cneextegiveup));
	setValue("endcause",endcause);
	setValue("enddate",dateformat(enddate));
	setValue("appyid",appyid);
	
	setEditable("giveupdate",false);
	setEditable("giveupchk",false);
	setEditable("cneextegiveup",true);
	setEditable("endcause",false);
	setEditable("enddate",false);
	setEditable("appyid",false);
	
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
	setTableData("table1",ret_memo);	
	//memo只開放符合idkey的那一筆。
	if(ret_memo.length>0){
		 for(int i=0;i<ret_memo.length;i++){
			if(!ret_memo[i][3].equals(idkey)){
				setEditable("table1",i,1, false);
			}
		}
	}	
	setValue("field1",sa.toString());
	}
	
	
	public void UPDATE()throws Throwable{                //儲存

	Vector vt=new Vector();
	StringBuffer sb=new StringBuffer();
	String dbbouns=getValue("dbbouns");
	String appydate=getValue("appydate");
	String idkey=getValue("idkey");
	String[][]ret=getTableData("table1");
	String memo="";
	for(int i=0;i<ret.length;i++)
	{
		if(idkey.equals(ret[i][3]))
		{
		memo=ret[i][1].trim();
		break;
		}	
	}
			
	String sql="update wptlproc set dbbouns='"+dbbouns+"',appydate="+noDateToNull(appydate)+",memo='"+memo+"' where idkey='"+idkey+"'";	
	vt.add(sql);	
	sb.append("\n更新wptlproc\n"+sql);                                //檢查
	setValue("field1",sb.toString());
	
	String giveupchk=getValue("giveupchk");
	String cneextegiveup=getValue("cneextegiveup");
	String endcause=getValue("endcause");
	String enddate=getValue("enddate");
	String meid=getValue("meid");
	
	sql="update wptmapply set giveupchk='"+giveupchk+"',cneextegiveup="+noDateToNull(cneextegiveup)+",endcause='"+endcause
				+"',enddate="+noDateToNull(enddate)+" where meid ='"+meid+"'";	
	vt.add(sql);		
	sb.append("\n更新wptmapply\n"+sql);                                //檢查	



	//存檔更新委託人傳回來的表格。
	String[][]ret_table2=getTableData("trustorTable");
	for(int i=0;i<ret_table2.length;i++){

	//存檔時更新wptlproc的資料
	sql="UPDATE wptlproc SET "                             
		+"plsmanid ='"+ret_table2[i][2].trim()+"',"
		+"conname ='"+ret_table2[i][3].trim()+"',"
		+"conEmail ='"+ret_table2[i][4].trim()+"',"
		+"othid ='"+ret_table2[i][5].trim()+"',"
		+"specialtitle ='"+ret_table2[i][6].trim()+"',"
		+"janloc ='"+ret_table2[i][7].trim()+"',"
		+"janid ='"+ret_table2[i][8].trim()+"'"
		+" WHERE IDKey ='"+ret_table2[i][1].trim()+"'";
 
	vt.add(sql);
	sb.append("\n存檔時更新wptlproc的資料:\n"+sql+"\n");	        //檢查
	//存檔時更新wptmapply的資料，當條件是1時。
	if(ret_table2[i][9].equals("1")){ 
    	sql="UPDATE wptmapply SET "
		+"plsmanid ='"+ret_table2[i][2].trim()+"',"
		+"conname ='"+ret_table2[i][3].trim()+"',"
		+"othid ='"+ret_table2[i][5].trim()+"',"
		+"specialtitle ='"+ret_table2[i][6].trim()+"',"
		+"janloc ='"+ret_table2[i][7].trim()+"',"
		+"janid ='"+ret_table2[i][8].trim()+"'"
		+" WHERE meid ='"+ret_table2[i][0].trim()+"'";
		vt.add(sql);
		sb.append("\n存檔時更新wptmapply的資料，當條件是1時:\n"+sql+"\n");	        //檢查	
	}
	}	
	setValue("field1",sb.toString());	        //檢查  	
		
	String[] t_sql=(String[])vt.toArray(new String[0]);	
	
	try{
		t.execFromPool(t_sql);
		message("更新成功");
	}catch(Exception e){
		message("更新失敗"+e);
	}		
					
	}
	
	
	public void DELETE()throws Throwable {                          //刪除
	Vector vt=new Vector();

	//取得日期及使用者
	String idkey = getValue("idkey");
	String sql="update wptlproc set dbbouns='0',appydate=null where idkey='"+idkey+"'";
	vt.add(sql);
	sa.append("\n刪除\n"+sql);
	
	String[] vtSql=(String[])vt.toArray(new String[0]);

	setValue("field1",sa.toString());
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
	
	
		//處理日期null的問題
	public static String noDateToNull(String date) throws Throwable{
		if(date.length()<8 || date.indexOf("1900")==0){
			return null;
		}else {
		date="'"+date+"'";
		return date;
		}
	}
	
	
	
}
