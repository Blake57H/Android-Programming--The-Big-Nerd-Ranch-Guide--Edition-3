package com.example.androidprogrammingbook02;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ShareCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import java.io.File;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class CrimeFragment extends Fragment {
    private Crime mCrime;
    private EditText mEditText;
    private Button mDateButton, mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportCrimeButton, mChooseSuspectButton, mCallSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private File mPhotoFile;
    private Callbacks mCallbacks;

    //private Button mJumpToStartButton, mJumpToEndButton;

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String ARG_CRIME_INDEX = "crime_index";
    private static final String ARG_IS_TABLET = "tablet_switch";
    private static final String DIALOG_DATE = "DialogDate", DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0, REQUEST_TIME = 1, REQUEST_CONTACT = 2, REQUEST_PHOTO = 3;

    private int crimeIndex;
    private boolean mTabletSwitch;

    public static CrimeFragment newInstance(int index, boolean tabletSwitch) {
        Bundle args = new Bundle();
        //args.putSerializable(ARG_CRIME_ID, crimeID);
        args.putInt(ARG_CRIME_INDEX, index);
        args.putBoolean(ARG_IS_TABLET, tabletSwitch);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CrimeFragment newInstance(int index) {
        return newInstance(index, false);
    }

    public interface Callbacks{
        void onCrimeUpdated(Crime crime);
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
        //UUID crimeID = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        //mCrime = CrimeLab.get(getActivity()).getCrimeByID(crimeID);
        crimeIndex = getArguments().getInt(ARG_CRIME_INDEX);
        mTabletSwitch = getArguments().getBoolean(ARG_IS_TABLET, false);
        mCrime = CrimeLab.get(getActivity()).getCrimeByIndex(crimeIndex);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        setHasOptionsMenu(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.delete_crime_button) {
            CrimeLab.get(getActivity()).deleteCrimeByIndex(crimeIndex);
            getActivity().setResult(RESULT_OK);
            if(!mTabletSwitch) getActivity().finish();
            updateCrime();
            return true;
        } else
            return super.onOptionsItemSelected(item);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull final LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mEditText = v.findViewById(R.id.crime_title_editText);
        mEditText.setText(mCrime.getTitle());
        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = v.findViewById(R.id.crime_date_button);
        UpdateDateButtonText();
        //mDateButton.setEnabled(false);
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
*/
                Intent intent = DateTimePickerActivity.newIntent(getActivity(), mCrime.getDate(), DateTimePickerActivity.EXTRA_DATE_FRAG);
                startActivityForResult(intent, REQUEST_DATE);
            }
        });

        mTimeButton = v.findViewById(R.id.crime_time_button);
        UpdateTimeButtonText();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
*/
                Intent intent = DateTimePickerActivity.newIntent(getActivity(), mCrime.getDate(), DateTimePickerActivity.EXTRA_TIME_FRAG);
                startActivityForResult(intent, REQUEST_TIME);
            }
        });


        mSolvedCheckBox = v.findViewById(R.id.crime_solved_checkbox);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
                updateCrime();
            }
        });

        mReportCrimeButton = v.findViewById(R.id.send_crime_button);
        mReportCrimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
