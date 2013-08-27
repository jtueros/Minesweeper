package com.minesweeper;

import android.app.Activity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.widget.TextView;

public class HelpActivity extends Activity {

	private TextView tv1;
	private TextView tv2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_help);
        
    	tv1 =(TextView)findViewById(R.id.tvhelpb);
    	tv2 =(TextView)findViewById(R.id.tvhelp2b);
    	
    	SpannableString content = new SpannableString("Descripción del juego");
    	content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
    	
    	tv1.setText(content);
    	tv2.setText("El juego consiste en despejar todas las casillas de una pantalla que no oculten una mina.\n\n" +
    			"Algunas casillas tienen un número, este número indica las minas que suman todas las casillas " +
    			"circundantes. Así, si una casilla tiene el número 3, significa que de las ocho casillas que hay " +
    			"alrededor (si no es en una esquina o borde) hay 3 con minas y 5 sin minas. " +
    			"Si se descubre una casilla sin número indica que ninguna de las casillas vecinas tiene mina y " +
    			"estas se descubren automáticamente.\n\nSi se descubre una casilla con una mina se pierde la " +
    			"partida.");
	}

}
