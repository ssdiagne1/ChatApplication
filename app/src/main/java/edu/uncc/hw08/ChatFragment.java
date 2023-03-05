/*
 * Chat Fragment
 * Samba Diagne
 */
package edu.uncc.hw08;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import edu.uncc.hw08.databinding.ChatListItemBinding;
import edu.uncc.hw08.databinding.FragmentChatBinding;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChatFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChatFragment extends Fragment {
    private static final String ARG_PARAM_MESSAGE = "ARG_PARAM_MESSAGE";
    ArrayList<Message> messageList = new ArrayList<>();
    private Message messageRec;
    MessagesAdapter messageAdapter;
    String userConnected = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    int pos;
    public ChatFragment() {
        // Required empty public constructor
    }

    public static ChatFragment newInstance(Message message1) {
        ChatFragment fragment = new ChatFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_MESSAGE,message1);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null){
            messageRec = (Message) getArguments().getSerializable(ARG_PARAM_MESSAGE);
        }

    }

    FragmentChatBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        FirebaseFirestore.getInstance().collection("Message")
                .orderBy("sendAt", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                messageList.clear();

                for (QueryDocumentSnapshot doc: value) {


                    Message messageGet = doc.toObject(Message.class);
                    if (messageGet.getSender().equals(messageRec.getReceiver()) && messageGet.getReceiver().equals(messageRec.getSender())
                        ||messageGet.getSender().equals(messageRec.getSender()) && messageGet.getReceiver().equals(messageRec.getReceiver()) ) {
                        messageList.add(messageGet);

                    }
                }

                messageAdapter.notifyDataSetChanged();
                
            }
        });
        getActivity().setTitle("Chat - "+messageRec.getSender());
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageAdapter = new MessagesAdapter(messageList);
        binding.recyclerView.setAdapter(messageAdapter);
        binding.buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = binding.editTextMessage.getText().toString();
                if (message.isEmpty()){
                    Toast.makeText(getActivity(), "Message required, enter a text", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    FirebaseFirestore db = FirebaseFirestore.getInstance();

                    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                    String time = sdf.format(Calendar.getInstance().getTime());
                    HashMap<String, Object> post = new HashMap<>();
                    post.put("sender", userConnected);
                    post.put("messageBody", message);
                    post.put("receiver", messageRec.getSender());
                    post.put("sendAt", time);
                    DocumentReference docRef = FirebaseFirestore.getInstance().collection("Message").document();
                    post.put("messageID", docRef.getId());
                    db.collection("Message").document(docRef.getId())
                            .set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getActivity(), "Message sent successfully", Toast.LENGTH_SHORT).show();
                                    binding.editTextMessage.setText("");


                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getActivity(), "Unable to add this user name to the database\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });
        binding.buttonDeleteChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (Message m:messageList) {
                    FirebaseFirestore.getInstance().collection("Message")
                            .document(m.getMessageID()).delete();
                    mListener.gotoMyChat();
                }

            }
        });
        binding.buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoMyChat();
            }
        });

    }


    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MessagesViewHolder>{

        ArrayList<Message> messageArrayList = new ArrayList<>();

        public MessagesAdapter(ArrayList<Message> message) {
            this.messageArrayList = message;
        }
        @NonNull
        @Override
        public MessagesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ChatListItemBinding binding = ChatListItemBinding.inflate(getLayoutInflater(), parent, false);
            return new MessagesViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull MessagesViewHolder holder, int position) {
            Message message = messageList.get(position);
            holder.setupUI(message);
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }

        class MessagesViewHolder extends RecyclerView.ViewHolder{
            ChatListItemBinding mBinding;
            Message mMessage;

            public MessagesViewHolder( ChatListItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public void setupUI(Message message){
                mMessage = message;
                SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
                mBinding.textViewMsgText.setText(mMessage.getMessage());
                if (mAuth.getCurrentUser().getDisplayName().equals(mMessage.getSender())) {
                    mBinding.textViewMsgBy.setText("Me");
                }
                else{
                    mBinding.textViewMsgBy.setText(message.getSender());
                }
                mBinding.textViewMsgOn.setText(mMessage.getSendAt());


                if(mAuth.getCurrentUser().getDisplayName().equals(mMessage.getSender())) {
                    mBinding.imageViewDelete.setVisibility(View.VISIBLE);

                    mBinding.imageViewDelete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //delete all the comments from this Message
                            //then delete the Message.
                            boolean isFinishing = false;

                            FirebaseFirestore.getInstance().collection("Message")
                            .document(mMessage.getMessageID()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {

                                            if (!isFinishing){
                                                new AlertDialog.Builder(getContext())
                                                        .setTitle("Do you want to delete this message?")
                                                        .setMessage(mMessage.getMessage())
                                                        .setCancelable(false)
                                                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {

                                                            }
                                                        })
                                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialog, int which) {
                                                            try {
                                                                FirebaseFirestore.getInstance().collection("Message").document(mMessage.getMessageID()).delete();

                                                            }catch (Exception e){
                                                                new AlertDialog.Builder(getContext())
                                                                        .setTitle("Do you want to delete this message?")
                                                                        .setMessage(e.getMessage())
                                                                        .setCancelable(false)
                                                                        .show();
                                                            }

                                                        }

                                                        }).show();

                                            }
                                        }
                                    });

                                }


                            });
                        }

                    });

                }else {
                    mBinding.imageViewDelete.setVisibility(View.INVISIBLE);
                }

            }

        }
    }


   ChatListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (ChatListener) context;
    }

    interface ChatListener {
        void gotoMyChat();

    }
}