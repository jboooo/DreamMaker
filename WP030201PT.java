package dai;
import jcx.jform.bTransaction;
import java.io.*;
import java.util.*;
import jcx.util.*;
import jcx.html.*;
import jcx.db.*;

public class WP030201PT extends bTransaction{
	talk t=null;
	talk w=null;
	StringBuffer sa=null;
	public boolean action(String value)throws Throwable{
		// �^�ǭȬ� true ��ܰ��汵�U�Ӫ���Ʈw���ʩάd��
		// �^�ǭȬ� false ��ܱ��U�Ӥ����������O
		// �ǤJ�� value �� "�s�W","�d��","�ק�","�R��","�C�L","PRINT" (�C�L�w�����C�L���s),"PRINTALL" (�C�L�w���������C�L���s) �䤤���@
		t=getTalk("TradeMark");
		w=getTalk("wp");
		sa=new StringBuffer();		
		String idkey=getQueryValue("idkey");
		String sql="select meid from wptlproc where idkey='"+idkey+"'";
		String[][]retMeid=t.queryFromPool(sql);
		String meid=retMeid[0][0].trim();
		
		selectMemanid(meid);//�j�M���ҥN�z�H
		selectMarkname(meid);//�j�M�ӼФδy�z�ʻ���
		selectGoodClass(meid);//�j�M���w�ӫ~���O
		selectPamanidAndId(meid);//�ӽФH�ΥN��H
		selectWptlproexte(idkey);//������s
	return false;
	}


	/*
		�j�M-�M�˥��ҥN�z�H(�h��)�A�ǤJ�ѼƬO���Ү׸�	
	*/
	public void selectMemanid(String meid)throws Throwable{

		String sql = "select memanid from wptlmeman where meid='" + meid + "'";
		String[][] ret_memanid = t.queryFromPool(sql);
		sa.append("\n�N�z�Hid\n" + sql+"\n");	                 // �ˬd
		StringBuffer sb = new StringBuffer();
		if (ret_memanid.length > 0) {
			for (int i = 0; i < ret_memanid.length; i++) {
				sb.append("'" + ret_memanid[i][0] + "',");
			}
			sb.setLength(sb.length() - 1);
			sql = "select memanid,memanname from wptcmeman where memanid in(" + sb.toString() + ") and tm='1'";
			sa.append("\n�N�z�Hid�Ωm�W\n" + sql+"\n");	                 // �ˬd
			String[][] ret_memanname = w.queryFromPool(sql);
			if (ret_memanname.length > 0) {
				setTableData("memanidTable", ret_memanname);
			}
		} // �j�M�N�z�Hif����
	}

