package com.minesweeper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class AbstractData extends Activity {

    /**
     * Get SharedPreferences 'Mis Datos' 
     * @return SharedPreferences
     */
    public SharedPreferences getPrefDatos(){
    	return getSharedPreferences("MisDatos",Context.MODE_PRIVATE);
    }
    
    /**
     * Create an object from AdminWinners
     * @return AdminWinners
     */
    public AdminWinners getBDWinners(){
    	return new AdminWinners(this, "DBWinners", null, 1);
    } 
	
	/**
	 * Set the parameter save game to false
	 */
	public void setBoolNewGame(){
		SharedPreferences.Editor editor = getPrefDatos().edit();
		editor.putBoolean("saveTable", false);
		editor.commit();
	}    
		
}
