package dai;
import jcx.jform.hproc;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WP0302BPL extends hproc{
   
	talk t=null;
	talk w=null;
	StringBuffer sa=null;
	Vector vt=null;
	public String action(String value)throws Throwable {
		// �^�ǭȬ� true ��ܰ��汵�U�Ӫ���Ʈw���ʩάd��
		// �^�ǭȬ� false ��ܱ��U�Ӥ����������O
		// �ǤJ�� value �� "�s�W","�d��","�ק�","�R��","�C�L","PRINT" (�C�L�w�����C�L���s),"PRINTALL" (�C�L�w���������C�L���s) �䤤���@
		getButton(3).setEnabled(true);
		t=getTalk("TradeMark");
		w=getTalk("wp");
		sa=new StringBuffer();
		vt=new Vector();
		
	    Query();

		return value;	
	}
	
	public boolean Query()throws Throwable {	
		String sql="";
		
		String idkey=getValue("idkey").trim();
		String meid=getValue("meid").trim();
		String row=getValue("row").trim();
		String keyno=getValue("keyno").trim();
		
		setValue("Idkey",idkey);
		setValue("Meid",meid);
		setValue("Row",row);
		setValue("keyno",keyno);
		
		//���oidkey

		//�U�b���Ҳ�
		selectMarkname(meid);//�j�M�ӼФδy�z�ʻ���
		selectGoodClass(meid);//�j�M���w�ӫ~���O
		selectWptlproexte(idkey);//�j�M������s

		setValue("field1",sa.toString());
		return true;
	}


	/*
		�j�M-�ӼФδy�z�ʻ����A�Ѽ�:���Ү׸�
	*/
	public void selectMarkname(String meid)throws Throwable {

		//�j�M���
		String sql="SELECT markname,picchk,marktype,markpic,markmeid,markappyid,markregid,cdesctext,desctext,relchk FROM wptmapply WHERE meid ='"+ meid+"'";
		String[][]retwptmapply=t.queryFromPool(sql);
		sa.append("\n�Ӽ�"+sql+"\n");
		sa.append("\n�ӼФδy�z�ʻ���\n" + sql); // �ˬd�I
		String markmeid="";
		//�]�w���
		setValue("markname",retwptmapply[0][0].trim());    //�ӼЦW��
		setValue("picchk",retwptmapply[0][1].trim());    //�ϼˤ����D�i�M���v
		setValue("marktype",retwptmapply[0][2].trim());    //�Ӽк���
		setValue("markpic",retwptmapply[0][3].trim());    //�Ӽйϼ�
		setValue("markmeid",retwptmapply[0][4].trim());    //(���Ӽ�)���Ү׸�
		markmeid=retwptmapply[0][4].trim();
		setValue("markappyid",retwptmapply[0][5].trim());    //�ӽи��X
		setValue("markregid",retwptmapply[0][6].trim());    //���U��
		setValue("cdesctext",retwptmapply[0][7].trim());    //����y�z�ʻ���
		setValue("desctext",retwptmapply[0][8].trim());    //�y�z�ʻ���
		setValue("relchk",retwptmapply[0][9].trim());    //���L���s�Ӽ�
		if(markmeid.trim().length()>0) {
			//�j�M���
			sql="SELECT markName,owneDate,ownsDate FROM wptmapply WHERE markmeid = '"+markmeid+"'";
			retwptmapply=t.queryFromPool(sql);
			sa.append("\n���Ӽ�\n"+sql+"\n");

			//�]�w���
			setValue("markName",retwptmapply[0][0].trim());    //�ӼЦW��
			setValue("owneDate",dateformat(retwptmapply[0][1].trim()));    //�M�κ�����
			setValue("ownsDate",dateformat(retwptmapply[0][2].trim()));    //�M�ΰ_�l��
		}
		setValue("field5",sa.toString());
	}
	/*
		�j�M-���w�ӫ~�A�Ѽ�:���Ү׸�
	*/
	public void selectGoodClass(String meid)throws Throwable {

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
		�j�M-������s
	*/
	public void selectWptlproexte(String idkey)throws Throwable {

		//�j�M���
		String sql="SELECT pamanid,prename1,desctext,goodname1,marktype,markmeid,markname,picchk,keyno FROM wptlproexte WHERE idkey ='"+ idkey+"'";
		String[][]retwptlproexte=t.queryFromPool(sql);
		sa.append("\n�j�M-������s\n"+sql);
		if(retwptlproexte.length>0) {
			//�]�w���
			setValue("ispamanid",retwptlproexte[0][0].trim());    //ispamanid//�ӽФH
			setValue("isprename1",retwptlproexte[0][1].trim());    //�N��H�@(��)
			setValue("isdesctext",retwptlproexte[0][2].trim());    //isdesctext//�y�z�ʻ���
			setValue("isgoodname1",retwptlproexte[0][3].trim());    //goodname1//���w�ӫ~1
			setValue("ismarktype",retwptlproexte[0][4].trim());    //�Ӽк���
			setValue("ismarkmeid",retwptlproexte[0][5].trim());    //���Ӽи��
			setValue("ismarkname",retwptlproexte[0][6].trim());    //���L���p�Ӽ�
			setValue("ispicchk",retwptlproexte[0][7].trim());    //���L���p�Ӽ�
			setValue("keyno",retwptlproexte[0][8].trim()); //�y����
			sa.append("\n�j�M�Ũ���s\n" + sql); // �ˬd�I

		}
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
}
