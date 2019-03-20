
saveWptlproc();//更新程序檔

/*
		更新程序檔
	*/
public void saveWptlproc()throws Throwable {

	String[][]table1=getTableData("table1");
	for(int i=0; i<table1.length; i++) {
		//取得資料
		String appydate=table1[i][6].trim();//延展申請日期
		String dbbouns=table1[i][7].trim();//規費加倍
		String rcvno=table1[i][8].trim();//收據號碼
		String idkey=table1[i][9].trim();

		//更新資料
		String sql="UPDATE wptlproc SET "
		           +"appydate='"+appydate
		           +"',appydate='"+appydate
		           +"',dbbouns='"+dbbouns
		           +"',rcvno='"+rcvno
		           +"' WHERE idkey ='"+idkey+"'";
		vt.add(sql);
	}

	execDate(vt);//異動資料庫

}