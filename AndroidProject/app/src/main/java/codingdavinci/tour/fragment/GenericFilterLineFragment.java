package codingdavinci.tour.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import codingdavinci.tour.R;

public class GenericFilterLineFragment extends Fragment {
    private String filteredText;
    private String hiddenText;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.generic_filtered_line, container, false);
    }

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TextView textView = getView().findViewById(R.id.btnFilteredCustom);
        setVisible(textView, false);
        filteredText = textView.getText().toString();

        textView = getView().findViewById(R.id.btnFilteredHidden);
        setVisible(textView, false);
        hiddenText = textView.getText().toString();
    }

    public void setFilterButtonOnClickListener(View.OnClickListener l) {
        ImageButton filterButton = getView().findViewById(R.id.btnFilter);
        filterButton.setOnClickListener(l);
    }

    public void setCustomButtonOnClickListener(View.OnClickListener l) {
        TextView textView = getView().findViewById(R.id.btnFilteredCustom);
        textView.setOnClickListener(l);
    }

    public void setHiddenButtonOnClickListener(View.OnClickListener l) {
        TextView textView = getView().findViewById(R.id.btnFilteredHidden);
        textView.setOnClickListener(l);
    }

    public void setCustomFilteredNumber(int number) {
        TextView textView = getView().findViewById(R.id.btnFilteredCustom);
        setNumber(textView, filteredText, number);
    }

    public void setHiddenFilteredNumber(int number) {
        TextView textView = getView().findViewById(R.id.btnFilteredHidden);
        setNumber(textView, hiddenText, number);
    }

    private void setNumber(TextView textView, String text, int number) {
        number = Math.max(0, number);
        textView.setText(text + " " + number);

        setVisible(textView, (number > 0));
    }

    private void setVisible(TextView textView, boolean visible) {
        if (visible) {
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.GONE);
        }
    }
}
