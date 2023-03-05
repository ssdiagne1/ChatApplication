/*
 * Create Chat Fragment
 * Samba Diagne
 */
package edu.uncc.hw08;

import static java.lang.System.currentTimeMillis;

import android.annotation.SuppressLint;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import edu.uncc.hw08.databinding.FragmentCreateChatBinding;
import edu.uncc.hw08.databinding.MyChatsListItemBinding;
import edu.uncc.hw08.databinding.UsersRowItemBinding;


public class CreateChatFragment extends Fragment {
    FragmentCreateChatBinding binding;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    UserAdapter userAdapter;
    String receiver;
    String userConnected = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
    int pos = 0 ;
    ArrayList<String> userList = new ArrayList<>();
    public int getCount(int pos){
        return pos;
    }
    public CreateChatFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentCreateChatBinding.inflate(inflater,container,false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("New Chat");

        db.collection("User").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                userList.clear();
                for (QueryDocumentSnapshot user: value){
                   String tempName = user.get("name").toString();
                    if (userConnected.equals(tempName)){

                        Log.d("demo23", "onViewCreated position: "+pos);
                    }
                    else{
                        pos+=1;
                    }
                    userList.add(tempName);

                }
                Log.d("demo413", "onViewCreated INSIDE CHAT : "+userList);

                userAdapter.notifyDataSetChanged();



            }



        });

        binding.listView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(userList);
        binding.listView.setAdapter(userAdapter);


        /*adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, android.R.id.text1,userList);
        binding.listView.setAdapter(adapter);*/



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
                    post.put("sender", FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                    post.put("messageBody", message);
                    post.put("receiver", receiver);
                    Log.d("demos", "onClick Nom : "+receiver);
                    post.put("sendAt", time);
                    DocumentReference docRef = FirebaseFirestore.getInstance().collection("Message").document();
                    post.put("messageID", docRef.getId());
                    db.collection("Message").document(docRef.getId())
                            .set(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(getActivity(), "Message sent successfully", Toast.LENGTH_SHORT).show();
                                    binding.editTextMessage.setText("");
                                    binding.textViewSelectedUser.setText("");
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
        binding.buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                binding.textViewSelectedUser.setText("No User Selected !!");
                binding.editTextMessage.setText("");
                mListener.gotoMyChat();
            }
        });

    }


    class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{
        ArrayList<String> arrayList = new ArrayList<>();


        public UserAdapter(ArrayList<String> name) {
            this.arrayList = name;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            UsersRowItemBinding rowItemBinding = UsersRowItemBinding.inflate(getLayoutInflater(),parent,false);
            return new UserAdapter.UserViewHolder(rowItemBinding);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            String name = userList.get(position);

            holder.setupUI(name);
        }

        @Override
        public int getItemCount() {
            return userList.size();
        }

        class UserViewHolder extends RecyclerView.ViewHolder{

            UsersRowItemBinding mBinding;


            public UserViewHolder(UsersRowItemBinding binding) {
                super(binding.getRoot());
                mBinding = binding;
            }

            public  void setupUI(String name){
               mBinding.textViewName.setText(name);
               if(userConnected.equals(name)) {
                   mBinding.imageViewOnline.setImageResource(R.drawable.ic_online);
               }
               else{
                   mBinding.imageViewOnline.setImageResource(0);
               }
               
                mBinding.getRoot().setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        binding.textViewSelectedUser.setText(userList.get(getAdapterPosition()));
                        receiver =userList.get(getAdapterPosition());
                    }
                });

            }


        }
    }



    CreateChatListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (CreateChatListener) context;
    }

    interface CreateChatListener {
       void gotoMyChat();

    }


}