//存檔更新委託人傳回來的表格。
saveWptlprocWptmapply();

/*
	0302委託人傳回來的資料，回存到程序檔及主檔。
	@戴勝台
*/

public void saveWptlprocWptmapply()throws Throwable {
	vt =new Vector();
	String[][]trustorTable=getTableData("trustorTable");
	for(int i=0; i<trustorTable.length; i++) {

		//存檔時更新wptlproc的資料
		sql="UPDATE wptlproc SET "
		    +"plsmanid ='"+trustorTable[i][2].trim()+"',"
		    +"conname ='"+trustorTable[i][3].trim()+"',"
		    +"conEmail ='"+trustorTable[i][4].trim()+"',"
		    +"othid ='"+trustorTable[i][5].trim()+"',"
		    +"specialtitle ='"+trustorTable[i][6].trim()+"',"
		    +"janloc ='"+trustorTable[i][7].trim()+"',"
		    +"janid ='"+trustorTable[i][8].trim()+"'"
		    +" WHERE IDKey ='"+trustorTable[i][1].trim()+"'";

		vt.add(sql);

		//存檔時更新wptmapply的資料，當條件是1時。
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
	//異動todo
}