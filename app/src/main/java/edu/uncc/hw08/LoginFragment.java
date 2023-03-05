/*
 * Login Fragment
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

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import edu.uncc.hw08.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {
    private static final String ARG_PARAM_USER = "ARG_PARAM_USER";
    User mUser;
    public LoginFragment() {
        // Required empty public constructor
    }
    public static LoginFragment newInstance(User user) {

        Bundle args = new Bundle();
        LoginFragment fragment = new LoginFragment();
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


   ArrayList<User> userArrayList = new ArrayList<>();

    FragmentLoginBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    boolean isFinishing = false;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.editTextEmail.getText().toString();
                String password = binding.editTextPassword.getText().toString();

                if(email.isEmpty()){
                    Toast.makeText(getContext(), "Email is required", Toast.LENGTH_SHORT).show();
                } else if(password.isEmpty()){
                    Toast.makeText(getContext(), "Password is required", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), task -> {
                        if(task.isSuccessful()){
                            FirebaseFirestore.getInstance().collection("User").addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                    for (QueryDocumentSnapshot doc : value) {
                                        User user1 = doc.toObject(User.class);
                                        if (user1.getEmail().equals(email)){
                                            DocumentReference ref = FirebaseFirestore.getInstance().collection("User").document(user1.getUserID());
                                            ref.update("status", true);
                                            break;
                                        }
                                    }
                                }
                            });

                            mListener.gotoMyChat();
                        } else {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (!isFinishing){
                                        new AlertDialog.Builder(getContext())
                                                .setTitle("Login Failed")
                                                .setMessage(task.getException().getMessage())
                                                .setCancelable(false)
                                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                            binding.editTextEmail.setText("");
                                                            binding.editTextPassword.setText("");
                                                    }
                                                }).show();

                                    }
                                }
                            });


                        }
                    });
                }
            }
        });
        binding.buttonCreateNewAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.gotoSignUp();
            }
        });

        getActivity().setTitle("Login");
    }

    LoginListener mListener;
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mListener = (LoginListener) context;
    }

    interface LoginListener {
        void gotoMyChat();
        void gotoSignUp();
    }
}