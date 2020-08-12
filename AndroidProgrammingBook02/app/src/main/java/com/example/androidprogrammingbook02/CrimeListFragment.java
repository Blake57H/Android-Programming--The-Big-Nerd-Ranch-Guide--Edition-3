package com.example.androidprogrammingbook02;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class CrimeListFragment extends Fragment {
    private RecyclerView mCrimeListRecyclerView;
    private CrimeAdapter mAdapter;
    private Button mNoCrimeAddOneButton;
    private LinearLayout mNoCrimeMessageLinearLayout;
    private Callbacks mCallbacks;


    private boolean mSubtitleVisible;
    private int clickedIndex;

    private static final int SERIOUSNESS_TYPE_CALL_POLICE = 1;
    private static final int SERIOUSNESS_TYPE_NORMAL = 0;
    private static final String SAVED_SUBTITLE_VISIBILITY = "subtitle";

    private static final String KEY_CLICKED_INDEX = "key_clicked_index";
    private static final int REQUEST_CRIME_DELETED = 0;
    //private static final String EXTRA_CRIME_ID = "com.example.androidprogrammingbook02.crime_id";

    public interface Callbacks{
        void onCrimeSelected(Crime crime);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);
        mCrimeListRecyclerView = view.findViewById(R.id.crime_list_recycler_view);
        mCrimeListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        if (savedInstanceState != null) {
            clickedIndex = savedInstanceState.getInt(KEY_CLICKED_INDEX);
            mSubtitleVisible = savedInstanceState.getBoolean(SAVED_SUBTITLE_VISIBILITY);
        }

        mNoCrimeAddOneButton = view.findViewById(R.id.nocrime_message_add_crime_button);
        mNoCrimeAddOneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                int index = CrimeLab.get(getActivity()).getIndexByID(crime.getID());
/*
                if(index < 0) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle("crime is null: " + index)
                            .setPositiveButton(android.R.string.ok, null).create().show();
                    return;
                }
*/
                if(index == -1) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_failed_to_create_crime_title)
                            .setPositiveButton(android.R.string.ok, null).create().show();
                    return;
                }
                Intent intent = CrimePagerActivity.newIntent(getActivity(), index);
                clickedIndex = index;
                startActivity(intent);
            }
        });

        mNoCrimeMessageLinearLayout = view.findViewById(R.id.no_crime_message);


        updateUI(0);
        return view;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.new_crime:
                Crime crime = new Crime();
                CrimeLab.get(getActivity()).addCrime(crime);
                int index = CrimeLab.get(getActivity()).getIndexByID(crime.getID());
                if (index == -1) {
                    new AlertDialog.Builder(getActivity())
                            .setTitle(R.string.dialog_failed_to_create_crime_title)
                            .setPositiveButton(android.R.string.ok, null).create().show();

                    return super.onOptionsItemSelected(item);
                }
//                Intent intent = CrimePagerActivity.newIntent(getActivity(), index);
                clickedIndex = index;
