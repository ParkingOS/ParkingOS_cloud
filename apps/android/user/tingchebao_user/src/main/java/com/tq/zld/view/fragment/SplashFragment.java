package com.tq.zld.view.fragment;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.tq.zld.R;
import com.tq.zld.TCBApp;
import com.tq.zld.view.SplashActivity;
import com.tq.zld.view.map.WebActivity;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SplashFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SplashFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SplashFragment extends Fragment {
    private static final String ARG_POSITION = "position";

    private int mPosition;

    private OnFragmentInteractionListener mListener;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param position fragment在ViewPage中的位置
     * @return A new instance of fragment SplashFragment.
     */
    public static SplashFragment newInstance(int position) {
        SplashFragment fragment = new SplashFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_POSITION, position);
        fragment.setArguments(args);
        return fragment;
    }

    public SplashFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPosition = getArguments().getInt(ARG_POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        switch (mPosition) {
            case 0:
                view.setBackgroundResource(R.drawable.bg_splash_0);
                break;
            case 1:
                view.setBackgroundResource(R.drawable.bg_splash_1);
                break;
            case 2:
                view.setBackgroundResource(R.drawable.bg_splash_2);
                break;
            case 3:
                view.setBackgroundResource(R.drawable.bg_splash);
                ViewStub viewStub = (ViewStub) view.findViewById(R.id.vs_splash);
                viewStub.setOnInflateListener(new ViewStub.OnInflateListener() {
                    @Override
                    public void onInflate(ViewStub stub, View inflated) {
                        inflated.findViewById(R.id.btn_splash_enter).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                TCBApp.getAppContext().
                                        saveInt(R.string.sp_splash_version, SplashActivity.SPLASH_VERSION);
                                ((SplashActivity) getActivity()).openMap();
                            }
                        });
                        if (TextUtils.isEmpty(TCBApp.mMobile)) {
                            inflated.findViewById(R.id.ib_splash_play).setVisibility(View.GONE);
                        } else {
                            inflated.findViewById(R.id.ib_splash_play).setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    TCBApp.getAppContext().
                                            saveInt(R.string.sp_splash_version, SplashActivity.SPLASH_VERSION);
                                    goToPlay();
                                }
                            });
                        }
                    }
                });
                viewStub.inflate();
                break;
        }
    }

    private void goToPlay() {
        Intent intent = new Intent(TCBApp.getAppContext(), WebActivity.class);
        intent.putExtra(WebActivity.ARG_TITLE, "停车挑战");
        intent.putExtra(WebActivity.ARG_URL, TCBApp.mServerUrl + "cargame.do?action=playgame&mobile=" + TCBApp.mMobile);
        // intent.putExtra(WebActivity.ARG_URL, "http://192.168.199.240/zld/cargame.do?action=sort&uin=21568&score=90&usercount=7168&sort=11&type=0&ticketid=38244");

        startActivityForResult(intent, 0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            ((SplashActivity) getActivity()).openMap();
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

}
