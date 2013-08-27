package com.minesweeper;

import android.os.Bundle;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AbstractData {

	private Button bContPartida;    

	/**
     * Enable button "Continua partida" if there is a saved game
     */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
        
        bContPartida = (Button) findViewById(R.id.button2);
        if (!existsPreviousGame()) {            
            bContPartida.setEnabled(false);
            bContPartida.setTextColor(Color.parseColor("#aaaaaa"));
        }        
        else {
            bContPartida.setEnabled(true);
            bContPartida.setTextColor(Color.parseColor("#ffffff"));       	
        }
	}
	
	/**
	 * Start help activity
	 * @param view
	 */
	public void gameInfo(View view){
		Intent i = new Intent(this, HelpActivity.class);
        startActivity(i);		
	}
	
	/**
	 * Start ranking activity
	 * @param view
	 */
	public void ranking(View view){
		Intent i = new Intent(this, RankingActivity.class);
        startActivity(i);
	}
	
	/**
	 * Start a new game. If there is a previous saved game ask if we want to lost it
	 * @param view
	 */
	public void showTable(View view) {
        
        if (existsPreviousGame()) {
        	AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);  
            dialogo1.setTitle("Existe una partida");  
            dialogo1.setMessage("¿Seguro que quieres empezar una nueva?");            
            dialogo1.setCancelable(false);  
            dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {  
                public void onClick(DialogInterface dialogo1, int id) {  
                	setBoolNewGame();
                	goToActivityGame();
                }  
            });  
            dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {  
                public void onClick(DialogInterface dialogo1, int id) {
                }  
            });            
            dialogo1.show(); 
        }
        else {
        	goToActivityGame();
        }
    }
	
	/**
	 * Start the game
	 */
	private void goToActivityGame(){
		Intent i = new Intent(this, MinesweeperActivity.class);
        startActivity(i);
	}
	
	/**
	 * Check if there is a saved game
	 * @return boolean
	 */
	private boolean existsPreviousGame(){
        return getPrefDatos().getBoolean("saveTable", false);
	}
	
	/**
	 * Continue from previous saved game 
	 * @param view
	 */
	public void contPart(View view){
    	goToActivityGame();
	}
	
    /**
     * Enable button "Continua partida" if there is a saved game
     */	
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
        
        if (!existsPreviousGame()) {            
            bContPartida.setEnabled(false);
            bContPartida.setTextColor(Color.parseColor("#aaaaaa"));
        }        
        else {
            bContPartida.setEnabled(true); 
            bContPartida.setTextColor(Color.parseColor("#ffffff"));      	
        }
	}
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
    	getMenuInflater().inflate(R.menu.main_menu, menu);
		return true; 
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.menu_about:

				AlertDialog.Builder builder = new AlertDialog.Builder(this);
		        builder.setTitle("Acerca de...");
		        builder.setMessage("Buscaminas\nv 1.0\njtueros");
		        builder.setPositiveButton("OK",null);
		        AlertDialog dialog = builder.show();
		        TextView messageText = (TextView)dialog.findViewById(android.R.id.message);
		        messageText.setGravity(Gravity.CENTER);
		        dialog.show();				
				
	            break;
		}
		return true;
	}	
	
}