//                startActivity(intent);
                mCallbacks.onCrimeSelected(crime);
                updateUI(index);
                return true;
            case R.id.show_subtitle:
                mSubtitleVisible = !mSubtitleVisible;
                getActivity().invalidateOptionsMenu();
                updateSubtitle();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI(clickedIndex);
    }

    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mCrimeTitleTextView;
        private TextView mCrimeDateTextView;
        private ImageView mCrimeSolvedImageView;
        private Crime mCrime;

        public CrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super((inflater.inflate(R.layout.list_item_crime, parent, false)));

            mCrimeTitleTextView = itemView.findViewById(R.id.listViewText_crime_title);
            mCrimeDateTextView = itemView.findViewById(R.id.listViewText_crime_date);
            mCrimeSolvedImageView = itemView.findViewById(R.id.solved_imageView);
            itemView.setOnClickListener(this);
        }

        public void bind(Crime crime) {
            mCrime = crime;
            mCrimeTitleTextView.setText(mCrime.getTitle());
            mCrimeDateTextView.setText(FormatDate(mCrime.getDate()));
            mCrimeSolvedImageView.setVisibility(mCrime.isSolved() ? View.VISIBLE : View.GONE);
        }


        @Override
        public void onClick(View v) {
            //Toast.makeText(getActivity(), mCrime.getTitle() + " clicked", Toast.LENGTH_SHORT).show();
            //mCrimeListRecyclerView.getAdapter().notifyItemMoved(0, 5);
            clickedIndex = getAdapterPosition();
            StartCrimeActivity(clickedIndex);
            //Toast.makeText(getActivity(), String.valueOf(getAdapterPosition()), Toast.LENGTH_SHORT).show();
        }
    }


    private class SeriousCrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private TextView mCrimeTitleTextView;
        private TextView mCrimeDateTextView;
        private Button mCallPoliceButton;
        private Crime mCrime;

        public SeriousCrimeHolder(LayoutInflater inflater, ViewGroup parent) {
            super((inflater.inflate(R.layout.list_item_severe_crime, parent, false)));

            mCrimeTitleTextView = itemView.findViewById(R.id.listViewText_crime_title);
            mCrimeDateTextView = itemView.findViewById(R.id.listViewText_crime_date);
            mCallPoliceButton = itemView.findViewById(R.id.button_call_police);

            itemView.setOnClickListener(this);
        }

        public void bind(final Crime crime) {
            mCrime = crime;
            mCrimeTitleTextView.setText(mCrime.getTitle());

            mCrimeDateTextView.setText(FormatDate(mCrime.getDate()));

            mCallPoliceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String police = getString(R.string.button_call_police_click);
                    Toast.makeText(getActivity(), police + " " + mCrime.getTitle(), Toast.LENGTH_SHORT).show();
                }
            });
        }


        @Override
        public void onClick(View v) {
            //Toast.makeText(getActivity(), mCrime.getTitle() + " clicked", Toast.LENGTH_SHORT).show();
            clickedIndex = getAdapterPosition();
            StartCrimeActivity(clickedIndex);
            //Toast.makeText(getContext(), String., Toast.LENGTH_SHORT).show();
        }
    }

    private class CrimeAdapter extends RecyclerView.Adapter {
        private List<Crime> mCrimes;

        @Override
        public int getItemViewType(int position) {
            switch (mCrimes.get(position).getSeriousness()) {
                case 1:
                    return SERIOUSNESS_TYPE_CALL_POLICE;
                case 0:
                    return SERIOUSNESS_TYPE_NORMAL;
                default:
                    return -1;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            switch (viewType) {
                case SERIOUSNESS_TYPE_NORMAL:
                    return new CrimeHolder(layoutInflater, parent);
                case SERIOUSNESS_TYPE_CALL_POLICE:
                    return new SeriousCrimeHolder(layoutInflater, parent);
                default:
                    return null;
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            Crime crime = mCrimes.get(position);
            switch (crime.getSeriousness()) {
                case SERIOUSNESS_TYPE_CALL_POLICE:
                    ((SeriousCrimeHolder) holder).bind(crime);
                    break;
                case SERIOUSNESS_TYPE_NORMAL:
                    ((CrimeHolder) holder).bind(crime);
                    break;
                default:
                    break;
            }
        }

        public int getItemCount() {
            return mCrimes.size();
        }

        public void setCrimes(List<Crime> crimes){
            mCrimes = crimes;
        }

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }
    }

    public void updateUI(int itemChangedIndex) {
        //-1 = not updating
        if(itemChangedIndex == -2) return;
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        mNoCrimeMessageLinearLayout.setVisibility(crimes.size() == 0 ? View.VISIBLE : View.INVISIBLE);

        if (mAdapter == null) {
            mAdapter = new CrimeAdapter(crimes);
            mCrimeListRecyclerView.setAdapter(mAdapter);
        } else {
            //mAdapter.notifyItemChanged(itemChangedIndex);
            mAdapter.setCrimes(crimes);
            mAdapter.notifyDataSetChanged();
        }
        updateSubtitle();
    }

    private void updateSubtitle() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        int crimeSize = crimeLab.getCrimes().size();
        String subtitle = getResources().getQuantityString(R.plurals.subtitle_plural, crimeSize, crimeSize);
        if (!mSubtitleVisible)
            subtitle = null;
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.getSupportActionBar().setSubtitle(subtitle);
    }

    private String FormatDate(Date input) {
        return DateFormat.getDateTimeInstance().format(input);
    }

    private void StartCrimeActivity(int index) {
        //Intent intent = CrimeActivity.newIntent(getActivity(), index);
        //Intent intent = CrimePagerActivity.newIntent(getActivity(), index);
        //startActivity(intent);
        mCallbacks.onCrimeSelected(CrimeLab.get(getActivity()).getCrimeByIndex(index));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_CLICKED_INDEX, clickedIndex);
        outState.putBoolean(SAVED_SUBTITLE_VISIBILITY, mSubtitleVisible);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime_list, menu);

        MenuItem subtitleItem = menu.findItem(R.id.show_subtitle);
        if (mSubtitleVisible)
            subtitleItem.setTitle(R.string.hide_subtitle);
        else
            subtitleItem.setTitle(R.string.show_subtitle);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == REQUEST_CRIME_DELETED && resultCode == Activity.RESULT_OK){
            updateUI(-1);
            clickedIndex = -2;
        }
    }
}
