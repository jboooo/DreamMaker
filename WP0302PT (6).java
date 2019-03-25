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
	StringBuffer sa=null;   //�ˬd
	StringBuffer sb=null;   //�d�ߨϥΪ�
	StringBuffer sc=null;   //�ˬd��վ�A����
	StringBuffer sd=null;   //��������ƪ�������
	String sql="";
	Vector vt=null;
	public boolean action(String value)throws Throwable{
		// �^�ǭȬ� true ��ܰ��汵�U�Ӫ���Ʈw���ʩάd��
		// �^�ǭȬ� false ��ܱ��U�Ӥ����������O
		// �ǤJ�� value �� "�s�W","�d��","�ק�","�R��","�C�L","PRINT" (�C�L�w�����C�L���s),"PRINTALL" (�C�L�w���������C�L���s) �䤤���@
		t=getTalk("TradeMark");
		w=getTalk("wp");
		vt=new Vector();
		sql="";		
		sa=new StringBuffer();
		sb=new StringBuffer();
		sc=new StringBuffer();
		sd=new StringBuffer();
				
		if(value.equals("�d��")){
			Query();
		}else if(value.equals("�ק�")){
			if(Save()){
				Query();
			}
		}else if(value.equals("�R��")){
			if(Delete()){
				Query();
			}
		}
		setValue("field1",sa.toString());
		return false;
	}
	public boolean Query()throws Throwable {
		sa.append("�d��\n");
		String idkey=getQueryValue("idkey").trim();	//���oidkey
		sql="select rcvid,meid from wptlproc where idkey='"+idkey+"'";	//���orcvid
		String[][] retIdkey=t.queryFromPool(sql);
		sa.append(sql+"\n");
		
		String rcvid=retIdkey[0][0].trim();
		String meid=retIdkey[0][1].trim();
		setValue("idkey",idkey.toUpperCase());
		setValue("rcvid",rcvid.toUpperCase());
		setValue("meid",meid.toUpperCase());
	
	
		selectTable1(rcvid);//�j�Mtable1���
		//�N�e���Ĥ@��meid�]�w�b�j�M�дڮM�ո̡C
		
		selectDebit(meid,rcvid);//�j�M�дڮM��
		selectMemanid(meid);//�j�M���ҥN�z�H
		selectPamanidAndId(meid);//�j�M�ӽФH�ΥN��H
		selectWptlproexte(idkey);//�j�M������s
		selectAnnex(idkey);//�j�M����
		return true;
	}
	
	public boolean Save()throws Throwable {
		sa.append("�ק�\n");
		String[][] table1=getTableData("table1");
		for (int i=0 ; i<table1.length ; i++ ) {
			String meid=table1[i][2].trim();
			String exteappydate=table1[i][6].trim();//�̷s�@�����i�ӽФ��
			String appydate=table1[i][6].trim();//���i�ӽФ��
			String dbbouns=table1[i][7].trim();//�W�O�[��
			String rcvno=table1[i][8].trim();//���ڸ��X
			String idkey=table1[i][9].trim();
			String rcvid=table1[i][12].trim();
			String keyno=table1[i][13].trim();
			//�u������A�ݭn���okeyno
			saveWptlproexte(idkey,meid,rcvid,keyno,table1[i][14].trim());//�x�swptlproexte��ƪ�
			//�䦸�A�ݭn�Ψ�keyno,�N�¸�Ƴƥ�
			
			String sql="select keyno from wptlproc where idkey='"+idkey+"'";
			String[][]keynoArray=t.queryFromPool(sql);
			sa.append(sql+"\n");
			if(keynoArray.length<=1) {
				backupPamanid(idkey,meid);	//�ƥ��ӽФH
				saveWptlappyOriApman(meid);//�ƥ��N��H
				backupMemanid(idkey);//�ƥ����ҥN�z�H
//				backupWptmapply(meid,idkey);//�ƥ��D��,�Ӽ�,�y�z�ʻ����A�ϥ�json
				backupWptmapplyGoodclass(meid,idkey);//json   1.�ƥ��D��,�Ӽ�,�y�z�ʻ���  2.�ƥ�goodclass
			}
					
			//�N�¸�ƻ\�L
			savePamanid(meid);//�x�s�ӽФH�A�N��H
			saveMemanid(meid);//�x�s�N�z�H
			execString(table1[i][11].trim()); //�x�s���ܩʰӫ~�B�ӼСB�y�z�ʻ���
			saveAnnex(idkey);//�x�s����
	
			saveWptlprocWrkman(idkey,meid);//�x�s-�ץ�{�Ǹg��ӿ���O����wptlproc_wrkman,�Y�s�ӿ���h�g�J��
			saveWptmapply(meid,exteappydate);//��s�D��
			saveWptlproc(appydate,dbbouns,rcvno,idkey);//��s�{����
		}
		saveWptlprocWptmapply();//�s�ɧ�s�e�U�H�Ǧ^�Ӫ����C
		return true;
	}
	/*
		�R��
		@���ӥx
	*/	
	public boolean Delete()throws Throwable{
		sa.append("�R��\n");				
		String[][] table1=getTableData("table1");
		if(table1.length<=0){
			message("�S����ơA����R��");
			return false;		
		}
		
		for (int i=0 ;i<table1.length ;i++ ){
			String meid=table1[i][2].trim();
			String idkey=table1[i][9].trim();
			String keyno=table1[i][13].trim();	
			//����ˮ�keyno
			if(keyno.equals("")){
				message("�S���x�s�L�A����R��");
				return false;
			}
			delWptlproexte(keyno); //�R��Wptlproexte
			delWptlprocWrkman(idkey,meid);//�R��-�ץ�{�Ǹg��ӿ���O����
			delWptlproc(idkey);//del�{����wptlproc			
			//�٭�ӽФH�A�N��H�A�N�z�H
			revertMark(idkey,meid);//�٭�-�D�ɡB�ӼСB�y�z�ʻ���
			revertMemanid(meid,idkey);//�^�_�N�z�H

		}
		return true;
	}
		
	/*
		�j�Mtable1���
	*/
	public void selectTable1(String rcvid)throws Throwable {
		sa.append("�j�Mtable1���\n");
		//���]�w
		sql="select distinct '','a'=case when d.idkey is null then '�_' else '�O' end,a.meid,"
           +"b.regid,b.markname,b.extedead,a.appydate,a.dbbouns,a.rcvno,a.idkey,'�ק�','',a.rcvid,d.keyno,'','1' as bb"
           +" from wptlproc a"
           +" left outer join wptmapply b on a.meid=b.meid"
           +" left outer join wptlapbale c on a.meid=c.meid and delmk=0"
           +" left outer join wptlproexte d on a.idkey=d.idkey"
           +" where a.rcvid='"+rcvid+"' and a.procid='0302'"
           +" union "
           +"select distinct '','a'=case when d.idkey is null then '�_' else '�O' end,a.meid,"
           +"b.regid,b.markname,b.extedead,a.appydate,a.dbbouns,a.rcvno,a.idkey,'�ק�','',a.rcvid,d.keyno,'','2' as bb"
           +" from wptlproc a"
           +" left outer join wptmapply b on a.meid=b.meid"
           +" left outer join wptlapbale c on a.meid=c.meid and delmk=0"
           +" left outer join wptlproexte d on a.idkey=d.idkey"
           +" where left(a.rcvid,10)=left('"+rcvid+"',10) and a.procid='0302' and a.rcvid<>'"+rcvid
		   +"' order by bb";

		String[][] retWptlproc=t.queryFromPool(sql);
		sa.append(sql+"\n");
		if(retWptlproc.length==0) {
			return ;
		}
		//�N����榡��
		for(int i=0; i<retWptlproc.length; i++) {
			retWptlproc[i][5]=dateformat(retWptlproc[i][5]);
			retWptlproc[i][6]=dateformat(retWptlproc[i][6]);
		}
	
		setTableData("table1",retWptlproc);
		setValueAt("table1","1",0,"0");
		setEditable("table1",0,"0",false);

	}
	
	/*
		�j�M�дڮM�����
	*/
	public void selectDebit(String meid,String rcvid)throws Throwable {
		sa.append("�j�M�дڮM�����\n");
		//���]�w
		sql="SELECT debitid,debitchk,ltdate,debitdate FROM wptlproc "
			+" WHERE meid ='"+ meid +"' AND rcvid ='"+rcvid+"' AND procid = '0302'";
	
		String[][]retwptlproc=t.queryFromPool(sql);
		sa.append(sql+"\n");
	
		if(retwptlproc.length>0) {
			setValue("debitid",retwptlproc[0][0]);
			setValue("debitchk",retwptlproc[0][1]);
			setValue("ltdate",dateformat(retwptlproc[0][2]));
			setValue("debitdate",dateformat(retwptlproc[0][3]));
		}
		
	}	
	
	/*
		�j�M-�M�˥��ҥN�z�H(�h��)
	*/
	public void selectMemanid(String meid)throws Throwable{
		sa.append("�j�M-�M�˥��ҥN�z�H(�h��)\n");
		sql="select memanid,memanname from wp..wptcmeman where memanid in(select memanid from trademark..wptlmeman where meid='"+meid+"') and tm='1'";
		String[][] ret_memanid = t.queryFromPool(sql);
		sa.append(sql+"\n");
		setTableData("memanidTable", ret_memanid);
	}


	/*
		�j�M-���w�ӫ~
	*/
	public void selectGoodClass(String meid)throws Throwable{	
		sa.append("�j�M-���w�ӫ~\n");
		//���w�ӫ~
		sql = "Select Goodclass,uniNOrigdname,uniNChgdname,delmk,'','',Goodclass,uniNOrigdname,uniNChgdname from wptlapbale where meid='"
			  + meid + "' and delmk<>1";
		String[][] ret_goodclass = t.queryFromPool(sql);
		sa.append(sql+"\n");	
		// �ˬd
		sa.append("\n�j�Mwptlapbale,���w�ӫ~\n" + sql);
			if (ret_goodclass.length > 0) {
				setTableData("goodclassTable", ret_goodclass);
				setEditable("goodclassTable", false);
				// �]�w�Ĥ@����ƨ�e���W
				setValue("checkclass", ret_goodclass[0][0]); // �ΨӰO�����O�N���A�s�ɪ��ɫ�ϥΪ��C
				setValue("goodclass_field", ret_goodclass[0][0]);
				setValue("uniNOrigdname", ret_goodclass[0][1]);
				setValue("uniNChgdname", ret_goodclass[0][2]);
			}
	}

	/*
		�j�M-������s	
	*/
	public void selectWptlproexte(String idkey)throws Throwable{	
		sa.append("�j�M-������s\n");
		//�j�M���
		sql="SELECT pamanid,prename1,desctext,goodname1,marktype,markmeid,markname,picchk,keyno FROM wptlproexte WHERE idkey ='"+ idkey+"'";
		String[][]retwptlproexte=t.queryFromPool(sql);
		sa.append(sql+"\n");
		if(retwptlproexte.length>0){
		//�]�w���
		setValue("ispamanid",retwptlproexte[0][0].trim());    //ispamanid//�ӽФH
		
		}
	}

	/*
		�j�M-����	
	*/
	public void selectAnnex(String idkey)throws Throwable{		
		sa.append("�j�M-����\n");		
		sql="select '�t��'=case when l.annexid is null then '�_' else '�O' end ,c.annex_name,isnull(l.description,''),"
			+"c.annexid,c.desc_format,desc_default,c.sort"
			+" from wptcannex c"
			+" left join wptlannex l on l.annexid = c.annexid and l.idkey='"+idkey
			+"' where c.procid ='0701' order by sort";
		String[][]retAnnex=t.queryFromPool(sql);
		sa.append(sql+"\n");
		if(retAnnex.length>0){
			setTableData("tableAnnex",retAnnex);
		}
	}		
	/*
		�x�s���ҥN�z�H
	*/
	public void saveMemanid(String meid)throws Throwable{
		sa.append("�x�s���ҥN�z�H\n");	
		vt=new Vector();
//		sa.append("\n-------���ҥN�z�H�A�x�s------:\n"); // �ˬd�I
		String[][] ret_table3 = getTableData("memanidTable");
		if (ret_table3.length != 0) {
			sql = "delete from wptlmeman where meid='" + meid + "'";
			vt.add(sql);
//			sa.append("\n�R��wptlmeman\n" + sql); // �ˬd�I
			for (int i = 0; i < ret_table3.length; i++) {
				sql = "insert into wptlmeman (meid,memanid) values ('" + meid + "','" + ret_table3[i][0] + "')";
				vt.add(sql);
//				sa.append("\n���ҥN�z�H\n" + sql); // �ˬd�I
			}
		} 
		execData(vt);
	}

	/*
		�x�s���ĸ��wptlproexte,�ǤJidkey,meid,rcvid
	*/
	public void saveWptlproexte(String idkey,String meid,String rcvid,String keyno,String str)throws Throwable{		
		sa.append("�x�s���ĸ��wptlproexte\n");	
		sc =new StringBuffer();
		sc.append(str);
		
		//���o���
		String ispamanid=getValue("ispamanid".trim());//�ӽФH
		if(keyno.length()==10){
			sql = "update wptlproexte set " + "pamanid='" + ispamanid + "'" + " where keyno='" + keyno + "'";
			vt.add(sql);
		}else{
			sql = "insert into wptlproexte (meid,pamanid,idkey,rcvid,keyno) values ('"
				  + meid + "','" + ispamanid + "','"+ idkey  + "','" + rcvid + "','aaa')";
			sc.append(sql+"@");
		}
		insertAddKeyno(sc.toString());
	}
	
	/*
		���ʸ�Ʈw
	*/
	public void execData(Vector vt)throws Throwable{	
	
	String[] vtsql = (String[]) vt.toArray(new String[0]);
	for(int i=0;i<vtsql.length;i++){
		sa.append(vtsql[i]+"\n");	
	}	
	
	try {
		t.execFromPool(vtsql);
		message("���ʸ�Ʈw���\");
	} catch (Exception e) {
		e.printStackTrace(System.err);
		message("���ʸ�Ʈw����" + e);
		return ;
	}

	}
	
	/*keyno�۰ʽs��
	*�^�ǭȬOString�A�Y�h���I�s�|�۰�+1
	*/
	static String keynoStr="";  //key�b�~�O�i�H�֥[
	public String getKeyno()throws Throwable {
		String year=operation.sub(getYear(),"1911");//���o����~
		if(keynoStr.length()==10){
			keynoStr= operation.add(keynoStr,"1");
		} else{
			sql = "select top 1 keyno from wptlproexte where left(keyno,2)='10' and  len(keyno)='10' order by keyno desc";                //���okeyno�B�z
			String[][]ret = t.queryFromPool(sql);
			sa.append(sql+"\n");
			if(ret.length>0) {
				keynoStr=ret[0][0].substring(4);
				keynoStr= operation.add(keynoStr,"1");//�r��[1�C
				keynoStr=convert.add0(keynoStr,"7");
				keynoStr=year+keynoStr;
			} else {
				keynoStr="1080000001";
			}
		}
		return keynoStr;
	}
	
	
	/*���o�褸�~
	*�פJimport java.text.DateFormat;import java.text.SimpleDateFormat;import java.util.Date;
	*/
	
	public static String getYear()throws Throwable {
		Date date = new Date();
		DateFormat dateformat= new SimpleDateFormat("yyyy");
		String dateTime=dateformat.format(date);
		return dateTime;
	}
	//todo���o����ɶ�
	
	
	
	
	/*
	 * �N�r����}�C ��Xinsert����ơA�å[�Jkeyno ���Ъ�keyno�A�h�I�s�ƪ��A�N��Ƨ令updata�A�Τ��e������ƪ�key�ȡC
	 */
	public void insertAddKeyno(String str) throws Throwable{
		vt=new Vector();
		Hashtable<String, String> ht = new Hashtable<String, String>();
		String keyno = "";// keyno����
		str = str.toLowerCase();
		String[] strArray = str.split("@");
//		insertAddKeyno(strArray);

		for (int i = 0; i < strArray.length; i++) {
			if ((strArray[i].indexOf("insert")) >= 0) {
				int pcNum = strArray[i].indexOf("pc"); // �M��idkey
				String pcStr = strArray[i].substring(pcNum, pcNum + 12);// ���oIDKEY
//				System.out.println(pcStr);
				sc.append(pcStr+"\n");
				if (ht.get(pcStr) != null) {
					// ���X�W�@����keyno��
					keyno = (String) ht.get(pcStr);
					//�Y�ⵧpc�ۦP�A�h�@���אּupdate�A�ñNkeyno�@�P
					sql = insertToUpdate(keyno, strArray[i]);
					vt.add(sql);
					continue;
				}
				keyno = getKeyno();// ���okeyno getKeyno();
				strArray[i] = strArray[i].replace("aaa", keyno);
				// ��J(idkey,keyno)
				ht.put(pcStr, keyno);
			}
			vt.add(strArray[i]);
		}
		execData(vt);//���ʸ�Ʈw
	}
	/* �N���Ъ�insert�ഫ��update
	 * 
	 */
	public static String insertToUpdate(String keyno, String str) {
		String[] strtemp = str.split(" ");
		String tableName = strtemp[2];
		String where = " where keyno='" + keyno + "'";
		int brackets = str.indexOf("(");
		int brackets2 = str.indexOf(")");
		String str2 = str.substring(brackets + 1, brackets2);// ���e���A���������e
		String str3 = str.substring(brackets2 + 1); // ���������q
		int brackets3 = str3.indexOf("(");
		int brackets4 = str3.indexOf(")");
		String str4 = str3.substring(brackets3 + 1, brackets4);// ���᭱�A���������e

		String[] fieldname = str2.split(",");
		String[] fieldvalue = str4.split(",");

		String sqlstr = "update " + tableName + " set ";
		StringBuffer sa = new StringBuffer();
		for (int i = 0; i < fieldname.length; i++) {
			if (fieldname[i].equals("keyno")) {
				continue;
			}
			sa.append(fieldname[i] + "=" + fieldvalue[i] + ",");
		}
		sa.setLength(sa.length() - 1);
		sqlstr = sqlstr + sa.toString() + where;

		return sqlstr;
	}
	/*
	����A�x�s
	*/
	public void saveAnnex(String idkey) throws Throwable{
		sa.append("����A�x�s\n");
		vt=new Vector();
		String[][]tableAnnex=getTableData("tableAnnex");
		
		//���okeyno��
		sql="select keyno from wptlproexte where idkey='"+idkey+"'";
		String[][]keynoArray=t.queryFromPool(sql);
		sa.append(sql+"\n");	
		String keyno=keynoArray[0][0];
		
		
		boolean isTrue=true;
		for(int i=0; i<tableAnnex.length; i++) {
			if("1".equals(tableAnnex[i][0])) {
				if(isTrue){
				//�R������
					sql="delete from wptlannex where keyno='"+keyno+"'";
					vt.add(sql);
					isTrue=false;
				}
				//���o���
				String sort=tableAnnex[i][6];//�Ƨ�
				String annexid=tableAnnex[i][3];//����N�X
				String description=tableAnnex[i][2];//��J��
		
				sql="insert into wptlannex(keyno,idkey,sort,annexid,description)"
					+" values "
					+"('"+keyno+"','"+idkey+"','"+sort+"','"+annexid+"','"+description+"')";
				vt.add(sql);
				}
		
			}
			execData(vt);//���ʸ�Ʈw
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
	/*
		�x�s-�ץ�{�Ǹg��ӿ���O����wptlproc_wrkman,�Y�s�ӿ���h�g�J��
	*/
	public void saveWptlprocWrkman(String idkey,String meid) throws Throwable {
		sa.append("�x�s-�ץ�{�Ǹg��ӿ���O����\n");
		vt=new Vector();
		talk t = getTalk("TradeMark");
		String mUser = getUser();
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String moddatetime=mdate+" "+mtime;
		
		sql = "select wrkman from wptlproc where idkey='" + idkey + "'"; // ���o�ӿ��
		String[][] str_wrkman = t.queryFromPool(sql);
		sa.append(sql+"\n");
		String wrkman = str_wrkman[0][0];
		String jobmark = "";
		if (wrkman.equals(mUser)) { // ��¾�ȥN�z��
			jobmark = "";
		} else {
			jobmark = "*";
		}			
		if(jobmark.length()>0) {
			sql="insert into wptlproc_wrkman(GCkey,idkey,meid,procWrkman,modDateTime,state)"
				+" VALUES ('"+getGcKey()+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(moddatetime)+",'MOD')";
			vt.add(sql);
		}
		execData(vt);//���ʸ�Ʈw
	}
	
	
	/*�s�ɮɨϥΡA�B�z���null�����D
	  �b�s�ɪ��a��A������[''�A�]��null�Ȯǥ[''�ܦ�'null'�A��Ʈw�L�k�����C
	*/
	public static String noDateToNull(String date) throws Throwable {
		if(date.length()< 8 || date.indexOf("1900")==0) {
			return null;
		} else {
			date="'"+date+"'";
			return date;
		}
	}
	
	
	/*���ogckey
	*�^�ǭȬOString�A�Y�h���I�s�|�۰�+1
	*/
	String gckey=null;  //key�b�~�O�i�H�֥[
	public String getGcKey()throws Throwable {
		talk t=getTalk("TradeMark");
		if(gckey==null) {
			sql = "select max(gckey) from wptlproc_wrkman";                //���ogckey�B�z
			String[][]ret = t.queryFromPool(sql);
			sa.append(sql+"\n");
			gckey= operation.add(ret[0][0].trim(),"1");//�r��[1�C
		} else {
			gckey= operation.add(gckey,"1");
		}
		if(gckey.length()<12) {
			gckey=convert.add0(gckey,"12");
		}
		return gckey;
	}
	


	/*
		��s�{����
	*/
	public void saveWptlproc(String appydate,String dbbouns,String rcvno,String idkey)throws Throwable {
		sa.append("��s�{����\n");
		vt=new Vector();
		//���o���
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String wrkdate = mdate+ " " + mtime; 			   //���oWrkdate

		sql="select keyno from wptlproexte where idkey='"+idkey+"'";
		String[][]keynoArray=t.queryFromPool(sql);
		sa.append(sql+"\n");
		String keyno=keynoArray[0][0].trim();
	
		//��s���
		sql="UPDATE wptlproc SET "
			+"appydate="+noDateToNull(appydate)
			+",dbbouns='"+dbbouns
			+"',rcvno='"+rcvno
			+"',keyno='"+keyno
			+"',wrkdate='"+wrkdate
			+"' WHERE idkey ='"+idkey+"'";
		vt.add(sql);
		execData(vt);//���ʸ�Ʈw
	
	}


/*
	��s�D��
	@���ӥx
*/

	public void saveWptmapply(String meid,String exteappydate)throws Throwable{
		sa.append("��s�D��\n");
		vt=new Vector();

		sql="UPDATE wptmapply SET "
		+"exteappydate='"+exteappydate
		+"'  WHERE meid ='"+meid+"'";
		vt.add(sql);
		execData(vt);//���ʸ�Ʈw

	}
	


	/*
		0302�e�U�H�Ǧ^�Ӫ���ơA�^�s��{���ɤΥD�ɡC
		@���ӥx
	*/
	
	public void saveWptlprocWptmapply()throws Throwable {
		sa.append("�e�U�H�Ǧ^�Ӫ���ơA�^�s��{���ɤΥD��\n");
		vt =new Vector();
		String[][]trustorTable=getTableData("trustorTable");
		for(int i=0; i<trustorTable.length; i++) {
	
			//�s�ɮɧ�swptlproc�����
			sql="UPDATE wptlproc SET "
				+"plsmanid ='"+trustorTable[i][2].trim()+"',"
				+"conname ='"+trustorTable[i][3].trim()+"',"
				+"conEmail ='"+trustorTable[i][4].trim()+"',"
				+"othid ='"+trustorTable[i][5].trim()+"',"
				+"specialtitle ='"+trustorTable[i][6].trim()+"',"
				+"janloc ='"+trustorTable[i][7].trim()+"',"
				+"janid ='"+trustorTable[i][8].trim()+"'"
				+" WHERE IDKey ='"+trustorTable[i][1].trim()+"'";
	
			vt.add(sql);
	
			//�s�ɮɧ�swptmapply����ơA�����O1�ɡC
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
		execData(vt);//���ʸ�Ʈw
	}
	
	/*
	�x�s�ӽФH�A�N��H,�ӽФH�@�w�n�����ʡA�~����olapkey����
	*/
	public void savePamanid(String meid)throws Throwable {
		sa.append("�x�s�ӽФH�A�N��H\n");
		vt= new Vector(); // �x�s�ӽФH�M�Ϊ�	
		String ispamanid=getValue("ispamanid".trim());//�ӽФH
		String[][] retPamanidTable = getTableData("pamanidTable");
		if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// �P�_�x�s�ӽФH
			//�ˮ�
			for (int i = 0; i < retPamanidTable.length; i++) {
				// �ˮ�:�P�_�ӽФH�s�����šC
				if (retPamanidTable[i][1].length() == 0) {
					message("�ӽФH�s������");
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
			sa.append(sql+"\n");
			//�R��wptmapman�����
			if(ontInPamanid.length>0){
			for(int i=0; i<ontInPamanid.length; i++) {
				sql="delete from wptlapman where meid='"+meid+"' and pamanid='"+ontInPamanid[i][0]+"'";
				vt.add(sql);
			}
			}
			//�s�Wwptmapman�����
			sql="select pamanid from wptlapman where meid='"+meid+"'";
			String[][] newWptlapmanPamanid=t.queryFromPool(sql);
			sa.append(sql+"\n");
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
		execData(vt);//���ʸ�Ʈw
	
		//�x�s�N��H
		vt=new Vector();
		if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// �x�s�ӽФH�P�_

			for (int i = 0; i < retPamanidTable.length; i++) {
				sql = "select lapkey from wptlapman where meid='" + meid + "' and pamanid ='" + retPamanidTable[i][0] + "'";
				String[][] ret_lapkey = t.queryFromPool(sql);
				sa.append(sql+"\n");
					if(ret_lapkey.length>0){
						sql="delete wptlapre where lapkey='"+ret_lapkey[0][0]+"'";
						vt.add(sql);
					    String[][]idTable=(String[][])get(retPamanidTable[i][0],new String[0][0]);
						for(int j=0;j<idTable.length;j++){
						sql = "insert into wptlapre (lapkey,id) values ('"+ret_lapkey[0][0]+"','" + idTable[j][0] + "')";
						vt.add(sql);
					}
					}
					// �x�s�N��H�A�s�Wwptlapre��lapkey,id�����
			} // ����i�j��
		} // �����x�s�N��H�P�_�C
	
		execData(vt);//���ʸ�Ʈw
	}


	/*	�ƥ��ӽФH
		@���ӥx
	*/
	
	public void backupPamanid(String idkey,String meid)throws Throwable {
		sa.append("�ƥ��ӽФH\n");
		vt=new Vector();
	
		//�R�����
		sql="DELETE FROM wptlappyOriApman  WHERE idkey ='"+idkey+"'";
		vt.add(sql);	
		
		sql = "select a.pamanid,b.uniChineseName,b.uniOriginName,b.unioriginaddr,b.unichineseaddr,a.lapkey "
					 +" from trademark..wptlapman a left join wp..wptmapman b on a.pamanid=b.pamanid "
					 +" where a.meid='"+meid+"' order by a.pamanid desc";				 
		
		String[][]pamanidTable=t.queryFromPool(sql);
		sa.append(sql+"\n");
		if(pamanidTable.length>0) {
			for(int i=0;i<pamanidTable.length;i++){
				//���o���
				String pamanid=pamanidTable[i][0].trim();//���P�H�s��
				String cmanname1=pamanidTable[i][1].trim();//���P�H�W(��)
				String manname1=pamanidTable[i][2].trim();//���P�H�W(��)
				String addr1=pamanidTable[i][3].trim();//���P�H�a�}(��)
				addr1=addr1.replace("/n","");
				String addr5=pamanidTable[i][4].trim();//���P�H�a�}(��)
				addr1=addr1.replace("/n","");
				String lapkey =pamanidTable[i][5].trim();//Key�s���N��H
				//���okeyno��
				sql="select keyno from wptlproexte where idkey='"+idkey+"'";
				String[][]keynoArray=t.queryFromPool(sql);
				sa.append(sql+"\n");
				String keyno=keynoArray[0][0];
		

				//�s�W���
				sql="INSERT INTO wptlappyOriApman (pamanidCHK,pamanid,cmanname1,manname1,addr1,addr5,lapkey,idkey,keyno)"
					+" values ('1','"+pamanid+"','"+cmanname1+"','"+manname1+"','"+addr1+"','"+addr5+"','"+lapkey+"','"+idkey+"','"+keyno+"')";
				vt.add(sql);
			}
		}
	
		execData(vt);//���ʸ�Ʈw
	}
	/*
		�j�M-�ӽФH�P�N��H�A�Ѽ�:���Ү׸�
	*/
	public void selectPamanidAndId(String meid)throws Throwable {
		sa.append("�j�M-�ӽФH�P�N��H\n");
		sql = "select a.pamanid,b.uniChineseName,b.uniOriginName,b.unioriginaddr,b.unichineseaddr,a.meid,a.lapkey "
			 +" from trademark..wptlapman a left join wp..wptmapman b on a.pamanid=b.pamanid "
			 +" where a.meid='"+meid+"' order by a.pamanid desc";
	
		String[][]pamanidTable = t.queryFromPool(sql);
		sa.append(sql+"\n");
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
			sa.append(sql+"\n");
			if(idTable.length>0){
				put(pamanidTable[i][0],idTable);
			}else{
				put(pamanidTable[i][0],null);
			}
	}
	
	}
	/*
	�ƥ�wptlappyoriapre�N��H
	@���ӥx
	*/
	
	public void saveWptlappyOriApman(String meid)throws Throwable {
		sa.append("�ƥ�wptlappyoriapre�N��H\n");
		vt=new Vector();
		//�j�M�N��H���
		sql="select a.id,b.unichineseprename,b.unioriginprename,a.lapkey "
			+" from trademark..wptlapre a left join wp..wptmapre b on a.id=b.id "
			+" where lapkey in "
			+" (select lapkey from trademark..wptlapman where  meid='"+meid+"')";
		//�R���¦���lapkey
		String[][]retLapkey=t.queryFromPool(sql);
		sa.append(sql+"\n");
		sql="delete from wptlappyoriapre where lapkey in "
			+" (select lapkey from trademark..wptlapman where  meid='"+meid+"')";
		vt.add(sql);
		//�s�W�s��lapkey
		if(retLapkey.length>0) {
			for(int i=0;i<retLapkey.length;i++){
			String id=retLapkey[i][0].trim();
			String unichineseprename=retLapkey[i][1].trim();
			String unioriginprename=retLapkey[i][2].trim();
			String lapkey=retLapkey[i][3].trim();
	
			//�s�W���
			sql="INSERT INTO wptlappyoriapre (idchk,id,oname,cname,lapkey) "
				+" values ('1','"+id+"','"+unichineseprename+"','"+unioriginprename+"','"+lapkey+"')";
			
			vt.add(sql);
			}
		}
		execData(vt);//���ʸ�Ʈw
	}
	
	/*
		�ƥ����ҥN�z�H
	*/
	public void backupMemanid(String idkey)throws Throwable{
		sa.append("�ƥ����ҥN�z�H\n");	
		//�R�����
		sql="DELETE FROM wptlappyOriMeman WHERE idkey ='"+ idkey+"'";
		vt.add(sql);
		//���okeyno��
		sql="select keyno from wptlproexte where idkey='"+idkey+"'";
		String[][]keynoArray=t.queryFromPool(sql);
		sa.append(sql+"\n");
		String keyno=keynoArray[0][0];	
		
		String[][]memanidTable=getTableData("memanidTable");
		for(int i=0;i<memanidTable.length;i++){
			//���o���
			String memanid=memanidTable[i][0].trim();//���ҥN�z�H�s��
			String memanname=memanidTable[i][1].trim();//���ҥN�z�H�W��
			//�s�W���
			sql="INSERT INTO wptlappyOriMeman (idkey,keyno,memanid,memanname,memanidCHK) "
				+" values ('"+idkey+"','"+keyno+"','"+memanid+"','"+memanname+"','1')";
			vt.add(sql);
		}
	execData(vt);
	
	}

	/*
		�奻@�奻�A���沧��
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
		del�{����wptlproc
	*/
	public void delWptlproc(String idkey)throws Throwable {
		sa.append("del�{����wptlproc\n");	
		vt=new Vector();
		//��s���
		sql="UPDATE wptlproc SET appydate=null,dbbouns='0',rcvno='',wrkdate=null,keyno='0' WHERE idkey ='"+idkey+"'";
		vt.add(sql);
		execData(vt);//���ʸ�Ʈw
	}	
	/*
	del ��s�D��
	@���ӥx
	*/

//	public void delWptmapply(String meid)throws Throwable{
//		sa.append("del ��s�D��\n");	
//		vt=new Vector();
//		sql="UPDATE wptmapply SET exteappydate=null WHERE meid ='"+meid+"'";
//		vt.add(sql);
//		execData(vt);//���ʸ�Ʈw
//
//	}
	/*
		�R��-�ץ�{�Ǹg��ӿ���O����
	*/
	public void delWptlprocWrkman(String idkey,String meid) throws Throwable {
		sa.append("�R��-�ץ�{�Ǹg��ӿ���O����\n");	
		vt=new Vector();
		talk t = getTalk("TradeMark");
		String mUser = getUser();
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String moddatetime=mdate+" "+mtime;
		
		sql = "select wrkman from wptlproc where idkey='" + idkey + "'"; // ���o�ӿ��
		String[][] str_wrkman = t.queryFromPool(sql);
		sa.append(sql+"\n");
		String wrkman = str_wrkman[0][0];
		String jobmark = "";
		if (wrkman.equals(mUser)) { // ��¾�ȥN�z��
			jobmark = "";
		} else {
			jobmark = "*";
		}			
		if(jobmark.length()>0) {
			sql="insert into wptlproc_wrkman(GCkey,idkey,meid,procWrkman,modDateTime,state)"
				+" VALUES ('"+getGcKey()+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(moddatetime)+",'DEL')";
			vt.add(sql);
		}
		execData(vt);//���ʸ�Ʈw
	}
	/*
		�R��wptlproexte
	*/
	public void delWptlproexte(String keyno)throws Throwable{	
		sa.append("�R��wptlproexte\n");		
		vt= new Vector();
		sql = "delete from wptlproexte where keyno='" + keyno + "'";
		vt.add(sql);
		execData(vt);//���ʸ�Ʈw
	}
	/*
		�ƥ��D��,�Ӽ�,�y�z�ʻ����A�ϥ�json
	*/
//	public void backupWptmapply(String meid,String idkey)throws Throwable {
//		sa.append("�ƥ��D��,�Ӽ�,�y�z�ʻ����A�ϥ�json\n");	
//		//�j�M���	
//		sql="SELECT markname,picchk,marktype,markmeid,markappyid,markregid,relchk,exteappydate,cdesctext,desctext "
//			+" FROM wptmapply"
//			+" WHERE meid ='"+meid+"'";
//					
//		String[][]retwptmapply=t.queryFromPool(sql);
//		sa.append(sql+"\n");
//		String sqlToArray=sqlToString(sql,retwptmapply);   //��k:�s�@��JSON���ɮ׮榡
//	//�NJSON������x�s�_��
//		sql="UPDATE wptlproc set bk='"+sqlToArray.toString()+"' where idkey='"+idkey+"'";
//		vt.add(sql);
//		execData(vt);//���ʸ�Ʈw			
//
//	}
	
	/*
		�ǤJsql��XJSON�榡���r��,�ǤJsql��ret�ɡC
	*/
//	public String sqlToString(String sqlstr,String[][] ret) {
//		sqlstr=sqlstr.toLowerCase();
//		sqlstr =sqlstr.replace("'","''");  //�s�W�@��'�ӡA�o�ˤ~��s�Jsql
//		int numSelect=sqlstr.indexOf("select");
//		int numFrom=sqlstr.indexOf("from");
//		int numWhere=sqlstr.indexOf("where");
//		String select=sqlstr.substring(numSelect+6,numFrom).trim();
//		String from=sqlstr.substring(numFrom+4,numWhere).trim();
//		String where=sqlstr.substring(numWhere+5).trim();
//		String[] selectToArray=select.split(",");
//		JSONObject jsonObject=new JSONObject();
//		jsonObject.put("table",from);
//		jsonObject.put("key",where);
//	
//		JSONArray jsonArray=new JSONArray();
//	
//		for(int j=0; j<ret.length; j++) {
//			JSONObject jsonObjToSelect=new JSONObject();
//			for(int i=0; i<selectToArray.length; i++) {
//				jsonObjToSelect.put(selectToArray[i],ret[j][i]);
//			}
//			jsonArray.add(jsonObjToSelect);
//		}
//	
//		jsonObject.put("field",jsonArray);
//		return jsonObject.toString();
//	}	
	/*
	�٭�-�D�ɡB�ӼСB�y�z�ʻ���
	*/
	
	public void revertMark(String idkey,String meid)throws Throwable {
		sa.append("�٭�-�D�ɡB�ӼСB�y�z�ʻ���\n");	
		vt=new Vector();
		sql="select bk from wptlproc where idkey='"+idkey+"'";
		String[][]bkArray=t.queryFromPool(sql);
		sa.append(sql+"\n");
		if(bkArray.length>0){
			String bk=bkArray[0][0].trim();
			bk =bk.replace("'","''");  //�s�W�@��'�ӡA�o�ˤ~��s�Jsql
			JSONObject object=JSONObject.fromObject(bk.trim());
			JSONArray field=object.getJSONArray("field");
			JSONObject data=field.getJSONObject(0);
		
			String markname=(String)data.get("markname");//�ӼЦW��
			String picchk=(String)data.get("picchk");//�ϼˤ����D�i�M���v
			String marktype=(String)data.get("marktype");//�Ӽк���
			String markmeid=(String)data.get("markmeid");//(���Ӽ�)���Ү׸�
			String markappyid=(String)data.get("markappyid");//�ӽи��X
			String markregid=(String)data.get("markregid");//���U��
			String relchk=(String)data.get("relchk");
			String exteappydate=(String)data.get("exteappydate");//�̫�@�����i�ӽФ�
			String cdesctext=(String)data.get("cdesctext");//�y�z�ʻ���(��)
			String desctext=(String)data.get("desctext");//�y�z�ʻ���
		
			//�x�s���
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
	/*
		�ƥ��٭�ӽФH�B�N��H
	*/
	public void revertWptlapman(String idkey)throws Throwable {
		sa.append("�ƥ��٭�ӽФH�B�N��H\n");	
		//�j�M���
		sql="SELECT a.pamanid,a.lapkey,b.meid FROM wptlappyOriApman a left join wptlproc b on a.idkey=b.idkey where a.idkey='"+idkey+"'";
		String[][]wptlappyOriApman=t.queryFromPool(sql);
		if(wptlappyOriApman.length>0) {
			for(int i=0; i<wptlappyOriApman.length; i++) {
				//���o���
				String pamanid=wptlappyOriApman[i][0].trim();//���P�H�s��
				String lapkey=wptlappyOriApman[i][1].trim();//Key�s���N��H(�۰ʨ���)
				String meid=wptlappyOriApman[i][2].trim();//�ǤJ\u53c2��txtidkey
				//��s���
				sql="UPDATE wptlapman SET "
					+"pamanid='"+pamanid
					+"',lapkey='"+lapkey
					+"' where meid='"+meid+"'";
				vt.add(vt);
	
				//�R�����
				sql="DELETE FROM wptlapre where lapkey='"+lapkey+"'";
				vt.add(vt);
				//�j�M���
				sql="SELECT id FROM wptlappyOriApre where lapkey='"+lapkey+"'";
				String[][]idArray=t.queryFromPool(sql);
				if(idArray.length>0) {
				for(int j=0; i<idArray.length; i++) {
						String id=idArray[j][0].trim();
						//�s�W���
						sql="INSERT INTO wptlapre (id,lapkey) values ('"+id+"','"+lapkey+"')";
						vt.add(vt);
						//�R�����
						sql="DELETE FROM wptlappyOriApman where lapkey='"+lapkey+"'";
						vt.add(vt);
					}
				}
			}
			//�R�����
			sql="DELETE FROM wptlappyOriApman where idkey='"+idkey+"'";
			vt.add(vt);		
		}
		execData(vt);	
	}
	/*
		�^�_�N�z�H
	*/
	
	public void revertMemanid(String meid,String idkey)throws Throwable{
		sa.append("�^�_�N�z�H\n");	
		vt=new Vector();
		sql="select memanid from wptlappyorimeman where idkey='"+idkey+"'";
		String[][]memanidArray=t.queryFromPool(sql);
		if(memanidArray.length>0){
			sql = "delete from wptlmeman where meid='" + meid + "'";
			vt.add(sql);		
			for(int i=0;i<memanidArray.length;i++){
				String memanid=memanidArray[i][0].trim();
				sql = "insert into wptlmeman (meid,memanid) values ('" + meid + "','" +memanid+"')";
				vt.add(sql);
			}
		sql="delete from wptlappyorimeman where idkey='"+idkey+"'";
		vt.add(sql);
		}
		execData(vt);	
	}
	/*
		json   1.�ƥ��D��,�Ӽ�,�y�z�ʻ���  2.�ƥ�goodclass
	*/
	public void backupWptmapplyGoodclass(String meid,String idkey)throws Throwable  {
		sa.append("1.�ƥ��ӫ~���O��2.�ƥ��D��,�Ӽ�,�y�z�ʻ����A�ϥ�json\n");
		JSONObject jsonObject=new JSONObject();
	
		sql="Select a.meid,a.Goodclass,a.uniNOrigdname,a.uniNChgdname,a.delmk,'"+idkey
			+"' as 'idkey' from wptlapbale a where a.meid='"+meid
			+"' and a.delmk<>1";
	
		jsonObject.put("wptlapbale",jsonArray(sql));//�ӫ~���O��
		sql="SELECT markname,picchk,marktype,markmeid,markappyid,markregid,relchk,exteappydate,cdesctext,desctext "
			+" FROM wptmapply"
			+" WHERE meid ='"+meid+"'";
	
		jsonObject.put("wptmapply",jsonArray(sql));//�D��
	
		//�NJSON������x�s�_��
		sql="UPDATE wptlproc set bk='"+jsonObject.toString()+"' where idkey='"+idkey+"'";
		vt.add(sql);
		execData(vt);//���ʸ�Ʈw
	
	}
	/*
		���ojsonArray����k
	*/
	public String jsonArray(String sqlstr)throws Throwable {
	
		String[][]ret=t.queryFromPool(sqlstr);
		sa.append(sql+"\n");
	
		sqlstr=sqlstr.toLowerCase();
//		sqlstr =sqlstr.replace("'","''");  //�s�W�@��'�ӡA�o�ˤ~��s�Jsql
	
		int numSelect=sqlstr.indexOf("select");
		int numFrom=sqlstr.indexOf("from");
		String select=sqlstr.substring(numSelect+6,numFrom).trim();
		String[] selectToArray=select.split(",");
	
		JSONArray jsonArray=new JSONArray();
	
		for(int i=0; i<ret.length; i++) {
			JSONObject jsonObjToSelect=new JSONObject();
			for(int j=0; j<selectToArray.length; j++) {
				jsonObjToSelect.put(selectToArray[j],ret[i][j]);
			}
			jsonArray.add(jsonObjToSelect);
		}
		return jsonArray.toString();
	
	}
}
