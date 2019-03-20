
/*
	附件，儲存

*/

public void saveAnnex(String keyno,String idkye){
String[][]tableAnnex=getTableData("tableAnnex");
String sql="";
for(int i=0; i<tableAnnex.length; i++) {
	if("1".equals(tableAnnex[i][0])) {
		//取得資料
		String sort=tableAnnex[i][6];//排序
		String annexid=tableAnnex[i][3];//附件代碼
		String description=tableAnnex[i][2];//輸入值

		//刪除附件
		sql="delete from wptlannex where keyno='"+keyno+"'";
		vt.add(sql);
		sql="insert into wptlannex(keyno,idkey,sort,annexid,description)"
		    +" values "
		    +"('"+keyno+"','"+idkey+"','"+sort+"','"+annexid+"','"+description+"')";
		vt.add(sql);
		}

	}
	//異動
}
