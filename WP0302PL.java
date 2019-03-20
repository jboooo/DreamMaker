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
import jcx.lib.pMethod;

public class WP0302PL extends bTransaction{
	talk t=null;
	talk w=null;
	StringBuffer sa=null;
	StringBuffer sb=null;
	StringBuffer sc=null;
	Vector vt=null;
	public boolean action(String value)throws Throwable{
		// �^�ǭȬ� true ��ܰ��汵�U�Ӫ���Ʈw���ʩάd��
		// �^�ǭȬ� false ��ܱ��U�Ӥ����������O
		// �ǤJ�� value �� "�s�W","�d��","�ק�","�R��","�C�L","PRINT" (�C�L�w�����C�L���s),"PRINTALL" (�C�L�w���������C�L���s) �䤤���@
		t=getTalk("TradeMark");
		w=getTalk("wp");
		sa=new StringBuffer();      //�ˬd��
		sb=new StringBuffer();      //�^��sql
		sc=new StringBuffer();		//�^��keyno��sql
		vt=new Vector();
		
		SAVE();

		return false;
	}
	
	public boolean SAVE()throws Throwable{
		String meid=getValue("Meid");
		String idkey=getValue("Idkey");
		String row=getValue("Row");
		String keyno=getValue("keyno");
		
		//���orcvid
		String sql="select rcvid from wptlproc where idkey='"+idkey+"'";
		String[][] retIdkey=t.queryFromPool(sql);
		String rcvid="";	
		if(retIdkey.length>0){
			rcvid=retIdkey[0][0].trim();
		}
		
		saveDesctext(meid);//�x�s�y�z�ʻ���
		saveMarkname(meid);//�x�s�Ӽ�
		saveGoodclass(meid);//�x�s���w�ӫ~���O
		saveWptlproexte(idkey,meid,rcvid,keyno);//�x�swptlproexte��ƪ�
		put("row",row);
		put("sql",sb.toString());
		put("sql2",sc.toString());
		
//		setValue(".field1",sb.toString());
//		setValueAt(".table1",sb.toString(),1,"idkey");
		hideDialog();
		//this.addScript("window.parent.GLOBAL.get('showdialog').setVisible(false);");//���ͥ���
		this.addScript("window.parent.document.getElementById('button3').click();");//���obutton3�ð���
		
		
	
		return true;	
	}
/*
		�x�s�y�z�ʻ���
	*/
	public void saveDesctext(String meid)throws Throwable {
		//�y�z�ʻ���
		sa.append("\n-------�y�z�ʻ����A�x�s------:\n"); // �ˬd�I
		String isdesctext = getValue("isdesctext").trim();
		String cdesctext = getValue("cdesctext").trim();
		String desctext = getValue("desctext").trim();
		if ("1".equals(isdesctext)) {
			String sql = "update wptmapply set cdesctext='" + cdesctext + "',desctext='" + desctext + "' where meid='" + meid
			             + "'";
			vt.add(sql);
			sa.append("\n�y�z�ʻ���\n" + sql); // �ˬd�I
		}
		execDate(vt);
	}
	/*
		�x�s�Ӽи��
	*/
	public void saveMarkname(String meid)throws Throwable {

		//���o���
		String ismarkname=getValue("ismarkname".trim());//�ӼЦW��
		String ismarktype=getValue("ismarktype".trim());//�Ӽк���
		String ismarkmeid=getValue("ismarkmeid".trim());//���Ӽи��
		String ispicchk=getValue("ispicchk".trim());//�ϼˤ����D�i�M���v

		//���o���
		String markname=getValue("markname".trim());//�ӼЦW��
		if(ismarkname.equals("1")) {
			String sql="UPDATE wptmapply SET "
			           +" markname='"+markname
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n�ӼЦW��:\n"+sql);
		}

		String picchk=getValue("picchk".trim());//�ϼˤ����D�i�M���v
		if(ispicchk.equals("1")) {
			String sql="UPDATE wptmapply SET "
			           +" picchk='"+picchk
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n�ӼЦW��:\n"+sql);
		}

		String marktype=getValue("marktype".trim());//�Ӽк���
		if(ismarktype.equals("1")) {
			String sql="UPDATE wptmapply SET "
			           +" marktype='"+marktype
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n�ӼЦW��:\n"+sql);
		}
		String markmeid=getValue("markmeid".trim());//(���Ӽ�)���Ү׸�
		String markappyid=getValue("markappyid".trim());//�ӽи��X
		String markregid=getValue("markregid".trim());//���U��
		if(ismarkmeid.equals("1")) {
			String sql="UPDATE wptmapply SET "
			           +" markmeid='"+markmeid
			           +"',markappyid='"+markappyid
			           +"',markregid='"+markregid
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n�ӼЦW��:\n"+sql);
		}
		String relchk=getValue("relchk".trim());//���L���s�Ӽ�
		if(markmeid.length()>0 || markappyid.length()>0 || markregid.length()>0) {
			String sql="UPDATE wptmapply SET "
			           +" relchk='"+relchk
			           +"' WHERE meid ='"+meid+"'";
			vt.add(sql);
			sa.append("\n�ӼЦW��:\n"+sql);
		}
		sa.append("\n�ӼЦW��:hello\n");
		execDate(vt);

	}

