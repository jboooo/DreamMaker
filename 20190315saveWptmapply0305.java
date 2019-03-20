saveWptmapply();//更新主檔

/*
	更新主檔
	@戴勝台
*/

	public void saveWptmapply()throws Throwable{
		vt=new Vector();
		String[][]table1=getTableData("table1");
	
		for(int i=0;i<table1.length;i++){
			//取得資料
			String exteappydate=table1[i][5].trim();//最新一次延展申請日期
			String meid=table1[i][2].trim();//本所案號
	
			//更新資料
			String sql="UPDATE wptmapply SET "
			+"exteappydate='"+exteappydate
			+"'  WHERE meid ='"+meid+"'";
			vt.add(sql);
		}
		execDate(vt);//異動資料庫

}
