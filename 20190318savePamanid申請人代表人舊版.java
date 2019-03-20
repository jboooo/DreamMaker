/*
	儲存儲存申請人，代表人,申請人一定要先異動，才能取得lapkey的值
	@戴勝台
	@20190318之前的版本
*/
public void savePamanid(String meid)throws Throwable {
//		sa.append("\n-------儲存申請人------:\n"); // 檢查點
	String ispamanid=getValue("ispamanid".trim());//申請人
//		sa.append("\nispamanid:"+ispamanid); // 檢查點
	vt= new Vector(); // 儲存申請人專用的
	String[][] retPamanidTable = getTableData("pamanidTable");
	String sql="";
	if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// 判斷儲存申請人
		// 刪除wptlapman同個本所案號的資料
		sql = "delete from wptlapman where meid='" + meid.trim() + "'";
		vt.add(sql);
//			sa.append("\n刪除wptlapman :\n" + sql); // 檢查點

		for (int i = 0; i < retPamanidTable.length; i++) {
			// 檢核:判斷申請人編號為空。
			if (retPamanidTable[i][1].length() == 0) {
				message("申請人編號為空");
				return ;
			}

			// 用insert into進wptlapman，本所案號及申請人編號，lapkey自動編號。
			sql = "insert into wptlapman (meid,pamanid) values ('" + meid + "','" + retPamanidTable[i][1] + "')";
			vt.add(sql);
//				sa.append("\n新增本所案號，申請人id :\n" + sql); // 檢查點

		}
	}
	execDate(vt);//異動資料庫

	//儲存代表人
//		sa.append("\n-------儲存代表人------:\n"); // 檢查點
	vt=new Vector();
	if (retPamanidTable.length != 0 && "1".equals(ispamanid)) {// 儲存申請人判斷
		for (int i = 0; i < retPamanidTable.length; i++) {
			if (retPamanidTable[i][10].length() > 0) {
				sql = "delete from wptlapre where lapkey='" + retPamanidTable[i][10] + "'";
				vt.add(sql);
//					sa.append("\n刪除同個lapkey的資料 :\n" + sql); // 檢查點
			}

			sql = "select lapkey,meid,pamanid from wptlapman where meid='" + meid + "' and pamanid ='"
			      + retPamanidTable[i][1] + "'";
			String[][] ret_lapkey = t.queryFromPool(sql);
			if(ret_lapkey.length>0) {
//					sa.append("\n新增lapkey :" + retPamanidTable[i][10]); // 檢查點
			}
			// 儲存代表人，新增wptlapre的lapkey,id的資料
			if (retPamanidTable[i][7].length() > 0) {
				sql = "insert into wptlapre (lapkey,id) values ('" + ret_lapkey[0][0].trim() + "','" + retPamanidTable[i][7] + "')";
				vt.add(sql);
//					sa.append("\n儲存代表人1 :\n" + sql); // 檢查點
			}
			if (retPamanidTable[i][8].length() > 0) {
				sql = "insert into wptlapre (lapkey,id) values ('" + ret_lapkey[0][0].trim() + "','" + retPamanidTable[i][8] + "')";
				vt.add(sql);
//					sa.append("\n儲存代表人2 :\n" + sql); // 檢查點
			}
			if (retPamanidTable[i][9].length() > 0) {
				sql = "insert into wptlapre (lapkey,id) values ('" + ret_lapkey[0][0].trim() + "','" + retPamanidTable[i][9] + "')";
				vt.add(sql);
//					sa.append("\n儲存代表人3 :\n" + sql); // 檢查點
			}
		} // 結束i迴圈
	} // 結束儲存代表人判斷。

	execDate(vt);//異動資料庫
}