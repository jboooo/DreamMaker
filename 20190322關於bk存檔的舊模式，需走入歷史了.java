/*
	備份主檔,商標,描述性說明，使用json
*/
public void backupWptmapply(String meid,String idkey)throws Throwable {
	sa.append("備份主檔,商標,描述性說明，使用json\n");
	//搜尋資料
	sql="SELECT markname,picchk,marktype,markmeid,markappyid,markregid,relchk,exteappydate,cdesctext,desctext "
	    +" FROM wptmapply"
	    +" WHERE meid ='"+meid+"'";

	String[][]retwptmapply=t.queryFromPool(sql);
	sa.append(sql+"\n");
	String sqlToArray=sqlToString(sql,retwptmapply);   //方法:製作成JSON的檔案格式
	//將JSON的資料儲存起來
	sql="UPDATE wptlproc set bk='"+sqlToArray.toString()+"' where idkey='"+idkey+"'";
	vt.add(sql);
	execData(vt);//異動資料庫

}

/*
	傳入sql轉出JSON格式的字串,傳入sql及ret檔。
*/
public String sqlToString(String sqlstr,String[][] ret) {
	sqlstr=sqlstr.toLowerCase();
	sqlstr =sqlstr.replace("'","''");  //新增一個'個，這樣才能存入sql
	int numSelect=sqlstr.indexOf("select");
	int numFrom=sqlstr.indexOf("from");
	int numWhere=sqlstr.indexOf("where");
	String select=sqlstr.substring(numSelect+6,numFrom).trim();
	String from=sqlstr.substring(numFrom+4,numWhere).trim();
	String where=sqlstr.substring(numWhere+5).trim();
	String[] selectToArray=select.split(",");
	JSONObject jsonObject=new JSONObject();
	jsonObject.put("table",from);
	jsonObject.put("key",where);

	JSONArray jsonArray=new JSONArray();

	for(int j=0; j<ret.length; j++) {
		JSONObject jsonObjToSelect=new JSONObject();
		for(int i=0; i<selectToArray.length; i++) {
			jsonObjToSelect.put(selectToArray[i],ret[j][i]);
		}
		jsonArray.add(jsonObjToSelect);
	}

	jsonObject.put("field",jsonArray);
	return jsonObject.toString();
}
/*
還原-主檔、商標、描述性說明
*/

public void revertMark(String idkey,String meid)throws Throwable {
	sa.append("還原-主檔、商標、描述性說明\n");
	vt=new Vector();
	sql="select bk from wptlproc where idkey='"+idkey+"'";
	String[][]bkArray=t.queryFromPool(sql);
	sa.append(sql+"\n");
	if(bkArray.length>0) {
		String bk=bkArray[0][0].trim();
		bk =bk.replace("'","''");  //新增一個'個，這樣才能存入sql
		JSONObject object=JSONObject.fromObject(bk.trim());
		JSONArray field=object.getJSONArray("field");
		JSONObject data=field.getJSONObject(0);

		String markname=(String)data.get("markname");//商標名稱
		String picchk=(String)data.get("picchk");//圖樣中不主張專用權
		String marktype=(String)data.get("marktype");//商標種類
		String markmeid=(String)data.get("markmeid");//(正商標)本所案號
		String markappyid=(String)data.get("markappyid");//申請號碼
		String markregid=(String)data.get("markregid");//註冊號
		String relchk=(String)data.get("relchk");
		String exteappydate=(String)data.get("exteappydate");//最後一次延展申請日
		String cdesctext=(String)data.get("cdesctext");//描述性說明(中)
		String desctext=(String)data.get("desctext");//描述性說明

		//儲存資料
		sql="UPDATE wptmapply SET markname='"+markname.trim()
		    +"',picchk='"+picchk.trim()
		    +"',marktype='"+marktype.trim()
		    +"',markmeid='"+markmeid.trim()
		    +"',markappyid='"+markappyid.trim()
		    +"',markregid='"+markregid.trim()
		    +"',relchk='"+relchk.trim()
		    +"',exteappydate='"+exteappydate.trim()
		    +"',cdesctext='"+cdesctext.trim()
		    +"',desctext='"+desctext.trim()
		    +"' WHERE meid ='"+meid.trim()+"'";
		vt.add(sql);
		execData(vt);
	}

}