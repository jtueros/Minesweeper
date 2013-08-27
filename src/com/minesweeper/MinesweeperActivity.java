package com.minesweeper;

import java.util.StringTokenizer;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Vibrator;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MinesweeperActivity extends AbstractData implements OnTouchListener {

    private Table table;
    private Box[][] box;
    private boolean playGame = true;
    private Vibrator vibra;
    private ImageButton imgBoton;
    private Chronometer crono;
    private boolean startGame;
    private boolean readTable;
    private boolean paused = false;
    private long stoppedTime = 0;
    private String nameWinner; 
    
    /**
     * Initialize the table, the chronometer and the vibrator
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);        

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_table);    
        
        vibra = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        
        imgBoton = (ImageButton) findViewById(R.id.imageButton1);
        crono = (Chronometer) findViewById(R.id.chronometer1);
        crono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {

            @Override
            public void onChronometerTick(Chronometer chronometer) {
                CharSequence text = chronometer.getText();
                if (text.length() == 5) {
                    chronometer.setText("00:" + text);
                } else if (text.length() == 7) {
                    chronometer.setText("0" + text);
                }
            }
        });
        
        LinearLayout layout = (LinearLayout)findViewById(R.id.linearLayout1);                
        table = new Table(this);
        table.setOnTouchListener(this);
        layout.addView(table);
        
        box = resetbox();
        
        setInitTable();
        table.invalidate();
    }
    
    /**
     * Reset the boxes
     * @return Box[][] 
     */
    private Box[][] resetbox() {
    	Box[][] aux = new Box[8][8];
    	for(int f = 0; f < 8; f++) {
            for(int c = 0; c < 8; c++) {
            	aux[f][c] = new Box();
            }
        } 
        
        return aux;
    }
    
    /**
     * Restore the previous saved game or create a new one
     */
    private void setInitTable() {
        readTable = getPrefDatos().getBoolean("saveTable", false);
        
        if (readTable) {
        	String contentMatrix = getPrefDatos().getString("contentMatrix", "");
            StringTokenizer st = new StringTokenizer(contentMatrix, ",");
            String boolMatrix = getPrefDatos().getString("boolMatrix", "");
            StringTokenizer st2 = new StringTokenizer(boolMatrix, ",");
            
            int k = 0;
            for (int i = 0; i < 8; i++) {
            	for (int j = 0; j < 8; ++j) {
            		box[i][j].setContent(Integer.parseInt(st.nextToken()));
            		k = Integer.parseInt(st2.nextToken());
            		
            		if (k == 1) {
            			box[i][j].setUncovered(true);
            		}
            		else {
            			box[i][j].setUncovered(false);
            		}
            	}
            }
          
            stoppedTime = getPrefDatos().getLong("stoppedTime", 0); 
            crono.setBase(SystemClock.elapsedRealtime() + stoppedTime);           
            String textCrono = getPrefDatos().getString("textCrono", "00:00:00");
            crono.setText(textCrono);
            crono.start();
            startGame = true;
        }
        else {
        	crono.setText("00:00:00");
            setBombs();
            calculateBombs();
            startGame = false;
        }
    }
    
    /**
     * Save the game
     */
    private void saveGame(){         	   	
    	StringBuilder str = new StringBuilder();
    	StringBuilder str2 = new StringBuilder();
    	
    	for (int i = 0; i < 8; i++) {
    		for (int j = 0; j < 8; ++j) {
    			str.append(box[i][j].getContent()).append(",");
    			str2.append((box[i][j].isUncovered()? 1 : 0)).append(",");
    		}
    	}

    	SharedPreferences.Editor editor = getPrefDatos().edit();
    	
    	editor.putString("contentMatrix", str.toString());
    	editor.putString("boolMatrix", str2.toString());
    	editor.putLong("stoppedTime", stoppedTime);
    	editor.putString("textCrono", crono.getText().toString());
    	editor.putBoolean("saveTable", true);
    	editor.commit();
    }
    
    /**
     * Ask if we want to restart the game
     * @param v
     */
    public void restartGame(View v) {
    	stoppedTime = crono.getBase() - SystemClock.elapsedRealtime();
    	crono.stop();
    	paused = true;
    	
    	AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);  
        dialogo1.setTitle("Reiniciar");  
        dialogo1.setMessage("¿Seguro que quieres reiniciar la partida?");            
        dialogo1.setCancelable(false);  
        dialogo1.setPositiveButton("Confirmar", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialogo1, int id) {
            	restartParams();
            }  
        });  
        dialogo1.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialogo1, int id) {  
            	if (playGame && startGame) {
            		paused = false;
            		crono.setBase(SystemClock.elapsedRealtime() + stoppedTime);
            		crono.start();
            	}
            }  
        });            
        dialogo1.show();       	
    }
    
    /**
     * Restart the game's parameters
     */
    public void restartParams() {
    	box = resetbox();            
        setBombs();
        calculateBombs();
        playGame = true;        
        readTable = false;
        startGame = false;
        paused = false;
        crono.setText("00:00:00"); 
        setBoolNewGame();
        table.invalidate();
    }
    
    /**
     * Inform that the player has already won. Get player's name.
     */
    private void saveWinner(){
		// get prompts.xml view
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.prompt_winner, null);

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

		// set prompts.xml to alertdialog builder
		alertDialogBuilder.setView(promptsView);

		final EditText userInput = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);

		// set dialog message
		alertDialogBuilder
			.setCancelable(false)
			.setPositiveButton("Ok",
			  new DialogInterface.OnClickListener() {
			    public void onClick(DialogInterface dialog,int id) {
			    	// get winner's name
			    	nameWinner = userInput.getText().toString();
			    	saveWinnerParams();
			    }
			  });

		// create alert dialog
		final AlertDialog alertDialog = alertDialogBuilder.create();
		
		alertDialog.setTitle("¡Enhorabuena has ganado!");
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) {
				// TODO Auto-generated method stub
				
				final Button alertBoton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				alertBoton.setEnabled(false);
				EditText et = (EditText)alertDialog.findViewById(R.id.editTextDialogUserInput);
				et.addTextChangedListener(new TextWatcher() {

					@SuppressLint("NewApi")
					@Override
					public void afterTextChanged(Editable arg0) {
						// TODO Auto-generated method stub
						if (arg0.toString().isEmpty() || arg0.toString().trim().length() < 1) {
							alertBoton.setEnabled(false);
						}
						else alertBoton.setEnabled(true);						
					}

					@Override
					public void beforeTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
						// TODO Auto-generated method stub
						
					}

					@Override
					public void onTextChanged(CharSequence arg0, int arg1,
							int arg2, int arg3) {
						// TODO Auto-generated method stub

					}
					
				});
			}
			
		});

		// show it
		alertDialog.show();	
    }
    
    /**
     * Save the time and the player's name
     */
    private void saveWinnerParams(){
    	SQLiteDatabase bd = getBDWinners().getWritableDatabase();
 
        if(bd != null) {                 
        	final String sql = "insert into winners(name, timeText) values (?, ?)";
    		bd.beginTransaction();
    		try {
        		SQLiteStatement stmt = bd.compileStatement(sql);
        		stmt.bindString(1, nameWinner);
        		stmt.bindString(2, crono.getText().toString());
        		stmt.execute();
        		bd.setTransactionSuccessful();
    		}
    		finally {
    			bd.endTransaction();
    			bd.close(); 
    		}        	
        	
        	Toast.makeText(this,"Añadido a la lista de ganadores",Toast.LENGTH_SHORT).show();
        	finish();    		
        }    	
    }
    
    /**
     * Ask if we want to play again or back to the menu
     */
    private void gameOver(){
    	AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);  
        dialogo1.setTitle("Perdiste");  
        dialogo1.setMessage("¿Quieres jugar otra vez o regresar al menú principal?");            
        dialogo1.setCancelable(false);  
        dialogo1.setPositiveButton("Volver a jugar", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialogo1, int id) {
            	restartParams();
            	imgBoton.setImageResource(R.drawable.img_face);
            }  
        });  
        dialogo1.setNegativeButton("Menú principal", new DialogInterface.OnClickListener() {  
            public void onClick(DialogInterface dialogo1, int id) {  
            	setBoolNewGame();          	
            	finish();
            }  
        });            
        dialogo1.show();      	
    }
    
    /**
     * Ask if we want to save the game
     */
    private void saveTable() {
    	AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);  
        dialogo1.setTitle("Guardar");
        dialogo1.setMessage("¿Deseas guardar la partida?");            
        dialogo1.setCancelable(false);  
        dialogo1.setPositiveButton("Si", new DialogInterface.OnClickListener() {
        	
            public void onClick(DialogInterface dialogo1, int id) {  
            	saveGame();
            	finish(); 
            }  
        });  
        dialogo1.setNegativeButton("No", new DialogInterface.OnClickListener() {
        	
            public void onClick(DialogInterface dialogo1, int id) {
            	setBoolNewGame();
                finish(); 
            }  
        });            
        dialogo1.show();  
    }
    
    /**
     * Set the bombs to 8 random boxes
     */
    private void setBombs () {
        int cant = 8;
        do {
            int fil = (int) (Math.random () * 8);
            int col = (int) (Math.random () * 8);
            if (box[fil][col].getContent() == 0) {
                box[fil][col].setContent(80);
                cant--;
            }
        }
        while (cant != 0);
    }
    
    /**
     * Check if we have won the game
     * @return boolean
     */
    private boolean won() {
        int cant=0;
        for (int f = 0; f < 8; f++) {
            for (int c = 0; c < 8; c++) {
                if (box[f][c].isUncovered()) {
                	cant++;	
                }
            }
        }

        return (cant==56);
    }

    /**
     * For each box set the number of bombs that there are around
     */
    private void calculateBombs() {
        for (int f = 0 ; f < 8 ; f++) {
            for (int c = 0 ; c < 8 ; c++) {
                if (box[f][c].getContent() == 0) {
                    int cant = countCoordinate (f, c);
                    box[f][c].setContent(cant);
                }
            }
        }
    }

    /**
     * Get the number of bombs around this position
     * @param fil
     * @param col
     * @return integer
     */
    int countCoordinate (int fil, int col) {
        int total = 0;
        
        if (fil - 1 >= 0 && col - 1 >= 0) {
            if (box[fil - 1][col - 1].getContent() == 80)
                total++;
        }
        
        if (fil - 1 >= 0) { 
            if (box[fil - 1][col].getContent() == 80)
                total++;
        }
        
        if (fil - 1 >= 0 && col + 1 < 8) {
            if (box[fil - 1][col + 1].getContent() == 80) {
            	total++;	
            }
        }

        if (col + 1 < 8) {
            if (box[fil][col + 1].getContent() == 80) {
            	total++;	
            }
        }
        
        if (fil + 1 < 8 && col + 1 < 8) {
            if (box[fil + 1][col + 1].getContent() == 80) {
            	total++;
            }
        }

        if (fil + 1 < 8) {
            if (box[fil + 1][col].getContent() == 80) {
            	total++;	
            }
        }
        
        if (fil + 1 < 8 && col - 1 >= 0) {
            if (box[fil + 1][col - 1].getContent() == 80) {
            	total++;	
            }
        }
        
        if (col - 1 >= 0) {
            if (box[fil][col - 1].getContent() == 80) {
            	total++;	
            }
        }
        
        return total;
    }
    
    /**
     * Run across all the boxes
     * @param fil
     * @param col
     */
    private void run(int fil, int col) {
    	if (fil >= 0 && fil < 8 && col >= 0 && col < 8) {
    		if (box[fil][col].getContent() == 0) {
    			box[fil][col].setUncovered(true);    			
    			box[fil][col].setContent(50);
    			run(fil, col + 1);
    			run(fil, col - 1);
    			run(fil + 1, col);
    			run(fil - 1, col);
                run(fil - 1, col - 1);
                run(fil - 1, col + 1);
                run(fil + 1, col + 1);
                run(fil + 1, col - 1);
    		}
            else {
            	if (box[fil][col].getContent() >= 1 &&  box[fil][col].getContent() <= 8) {
            		box[fil][col].setUncovered(true);
            	}
            }
    	}
    }    
    
    /**
     * Check if we can continue playing or not
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (playGame) {
        	boolean findBox = true;
            for(int f=0; f<8 && findBox; f++) {
                for(int c=0; c<8 && findBox; c++) {
                    if (box[f][c].isInside((float)event.getX(),(float)event.getY())) {
                    	if (!box[f][c].isUncovered()) {                    		
                    		vibra.vibrate(50);
                    		box[f][c].setUncovered(true);
                    	}
                    	
                    	if (!startGame) {
                    		startGame = true;
                    		if (!readTable) {
                    			crono.setBase(SystemClock.elapsedRealtime());
                    			crono.start();
                    		}                    		
                    	}
                        
                        if (box[f][c].getContent() == 80) {
                        	paused = true;
                        	imgBoton.setImageResource(R.drawable.img_gameover);
                        	gameOver();
                            crono.stop();
                            playGame = false;
                        }
                        else {
                    		imgBoton.setImageResource(R.drawable.img_face2);
                    		Handler handler = new Handler(); 
                    	    handler.postDelayed(new Runnable() {
								@Override
								public void run() {
									// TODO Auto-generated method stub
									if (playGame) {
										imgBoton.setImageResource(R.drawable.img_face);
									}
								}}, 125);                        	
                            if (box[f][c].getContent() == 0) {
                                run(f, c);
                            }
                        }
                        
                        table.invalidate();
                        findBox = false;
                    }
                }
            }
        }
        
        if (won() && playGame) {
        	imgBoton.setImageResource(R.drawable.img_face3);
            playGame = false;
            crono.stop();
            paused = true;
            saveWinner();
            setBoolNewGame(); 
        }
        
        return true;
    }     
    
    class Table extends View {

    	private Paint paintRect = new Paint();
    	private Paint paintNumber = new Paint();
    	private Paint paintLine = new Paint(); 
    	private Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.img_mine);
    	
    	/**
    	 * Constructor
    	 * @param context
    	 */
        public Table(Context context) {
            super(context);
        }
        
        /**
         * Draw the game
         */
        protected void onDraw(Canvas canvas) {
            canvas.drawRGB(0, 0, 0);
            float widthBox = getWidthBox(canvas);            
        	Bitmap bitmap2 = getResizedBitmap(bitmap, widthBox, widthBox);
        	setPaints();
            
            int boxContent = 0;
            float fileAct = 0;
            for(int f=0; f<8; f++) {
                for(int c=0; c<8; c++) {
                    box[f][c].setXY(c*(widthBox + 2), fileAct, (widthBox + 2));
                    if (!box[f][c].isUncovered()) {
                    	paintRect.setARGB(153, 204,204,204);
                    }
                    else {
                    	paintRect.setARGB(255, 153,153, 153);
                    }
                    
                    canvas.drawRect(c*(widthBox + 2) + 2, fileAct + 2, c*(widthBox + 2) + widthBox, 
                    		fileAct + widthBox, paintRect);
                    
                    //horizontal
                    canvas.drawLine(c*(widthBox + 2), fileAct, c*(widthBox + 2) + (widthBox + 2), fileAct, 
                    		paintLine);
                    
                    //vertical   
                    canvas.drawLine(c*(widthBox + 2), fileAct, c*(widthBox + 2), (widthBox + 2) + fileAct, 
                    		paintLine);
                    
                    boxContent = box[f][c].getContent();
                    if (boxContent >= 1 && boxContent <= 8 && box[f][c].isUncovered()) {
                    	if (boxContent == 2) {
                    		paintNumber.setARGB(255, 0, 255, 0);
                    	}
                    	else if (boxContent == 3) {
                    		paintNumber.setARGB(255, 255, 0, 0);
                    	}
                    	else if (boxContent == 1){
                    		paintNumber.setARGB(255, 0, 0, 255);
                    	}
                    	else if (boxContent == 4) {
                    		paintNumber.setARGB(255, 0, 255, 255);
                    	}
                    	else if (boxContent == 5) {
                    		paintNumber.setARGB(255, 255, 255, 0);
                    	}
                    	else if (boxContent == 6) {
                    		paintNumber.setARGB(255, 255, 0, 255);
                    	}
                    	
                        canvas.drawText(String.valueOf(boxContent), c*(widthBox + 2)+((widthBox + 2) * (float)0.345), 
                        		fileAct + ((widthBox + 2) * (float)0.66), paintNumber);
                    }

                    if (boxContent == 80 && box[f][c].isUncovered()) {
                    	canvas.drawBitmap(bitmap2, c*(widthBox + 2), fileAct, null);
                    }
                    boxContent = c;
                }
                fileAct = fileAct + widthBox + 2;
            }
            
            for(int c=0;c<8;c++) {
            	canvas.drawLine(c*(widthBox + 2), fileAct, c*(widthBox + 2) + (widthBox + 2), fileAct, 
            			paintLine);
            	canvas.drawLine(fileAct - 1, c*(widthBox + 2), fileAct - 1, c*(widthBox + 2) + (widthBox + 2), 
            			paintLine);
            }
        }
        
        /**
         * Resize the img_mine
         * @param bm
         * @param newHeight
         * @param newWidth
         * @return Bitmap
         */
        private Bitmap getResizedBitmap(Bitmap bm, float newHeight, float newWidth) {
            int width = bm.getWidth();
            int height = bm.getHeight();
            float scaleWidth = newWidth / width;
            float scaleHeight = newHeight / height;
            // create a matrix for the manipulation
            Matrix matrix = new Matrix();
            // resize the bit map
            matrix.postScale(scaleWidth, scaleHeight);
            // recreate the new Bitmap
            Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
            return resizedBitmap;
        }        
        
        /**
         * Calculate the width of the box
         * @param tabCanvas
         * @return float
         */
        private float getWidthBox(Canvas tabCanvas) {
            float aux = 0;
            if (tabCanvas.getWidth() < tabCanvas.getHeight()) {
                aux = table.getWidth();
            }
            else {
                aux = table.getHeight();
            }
            
            float widthBox = ((1 + aux - (float)(7 * 3 - 2 * 2)) / (float)8);
            return widthBox;
        }
        
        /**
         * Set the paints
         */
        private void setPaints(){
        	paintRect.setTextSize(20);
            paintNumber.setTextSize(20);
            paintNumber.setTypeface(Typeface.DEFAULT_BOLD);
            paintNumber.setARGB(255, 0,0, 255);
            paintLine.setARGB(255,255,255,255);
        }
        
    }
    
    /**
     * If home button is press down then stop the chronometer
     */
    @Override
    protected void onStop() {
        super.onStop();
		if (crono.isEnabled() && startGame && !paused) {
			stoppedTime = crono.getBase() - SystemClock.elapsedRealtime();
	   		crono.stop();
		}
    }   
    
    /**
     * If the home button was pressed and we were playing then start the chronometer
     */
	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
        
		if (!crono.isActivated() && startGame && !paused) {
			crono.setBase(SystemClock.elapsedRealtime() + stoppedTime);
			crono.start();	
		}	
	}    
    
    /**
     * Ask if we really want to finish the game
     */
    @Override
    public void onBackPressed() {    	
    	stoppedTime = crono.getBase() - SystemClock.elapsedRealtime();    	
    	crono.stop();
    	paused = true;

    	AlertDialog.Builder dialogo1 = new AlertDialog.Builder(this);  
        dialogo1.setTitle("Salir");
        dialogo1.setMessage("¿Seguro que quieres salir de la partida?");            
        dialogo1.setCancelable(false);  
        dialogo1.setPositiveButton("Seguir jugando", new DialogInterface.OnClickListener() {
        	
            public void onClick(DialogInterface dialogo1, int id) {
            	if (playGame && startGame) {
            		paused = false;
            		crono.setBase(SystemClock.elapsedRealtime() + stoppedTime);
            		crono.start();
            	}            	
            }  
        });  
        dialogo1.setNegativeButton("Menú principal", new DialogInterface.OnClickListener() {
        	
            public void onClick(DialogInterface dialogo1, int id) {  
            	if (playGame && startGame) {
            		saveTable();
            	}            
            	else {
            		finish();
            	}
            }
        });            
        dialogo1.show();   
    }	
	
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
    	getMenuInflater().inflate(R.menu.help_menu, menu);
		return true;
	}

    /**
     * Show the help information 
     */
	public void showInfo(){
		// get prompthelp.xml view
		LayoutInflater li = LayoutInflater.from(this);
		View promptsView = li.inflate(R.layout.dialog_help, null);
		
		final Dialog alertDialog = new Dialog(this, R.style.FullHeightDialog);
		alertDialog.setContentView(promptsView);
		
		alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

			@Override
			public void onShow(DialogInterface arg0) { 
				// TODO Auto-generated method stub
				
				TextView tv2 = (TextView)alertDialog.findViewById(R.id.textView2);
				TextView tv3 = (TextView)alertDialog.findViewById(R.id.textView3);
				
		    	SpannableString content = new SpannableString("Ayuda");
		    	content.setSpan(new UnderlineSpan(), 0, content.length(), 0);				
				
		    	tv2.setText(content);
		    	tv3.setText("\na) Pulsar casilla: se puede realizar con un simple clic sobre la casilla que se desea" +
		    			" destapar.\n\nb) Reiniciar partida: se puede realizar con un simple clic sobre el boton " +
		    			"de la carita sonriente.\n\nc) Guardar partida: se puede realizar despues de seleccionar " +
		    			"'Menú principal' al pulsar el boton volver mientras se esta jugando.");				
			}
			
		});
		alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

			@Override
			public void onDismiss(DialogInterface arg0) {
				// TODO Auto-generated method stub
				if (!crono.isActivated() && startGame) {
					paused = false;
					crono.setBase(SystemClock.elapsedRealtime() + stoppedTime);
					crono.start();	
				}		
			}
			
		});

		// show it
		alertDialog.show();		
	}    
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
			case R.id.item1:
				if (crono.isEnabled() && startGame) {
					paused = true; 
					stoppedTime = crono.getBase() - SystemClock.elapsedRealtime();
			   		crono.stop();
				}
				
				showInfo();				
	            break;
		}
		return true;
	} 	
}