	/*
		�j�M-�ӼФδy�z�ʻ����A�Ѽ�:���Ү׸�	
	*/
	public void selectMarkname(String meid)throws Throwable{

		//�j�M���
		String sql="SELECT markname,picchk,marktype,markpic,markmeid,markappyid,markregid,cdesctext,desctext FROM wptmapply WHERE meid ='"+ meid+"'";
		String[][]retwptmapply=t.queryFromPool(sql);
		sa.append("\n�Ӽ�"+sql+"\n");

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
		if(markmeid.trim().length()>0){
			//�j�M���
			sql="SELECT markName,owneDate,ownsDate FROM wptmapply WHERE markmeid = '"+markmeid+"'";
			retwptmapply=t.queryFromPool(sql);
			sa.append("\n���Ӽ�\n"+sql+"\n");

			//�]�w���
			setValue("markName",retwptmapply[0][0].trim());    //�ӼЦW��
			setValue("owneDate",retwptmapply[0][1].trim());    //�M�κ�����
			setValue("ownsDate",retwptmapply[0][2].trim());    //�M�ΰ_�l��
		}
		setValue("field5",sa.toString());
	}
	/*
		�j�M-���w�ӫ~�A�Ѽ�:���Ү׸�	
	*/
	public void selectGoodClass(String meid)throws Throwable{	
	
	//���w�ӫ~
	String sql = "Select Goodclass,uniNOrigdname,uniNChgdname,delmk,'','',Goodclass,uniNOrigdname,uniNChgdname from wptlapbale where meid='"
		  + meid + "' and delmk<>1";
	String[][] ret_goodclass = t.queryFromPool(sql);
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
		�j�M-�ӽФH�P�N��H�A�Ѽ�:���Ү׸�	
	*/
	public void selectPamanidAndId(String meid)throws Throwable{	
	//�ӽФH�P�N��H
	//�j�M�ӽФH�s���Alapkey�bwptlapman�A���󬰥��Ү׸�,�]�w�btable1
		String sql = "select '',pamanid,'','','','',meid,lapkey,'','','' from wptlapman  where meid='" + meid
		      + "' order by pamanid desc";
		String[][]ret = t.queryFromPool(sql);
		sa.append("\n���:���Ү׸��A�ӽФHid�Alapkey\n" + sql);	     	// �ˬd
		if (ret.length > 0) {
			for (int i = 0; i < ret.length; i++) {// �Q�ΥӽФH�s���j�M�ӽФH�W�١A�]�w�b���
				sql = "select b.uniChineseName,b.uniOriginName,unioriginaddr,unichineseaddr from wptmapman b WHERE pamanid='" + ret[i][1].trim()
				      + "' order by b.uniChineseName";
				String[][] ret_Name = w.queryFromPool(sql);
				sa.append("\n�]�w�ӽФH�W��(��),�]�w�ӽФH�W��(��)\n"+sql);              //�ˬd
				if (ret_Name.length > 0) {
					ret[i][2] = ret_Name[0][0]; // �]�w�ӽФH�W��(��)
					ret[i][3] = ret_Name[0][1]; // �]�w�ӽФH�W��(��)
					ret[i][4] = ret_Name[0][2]; // �]�w�ӽФH�a�}(��)
					ret[i][5] = ret_Name[0][3]; // �]�w�ӽФH�a�}(��)
				}
				if (ret[i][7].length() > 0) {
					sql = "select id from wptlapre where lapkey='" + ret[i][7] + "'";
					String[][] ret_id = t.queryFromPool(sql);
					sa.append("\nlapkey\n"+sql);                          //�ˬd
					if (ret_id.length == 3) {
						ret[i][8] = ret_id[0][0].trim();
						ret[i][9] = ret_id[1][0].trim();
						ret[i][10] = ret_id[2][0].trim();
					} else if (ret_id.length == 2) {
						ret[i][8] = ret_id[0][0].trim();
						ret[i][9] = ret_id[1][0].trim();
					} else if (ret_id.length == 1) {
						ret[i][10] = ret_id[0][0].trim();
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
		String sql="SELECT pamanid,prename1,desctext,goodname1,marktype,markmeid,relchk,markname,picchk FROM wptlproexte WHERE idkey ='"+ idkey+"'";
		String[][]retwptlproexte=t.queryFromPool(sql);
		if(retwptlproexte.length>0){
		//�]�w���
		setValue("ispamanid",retwptlproexte[0][0].trim());    //ispamanid//�ӽФH
		setValue("isprename1",retwptlproexte[0][1].trim());    //�N��H�@(��)
		setValue("isdesctext",retwptlproexte[0][2].trim());    //isdesctext//�y�z�ʻ���
		setValue("isgoodname1",retwptlproexte[0][3].trim());    //goodname1//���w�ӫ~1
		setValue("ismarktype",retwptlproexte[0][4].trim());    //�Ӽк���
		setValue("ismarkmeid",retwptlproexte[0][5].trim());    //���Ӽи��
		setValue("isrelchk",retwptlproexte[0][6].trim());    //���L���p�Ӽ�
		setValue("ismarkname",retwptlproexte[0][7].trim());    //���L���p�Ӽ�
		setValue("ispicchk",retwptlproexte[0][8].trim());    //���L���p�Ӽ�
		
		
		}
	}	
	
	
	
	
}