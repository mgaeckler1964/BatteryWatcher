package at.gaeckler.batterywatcher;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class WatcherActivity extends ActionBarActivity {

	static final String CONFIGURATION_FILE = "BatteryWatcher.cfg";

	static final String	TOTAL_MIN_KEY = "totalMinimum";
	static final String	TOTAL_MAX_KEY = "totalMaximum";
	static final String	LAST_MIN_KEY = "lastMinimum";
	static final String	LAST_MAX_KEY = "lastMaximum";

	static final String	TOTAL_MIN_DATE_KEY = "totalMinimumDate";
	static final String	TOTAL_MAX_DATE_KEY = "totalMaximumDate";
	static final String	LAST_MIN_DATE_KEY = "lastMinimumDate";
	static final String	LAST_MAX_DATE_KEY = "lastMaximumDate";
	
	static final double START_MIN = 100; 
	static final double START_MAX = 0;
	
	static final int INTERVALL = 1*10*1000;
	static final int MAX_INTERVALL = INTERVALL*1000;

	TextView 			m_batteryLevel = null;
	TextView 			m_batteryStatus = null;

	TextView			m_totalMinView = null;
	TextView			m_totalMaxView = null;

	TextView			m_lastMinView = null;
	TextView			m_lastMaxView = null;
	
	CountDownTimer		m_batteryTimer = null;
	
	double				m_totalMin=START_MIN;
	String				m_totalMinDate = "";
	
	double				m_totalMax=START_MAX;
	String				m_totalMaxDate = "";

	double				m_lastMin=START_MIN;
	String				m_lastMinDate = "";

	double				m_lastMax=START_MAX;
	String				m_lastMaxDate = "";
	
	int					m_lastStatus = -1;

	private void createBatteryTimer()
	{
		if (m_batteryTimer!=null)
		{
			m_batteryTimer.cancel();
		}
	    m_batteryTimer = new CountDownTimer(MAX_INTERVALL, INTERVALL ) {
	    	
	    	@Override
	    	public void onTick(long millisUntilFinished) {
	    		checkBatteryLevel(millisUntilFinished);
	    	}
		
	    	@Override
	    	public void onFinish() {
	    		m_batteryTimer.start();
	    	}
		}.start();
	}

	void checkBatteryLevel(long millisUntilFinished)
	{
        IntentFilter 	ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent 			batteryStatus = null;

		batteryStatus = getBaseContext().registerReceiver(null, ifilter);

		// battery level (%)
		SimpleDateFormat sdf = new SimpleDateFormat("dd. MM. yyyy  HH:mm:ss", Locale.getDefault());
		String currentDateAndTime = sdf.format(new Date());
		
		int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        double batteryPct = (double)(level * 100) / (double)scale;
        m_batteryLevel.setText("Level: " + Double.toString(batteryPct)+"% "+Long.toString(millisUntilFinished) + " " + currentDateAndTime);
        
		// battery status
        int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
        if( status == BatteryManager.BATTERY_STATUS_CHARGING )
        {
            m_batteryStatus.setText("Charging");
        }
        else if( status == BatteryManager.BATTERY_STATUS_FULL )
        {
            m_batteryStatus.setText("Full");
            status = BatteryManager.BATTERY_STATUS_CHARGING;
        }
        else if( status == BatteryManager.BATTERY_STATUS_DISCHARGING )
        {
            m_batteryStatus.setText("Discharging");
        }
        else if( status == BatteryManager.BATTERY_STATUS_NOT_CHARGING )
        {
            m_batteryStatus.setText("Not Charging");
            status = BatteryManager.BATTERY_STATUS_DISCHARGING;
        }
        else if( status == BatteryManager.BATTERY_STATUS_UNKNOWN )
        {
            m_batteryStatus.setText("???");
        }

		// battery minimum
        if( batteryPct<m_totalMin )
        {
        	m_totalMin = batteryPct;
        	m_totalMinDate = currentDateAndTime;
        }
        m_totalMinView.setText("Min: " + Double.toString(m_totalMin)+"% " + m_totalMinDate);

        // battery maximum
        if( batteryPct>m_totalMax )
        {
        	m_totalMax = batteryPct; 
        	m_totalMaxDate = currentDateAndTime; 
        }
        m_totalMaxView.setText("Max: " + Double.toString(m_totalMax)+"% " + m_totalMaxDate);

        // last minimum/maximum
        if( status != m_lastStatus )
        {
            if( status == BatteryManager.BATTERY_STATUS_CHARGING )
            {
                m_lastMin = batteryPct;
                m_lastMinDate = currentDateAndTime;
            }
            else if( status == BatteryManager.BATTERY_STATUS_DISCHARGING )
            {
                m_lastMax = batteryPct;
                m_lastMaxDate = currentDateAndTime;
            }
        	m_lastStatus = status;
        }
        m_lastMinView.setText("Last Min: " + Double.toString(m_lastMin)+"% " + m_lastMinDate);
        m_lastMaxView.setText("Last Max: " + Double.toString(m_lastMax)+"% " + m_lastMaxDate);

	}
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watcher);

        m_batteryLevel = (TextView)findViewById( R.id.batteryLevel );
        m_batteryStatus = (TextView)findViewById( R.id.batteryStatus );

        m_totalMinView = (TextView)findViewById( R.id.totalMin );
        m_totalMaxView = (TextView)findViewById( R.id.totalMax );

        m_lastMinView = (TextView)findViewById( R.id.lastMin );
        m_lastMaxView = (TextView)findViewById( R.id.lastMax );


    	if( savedInstanceState != null )
        {
            m_totalMin = savedInstanceState.getDouble(TOTAL_MIN_KEY,START_MIN);
            m_totalMax = savedInstanceState.getDouble(TOTAL_MAX_KEY,START_MAX);
            m_lastMin = savedInstanceState.getDouble(LAST_MIN_KEY,START_MIN);
            m_lastMax = savedInstanceState.getDouble(LAST_MAX_KEY,START_MAX);

            m_totalMinDate = savedInstanceState.getString(TOTAL_MIN_DATE_KEY);
            m_totalMaxDate = savedInstanceState.getString(TOTAL_MAX_DATE_KEY);
            m_lastMinDate = savedInstanceState.getString(LAST_MIN_DATE_KEY);
            m_lastMaxDate = savedInstanceState.getString(LAST_MAX_DATE_KEY);
        }
        else
        {
        	SharedPreferences settings = getSharedPreferences(CONFIGURATION_FILE, 0);
        	m_totalMin = settings.getFloat(TOTAL_MIN_KEY,(float) START_MIN);
        	m_totalMax = settings.getFloat(TOTAL_MAX_KEY,(float) START_MAX);
        	m_lastMin = settings.getFloat(LAST_MIN_KEY,(float) START_MIN);
        	m_lastMax = settings.getFloat(LAST_MAX_KEY,(float) START_MAX);

            m_totalMinDate = settings.getString(TOTAL_MIN_DATE_KEY,"");
            m_totalMaxDate = settings.getString(TOTAL_MAX_DATE_KEY,"");
            m_lastMinDate = settings.getString(LAST_MIN_DATE_KEY,"");
            m_lastMaxDate = settings.getString(LAST_MAX_DATE_KEY,"");
        }

    	createBatteryTimer();
        checkBatteryLevel(0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.watcher, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_exit) {
            finish();
        }
        else if( id == R.id.action_init) {
        	m_totalMin = START_MIN;
        	m_totalMax = START_MAX;
        	m_lastMin = START_MIN;
        	m_lastMax = START_MAX;
            checkBatteryLevel(0);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPause()
    {
    	checkBatteryLevel(0);
    	saveSharedPreferences();
        super.onPause();
    }
	@Override
	public void onDestroy()
	{
    	checkBatteryLevel(0);
    	saveSharedPreferences();
        super.onDestroy();
    }
	
	@Override
	protected void  onSaveInstanceState (Bundle outState)
	{
		outState.putDouble(TOTAL_MIN_KEY, m_totalMin);
		outState.putDouble(TOTAL_MAX_KEY, m_totalMax);
		outState.putDouble(LAST_MIN_KEY, m_lastMin);
		outState.putDouble(LAST_MAX_KEY, m_lastMax);

		outState.putString(TOTAL_MIN_DATE_KEY, m_totalMinDate );
		outState.putString(TOTAL_MAX_DATE_KEY, m_totalMaxDate );
		outState.putString(LAST_MIN_DATE_KEY, m_lastMinDate );
		outState.putString(LAST_MAX_DATE_KEY, m_lastMaxDate );
	}

	private void saveSharedPreferences()
    {
    	SharedPreferences settings = getSharedPreferences(CONFIGURATION_FILE, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putFloat(TOTAL_MIN_KEY, (float) m_totalMin );
        editor.putFloat(TOTAL_MAX_KEY, (float) m_totalMax );
        editor.putFloat(LAST_MIN_KEY, (float) m_lastMin );
        editor.putFloat(LAST_MAX_KEY, (float) m_lastMax );

        editor.putString(TOTAL_MIN_DATE_KEY, m_totalMinDate );
        editor.putString(TOTAL_MAX_DATE_KEY, m_totalMaxDate );
        editor.putString(LAST_MIN_DATE_KEY, m_lastMinDate );
        editor.putString(LAST_MAX_DATE_KEY, m_lastMaxDate );

        // Commit the edits!
        editor.commit();
    }
}
