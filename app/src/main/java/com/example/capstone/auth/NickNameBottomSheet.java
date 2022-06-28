package com.example.capstone.auth;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.capstone.databinding.BottomSheetNickNameBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import timber.log.Timber;


public class NickNameBottomSheet extends BottomSheetDialogFragment {

    public static final String REQUEST_UPDATE_NICK_NAME = "REQUEST_UPDATE_NICK_NAME";
    public static final String TAG = "NickNameBottomSheet";

    private BottomSheetNickNameBinding binding;
    private final FirebaseAuth auth = FirebaseAuth.getInstance();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetNickNameBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.nickNameTextField.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                binding.okButton.setEnabled(
                        !binding.nickNameTextField.getEditText().getText().toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(android.text.Editable editable) {
            }
        });

        binding.okButton.setOnClickListener(v -> setNickName());
        binding.cancelButton.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);

        getParentFragmentManager().setFragmentResult(REQUEST_UPDATE_NICK_NAME, new Bundle());
    }

    private void setNickName() {
        FirebaseUser user = auth.getCurrentUser();
        assert user != null;

        String nickName = binding.nickNameTextField.getEditText().getText().toString().trim();

        UserProfileChangeRequest request = new UserProfileChangeRequest.Builder()
                .setDisplayName(nickName)
                .build();

        user.updateProfile(request).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                Timber.d("Update display name");
            }
        });

        Bundle bundle = new Bundle();
        bundle.putString("NickName", nickName);

        getParentFragmentManager().setFragmentResult(REQUEST_UPDATE_NICK_NAME, bundle);

        dismiss();
    }
}
