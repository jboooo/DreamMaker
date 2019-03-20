package dai;
import jcx.jform.hproc;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;

public class WP_0112_A extends hproc{
	public String action(String value)throws Throwable{
		// 可自定HTML版本各欄位的預設值與按鈕的動作 
		// 傳入值 value 
		String name=getName();
		if("SAVE".equals(name)){
			SAVE();
		}else if("FROM_STATUS".equals(value)){
			FROM_STATUS();
		}
		 return value;
	}
	public void SAVE()throws Throwable{
		String[][]ret=getTableData("table1");
		setTableData(".trustorTable",ret);
	    hideDialog();
	}

	public void FROM_STATUS()throws Throwable{
		Vector vt=new Vector();
		talk t = getTalk("TradeMark");
		talk w = getTalk("wp");
		String sql="";
		String[][] ret;
		String meid = getValue("meid").trim();		//	本所案號
		String idkey = getValue("idkey").trim();		//	idkey
	    
		setValue("field1",meid+idkey);
		String pamanid="";   //申請人代碼
		
		sql="select pamanid from wptlapman where meid='"+meid+"'";
		String[][]ret_pamanid=t.queryFromPool(sql);
		if(ret_pamanid.length>0){
		pamanid=ret_pamanid[0][0].trim();
		}
		setValue("pamanid",pamanid);
			
		sql=" select a.meid,a.idkey,a.plsmanid,a.conname,a.conemail,a.othid,a.specialtitle,b.janloc,b.janid,''"
			+" from wptlproc a left join wptmxfile b on a.meid=b.meid"
			+" where idkey='"+idkey.trim()+"'";
		ret=t.queryFromPool(sql);
		setTableData("table1",ret);	
		String custid ="";
		if(ret.length>0){
			custid=ret[0][2].trim();
		}
		//前一頁取得申請人代碼
		//取得caseid
		sql="select caseid from wptlproc where idkey='"+idkey+"'";              //取得dibitid及取得dbtitle
		String[][]ret_caseid=t.queryFromPool(sql);
		String caseid="";
		if(ret_caseid.length>0){
			caseid=ret_caseid[0][0].trim();
		}
		 
		//sql="select unidebitname from wptmcust where custid='"+custid.trim()+"'"; //取得委託人名稱
		sql="select unidebitname from wptmcust where custid='"+custid+"'";
		String[][]ret_unidebitname=w.queryFromPool(sql);
		String unidebitname=ret_unidebitname[0][0].trim(); 

		sql="select specialtitle from wptlproc where idkey='"+idkey+"'";          //查詢特殊請款抬頭
		String[][] ret_specialtitle=t.queryFromPool(sql);
		String specialtitle="";             
		if(ret_specialtitle.length>0){
			specialtitle=ret_specialtitle[0][0].trim();
		}

		sql="get_custdibit_data '"+idkey+"','"+caseid+"',null,'"+custid+"'";//取得vatid,debitid,debittile(idkey,caseid,null,custid)
			String[][]ret_get_custdibit=t.queryFromPool(sql);
			String vatid=ret_get_custdibit[0][0].trim();     									  //取得vatid
			setValue("vatid",vatid);                       
			String debitid=ret_get_custdibit[0][1].trim();   									  //
			String debittitle=ret_get_custdibit[0][2].trim(); 									  //請款抬頭:1,2
			setValue("text3","get,"+sql);
		if(debittitle.equals("1")){

			sql="Select unidebitname,unidebitaddr from wptmcust where custid ='"+custid+"'"; //利用委託人代碼取得unidebitname,unidebitaddr	

			String[][]ret_uni=w.queryFromPool(sql);

			setValue("debittitle",ret_uni[0][0].trim()+"\n"+ret_uni[0][1].trim()+"\n"+specialtitle);//請款抬頭=unidebitname+unidebitaddr+特殊請款抬頭
			setValue("text3",1+","+sql);		 
		}else if(debittitle.equals("2")){
			sql="Select top 1 Unioriginname,unioriginaddr from wptmapman where pamanid in("+pamanid+")" +"order by pamanid";//取得Unioriginname,unioriginaddr
			String[][]ret_name=w.queryFromPool(sql);
    		StringBuffer sb_ret_name=new StringBuffer();
	//		for(int i=0;i<ret_name.length;i++){          //要印多筆才要用
			sb_ret_name.append(ret_name[0][0].trim()+"\n"+ret_name[0][1].trim()+"\n"+specialtitle+"\n"+"Via "+custid+" "+unidebitname+"\n"+vatid);
	//		}
			setValue("debittitle",sb_ret_name.toString());//請款抬頭=unioriginaddr+特殊請款抬頭+"via"+委託人編號+委託人名稱+sp.vatid
		setValue("text3",2+","+sql);
		}else{
			//請款抬頭=特殊請款抬頭+via+委託人編號+委託人名稱+sp.vatid
			setValue("debittitle",debittitle.trim()+specialtitle+"\nVia "+custid+" "+unidebitname+"\n"+vatid);
//			setValue("debittitle",debittitle.trim()+"\n"+specialtitle+"\n"+"Via "+custid+" "+unidebitname+"\n"+vatid);
		
		}
		sql="select debitclass from wptcdebit where tm=1 and debitid='"+debitid+"'";        //設定debitid
		String[][]ret_debitclass=w.queryFromPool(sql);

		setValue("dbtype",ret_debitclass[0][0].trim());                            //請款類別
		//-----------------------------------------------------------------------------------------------------
			 //wp,a.委託人名稱unidebitname,a.備註meno,b.傳真confaxa統一編號confid,a客戶等級degree,b電話contel
			 sql="select a.unidebitname,a.memo,b.confax,a.confid,a.degree,b.contel"
			    +" from wptmcust a left join wptmcustcon b on a.custid=b.custid"
				+" where a.custid='"+custid.trim()+"'";
		String[][]ret1=w.queryFromPool(sql);

			setValue("unidebitname",ret1[0][0].trim());// 委託人名稱
			setValue("memo",ret1[0][1].trim());// 備註
			setValue("confax",ret1[0][2].trim());// 傳真
			setValue("confid",ret1[0][3].trim());// 統一編號
			setValue("degree",ret1[0][4].trim());// 客戶等級
			setValue("contel",ret1[0][5].trim());// 電話
	}
}
