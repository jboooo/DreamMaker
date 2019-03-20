package dai;
import jcx.jform.bTransaction;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class WP0113_PT extends bTransaction{
	public boolean action(String value)throws Throwable{
		// 回傳值為 true 表示執行接下來的資料庫異動或查詢
		// 回傳值為 false 表示接下來不執行任何指令
		// 傳入值 value 為 "新增","查詢","修改","刪除","列印","PRINT" (列印預覽的列印按鈕),"PRINTALL" (列印預覽的全部列印按鈕) 其中之一
		StringBuffer sa=new StringBuffer();
		
		if("查詢".equals(value)){
			query();
		}else if("修改".equals(value)){
			save();
		}else if("刪除".equals(value)){
			DELETE();
		}
		return false;
	}
	
	public void query()throws Throwable{
		StringBuffer sa=new StringBuffer();
		talk t=getTalk("TradeMark");
		String idkey=getQueryValue("idkey");
		if(idkey.length()==0){
			message("請輸入idkey");
			return ;
		}
		String sql="select rcvid,meid,lawdate,debitchk,ltdate,debitdate,debitid,idKey from wptlproc where idkey='"+idkey+"'";
		String[][]ret_wptlproc=t.queryFromPool(sql);
		sa.append("\nsql\n"+sql);                                  //檢查
		if(ret_wptlproc.length==0){
			message("請先取得idkey，再進行程序作業。");
			return ;
		}
		setValue("rcvid",ret_wptlproc[0][0].trim());
		setValue("meid",ret_wptlproc[0][1].trim());
		setValue("lawdate",dateformat(ret_wptlproc[0][2].trim()));
		setValue("debitchk",ret_wptlproc[0][3].trim());
		setValue("ltdate",dateformat(ret_wptlproc[0][4].trim()));
		setValue("debitdate",dateformat(ret_wptlproc[0][5].trim()));
		setValue("debitid",ret_wptlproc[0][6].trim());		
		setValue("idkey",ret_wptlproc[0][7].trim());	
		setEditable("rcvid",false);
		setEditable("meid",false);
		setEditable("lawdate",false);
		setEditable("debitchk",false);
		setEditable("ltdate",false);
		setEditable("debitdate",false);
		setEditable("debitid",false);
		setEditable("idkey",false);		
		String meid=ret_wptlproc[0][1].trim();
		idkey=ret_wptlproc[0][7].trim();		
		
		sql="select meid,appyid,giveupchk,giveupdate,cneextegiveup,enddate,endcause,appydate "
			+" from wptmapply where meid='"+meid+"'";
		String[][]ret_wptmapply=t.queryFromPool(sql);
		sa.append("\nsql\n"+sql);                                  //檢查
		if(ret_wptmapply.length==0){
			message("資料庫中無此筆資料。");
			return ;
		}		
		setValue("appyid",ret_wptmapply[0][1].trim());
		setValue("giveupchk",ret_wptmapply[0][2].trim());
		setValue("giveupdate",dateformat(ret_wptmapply[0][3].trim()));	
		setValue("cneextegiveup",dateformat(ret_wptmapply[0][4].trim()));		
		setValue("enddate",dateformat(ret_wptmapply[0][5].trim()));		
		setValue("endcause",ret_wptmapply[0][6].trim());		
		setValue("appydate",dateformat(ret_wptmapply[0][7].trim()));	
		
		setEditable("appyid",false);
		setEditable("giveupchk",false);
		setEditable("giveupdate",false);				
		setEditable("cneextegiveup",false);		
		setEditable("appydate",false);		

//memo舊		
//		sql="select rcvid,memo,idkey from wptlproc where meid='"+meid+"'";
//		String[][]ret=t.queryFromPool(sql);
//		setTableData("table1",ret);
//		if(ret.length>0){
//			for(int i=0;i<ret.length;i++){
//				if(!ret[i][2].equals(idkey)){
//					setEditable("table1",i,1, false);
//				}
//			}
//		}
//		sa.append("\nsql\n"+sql);   

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
		sql="select findate from wptlproc where idkey='"+idkey+"'";
		String[][] retWptlprocIdkey=t.queryFromPool(sql);	
		
		String[][]retWptlprocProcid=null;
		if(retWptlprocIdkey.length>0 && retWptlprocIdkey[0][0].length()>0){
			//還原bk內的資料
			sql="select bk from wptlproc where idkey='"+idkey+"'";
			String[][] retBk=t.queryFromPool(sql);
			StringBuffer sbIdkey=new StringBuffer();
			if(retBk.length>0){
				sa.append("\nBK\n"+retBk[0][0]);
				JSONArray array=JSONArray.fromObject(retBk[0][0].trim());
				JSONObject jsonObject=array.getJSONObject(0);
				JSONArray field=jsonObject.getJSONArray("field");
				for(int i=0;i<field.size();i++){
					JSONObject data=field.getJSONObject(i);
					String jsIdkey=(String)data.get("idkey");
					sbIdkey.append("'"+jsIdkey+"',");
				}
				sbIdkey.setLength(sbIdkey.length()-1);
				sql="select '',idkey,findate,wrkdate,wrkman,meid,lawdate,procname,empname"
					+" from wptlproc PL left join wptcproc PC on PL.procid=PC.procid left join wp..wptmemp E on PL.wrkman=E.empid " 
					+" where idkey in ("+sbIdkey.toString()+")";			
				retWptlprocProcid=t.queryFromPool(sql);
				for(int i=0;i<retWptlprocProcid.length;i++){
					retWptlprocProcid[i][0]="1";
					setEditable("table3",i,0,false);
				}
			}
		}else{
			//wptlproc程序檔選擇完成。
			sql="usp_relparent '"+meid+"'";
			String[][]meidIn=t.queryFromPool(sql);
			
			sql="select '',idkey,findate,wrkdate,wrkman,meid,lawdate,procname,empname"
				+" from wptlproc PL left join wptcproc PC on PL.procid=PC.procid left join wp..wptmemp E on PL.wrkman=E.empid " 
				+" where meid in ('"+meidIn[0][0]+"') and findate is null and len(replace(meid,' ',''))>0";
	
			retWptlprocProcid=t.queryFromPool(sql);
			sa.append("\nsql\n"+sql); 		
			if(retWptlprocProcid.length>0){
				for(int i=0;i<retWptlprocProcid.length;i++){
					if(retWptlprocProcid[i][1].equals(idkey)){
						retWptlprocProcid[i][0]="1";
						setEditable("table3",i,0,false);  //TODO
					}
				}
			}
		
		}
			
		setTableData("table3",retWptlprocProcid);		
		
		
		setValue("field1",sa.toString());                         //檢查
	}
	
	public void save()throws Throwable{
		talk t=getTalk("TradeMark");
		Vector vt=new Vector();
		StringBuffer sb=new StringBuffer();
		String idkey=getValue("idkey").trim().toUpperCase();
		String meid=getValue("meid").trim().toUpperCase();
		

		
		String sql="select findate from wptlproc where idkey='"+idkey+"'";
		String[][] retWptlprocIdkey=t.queryFromPool(sql);	

	
		if(retWptlprocIdkey.length>0 && retWptlprocIdkey[0][0].length()>0){
			message("已經修改過了。");
			return ;
		}

		
		
		
		
		//更新放棄領證及指示放棄領證
		String enddate=getValue("enddate");
		String endcause=getValue("endcause");
		sql="UPDATE wptmapply SET enddate="+noDateToNull(enddate)+",endcause='"+endcause+"' WHERE meid='"+meid+"'";
		vt.add(sql);	
		sb.append("\n更新放棄領證及指示放棄領證:\n"+sql+"\n");	        //檢查	
		
		//取得memo
		String[][]ret_memo=getTableData("table1");
		String memo="";
		for(int i=0;i<ret_memo.length;i++){
			if(idkey.equals(ret_memo[i][3]))	{
				memo=ret_memo[i][1].trim();
				break;
			}	
		}
				
		//取得日期及使用者
		String mdate = datetime.getToday("YYYY/mm/dd"); 
		String mtime = datetime.getTime("h:m:s");		
		String wrkdate = mdate+ " " + mtime; 			   //取得Wrkdate		
		String mUser=getUser();  //取得系統操作者

		//取得承辦員從wptmrcv檔，條件是idkey。取一筆。			
		sql="select wrkman from wptlproc where idkey='"+idkey+"'";   //取得承辦員
		String[][]str_wrkman=t.queryFromPool(sql);
		String wrkman="";
		if(str_wrkman.length>0){
			wrkman=str_wrkman[0][0];
		}

		//修改程序記錄檔				
		sql="update wptlproc set " 
			+"Jobmark ='"+(wrkman.equals(mUser)?"":"*")               //由職務代理時
			+"',findate ="+noDateToNull(mdate)                                 //本程序完成日
			+", wrkdate ="+noDateToNull(mdate)
			+", wrkman='"+mUser.trim() 
			+"',memo='"+memo 
			+"' where idkey = '"+idkey+"'";
		vt.add(sql);              
		sb.append("\n 修改wptlproc :\n"+sql);                        //檢查
		
	
		//案件程序經手承辦員記錄檔,若新承辦員則寫入檔
		if(!wrkman.equals(mUser)){//如果承辦人員不等於系統操作者
		
		sql="insert into wptlproc_wrkman(GCkey,IDkey,meid,procWrkman,modDateTime,state)"
			+" VALUES ('"+getGcKey()+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(wrkdate)+",'MOD')";   
		sb.append("\n案件程序經手承辦員記錄檔,若新承辦員則寫入檔\n"+sql);			
		vt.add(sql);
		}
		
		//存檔更新委託人傳回來的表格。
		String[][]ret_table2=getTableData("trustortable");
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
		}	//table2迴圈結束

		//修改處理中程序: 
		Vector vtBK=new Vector();
		String[][]retTable3=getTableData("table3");
		for(int i=0;i<retTable3.length;i++){
			if("1".equals(retTable3[i][0].trim())){
				sql="update wptlproc set" 
					+" findate = "+noDateToNull(mdate)
					+", wrkdate ="+noDateToNull(mdate)
					+", wrkman='"+mUser.trim() 
					+"' where idkey='"+retTable3[i][1].trim() +"'";
				vt.add(sql);
				vtBK.add(new String[]{retTable3[i][1],retTable3[i][2],retTable3[i][3],retTable3[i][4].trim()});
				sb.append("\n修改處理中程序:sql:\n"+sql);
			}
		}
		String[][]retBK=(String[][])vtBK.toArray(new String[0][0]);
		String sqlToArray=sqlToString("select idkey,findate,wrkdate,wrkman from wptlproc where ",retBK);   //方法:製作成JSON的檔案格式	
		sb.append("\n用JSON轉成文字檔:\n"+sqlToArray);                        //檢查
		
		//將wptmapply內的giveupchk,giveupdate,cneextegiveup資料用JSON轉成文字檔。
		sql="select giveupchk,giveupdate,cneextegiveup from wptmapply where meid='"+meid+"'";
		String[][]ret= t.queryFromPool(sql);
		String sqlToArray2=sqlToString(sql,ret);   //方法:製作成JSON的檔案格式
		sb.append("\n用JSON轉成文字檔:\n"+sqlToArray2);                        //檢查
		
		JSONArray jsonArray=new JSONArray();
		jsonArray.add(sqlToArray);
		jsonArray.add(sqlToArray2);
		sqlToArray=jsonArray.toString();
		
		//將JSON的資料儲存起來
		sql="UPDATE wptlproc set bk='"+sqlToArray+"' where idkey='"+idkey+"'";
		vt.add(sql);		
		sb.append("\n將文字檔儲存至wptlproc，key為idkey:\n"+sql);                        //檢查		
		
		
		
		
		setValue("field1",sb.toString());	        //檢查  	
				
		String[] t_sql=(String[])vt.toArray(new String[0]);	
	
		try{
			t.execFromPool(t_sql);
			message("更新成功");
		}catch(Exception e){
			message("更新失敗"+e);
		}		
	
	}
	
	public void DELETE()throws Throwable{                           //刪除
	StringBuffer sa=new StringBuffer();	

	talk t=getTalk("TradeMark");	
	Vector vt=new Vector();
	sa.append("\n＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿ 指示不繳費，刪除:＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿＿\n");                        //檢查點

	
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
	//TODO

	//還原bk內的資料
	sql="select bk from wptlproc where idkey='"+idkey+"'";
	String[][] retBk=t.queryFromPool(sql);
	if(retBk.length>0){
		sa.append("\nBK\n"+retBk[0][0]);
		JSONArray array=JSONArray.fromObject(retBk[0][0].trim());
		JSONObject jsonObject=array.getJSONObject(0);
//		sa.append("\njsonObject:"+jsonObject.toString());
		String jsTable=(String)jsonObject.get("table");
//		sa.append("\ntable:"+jsTable);
		JSONArray field=jsonObject.getJSONArray("field");
//		sa.append("\nfield:"+field.toString());
		for(int i=0;i<field.size();i++){
			JSONObject data=field.getJSONObject(i);
			String jsIdkey=(String)data.get("idkey");
			String jsFndate=(String)data.get("findate");
			String jsWrkdate=(String)data.get("wrkdate");	
			String JsWrkman=(String)data.get("wrkman");		
			sql= "update "+jsTable+" set findate="+noDateToNull(jsFndate)+",wrkdate="+noDateToNull(jsWrkdate)+",wrkman='"+JsWrkman+"' where idkey='"+jsIdkey+"'";
			vt.add(sql);
		sa.append("\n回存wptlproc:"+sql);		
		}
	}	
	sql="update wptlproc set jobmark='',findate=null,wrkdate="+noDateToNull(mdate)+" where idkey='"+idkey+"'";
	vt.add(sql);
	sa.append("\n更新wptlproc:"+sql);	
	
	String[] vtSql=(String[])vt.toArray(new String[0]);
	
	setValue("field1",sa.toString());	
	try{
		t.execFromPool(vtSql);
		message("更新成功");
	}catch(Exception e){
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
	
	

	/*noDateToNull沒日期轉成null
	  存檔時使用，處理日期null的問題
	  在存檔的地方，日期不加''，因為null值旁加''變成'null'，資料庫無法接受。
	*/
	public static String noDateToNull(String date) throws Throwable{
		if(date.length()==0 || date.indexOf("1900")==0){
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
	
	/*取得gckey
	*回傳值是String，若多筆呼叫會自動+1
	*/
	String gckey=null;  //key在外是可以累加
	public String getGcKey()throws Throwable{
		talk t=getTalk("TradeMark");	
		if(gckey==null){
			String sql = "select max(gckey) from wptlproc_wrkman";                //取得gckey處理
			String[][]ret = t.queryFromPool(sql);
			gckey= operation.add(ret[0][0].trim(),"1");//字串加1。				
		}else{
			gckey= operation.add(gckey,"1");
		}
		if(gckey.length()<12){
			gckey=convert.add0(gckey,"12");
		}
		return gckey;				
	}
	
	
}
