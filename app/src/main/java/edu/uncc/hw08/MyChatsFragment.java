/*
 * My Chats Fragment
 * Samba Diagne
 */
package edu.uncc.hw08;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import edu.uncc.hw08.databinding.FragmentMyChatsBinding;
import edu.uncc.hw08.databinding.MyChatsListItemBinding;

public class MyChatsFragment extends Fragment {
    private static final String ARG_PARAM_USER = "ARG_PARAM_USER";
    FragmentMyChatsBinding binding;
    User user;
    MyChatsListItemBinding bindingItem;
    FirebaseAuth mAuth;
    ArrayList<Message> messageList = new ArrayList<>();
    MessagesAdapter messagesAdapter;
    User mUser,newUser;
    ArrayList<User> userList = new ArrayList<>();
    String name = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();

    String userID;
    int pos;
    public MyChatsFragment() {
        // Required empty public constructor
    }

    public static MyChatsFragment newInstance(User user) {

        Bundle args = new Bundle();
        MyChatsFragment fragment = new MyChatsFragment();
        args.putSerializable(ARG_PARAM_USER,user);
        fragment.setArguments(args);
        return fragment;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = (User) getArguments().getSerializable(ARG_PARAM_USER);

        }

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().setTitle("My Chats");
        // Inflate the layout for this fragment
        binding = FragmentMyChatsBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }
    ArrayList<Message> customList =  new ArrayList<>();
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mAuth= FirebaseAuth.getInstance();
        Log.d("demos", "onEvent: "+mAuth.getCurrentUser().getDisplayName());
        FirebaseFirestore.getInstance().collection("Message")
                .orderBy("sendAt", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        messageList.clear();

                        for (QueryDocumentSnapshot doc: value) {
                            Message message = doc.toObject(Message.class);

                            messageList.add(message);

                        }
                        customList.clear();
                        for (Message message:messageList) {
                            if (!(message.getSender().equals(mAuth.getCurrentUser().getDisplayName())) && (message.getReceiver().equals(mAuth.getCurrentUser().getDisplayName()))){
                                customList.add(message);
                            }
                        }
                        messagesAdapter.notifyDataSetChanged();
                    }
                });

        binding.listView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesAdapter = new MessagesAdapter(customList);
        binding.listView.setAdapter(messagesAdapter);


        binding.buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseFirestore.getInstance().collection("User").addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        userList.clear();
                        for (QueryDocumentSnapshot doc: value) {
                            User user1 = doc.toObject(User.class);
                            userList.add(user1);
                        }
                        Log.d("demoss", "onEvent: "+userList.get(0).getName());
                        for (int i = 0; i < userList.size(); i++) {
                            if (userList.get(i).getName().equals(name)){
                                pos = i;

                            }
                        }
                        DocumentReference ref = FirebaseFirestore.getInstance().collection("User").document(userList.get(pos).getUserID());
                        ref.update("status",false);


                        FirebaseAuth.getInstance().signOut();
                        mListener.goToLogin();
                    }
                });

            }
        });


        binding.buttonNewChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.goToCreateChatFragment();
            }
        });

    }

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>{
        ArrayList<Message> messageArrayList = new ArrayList<>();

        public MessagesAdapter(ArrayList<Message> message) {
            this.messageArrayList = message;
        }

        @NonNull
        @Override
        public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            MyChatsListItemBinding binding = MyChatsListItemBinding.inflate(getLayoutInflater(), parent, false);
            return new MessagesViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
            Message message = customList.get(position);
            holder.setupUI(message);
        }

        @Override
        public int getItemCount() {
            return customList.size();
        }

        class MessagesViewHolder extends RecyclerView.ViewHolder{

            MyChatsListItemBinding mBinding;
            Message mMessage;

            public MessagesViewHolder(MyChatsListItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public  void setupUI(Message message){

                mBinding.textViewMsgOn.setText(message.getSendAt());
                mBinding.textViewMsgText.setText(message.getMessage());
                mBinding.textViewMsgBy.setText(message.getSender());
                mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mListener.goToSpecificChat(message);
                        Log.d("moki", "onClick: "+message.sender);
                    }
                });

            }


        }
    }


    MyChatsListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (MyChatsListener) context;
    }

    interface MyChatsListener {
        void goToLogin();
        void goToCreateChatFragment();
        void goToSpecificChat(Message message);

    }


}