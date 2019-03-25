/*
	回復代理人
*/

public void revertMemanid(String meid,String idkey){
	vt=new Vector();
	sql="select memanid from wptlappyorimeman where idkey='"+idkey+"'";
	String[][]memanidArray=t.queryFromPool(sql);
	if(memanid.length>0){
		sql = "delete from wptlmeman where meid='" + meid + "'";
		vt.add(sql);		
		for(int i=0;i<memanidArray.length;i++){
			String memanid=memanidArray[i][0].trim();
			sql = "insert into wptlmeman (meid,memanid) values ('" + meid + "','" +memanid+"')";
			vt.add(sql);
		}
	sql="delete from wptlappyorimeman where idkey='"+idkey+"'";
	vt.add(sql);
	}
	execData(vt);	
}
		
	