	/*
		�x�s���w�ӫ~,�Ѽƥ��Ү׸�
	*/
	public void saveGoodclass(String meid)throws Throwable {
		sa.append("\n-------���w�ӫ~�A�x�s�C����:isgoodname1���ĴN�|�x�s:\n"); // �ˬd�I
		String isgoodname1=getValue("isgoodname1").trim();
		String[][] retGoodclassTable = getTableData("goodclassTable");   //���GoodclassTable
		sa.append("isgoodname1:"+isgoodname1);
		sa.append("retGoodclassTable"+retGoodclassTable.length+"");
		setValue("field1",sa.toString());
		if ("1".equals(isgoodname1)) {
			for (int i = 0; i < retGoodclassTable.length; i++) {
				String sql2 = "update wptlapbale set uninorigdname='" + retGoodclassTable[i][1].trim() + "',uninchgdname='"
				              + retGoodclassTable[i][2].trim() + "',goodclass='" + retGoodclassTable[i][0].trim() + "',delmk='" + retGoodclassTable[i][3].trim()
				              + "' where meid='" + meid + "' and goodclass='" + retGoodclassTable[i][6].trim() + "'";
				vt.add(sql2);
				sa.append("\n�x�s���w�ʰӫ~\n" + sql2); // �ˬd�I
				execDate(vt);
			}
		}

	}


	/*
		�x�s���ĸ��wptlproexte,�ǤJidkey,meid,rcvid
	*/
	public void saveWptlproexte(String idkey,String meid,String rcvid,String keyno)throws Throwable {
//		sa.append("\n-------�x�s���ĸ��wptlproexte�A�x�s------:\n"); // �ˬd�I

		//���o���
		String isdesctext=getValue("isdesctext".trim());//�y�z�ʻ���
		String isgoodname1=getValue("isgoodname1".trim());//���w�ӫ~
		String ismarkname=getValue("ismarkname".trim());//�ӼЦW��
		String ismarktype=getValue("ismarktype".trim());//�Ӽк���
		String ismarkmeid=getValue("ismarkmeid".trim());//���Ӽи��
		String ispicchk=getValue("ispicchk".trim());//���Ӽи��

		if (keyno.length() > 0) {
			String sql = "update wptlproexte set " + "desctext='" + isdesctext
			      + "'," + "goodname1='" + isgoodname1 + "'," + "markname='" + ismarkname
			      + "'," + "marktype='" + ismarktype + "'," + "markmeid='" + ismarkmeid+ "'," + "picchk='" + ispicchk
			      + "'" + " where keyno='" + keyno + "'";
//			vt.add(sql);
			sc.append(sql+"@");
		} else {
//			keyno=getKeyno();
			String sql = "insert into wptlproexte (meid,desctext,goodname1,markname,marktype,markmeid,idkey,rcvid,keyno) values ('"
			      + meid + "','" + isdesctext+ "','" + isgoodname1 + "','"
			      + ismarkname + "','" + ismarktype + "','" + ismarkmeid   + "','" + idkey  + "','" + rcvid + "','aaa')";
//			vt.add(sql);
			sc.append(sql+"@");
		}
//		execDate(vt);

	}

	/*
		���ʸ�Ʈw
	*/

	public void execDate(Vector vt)throws Throwable {
		sb=new StringBuffer();
		String[] vtsql = (String[]) vt.toArray(new String[0]);
		for(int i=0; i<vtsql.length; i++) {
			sb.append(vtsql[i]+"@");
		}
	}

	/*keyno�۰ʽs��
	*�^�ǭȬOString�A�Y�h���I�s�|�۰�+1
	*/
//	String keyno="";  //key�b�~�O�i�H�֥[
//	public String getKeyno()throws Throwable {
//		talk t=getTalk("TradeMark");
//		String year=operation.sub(getYear(),"1911");//���o����~
//		if(keyno.length()==10){
//			keyno= operation.add(keyno,"1");
//		} else{
//			String sql = "select top 1 keyno from wptlproexte where left(keyno,2)='10' and  len(keyno)='10' order by keyno desc";                //���okeyno�B�z
//			String[][]ret = t.queryFromPool(sql);
//			if(ret.length>0) {
//				keyno=ret[0][0].substring(4);
//				keyno= operation.add(keyno,"1");//�r��[1�C
//				keyno=convert.add0(keyno,"7");
//				keyno=year+keyno;
//			} else {
//				keyno="1080000001";
//			}
//		}
//		return keyno;
//	}

	/*���o�褸�~
	*�פJimport java.text.DateFormat;import java.text.SimpleDateFormat;import java.util.Date;
	*/
//
//	public static String getYear() {
//		Date date = new Date();
//		DateFormat dateformat= new SimpleDateFormat("yyyy");
//		String dateTime=dateformat.format(date);
//		return dateTime;
//	}
}
