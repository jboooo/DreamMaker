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
		// �^�ǭȬ� true ��ܰ��汵�U�Ӫ���Ʈw���ʩάd��
		// �^�ǭȬ� false ��ܱ��U�Ӥ����������O
		// �ǤJ�� value �� "�s�W","�d��","�ק�","�R��","�C�L","PRINT" (�C�L�w�����C�L���s),"PRINTALL" (�C�L�w���������C�L���s) �䤤���@
		StringBuffer sa=new StringBuffer();
		
		if("�d��".equals(value)){
			query();
		}else if("�ק�".equals(value)){
			save();
		}else if("�R��".equals(value)){
			DELETE();
		}
		return false;
	}
	
	public void query()throws Throwable{
		StringBuffer sa=new StringBuffer();
		talk t=getTalk("TradeMark");
		String idkey=getQueryValue("idkey");
		if(idkey.length()==0){
			message("�п�Jidkey");
			return ;
		}
		String sql="select rcvid,meid,lawdate,debitchk,ltdate,debitdate,debitid,idKey from wptlproc where idkey='"+idkey+"'";
		String[][]ret_wptlproc=t.queryFromPool(sql);
		sa.append("\nsql\n"+sql);                                  //�ˬd
		if(ret_wptlproc.length==0){
			message("�Х����oidkey�A�A�i��{�ǧ@�~�C");
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
		sa.append("\nsql\n"+sql);                                  //�ˬd
		if(ret_wptmapply.length==0){
			message("��Ʈw���L������ơC");
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

//memo��		
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
		sa.append("\nmemo:\n"+sql);                           //�ˬd
		setTableData("table1",ret_memo);	
		//memo�u�}��ŦXidkey�����@���C
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
			//�٭�bk�������
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
			//wptlproc�{���ɿ�ܧ����C
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
		
		
		setValue("field1",sa.toString());                         //�ˬd
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
			message("�w�g�ק�L�F�C");
			return ;
		}

		
		
		
		
		//��s�����ҤΫ��ܩ�����
		String enddate=getValue("enddate");
		String endcause=getValue("endcause");
		sql="UPDATE wptmapply SET enddate="+noDateToNull(enddate)+",endcause='"+endcause+"' WHERE meid='"+meid+"'";
		vt.add(sql);	
		sb.append("\n��s�����ҤΫ��ܩ�����:\n"+sql+"\n");	        //�ˬd	
		
		//���omemo
		String[][]ret_memo=getTableData("table1");
		String memo="";
		for(int i=0;i<ret_memo.length;i++){
			if(idkey.equals(ret_memo[i][3]))	{
				memo=ret_memo[i][1].trim();
				break;
			}	
		}
				
		//���o����ΨϥΪ�
		String mdate = datetime.getToday("YYYY/mm/dd"); 
		String mtime = datetime.getTime("h:m:s");		
		String wrkdate = mdate+ " " + mtime; 			   //���oWrkdate		
		String mUser=getUser();  //���o�t�ξާ@��

		//���o�ӿ���qwptmrcv�ɡA����Oidkey�C���@���C			
		sql="select wrkman from wptlproc where idkey='"+idkey+"'";   //���o�ӿ��
		String[][]str_wrkman=t.queryFromPool(sql);
		String wrkman="";
		if(str_wrkman.length>0){
			wrkman=str_wrkman[0][0];
		}

		//�ק�{�ǰO����				
		sql="update wptlproc set " 
			+"Jobmark ='"+(wrkman.equals(mUser)?"":"*")               //��¾�ȥN�z��
			+"',findate ="+noDateToNull(mdate)                                 //���{�ǧ�����
			+", wrkdate ="+noDateToNull(mdate)
			+", wrkman='"+mUser.trim() 
			+"',memo='"+memo 
			+"' where idkey = '"+idkey+"'";
		vt.add(sql);              
		sb.append("\n �ק�wptlproc :\n"+sql);                        //�ˬd
		
	
		//�ץ�{�Ǹg��ӿ���O����,�Y�s�ӿ���h�g�J��
		if(!wrkman.equals(mUser)){//�p�G�ӿ�H��������t�ξާ@��
		
		sql="insert into wptlproc_wrkman(GCkey,IDkey,meid,procWrkman,modDateTime,state)"
			+" VALUES ('"+getGcKey()+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(wrkdate)+",'MOD')";   
		sb.append("\n�ץ�{�Ǹg��ӿ���O����,�Y�s�ӿ���h�g�J��\n"+sql);			
		vt.add(sql);
		}
		
		//�s�ɧ�s�e�U�H�Ǧ^�Ӫ����C
		String[][]ret_table2=getTableData("trustortable");
		for(int i=0;i<ret_table2.length;i++){

		//�s�ɮɧ�swptlproc�����
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
		sb.append("\n�s�ɮɧ�swptlproc�����:\n"+sql+"\n");	        //�ˬd
		//�s�ɮɧ�swptmapply����ơA�����O1�ɡC
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
			sb.append("\n�s�ɮɧ�swptmapply����ơA�����O1��:\n"+sql+"\n");	        //�ˬd	
		}
		}	//table2�j�鵲��

		//�ק�B�z���{��: 
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
				sb.append("\n�ק�B�z���{��:sql:\n"+sql);
			}
		}
		String[][]retBK=(String[][])vtBK.toArray(new String[0][0]);
		String sqlToArray=sqlToString("select idkey,findate,wrkdate,wrkman from wptlproc where ",retBK);   //��k:�s�@��JSON���ɮ׮榡	
		sb.append("\n��JSON�ন��r��:\n"+sqlToArray);                        //�ˬd
		
		//�Nwptmapply����giveupchk,giveupdate,cneextegiveup��ƥ�JSON�ন��r�ɡC
		sql="select giveupchk,giveupdate,cneextegiveup from wptmapply where meid='"+meid+"'";
		String[][]ret= t.queryFromPool(sql);
		String sqlToArray2=sqlToString(sql,ret);   //��k:�s�@��JSON���ɮ׮榡
		sb.append("\n��JSON�ন��r��:\n"+sqlToArray2);                        //�ˬd
		
		JSONArray jsonArray=new JSONArray();
		jsonArray.add(sqlToArray);
		jsonArray.add(sqlToArray2);
		sqlToArray=jsonArray.toString();
		
		//�NJSON������x�s�_��
		sql="UPDATE wptlproc set bk='"+sqlToArray+"' where idkey='"+idkey+"'";
		vt.add(sql);		
		sb.append("\n�N��r���x�s��wptlproc�Akey��idkey:\n"+sql);                        //�ˬd		
		
		
		
		
		setValue("field1",sb.toString());	        //�ˬd  	
				
		String[] t_sql=(String[])vt.toArray(new String[0]);	
	
		try{
			t.execFromPool(t_sql);
			message("��s���\");
		}catch(Exception e){
			message("��s����"+e);
		}		
	
	}
	
	public void DELETE()throws Throwable{                           //�R��
	StringBuffer sa=new StringBuffer();	

	talk t=getTalk("TradeMark");	
	Vector vt=new Vector();
	sa.append("\n�ġġġġġġġġġġġġġġ� ���ܤ�ú�O�A�R��:�ġġġġġġġġġġġġġġġ�\n");                        //�ˬd�I

	
	//���o����ΨϥΪ�
	String mdate = datetime.getToday("YYYY/mm/dd"); 
	String mtime = datetime.getTime("h:m:s");		
	String wrkdate = mdate+ " " + mtime; 			   //���oWrkdate		
	String mUser=getUser();  //���o�t�ξާ@��
	String idkey=getValue("idkey");
	String meid=getValue("meid");
	String gckey=getGcKey();
	
	String sql="insert into wptlproc_wrkman(gckey,idkey,meid,procWrkman,modDateTime,state)"           
				+" VALUES ('"+gckey+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(wrkdate)+",'DEL')";  
	vt.add(sql);             
	sa.append("\n �s�WDEL,wptlproc_wrkman :\n"+sql);                        //�ˬd�I
	//TODO

	//�٭�bk�������
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
		sa.append("\n�^�swptlproc:"+sql);		
		}
	}	
	sql="update wptlproc set jobmark='',findate=null,wrkdate="+noDateToNull(mdate)+" where idkey='"+idkey+"'";
	vt.add(sql);
	sa.append("\n��swptlproc:"+sql);	
	
	String[] vtSql=(String[])vt.toArray(new String[0]);
	
	setValue("field1",sa.toString());	
	try{
		t.execFromPool(vtSql);
		message("��s���\");
	}catch(Exception e){
		message("��s����"+e);
	}			
	return ;
	}
	
	
	
	/*��k:����榡����Ʀ�2013/01/01
	  Ū�ɮɨϥ�:�Y������Ū��"1900"�}�Y�A�^��"";
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
	
	

	/*noDateToNull�S����নnull
	  �s�ɮɨϥΡA�B�z���null�����D
	  �b�s�ɪ��a��A������[''�A�]��null�Ȯǥ[''�ܦ�'null'�A��Ʈw�L�k�����C
	*/
	public static String noDateToNull(String date) throws Throwable{
		if(date.length()==0 || date.indexOf("1900")==0){
			return null;
		}else {
		date="'"+date.trim()+"'";
		return date;
		}
	}
	
	//�ǤJsql��XJSON�榡���r��,�ǤJsql��ret�ɡC
	public static String sqlToString(String sql,String[][] ret) {
		sql =sql.replace("'","");  //�h��'�A�]���n�x�s�bsql�ɡAsql�L�k�����C
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
	
	/*���ogckey
	*�^�ǭȬOString�A�Y�h���I�s�|�۰�+1
	*/
	String gckey=null;  //key�b�~�O�i�H�֥[
	public String getGcKey()throws Throwable{
		talk t=getTalk("TradeMark");	
		if(gckey==null){
			String sql = "select max(gckey) from wptlproc_wrkman";                //���ogckey�B�z
			String[][]ret = t.queryFromPool(sql);
			gckey= operation.add(ret[0][0].trim(),"1");//�r��[1�C				
		}else{
			gckey= operation.add(gckey,"1");
		}
		if(gckey.length()<12){
			gckey=convert.add0(gckey,"12");
		}
		return gckey;				
	}
	
	
}