/*
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, R.string.crime_report_subject);
                i = Intent.createChooser(i, getString(R.string.send_report_via));
                startActivity(i);
*/

                ShareCompat.IntentBuilder
                        .from(getActivity())
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setType("text/plain")
                        .startChooser();
            }
        });

        mChooseSuspectButton = v.findViewById(R.id.choose_suspect_button);
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        //pickContact.addCategory(Intent.CATEGORY_APP_BROWSER);
        mChooseSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        mCallSuspectButton = v.findViewById(R.id.call_suspect_button);
        mCallSuspectButton.setEnabled(false);
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri numberUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
                String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER};
                String selection = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?";
                String[] selectionArgs = new String[]{mCrime.getSuspect()};
                try {
                    Cursor cursor = getActivity().getContentResolver().query(numberUri, projection, selection, selectionArgs, null);
                    if (cursor != null) {
                        cursor.moveToFirst();
                        //new AlertDialog.Builder(getActivity()).setTitle(cursor.getString(numberColumn) + ";" + numberColumn).create().show();
                        Intent callContact = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + cursor.getString(0)));
                        startActivity(callContact);
                    } else {
                        Toast.makeText(getActivity(), "no number", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });

        if (mCrime.getSuspect() != null) {
            mChooseSuspectButton.setText(mCrime.getSuspect());
            mCallSuspectButton.setEnabled(true);
        }


        TextView mCrimeSeriousness = v.findViewById(R.id.crimeSeriousnessTextView);
        String mCrimeSeriousnessString = getString(R.string.crime_severity_title) + " ";
        switch (mCrime.getSeriousness()) {
            case 0:
                mCrimeSeriousnessString = mCrimeSeriousnessString.concat(getString(R.string.crime_severity_level0));
                break;
            case 1:
                mCrimeSeriousnessString = mCrimeSeriousnessString.concat(getString(R.string.crime_severity_level1));
                break;
            default:
                mCrimeSeriousnessString = "severity level error";
                break;
        }
        mCrimeSeriousness.setText(mCrimeSeriousnessString);

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null)
            mChooseSuspectButton.setEnabled(false);

        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoButton != null && captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(), "com.example.androidprogrammingbook02.fileprovider", mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity().getPackageManager().queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);
                for(ResolveInfo activity : cameraActivities){
                    getActivity().grantUriPermission(activity.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });


        mPhotoView = v.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mPhotoFile.exists()){
                    Toast.makeText(getActivity(), R.string.dialog_fragment_no_photo, Toast.LENGTH_SHORT).show();
                    return;
                }
                FragmentManager manager = getFragmentManager();
                PhotoViewFragment dialog = PhotoViewFragment.newInstance(mPhotoFile.getPath());
                dialog.show(manager, null);
            }
        });

        ViewTreeObserver observer = mPhotoView.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                updatePhotoView();
            }
        });


        //updatePhotoView();

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        Date date;
        if (data != null) {
            switch (requestCode) {
                case REQUEST_DATE:
                    date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
                    mCrime.setDate(date);
                    UpdateDateButtonText();
                    break;
                case REQUEST_TIME:
                    date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
                    mCrime.setDate(date);
                    UpdateTimeButtonText();
                    break;
                case REQUEST_CONTACT:
                    Uri contactUri = data.getData();
                    String[] queryFields = new String[]{
                            ContactsContract.Contacts.DISPLAY_NAME
                    };
                    Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);
                    try {
                        if (c.getCount() == 0) return;
                        c.moveToFirst();
                        String suspect = c.getString(0);
                        mCrime.setSuspect(suspect);
                        mChooseSuspectButton.setText(suspect);
                        mCallSuspectButton.setEnabled(true);
                    } finally {
                        c.close();
                    }
                    break;
                case REQUEST_PHOTO:
                    Uri uri = FileProvider.getUriForFile(getActivity(), "com.example.androidprogrammingbook02.fileprovider", mPhotoFile);
                    getActivity().revokeUriPermission(uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    //updatePhotoView();
                    break;
                default:
                    new AlertDialog.Builder(getActivity()).setTitle(R.string.error_crime_fragment_on_activity_result_code).create().show();
                    break;
            }
            updateCrime();
        }
    }

    private void UpdateDateButtonText() {
        DateFormat dateFormat = DateFormat.getDateInstance();
        mDateButton.setText(dateFormat.format(mCrime.getDate()));
    }

    private void UpdateTimeButtonText() {
        DateFormat dateFormat = DateFormat.getTimeInstance();
        mTimeButton.setText(dateFormat.format(mCrime.getDate()));
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = android.text.format.DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_noSuspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report_body, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    private void updatePhotoView(){

        if(mPhotoFile == null||!mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
        }else{
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(), mPhotoView.getWidth(), mPhotoView.getHeight());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallbacks.onCrimeUpdated(mCrime);
    }
}
