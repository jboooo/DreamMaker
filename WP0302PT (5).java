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
	
	Vector vt=null;
	public boolean action(String value)throws Throwable{
		// �^�ǭȬ� true ��ܰ��汵�U�Ӫ���Ʈw���ʩάd��
		// �^�ǭȬ� false ��ܱ��U�Ӥ����������O
		// �ǤJ�� value �� "�s�W","�d��","�ק�","�R��","�C�L","PRINT" (�C�L�w�����C�L���s),"PRINTALL" (�C�L�w���������C�L���s) �䤤���@
		t=getTalk("TradeMark");
		w=getTalk("wp");
		vt=new Vector();
				
		sa=new StringBuffer();
		sb=new StringBuffer();
		sc=new StringBuffer();
		sd=new StringBuffer();
				
		if(value.equals("�d��")){
			Query();
		}else if(value.equals("�ק�")){
			Save();
		}else if(value.equals("�R��")){
			Delete();
		}

		return false;
	}
	public boolean Query()throws Throwable {
	String sql="";
	//���oidkey
	String idkey=getQueryValue("idkey").trim();
	setValue("idkey",idkey.toUpperCase());
	//���orcvid
	sql="select rcvid,meid from wptlproc where idkey='"+idkey+"'";
	String[][] retIdkey=t.queryFromPool(sql);

	String rcvid=retIdkey[0][0].trim();
	String meid=retIdkey[0][1].trim();
	setValue("rcvid",rcvid.toUpperCase());
	setValue("meid",meid.toUpperCase());


	selectTable1(rcvid);//�j�Mtable1���
	//�N�e���Ĥ@��meid�]�w�b�j�M�дڮM�ո̡C
	String[][]table1=getTableData("table1");
	String strMeid=table1[0][2].trim();
	String strRcvid=table1[0][12].trim();
	
	selectDebit(strMeid,strRcvid);//�j�M�дڮM��

	selectMemanid(meid);//�j�M���ҥN�z�H
	selectPamanidAndId(meid);//�j�M�ӽФH�ΥN��H
	selectWptlproexte(idkey);//�j�M������s
	selectAnnex(idkey);//�j�M����
	setValue("field1",sa.toString());
	return true;
	}
	
	public boolean Save()throws Throwable {
	
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
			if(keynoArray.length<=1) {
				backupPamanid(idkey,meid);	//�ƥ��ӽФH
				saveWptlappyOriApman(meid);//�ƥ��N��H
				backupMemanid(idkey);//�ƥ����ҥN�z�H
				backupWptmapply(meid,idkey);//�ƥ��D��,�Ӽ�,�y�z�ʻ����A�ϥ�json
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
	
		setValue("field1",sa.toString());//�O���Ҧ����ʪ�sql
	
		return true;
	}
	/*
		�R��
		@���ӥx
	*/	
		public boolean Delete()throws Throwable{
				
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

		}
		setValue("field1",sa.toString());//�O���Ҧ����ʪ�sql	
		return true;
	}
		
	/*
		�j�Mtable1���
	*/
	public void selectTable1(String rcvid)throws Throwable {
	
		//���]�w
		String sql="select distinct '','a'=case when d.idkey is null then '�_' else '�O' end,a.meid,"
			+"b.regid,b.markname,b.extedead,a.appydate,a.dbbouns,a.rcvno,a.idkey,'�ק�','',a.rcvid,d.keyno,''"
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
		//�N����榡��
		for(int i=0; i<retWptlproc.length; i++) {
			retWptlproc[i][5]=dateformat(retWptlproc[i][5]);
			retWptlproc[i][6]=dateformat(retWptlproc[i][6]);
		}
	
		setTableData("table1",retWptlproc);
	}
	
	/*
		�j�M�дڮM�����
	*/
	public void selectDebit(String meid,String rcvid)throws Throwable {
		//���]�w
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
		�j�M-�M�˥��ҥN�z�H(�h��)�A�ǤJ�ѼƬO���Ү׸�	
	*/
	public void selectMemanid(String meid)throws Throwable{

		String sql="select memanid,memanname from wp..wptcmeman where memanid in(select memanid from trademark..wptlmeman where meid='"+meid+"') and tm='1'";
		String[][] ret_memanid = t.queryFromPool(sql);
		setTableData("memanidTable", ret_memanid);
	}


	/*
		�j�M-���w�ӫ~�A�Ѽ�:���Ү׸�	
	*/
	public void selectGoodClass(String meid)throws Throwable{	
	
	//���w�ӫ~
	String sql = "Select Goodclass,uniNOrigdname,uniNChgdname,delmk,'','',Goodclass,uniNOrigdname,uniNChgdname from wptlapbale where meid='"
		  + meid + "' and delmk<>1";
	String[][] ret_goodclass = t.queryFromPool(sql);
	sa.append("\n�j�M���w�ӫ~\n" + sql); // �ˬd�I		
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
		old�j�M-�ӽФH�P�N��H�A�Ѽ�:���Ү׸�
	*/
	public void oldselectPamanidAndId(String meid)throws Throwable{	
	//�ӽФH�P�N��H
	//�j�M�ӽФH�s���Alapkey�bwptlapman�A���󬰥��Ү׸�,�]�w�btable1
		String sql = "select '',pamanid,'','','','',meid,'','','',lapkey,'' from wptlapman  where meid='" + meid
		      + "' order by pamanid desc";
		String[][]ret = t.queryFromPool(sql);
		sa.append("\n�j�M�ӽФH\n" + sql); // �ˬd�I		;
		sa.append("\n���:���Ү׸��A�ӽФHid�Alapkey\n" + sql);	     	// �ˬd
		if (ret.length > 0) {
			for (int i = 0; i < ret.length; i++) {// �Q�ΥӽФH�s���j�M�ӽФH�W�١A�]�w�b���
				sql = "select b.uniChineseName,b.uniOriginName,unioriginaddr,unichineseaddr from wptmapman b WHERE pamanid='" + ret[i][1].trim()
				      + "' order by b.uniChineseName";
				String[][] ret_Name = w.queryFromPool(sql);
				sa.append("\n�j�M�N��H\n" + sql); // �ˬd�I		
				sa.append("\n�]�w�ӽФH�W��(��),�]�w�ӽФH�W��(��)\n"+sql);              //�ˬd
				if (ret_Name.length > 0) {
					ret[i][2] = ret_Name[0][0]; // �]�w�ӽФH�W��(��)
					ret[i][3] = ret_Name[0][1]; // �]�w�ӽФH�W��(��)
					ret[i][4] = ret_Name[0][2]; // �]�w�ӽФH�a�}(��)
					ret[i][5] = ret_Name[0][3]; // �]�w�ӽФH�a�}(��)
				}
				if (ret[i][10].length() > 0) {
					sql = "select id from wptlapre where lapkey='" + ret[i][10] + "'";
					String[][] ret_id = t.queryFromPool(sql);
					sa.append("\nlapkey\n"+sql);                          //�ˬd
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
		�j�M-������s	
	*/
	public void selectWptlproexte(String idkey)throws Throwable{	

		//�j�M���
		String sql="SELECT pamanid,prename1,desctext,goodname1,marktype,markmeid,markname,picchk,keyno FROM wptlproexte WHERE idkey ='"+ idkey+"'";
		String[][]retwptlproexte=t.queryFromPool(sql);
		sa.append("\n�j�M-������s\n"+sql);
		if(retwptlproexte.length>0){
		//�]�w���
		setValue("ispamanid",retwptlproexte[0][0].trim());    //ispamanid//�ӽФH
		sa.append("\n�j�M������s\n" + sql); // �ˬd�I		
		
		}
	}

	/*
		�j�M-����	
	*/
	public void selectAnnex(String idkey)throws Throwable{		
		
		String sql="select '�t��'=case when l.annexid is null then '�_' else '�O' end ,c.annex_name,isnull(l.description,''),"
			+"c.annexid,c.desc_format,desc_default,c.sort"
			+" from wptcannex c"
			+" left join wptlannex l on l.annexid = c.annexid and l.idkey='"+idkey
			+"' where c.procid ='0701' order by sort";
		String[][]retAnnex=t.queryFromPool(sql);
		sa.append("\n�j�M����\n" + sql); // �ˬd�I
		if(retAnnex.length>0){
			setTableData("tableAnnex",retAnnex);
		}
	}		
	/*
		�x�s���ҥN�z�H
	*/
	public void saveMemanid(String meid)throws Throwable{
		vt=new Vector();
//		sa.append("\n-------���ҥN�z�H�A�x�s------:\n"); // �ˬd�I
		String[][] ret_table3 = getTableData("memanidTable");
		if (ret_table3.length != 0) {
			String sql = "delete from wptlmeman where meid='" + meid + "'";
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
		sc =new StringBuffer();
		sc.append(str);
		
		//���o���
		String ispamanid=getValue("ispamanid".trim());//�ӽФH
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
			String sql = "select top 1 keyno from wptlproexte where left(keyno,2)='10' and  len(keyno)='10' order by keyno desc";                //���okeyno�B�z
			String[][]ret = t.queryFromPool(sql);
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
					String sql = insertToUpdate(keyno, strArray[i]);
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
	����A�x�s

*/

	public void saveAnnex(String idkey) throws Throwable{
	vt=new Vector();
	String[][]tableAnnex=getTableData("tableAnnex");
	String sql="";
	
	//���okeyno��
	sql="select keyno from wptlproexte where idkey='"+idkey+"'";
	String[][]keynoArray=t.queryFromPool(sql);
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
			String sql = "select wrkman from wptlproc where idkey='" + idkey + "'"; // ���o�ӿ��
			String[][] str_wrkman = t.queryFromPool(sql);
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
//		}
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
			String sql = "select max(gckey) from wptlproc_wrkman";                //���ogckey�B�z
			String[][]ret = t.queryFromPool(sql);
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
		vt=new Vector();
		//���o���
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String wrkdate = mdate+ " " + mtime; 			   //���oWrkdate

		String sql="select keyno from wptlproexte where idkey='"+idkey+"'";
		String[][]keynoArray=t.queryFromPool(sql);
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
		vt=new Vector();

		String sql="UPDATE wptmapply SET "
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
		vt =new Vector();
		String[][]trustorTable=getTableData("trustorTable");
		for(int i=0; i<trustorTable.length; i++) {
	
			//�s�ɮɧ�swptlproc�����
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
		vt= new Vector(); // �x�s�ӽФH�M�Ϊ�	
		String ispamanid=getValue("ispamanid".trim());//�ӽФH
		String[][] retPamanidTable = getTableData("pamanidTable");
		String sql="";
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
		vt=new Vector();
		String sql="";
	
		//�R�����
		sql="DELETE FROM wptlappyOriApman  WHERE idkey ='"+idkey+"'";
		vt.add(sql);	
		
		sql = "select a.pamanid,b.uniChineseName,b.uniOriginName,b.unioriginaddr,b.unichineseaddr,a.lapkey "
					 +" from trademark..wptlapman a left join wp..wptmapman b on a.pamanid=b.pamanid "
					 +" where a.meid='"+meid+"' order by a.pamanid desc";				 
		
		String[][]pamanidTable=t.queryFromPool(sql);
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
	�ƥ�wptlappyoriapre�N��H
	@���ӥx
	*/
	
	public void saveWptlappyOriApman(String meid)throws Throwable {
		vt=new Vector();
		//�j�M�N��H���
		String sql="select a.id,b.unichineseprename,b.unioriginprename,a.lapkey "
			+" from trademark..wptlapre a left join wp..wptmapre b on a.id=b.id "
			+" where lapkey in "
			+" (select lapkey from trademark..wptlapman where  meid='"+meid+"')";
		//�R���¦���lapkey
		String[][]retLapkey=t.queryFromPool(sql);
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
	
	//�R�����
	String sql="DELETE FROM wptlappyOriMeman WHERE idkey ='"+ idkey+"'";
	vt.add(sql);
	//���okeyno��
	sql="select keyno from wptlproexte where idkey='"+idkey+"'";
	String[][]keynoArray=t.queryFromPool(sql);
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
		vt=new Vector();
		//��s���
		String sql="UPDATE wptlproc SET appydate=null,dbbouns='0',rcvno='',wrkdate=null,keyno='0' WHERE idkey ='"+idkey+"'";
		vt.add(sql);
		execData(vt);//���ʸ�Ʈw
	}	
	/*
	del ��s�D��
	@���ӥx
	*/

	public void delWptmapply(String meid)throws Throwable{
		vt=new Vector();
		String sql="UPDATE wptmapply SET exteappydate=null WHERE meid ='"+meid+"'";
		vt.add(sql);
		execData(vt);//���ʸ�Ʈw

	}
	/*
		�R��-�ץ�{�Ǹg��ӿ���O����
	*/
	public void delWptlprocWrkman(String idkey,String meid) throws Throwable {
		vt=new Vector();
		talk t = getTalk("TradeMark");
		String mUser = getUser();
		String mdate = datetime.getToday("YYYY/mm/dd");
		String mtime = datetime.getTime("h:m:s");
		String moddatetime=mdate+" "+mtime;
		
		String sql = "select wrkman from wptlproc where idkey='" + idkey + "'"; // ���o�ӿ��
		String[][] str_wrkman = t.queryFromPool(sql);
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
		vt= new Vector();
		String sql = "delete from wptlproexte where keyno='" + keyno + "'";
		vt.add(sql);
		execData(vt);//���ʸ�Ʈw
	}
	/*
		�ƥ��D��,�Ӽ�,�y�z�ʻ����A�ϥ�json
	*/
	public void backupWptmapply(String meid,String idkey)throws Throwable {
		//�j�M���	
		String sql="SELECT markname,picchk,marktype,markmeid,markappyid,markregid,relchk,exteappydate,cdesctext,desctext "
					+" FROM wptmapply"
					+" WHERE meid ='"+meid+"'";
					
		String[][]retwptmapply=t.queryFromPool(sql);
		String sqlToArray=sqlToString(sql,retwptmapply);   //��k:�s�@��JSON���ɮ׮榡
	//�NJSON������x�s�_��
		sql="UPDATE wptlproc set bk='"+sqlToArray.toString()+"' where idkey='"+idkey+"'";
		vt.add(sql);
		execData(vt);//���ʸ�Ʈw			

	}
	
	/*
		�ǤJsql��XJSON�榡���r��,�ǤJsql��ret�ɡC
	*/
	public String sqlToString(String sql,String[][] ret) {
		sql=sql.toLowerCase();
		sql =sql.replace("'","''");  //�s�W�@��'�ӡA�o�ˤ~��s�Jsql
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
	�٭�-�D�ɡB�ӼСB�y�z�ʻ���
	*/
	
	public void revertMark(String idkey,String meid)throws Throwable {
		vt=new Vector();
		String sql="select bk from wptlproc where idkey='"+idkey+"'";
		String[][]bkArray=t.queryFromPool(sql);
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
	
}
