//備份申請人
backupPamanid(idkey);

/*	備份申請人
	@戴勝台
*/

public boolean backupPamanid(String idkey)throws Throwable {
	vt=new Vector();

	String sql="select keyno from wptlproc where idkey='"+idkey+"'";
	String[][]keynoArray=t.queryFromPool(sql);
	if(keynoArray.length>0) {
		return ;
	}

	String[][]pamanidTable=getTableData("pamanidTable");
	if(pamanidTable.length>0) {
		//取得資料
		String pamanid=pamanidTable[i][1].trim();//讓與人編號
		String cmanname1=pamanidTable[i][2].trim();//讓與人名(中)
		String manname1=pamanidTable[i][3].trim();//讓與人名(原)
		String addr1=pamanidTable[i][4].trim();//讓與人地址(原)
		String addr5=pamanidTable[i][5].trim();//讓與人地址(中)
		String lapkey =pamanidTable[i][10].trim();//Key連結代表人
		//nkeyno
		sql="select keyno from wptlproexte where idkey='"+idkey+"'";
		keynoArray=t.queryFromPool(sql);
		String keyno=keynoArray[0][0];

		//刪除資料
		sql="DELETE FROM wptlappyOriApman  WHERE idkey ='"+idkey+"'";
		vt.add(sql);
		//新增資料
		sql="INSERT INTO wptlappyOriApman (pamanidCHK,pamanid,cmanname1,manname1,addr1,addr5,lapkey,idkey,keyno)"
		    +"values ('1','"+pamanid+"','"+cmanname1+"','"+manname1+"','"+addr1+"','"+addr5+"','"+lapkey+"','"+idkey+"','"+keyno+"')";
		vt.add(sql);
	}

	//異動 todo
}

//==============================================================================
//還原申請人
restorePamanid(idkey);

/*	還原申請人
	@戴勝台
*/
public boolean restorePamanid(String idkey)throws Throwable {
	
//搜尋資料
sql="SELECT pamanidCHK,pamanid,manname1,cmanname1,addr1,addr5,lapkey,idkey,keyno FROM wptlappyOriApman";
String[][]restoreWptlappyOriApman=t.queryFromPool(sql);

//取得資料
String pamanidCHK=restoreWptlappyOriApman[0][0].trim();//讓與人是否變更
String pamanid=restoreWptlappyOriApman[0][0].trim();//讓與人編號
String manname1=restoreWptlappyOriApman[0][0].trim();//讓與人名(原)
String cmanname1=restoreWptlappyOriApman[0][0].trim();//讓與人名(中)
String addr1=restoreWptlappyOriApman[0][0].trim();//讓與人地址(原)
String addr5=restoreWptlappyOriApman[0][0].trim();//讓與人地址(中)
String lapkey=restoreWptlappyOriApman[0][0].trim();//Key連結代表人(自動取號)
String idkey=restoreWptlappyOriApman[0][0].trim();//傳入参數txtidkey 
String keyno=restoreWptlappyOriApman[0][0].trim();//nkeyno (隠藏)
	
}





	/*
		儲存儲存申請人，代表人,申請人一定要先異動，才能取得lapkey的值
	*/
	public void savePamanid(String meid)throws Throwable{		
		String ispamanid=getValue("ispamanid".trim());//申請人
		vt= new Vector(); // 儲存申請人專用的
		String[][] retPamanidTable = getTableData("pamanidTable");
		String sql="";
		if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// 判斷儲存申請人
			// 刪除wptlapman同個本所案號的資料
			sql = "delete from wptlapman where meid='" + meid.trim() + "'";
			vt.add(sql);
		
			for (int i = 0; i < retPamanidTable.length; i++) {
				// 檢核:判斷申請人編號為空。
				if (retPamanidTable[i][1].length() == 0) {
					message("申請人編號為空");
					return ;
				}
		
				// 用insert into進wptlapman，本所案號及申請人編號，lapkey自動編號。
				sql = "insert into wptlapman (meid,pamanid) values ('" + meid + "','" + retPamanidTable[i][1] + "')";
				vt.add(sql);

			} 
		} 
		execDate(vt);//異動資料庫

		//儲存代表人
		vt=new Vector();
		if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// 儲存申請人判斷
			for (int i = 0; i < retPamanidTable.length; i++) {
				if (retPamanidTable[i][10].length() > 0) {
					sql = "delete from wptlapre where lapkey='" + retPamanidTable[i][10] + "'";
					vt.add(sql);
				}
		
				sql = "select lapkey,meid,pamanid from wptlapman where meid='" + meid + "' and pamanid ='"
					  + retPamanidTable[i][1] + "'";
				String[][] ret_lapkey = t.queryFromPool(sql);
				if(ret_lapkey.length>0){
				}
				// 儲存代表人，新增wptlapre的lapkey,id的資料
				if (retPamanidTable[i][7].length() > 0) {
					sql = "insert into wptlapre (lapkey,id) values ('" + ret_lapkey[0][0].trim() + "','" + retPamanidTable[i][7] + "')";
					vt.add(sql);
				}
				if (retPamanidTable[i][8].length() > 0) {
					sql = "insert into wptlapre (lapkey,id) values ('" + ret_lapkey[0][0].trim() + "','" + retPamanidTable[i][8] + "')";
					vt.add(sql);
				}
				if (retPamanidTable[i][9].length() > 0) {
					sql = "insert into wptlapre (lapkey,id) values ('" + ret_lapkey[0][0].trim() + "','" + retPamanidTable[i][9] + "')";
					vt.add(sql);
				}
			} // 結束i迴圈
		} // 結束儲存代表人判斷。

		execDate(vt);//異動資料庫
	}	
