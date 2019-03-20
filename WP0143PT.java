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
		// �^�ǭȬ� true ��ܰ��汵�U�Ӫ���Ʈw���ʩάd��
		// �^�ǭȬ� false ��ܱ��U�Ӥ����������O
		// �ǤJ�� value �� "�s�W","�d��","�ק�","�R��","�C�L","PRINT" (�C�L�w�����C�L���s),"PRINTALL" (�C�L�w���������C�L���s) �䤤���@
		sa=new StringBuffer();
		t=getTalk("TradeMark");	
		w=getTalk("wp");
		
		if("�d��".equals(value)){
			Query();
		}else if("�ק�".equals(value)){
			Update();		
		}else if("�R��".equals(value)){
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
		sa.append("\ndebitchk,debitid,ltdate,debitdate,meid,rcvid,findate,appydate,idkey\n"+sql);//�ˬd
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
		sa.append("\nendcause,giveupchk,AgreeRegNo,AgreeRegistrant,chkdoc,Ownedate,Ownsdate,Regid,Appydate,Regdate,Extesay,Extecanceldate\n"+sql);//�ˬd
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
		sa.append("\nmemo:\n"+sql);                           //�ˬd
		setTableData("memoTable",ret_memo);	
		
		//�ӽФH�P�N��H
		//�j�M�ӽФH�s���Alapkey�bwptlapman�A
		sql = "select '',meid,pamanid,'','','','','',lapkey from wptlapman  where meid='" + meid
		      + "' order by pamanid desc";
		String[][] ret = t.queryFromPool(sql);
		sa.append("\n���:���Ү׸��A�ӽФHid�Alapkey\n" + sql);	     	// �ˬd
		if (ret.length > 0) {
			for (int i = 0; i < ret.length; i++) {// �Q�ΥӽФH�s���j�M�ӽФH�W�١A�]�w�b���
				sql = "select b.uniChineseName,b.uniOriginName from wptmapman b WHERE pamanid='" + ret[i][2].trim()
				      + "' order by b.uniChineseName";
				String[][] ret_Name = w.queryFromPool(sql);
				sa.append("\n�]�w�ӽФH�W��(��),�]�w�ӽФH�W��(��)\n"+sql);              //�ˬd
				if (ret_Name.length > 0) {
					ret[i][3] = ret_Name[0][0]; // �]�w�ӽФH�W��(��)
					ret[i][4] = ret_Name[0][1]; // �]�w�ӽФH�W��(��)
				}
				if (ret[i][8].length() > 0) {
					sql = "select id from wptlapre where lapkey='" + ret[i][8] + "'";
					String[][] ret_id = t.queryFromPool(sql);
					sa.append("\nlapkey\n"+sql);                          //�ˬd
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

		
		//�j�M���ҥN�z�H(�h��)�A�Ӧ�wptlmeman,����O���Ү׸�
		sql = "select memanid from wptlmeman where meid='" + meid + "'";
		String[][] ret_memanid = t.queryFromPool(sql);
		sa.append("\n�N�z�Hid\n" + sql);	                 // �ˬd	
		StringBuffer sb = new StringBuffer();
		if (ret_memanid.length > 0) {
			for (int i = 0; i < ret_memanid.length; i++) {
				sb.append("'" + ret_memanid[i][0] + "',");
			}
			sb.setLength(sb.length() - 1);
			sql = "select memanid,memanname from wptcmeman where memanid in(" + sb.toString() + ") and tm='1'";
			sa.append("\n�N�z�Hid�Ωm�W\n" + sql);	                 // �ˬd	
			String[][] ret_memanname = w.queryFromPool(sql);
			if (ret_memanname.length > 0) {
				setTableData("memanidTable", ret_memanname);
			}
		} // �j�M�N�z�Hif����
		
		//�Ӽ�
		sql = "SELECT Cdesctext,desctext,markname,markpic,marktype,picchk,agreeregistrant,agreeregno FROM wptmapply where meid='"
		      + meid + "'";
		ret = t.queryFromPool(sql);
			sa.append("\n����y�z�ʻ���,�y�z�ʻ���,�ӼЦW��,�ӼйϮ�path,�Ӽк���,�ϼˤ����D�i�M�Q�v,�P�N�ѰӼ��v�H,�P�N�ѵ��U����\n" + sql);	  // �ˬd			
		if (ret.length > 0) {
			setValue("markname", ret[0][2]); // �ӼЦW��
			setValue("markpic", ret[0][3]); // �ӼйϮ�path
			setValue("marktype", ret[0][4]); // �Ӽк���
			setValue("picchk", ret[0][5]); // �ϼˤ����D�i�M�Q�v
			setValue("agreeregistrant", ret[0][6]); // �P�N�ѰӼ��v�H
			setValue("agreeregno", ret[0][7]); // �P�N�ѵ��U����
		//�y�z�ʻ���
			setValue("Cdesctext", ret[0][0]); // ����y�z�ʻ���
			setValue("desctext", ret[0][1]); // �y�z�ʻ���
		}		
		
		//���w�ӫ~
		sql = "Select Goodclass,uniNOrigdname,uniNChgdname,delmk,'','',Goodclass,uniNOrigdname,uniNChgdname from wptlapbale where meid='"
		      + meid + "' and delmk<>1";
		String[][] ret_goodclass = t.queryFromPool(sql);
		// �ˬd
		sa.append("\n�j�Mwptlapbale,���w�ӫ~\n" + sql);
		if (ret_goodclass.length > 0) {
			setTableData("goodclassTable", ret_goodclass);
			// �]�w�Ĥ@����ƨ�e���W
			setValue("checkclass", ret_goodclass[0][0]); // �ΨӰO�����O�N���A�s�ɪ��ɫ�ϥΪ��C
			setValue("goodclass_field", ret_goodclass[0][0]);
			setValue("uniNOrigdname", ret_goodclass[0][1]);
			setValue("uniNChgdname", ret_goodclass[0][2]);
		}
	
		// �j�M�d�����
		sql = "select pamanid,desctext,goodname1,goodname2,markname from wptladjchgaft where idkey='" + idkey+ "'";
		String[][] ret_check = t.queryFromPool(sql);
		sa.append("\n�j�M�d�����\n" + sql);                       //�ˬd
		if (ret_check.length != 0) {
			setValue("ispamanid", ret_check[0][0]); // �ӽФH
			setValue("isdesctext", ret_check[0][1]); //�y�z�ʻ���
			setValue("goodname1", ret_check[0][2]); //���w�ӫ~1
			setValue("goodname2", ret_check[0][3]); //���w�ӫ~2
			setValue("ismarkname", ret_check[0][4]); //�Ӽ�
		}
		
//		//�٭�bk�������
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
//			sa.append("\n�B�z���{���٭�BK�Asql\n"+sql);
//			for(int i=0; i<retWptlprocProcid.length; i++) {  //����e�A����ק�C
//				retWptlprocProcid[i][0]="1";
//				setEditable("procidTable",i,0,false);
//			}
//		} else { 
			//wptlproc�{���ɿ�ܧ����C
			sql="usp_relparent '"+meid+"'";
			String[][]meidIn=t.queryFromPool(sql);
		
			sql="select '',idkey,findate,wrkdate,wrkman,meid,lawdate,procname,empname"
				+" from wptlproc PL left join wptcproc PC on PL.procid=PC.procid left join wp..wptmemp E on PL.wrkman=E.empid "
				+" where meid in ('"+meidIn[0][0]+"') and findate is null and len(replace(meid,' ',''))>0";
		
			retWptlprocProcid=t.queryFromPool(sql);
			sa.append("\n�B�z���{��sql\n"+sql);
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

	
		//�]�w�n�ꪺ���
		if(!findateToOpen(findate)) {
			setEditableField();	//�����
		} else {
			setEditableField();	//�����
			//����ɷ|���}��
			setEditable("memoTable",true);
			setEditable("chkdoc",true);
			setEditable("appydate",true);
			setEditable("agree",true);
		
			// memo�O�_�i�H�g�J
			if (ret_memo.length > 0) {
				for (int i = 0; i < ret_memo.length; i++) {
					if (!ret_memo[i][3].equals(idkey)) {
						setEditable("memoTable", i, 1, false);
					}
				}
			}
		
			// �j�M�d�����
			if (ret_check.length > 0) {
				setEditable("memanidTable", true);          //�N�z�H
				if (ret_check[0][0].equals("1")) {                              // �ӽФH
					setEditable("ispamanid", false);
					setEditable("pamanidTable", "pamanid", true);
					addScript("EMC['pamanidTable'].getButton(0).setEditable(true);");// �s�W,�}��
					addScript("EMC['pamanidTable'].getButton(2).setEditable(true);");// �R��,�}��
					setEditable("pamanidTable", "btn", true);
				}else{
					setEditable("ispamanid", true);				
				}

				if (ret_check[0][1].equals("1")) {                             //�y�z�ʻ���
					setEditable("Cdesctext", true);
					setEditable("desctext", true);
				}else{
					setEditable("isdesctext", true);
				}
				if (ret_check[0][2].equals("1")) {                                 //���w�ӫ~1
					setEditable("goodclass_field", true);
				}else{
					setEditable("goodname1", true);
				}
				if (ret_check[0][3].equals("1")) {                                 //���w�ӫ~2

					setEditable("uniNOrigdname", true);
					setEditable("uniNChgdname", true);
				}else{
					setEditable("goodname2", true);
				}
				if (ret_check[0][4].equals("1")) {                               //�Ӽ�
					setEditable("agreeregistrant", true);// �֤J�Ӽ��v�H
					setEditable("agreeregno", true);// �֦b���U��
				}else{
					setEditable("ismarkname", true);// �֦b���U��
				
				}
			}else{
				setEditable("memanidTable", true);          //�N�z�H
				setEditable("ispamanid", true);				
				setEditable("isdesctext", true);
				setEditable("goodclass_field", true);
				setEditable("goodname1", true);
				setEditable("goodname2", true);
				setEditable("ismarkname", true);// �֦b���U��
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
		
		Date date = new Date();  //���o�u�@���
        DateFormat dateformat= new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String wrkdate=dateformat.format(date);
		String mUser=getUser();		//���o�ާ@��		
		
		sql="select wrkman from wptlproc where idkey='"+idkey+"'";//���o�ӿ��
		String[][]str_wrkman=t.queryFromPool(sql);
		String wrkman=((str_wrkman.length>0)?str_wrkman[0][0]:"");

		String jobmark=((wrkman.equals(mUser))?"":"*");		//��¾�ȥN�z��
		String appydate=getValue("appydate").trim();//�󥿴��X��

		//�ӼХD�ɭק�
		String chkok=getValue("chkok").trim();
		String Auditid=getValue("Regid").trim();//�s���U���X
		String Auditdate=getValue("Regdate").trim(); //�f�w���i��A�s���U���i��
		String Regdate=getValue("Regdate").trim();//���U���i��
		String Regid=getValue("Regid").trim(); //���U���X
		String Ownsdate=getValue("Ownsdate").trim();//�M�ΰ_�l��
		String Ownedate=getValue("Ownedate").trim();//�M�κ�����
		String Regstat= (appydate.equals("")?"V":(chkok.equals("1")?"V":"A"));//�ҮѪ��A
		String Chkdoc=getValue("Chkdoc").trim();//���ܭn�ҮѦ������i
		String AgreeRegNo=getValue("AgreeRegNo").trim();//���U����
		String meid=getValue("meid").trim();//���Ү׸�
		String Extesay="2";   //���q�����i
		String Saydate=getValue("Saydate").trim();//���ܤ��
		
		if(Chkdoc.equals("1") && Saydate.equals("")){
			message("���ܭn�ҮѦ������i���Ŀ�A���ܤ�������n����");
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
		
		//�ק�ץ�{�ǰO����
		String Findate=("".equals(appydate)?wrkdate:"");//���{�ǧ�����
		String[][]ret_memo=getTableData("memoTable");//�Ƶ�
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
		
		//��s�e�U�H���C
		String[][]retTrustortable=getTableData("trustorTable");
		if(retTrustortable.length>0){
			for(int i=0; i<retTrustortable.length; i++) {
				//��swptlproc�e�U�H�����
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
				sa.append("\n----�e�U�H���---\n�s�ɮɧ�swptlproc�����:\n"+sql);	        //�ˬd
				
				//��swptmapply�e�U�H����ơA�����O1�ɡC
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
					sa.append("\n��swptmapply�e�U�H����ơA�����O1��:\n"+sql+"\n");	        //�ˬd
				}
			}
		}
		//�ק�ӽФ��N�X��010G�άO010F�A�N�ӽФ����{�ǧאּ�w���U�C
		sql="Update wptlproc set"
			+" procid='"+("A".equals(Regstat)?"010F":"010G")//TODO
			+"' where idkey=(Select ing_idKey from Wptlproposal where meid='"+meid+"')";
		sa.append("\n�ק�ӽФ��N�X��010G�άO010F�A�N�ӽФ����{�ǧאּ�w���U�C\n"+sql);
		
	//�x�s���ĸ��wptladjchgaft
		sa.append("\n-------�x�s���ĸ��wptladjchgaft�A�x�s------:\n"); // �ˬd�I
		String ispamanid = getValue("ispamanid").trim(); // �O�ӽФH�N��H
		String isdesctext = getValue("isdesctext").trim();
		String goodname1 = getValue("goodname1").trim();
		String goodname2 = getValue("goodname2").trim();
		String ismarkname = getValue("ismarkname").trim();
	
		sql = "select * from wptladjchgaft where idkey='" + idkey + "'";
		String[][] ret_wptladjchgaft = t.queryFromPool(sql);
		sa.append("\nwptladjchgaft�̬O�_�����?\n" + (ret_wptladjchgaft.length != 0)); // �ˬd�I
		if (ret_wptladjchgaft.length != 0) {
			sql = "update wptladjchgaft set " + "pamanid='" + ispamanid + "',"
				  + "desctext='" + isdesctext + "'," + "goodname1='" + goodname1 + "'," + "goodname2='" + goodname2
				  + "'," + "markname='" + ismarkname + "'" + " where rcvid='" + rcvid + "'";
			vt.add(sql);
			sa.append("\n��swptladjchgaft:\n" + sql); // �ˬd�I
		} else {
			sql = "insert into wptladjchgaft (meid,pamanid,idkey,desctext,goodname1,goodname2,markname,rcvid) values ('"
				  + meid + "','" + ispamanid + "','" +idkey + "','" + isdesctext + "','"
				  + goodname1 + "','" + goodname2 + "','" + ismarkname + "','" + rcvid + "')";
			vt.add(sql);
			sa.append("\n�s�Wwptladjchgaft:\n" + sql); // �ˬd�I
		}
	
	//�ץ�{�Ǹg��ӿ��������:
		//�ץ�{�Ǹg��ӿ���O����,�Y�s�ӿ���h�g�J��
		if(jobmark.length()>0) {
			sql="insert into wptlproc_wrkman(GCkey,IDkey,meid,procWrkman,modDateTime,state)"
				+" VALUES ('"+getGcKey()+"','"+idkey+"','"+meid+"','"+mUser+"',"+noDateToNull(wrkdate)+",'MOD')";
			sa.append("\n�ץ�{�Ǹg��ӿ���O����,�Y�s�ӿ���h�g�J��\n"+sql);
			vt.add(sql);
		}

	//�ק�B�z���{��:
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
			sa.append("\n�ק�B�z���{��:sql:\n"+sql);
		}
	}
	String[][]retBK=(String[][])vtBK.toArray(new String[0][0]);
	String sqlToArray=sqlToString("select idkey,findate,wrkdate,wrkman from wptlproc where ",retBK);   //��k:�s�@��JSON���ɮ׮榡
	sa.append("\n��JSON�ন��r��:\n"+sqlToArray);                        //�ˬd
	
	//�NJSON������x�s�_��
	sql="UPDATE wptlproc set bk='"+sqlToArray+"' where idkey='"+idkey+"'";
	vt.add(sql);
	sa.append("\n�N��r���x�s��wptlproc�Akey��idkey:\n"+sql);                        //�ˬd	
		
	setValue("field1",sa.toString());	
	
	String[] vtSql=(String[])vt.toArray(new String[0]);
	try{
		t.execFromPool(vtSql);
		message("��s���\");
	}catch(Exception e){
		message("��s����"+e);
	}

		return true;
	}
	
	public void DELETE()throws Throwable {                          //�R��
	setValue("field1","hello");
	Vector vt=new Vector();

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
	setValue("field1",sa.toString());                       //�ˬd
	//�٭�bk�������
	sql="select bk from wptlproc where idkey='"+idkey+"'";
	sa.append("\nbk:\n"+sql);
	setValue("field1",sa.toString());                       //�ˬd
	String[][] retBk=t.queryFromPool(sql);
	if(retBk.length>0) {
		sa.append("\nBK\n"+retBk[0][0]);
		setValue("field1",sa.toString());                       //�ˬd
//		JSONArray array=JSONArray.fromObject(retBk[0][0].trim());
//		sa.append("\narray:"+array.toString());
//		setValue("field1",sa.toString());                       //�ˬd		
		JSONObject jsonObject=JSONObject.fromObject(retBk[0][0].trim());
		sa.append("\njsonObject:"+jsonObject.toString());
		setValue("field1",sa.toString());                       //�ˬd		
		String jsTable=(String)jsonObject.get("table");
		sa.append("\ntable:"+jsTable);
		setValue("field1",sa.toString());                       //�ˬd
		JSONArray field=jsonObject.getJSONArray("field");
		sa.append("\nfield:"+field.toString());
		setValue("field1",sa.toString());                       //�ˬd
		for(int i=0; i<field.size(); i++) {
			JSONObject data=field.getJSONObject(i);
			String jsIdkey=(String)data.get("idkey");
			String jsFndate=(String)data.get("findate");
			String jsWrkdate=(String)data.get("wrkdate");
			String JsWrkman=(String)data.get("wrkman");
			sql= "update "+jsTable+" set findate="+noDateToNull(jsFndate)+",wrkdate="+noDateToNull(jsWrkdate)+",wrkman='"+JsWrkman+"' where idkey='"+jsIdkey+"'";
			vt.add(sql);
			sa.append("\n�^�swptlproc:"+sql);
			setValue("field1",sa.toString());                       //�ˬd
		}
	}
	setValue("field1",sa.toString());                       //�ˬd
	sql="update wptlproc set jobmark='',findate=null,wrkdate="+noDateToNull(mdate)+" where idkey='"+idkey+"'";
	vt.add(sql);
	sa.append("\n��swptlproc:"+sql);

	sql="update wptmapply set Auditid='',Regdate=null,Regid='',Ownsdate=null"
	    +",Ownedate=null,Regstat='',Chkdoc='',AgreeRegNo='',Extesay='' where meid='"+meid+"'";
	vt.add(sql);
	sa.append("\n�R��wptmapply���:\n"+sql);
	
	setValue("field1",sa.toString());                       //�ˬd
	String[] vtSql=(String[])vt.toArray(new String[0]);


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
	/*�q��Ʈw�����o�Ҧ���쪺��ơA�öi��T��C
	
	*/
	public void setEditableField()throws Throwable{
	
		String sql="select CompoName from wptmFormComp where FormName='"+getFunctionName().trim()+"'";
		String[][]abc=t.queryFromPool(sql);
		
		for(int i=0;i<abc.length;i++){
			setEditable(abc[i][0],false);
		}

		return ;
	}
	
	/*findateToOpen findate�}��ק��檺����
	  import java.text.DateFormat;  import java.text.SimpleDateFormat;  import java.util.Date;
	  ������פp��8�Atrue
	  �������1900-01-01 00:00:00.0, true
	  ����j�󤵤ѡAtrue
	*/
	public boolean findateToOpen(String findate) throws Throwable {

		if (findate.length() < 8 || findate.equals("1900-01-01 00:00:00.0")) {
			return true;
		}

		//���o���Ѥ��
		Date date = new Date();
		DateFormat dateformat= new SimpleDateFormat("yyyy/MM/dd");
		String today=dateformat.format(date);
		Date dateToday=dateformat.parse(today);
		//�Nfindate�榡��
		Date finDate=dateformat.parse(findate);
		//�������A�ݽ֦b�᭱�C

		return finDate.after(dateToday);
	
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
	/*�s�ɮɨϥΡA�B�z���null�����D
	  �b�s�ɪ��a��A������[''�A�]��null�Ȯǥ[''�ܦ�'null'�A��Ʈw�L�k�����C
	  �^�ǫe�ٷ|�h���e��ť�
	*/
	public static String noDateToNull(String date) throws Throwable{
		if(date.length()<8 || date.indexOf("1900")==0){
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
	
	
	
	
}
