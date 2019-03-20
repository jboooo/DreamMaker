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
		// �^�ǭȬ� true ��ܰ��汵�U�Ӫ���Ʈw���ʩάd��
		// �^�ǭȬ� false ��ܱ��U�Ӥ����������O
		// �ǤJ�� value �� "�s�W","�d��","�ק�","�R��","�C�L","PRINT" (�C�L�w�����C�L���s),"PRINTALL" (�C�L�w���������C�L���s) �䤤���@
		sa=new StringBuffer();
		t=getTalk("TradeMark");
		 if("�d��".equals(value)){
		    QUERY();
		 } 
		 else if ("�ק�".equals(value)){
		    UPDATE();
		 }
		 else if("�R��".equals(value)){
		 DELETE();
		 }
		return false;
	}
	
	
	public void QUERY()throws Throwable{           //�d��

	String idkey=getQueryValue("idkey");
	//�ˮ֡A�S����ƴN���X�C
	if(idkey.length()==0){message("�S�����");return ;}	
	
	String sql="Select rcvid,meid,lawdate,dbbouns,appydate,debitchk,ltdate,debitdate,debitid,idkey from wptlproc where idkey='"+idkey+"'";
	String[][]ret_wptlproc=t.queryFromPool(sql);
	sa.append("\nsql:\n"+sql);                            //�ˬd
	if(ret_wptlproc.length==0){ 
		message("����idkey�䤣���ơA�д���idkey��J");	
	}
	
	String rcvid=ret_wptlproc[0][0].trim();//	���帹
	String meid=ret_wptlproc[0][1].trim();//		���Ү׸�
	String lawdate=ret_wptlproc[0][2].trim();//		ú�O����
	String dbbouns=ret_wptlproc[0][3].trim();//		�e������ú�O
	String appydate=ret_wptlproc[0][4].trim();//		ú�O���
	String debitchk=ret_wptlproc[0][5].trim();//		�O�_�д�
	String ltdate=ret_wptlproc[0][6].trim();//		�ѫH���
	String debitdate=ret_wptlproc[0][7].trim();//		�дڤ��
	String debitid=ret_wptlproc[0][8].trim();//		�дڳ渹
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
		message("����meid�O�Ū��άO���s�b");	
	}

	sql="Select giveupdate,giveupchk,cneextegiveup,endcause,enddate,appyid from wptmapply where meid='"+meid+"'";
	String[][]ret_wptmapply=t.queryFromPool(sql);
	sa.append("\nsql:\n"+sql);                            //�ˬd
	if(ret_wptmapply.length>0){
	String giveupdate=ret_wptmapply[0][0].trim(); //	���ܩ�m���
	String giveupchk=ret_wptmapply[0][1].trim(); //		�s�ש�m
	String cneextegiveup=ret_wptmapply[0][2].trim(); //		�������ܩ�m��
	String endcause=ret_wptmapply[0][3].trim(); //		���׭�]
	String enddate=ret_wptmapply[0][4].trim(); //		���פ��
	String appyid=ret_wptmapply[0][5].trim(); //		�ӽи��X

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
	setValue("field1",sa.toString());
	}
	
	
	public void UPDATE()throws Throwable{                //�x�s

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
	sb.append("\n��swptlproc\n"+sql);                                //�ˬd
	setValue("field1",sb.toString());
	
	String giveupchk=getValue("giveupchk");
	String cneextegiveup=getValue("cneextegiveup");
	String endcause=getValue("endcause");
	String enddate=getValue("enddate");
	String meid=getValue("meid");
	
	sql="update wptmapply set giveupchk='"+giveupchk+"',cneextegiveup="+noDateToNull(cneextegiveup)+",endcause='"+endcause
				+"',enddate="+noDateToNull(enddate)+" where meid ='"+meid+"'";	
	vt.add(sql);		
	sb.append("\n��swptmapply\n"+sql);                                //�ˬd	



	//�s�ɧ�s�e�U�H�Ǧ^�Ӫ����C
	String[][]ret_table2=getTableData("trustorTable");
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
	}	
	setValue("field1",sb.toString());	        //�ˬd  	
		
	String[] t_sql=(String[])vt.toArray(new String[0]);	
	
	try{
		t.execFromPool(t_sql);
		message("��s���\");
	}catch(Exception e){
		message("��s����"+e);
	}		
					
	}
	
	
	public void DELETE()throws Throwable {                          //�R��
	Vector vt=new Vector();

	//���o����ΨϥΪ�
	String idkey = getValue("idkey");
	String sql="update wptlproc set dbbouns='0',appydate=null where idkey='"+idkey+"'";
	vt.add(sql);
	sa.append("\n�R��\n"+sql);
	
	String[] vtSql=(String[])vt.toArray(new String[0]);

	setValue("field1",sa.toString());
	try {
		t.execFromPool(vtSql);
		message("��s���\");
	} catch(Exception e) {
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
	
	
		//�B�z���null�����D
	public static String noDateToNull(String date) throws Throwable{
		if(date.length()<8 || date.indexOf("1900")==0){
			return null;
		}else {
		date="'"+date+"'";
		return date;
		}
	}
	
	
	
}
