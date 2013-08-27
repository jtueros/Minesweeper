package com.minesweeper;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.TextView;
import android.widget.Toast;

public class RankingActivity extends Activity {

	private TextView tv;	
	private TextView tv2;
	private TextView tv3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_ranking);
        
    	tv =(TextView)findViewById(R.id.tvhelp4b);
    	tv2 =(TextView)findViewById(R.id.tvhelp5b);
    	tv3 =(TextView)findViewById(R.id.tvRanking);

    	AdminWinners usdbh = new AdminWinners(this, "DBWinners", null, 1);
        SQLiteDatabase bd = usdbh.getReadableDatabase();
        
        Cursor c = bd.rawQuery("Select name, timeText From winners Order By timeText", null);
        
        StringBuffer namesWinners = new StringBuffer();
        StringBuffer timesWinners = new StringBuffer();
        if(c.moveToFirst()) {
        	do {
        		namesWinners.append(c.getString(0) + " \n");
        		timesWinners.append(" " + c.getString(1) + "\n");        		
        	}while(c.moveToNext());
        }
        else {
        	Toast.makeText(this,"De momento ningun jugador ha ganado el juego.",Toast.LENGTH_SHORT).show();
        }
        
        bd.close();    	
        tv.setText(namesWinners.toString());
        tv2.setText(timesWinners.toString());
        
    	SpannableString content = new SpannableString("Clasificación");
    	content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
    	tv3.setText(content);        
	}

}
