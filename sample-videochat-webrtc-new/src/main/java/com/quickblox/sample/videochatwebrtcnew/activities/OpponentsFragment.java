package com.quickblox.sample.videochatwebrtcnew.activities;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBSignaling;
import com.quickblox.chat.QBVideoChatWebRTCSignalingManager;
import com.quickblox.chat.QBWebRTCSignaling;
import com.quickblox.chat.listeners.QBVideoChatSignalingManagerListener;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.sample.videochatwebrtcnew.R;
import com.quickblox.sample.videochatwebrtcnew.adapters.OpponentsAdapter;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.videochat.webrtcnew.QBRTCClient;
import com.quickblox.videochat.webrtcnew.QBRTCSession;
import com.quickblox.videochat.webrtcnew.model.QBRTCTypes;
import com.quickblox.videochat.webrtcnew.view.QBRTCVideoTrack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by tereha on 16.02.15.
 */
public class OpponentsFragment extends Fragment implements QBEntityCallback<ArrayList<QBUser>>, View.OnClickListener {

    private OpponentsAdapter opponentsAdapter;
    private PullToRefreshListView opponentsList;
    public static String login;
    private Button btnAudioCall;
    private Button btnVideoCall;
    private List<QBUser> users = new ArrayList<QBUser>();
    private static final int PAGE_SIZE = 10;
    private int currentPage = 0;
    private int listViewIndex;
    private int listViewTop;
    private QBRTCTypes.QBConferenceType qbConferenceType;



    public static OpponentsFragment getInstance() {
        return new OpponentsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_opponents, container, false);

        initUI(view);

        opponentsList.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ListView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ListView> refreshView) {
                // Do work to refresh the list here.
                loadNextPage();
                listViewIndex = opponentsList.getRefreshableView().getFirstVisiblePosition();
                View v = opponentsList.getRefreshableView().getChildAt(0);
                listViewTop = (v == null) ? 0 : v.getTop();
            }
        });

        loadNextPage();

        return view;
    }

    private void initUI(View view) {

        opponentsList = (PullToRefreshListView) view.findViewById(R.id.opponentsList);
        login = getActivity().getIntent().getStringExtra("login");

        btnAudioCall = (Button)view.findViewById(R.id.btnAudioCall);
        btnVideoCall = (Button)view.findViewById(R.id.btnVideoCall);

        btnAudioCall.setOnClickListener(this);
        btnVideoCall.setOnClickListener(this);

    }

    public static int searchIndexLogginedUser(ArrayList<QBUser> usersList) {

        int indexLogginedUser = -1;

        for (QBUser usr : usersList) {
            if (usr.getLogin().equals(login)) {
                indexLogginedUser = usersList.indexOf(usr);
                break;
            }
        }

        return indexLogginedUser;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnAudioCall:


                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_AUDIO;

                break;

            case R.id.btnVideoCall:

                // get call type
                qbConferenceType = QBRTCTypes.QBConferenceType.QB_CONFERENCE_TYPE_VIDEO;


                // prepare custom user data
                Map<String, String> userInfo =  new HashMap<>();
                userInfo.put("any_custom_data", "some data");
                userInfo.put("my_avatar_url", "avatar_reference");

                ((NewDialogActivity)getActivity())
                        .startCanversationFragmentWithParameters(getOpponentsIds(opponentsAdapter.getSelected()),
                                qbConferenceType, userInfo);

                break;
        }
    }

    @Override
    public void onSuccess(ArrayList<QBUser> qbUsers, Bundle bundle) {
        users.addAll(qbUsers);
        int i = searchIndexLogginedUser((ArrayList<QBUser>) users);
        if (i>=0)
            users.remove(i);

        // Prepare users list for simple adapter.
        //
        opponentsAdapter = new OpponentsAdapter(getActivity(), users);
        opponentsList.setAdapter(opponentsAdapter);
        opponentsList.onRefreshComplete();
        opponentsList.getRefreshableView().setSelectionFromTop(listViewIndex, listViewTop);
        //progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onSuccess() {

    }

    @Override
    public void onError(List<String> errors){
        AlertDialog.Builder dialog = new AlertDialog.Builder(OpponentsFragment.getInstance().getActivity());
        dialog.setMessage("get users errors: " + errors).create().show();
    }

    public static QBPagedRequestBuilder getQBPagedRequestBuilder(int page) {
        QBPagedRequestBuilder pagedRequestBuilder = new QBPagedRequestBuilder();
        pagedRequestBuilder.setPage(page);
        pagedRequestBuilder.setPerPage(PAGE_SIZE);

        return pagedRequestBuilder;
    }

    private void loadNextPage() {
        ++currentPage;

        QBUsers.getUsers(getQBPagedRequestBuilder(currentPage), OpponentsFragment.this);

    }

    public static ArrayList<Integer> getOpponentsIds(List<QBUser> opponents){
        ArrayList<Integer> ids = new ArrayList<Integer>();
        for(QBUser user : opponents){
            ids.add(user.getId());
        }
        return ids;
    }

}
