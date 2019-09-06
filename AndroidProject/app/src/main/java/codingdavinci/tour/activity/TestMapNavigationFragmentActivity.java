package codingdavinci.tour.activity;

import android.graphics.Point;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import codingdavinci.tour.R;

public class TestMapNavigationFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_map_navigation_fragment);

        final MapNavigationFragment mapNavigationFragment = (MapNavigationFragment)getSupportFragmentManager().findFragmentById( R.id.mapNavigation );

        //final RadioGroup switchRadioGroup = (RadioGroup)findViewById(R.id.ElementRadioGroup1);

        final EditText xEditText =(EditText)findViewById(R.id.yEditText);
        xEditText.setText("30");
        final EditText yEditText =(EditText)findViewById(R.id.xEditText);
        yEditText.setText("30");

        final Button buttonSetLocation = (Button) findViewById(R.id.btnSetLocation);
                /*
        buttonSetLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapNavigationFragment.setLocation(MapNavigationFragment.FROM,
                        new Point(Integer.parseInt(xEditText.getText().toString()), Integer.parseInt(yEditText.getText().toString())),
                        new Point(Integer.parseInt(xEditText.getText().toString()), Integer.parseInt(yEditText.getText().toString()))
                        );
                // your handler code here
                if (switchRadioGroup.getCheckedRadioButtonId() == -1)
                {
                    // no radio buttons are checked
                }
                else
                {
                    int mapElement = 0;
                    switch(switchRadioGroup.getCheckedRadioButtonId()){
                        case R.id.radioButtonMap:
                            // do operations specific to this selection
                            mapElement = 0;
                            break;
                        case R.id.radioButtonFrom:
                            // do operations specific to this selection
                            mapElement = 1;
                            break;
                        case R.id.radioButtonTo:
                            // do operations specific to this selection
                            mapElement = 2;
                            break;
                    }
                    mapNavigationFragment.setLocation(mapElement,
                               new Point(Integer.parseInt(xEditText.getText().toString()),
                                       Integer.parseInt(yEditText.getText().toString())
                               ));
                    // one of the radio buttons is checked
                }
            }
        });

        final Button buttonRotate = (Button) findViewById(R.id.btnRotate);
        buttonRotate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mapNavigationFragment.setLocation(MapNavigationFragment.TO,
                        new Point(Integer.parseInt(xEditText.getText().toString()), Integer.parseInt(yEditText.getText().toString())),
                        new Point(Integer.parseInt(xEditText.getText().toString()), Integer.parseInt(yEditText.getText().toString()))
                );
            }
        });
                */
    }
}
